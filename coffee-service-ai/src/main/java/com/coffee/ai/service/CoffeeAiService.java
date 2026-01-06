package com.coffee.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coffee.ai.tools.OrderTools;
import com.coffee.system.domain.entity.Product;
import com.coffee.system.domain.entity.SkuStock;
import com.coffee.system.service.ProductService;
import com.coffee.system.service.SkuStockService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 服务类：负责与向量数据库交互
 */
@Service
@Slf4j
public class CoffeeAiService {


    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    // 增加一个简单的 ChatClient，用于非 RAG 任务（如查询重写），且不带记忆功能
    private final ChatClient simpleChatClient;
    private final ProductService productService;
    private final SkuStockService skuStockService;
    private final ChatMemory chatMemory; // 保存引用以便手动访问

    private final OrderTools orderTools;

    // 文本切分器，用于将长文档切分成小块，提高 RAG 效果
    private final TokenTextSplitter textSplitter = new TokenTextSplitter();

    public CoffeeAiService(VectorStore vectorStore,
                           ChatClient.Builder chatClientBuilder,
                           ProductService productService,
                           SkuStockService skuStockService,
                           ChatMemory chatMemory,
                           ChatModel chatModel,
                           OrderTools orderTools) { // 注入底层 ChatModel
        this.vectorStore = vectorStore;
        this.productService = productService;
        this.skuStockService = skuStockService;
        this.chatMemory = chatMemory;
        this.orderTools = orderTools;

        // 1. 初始化无记忆的简单 ChatClient (用于查询重写)
        // 使用 ChatClient.create(chatModel) 创建的客户端是"干净"的，不会自动挂载 Advisor
        this.simpleChatClient = ChatClient.create(chatModel);

        // 2. 初始化主对话 RAG ChatClient (带记忆功能)
        this.chatClient = chatClientBuilder
                .defaultAdvisors(PromptChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools(orderTools)
                .build();
    }


    /**
     * 导入管理员上传的多种格式数据 (CSV, PDF, Word, Markdown) 到向量数据库
     *
     * @param file 上传的文件
     * @return 结果消息
     */
    public String importData(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) return "文件名不能为空";

        String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        List<Document> documents = new ArrayList<>();

        try {
            switch (extension) {
                case ".csv":
                    documents = parseCsv(file);
                    break;
                case ".pdf":
                    documents = parsePdf(file);
                    break;
                case ".doc":
                case ".docx":
                    documents = parseWord(file);
                    break;
                case ".md":
                case ".txt":
                    documents = parseText(file);
                    break;
                default:
                    return "暂不支持该文件格式: " + extension;
            }

            if (documents.isEmpty()) {
                return "未在文件中找到有效内容";
            }

            // 对长文档进行切分，防止超过模型上下文限制并提高检索精度
            List<Document> splitDocuments = textSplitter.apply(documents);

            // 将文档列表添加到向量数据库中
            vectorStore.add(splitDocuments);

            return "成功导入 " + splitDocuments.size() + " 条知识片段到 AI 知识库";
        } catch (Exception e) {
            log.error("文件导入失败: " + fileName, e);
            return "导入失败: " + e.getMessage();
        }
    }

    private List<Document> parseCsv(MultipartFile file) throws IOException {
        InputStreamReader reader = new java.io.InputStreamReader(file.getInputStream(), "UTF-8");
        CSVParser csvParser = CSVFormat.DEFAULT.builder()
                .setHeader().setSkipHeaderRecord(true).build().parse(reader);

        List<Document> list = new ArrayList<>();
        for (CSVRecord record : csvParser) {
            if (record.size() < 2) continue;
            String question = record.get(0);
            String answer = record.get(1);
            if (question != null && !question.trim().isEmpty()) {
                list.add(new Document("问题: " + question + "\n答案: " + answer));
            }
        }
        csvParser.close();
        return list;
    }

    private List<Document> parsePdf(MultipartFile file) throws IOException {
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(new InputStreamResource(file.getInputStream()));
        return pdfReader.get();
    }

    private List<Document> parseWord(MultipartFile file) throws IOException {
        TikaDocumentReader tikaReader = new TikaDocumentReader(new InputStreamResource(file.getInputStream()));
        return tikaReader.get();
    }

    private List<Document> parseText(MultipartFile file) throws IOException {
        TextReader textReader = new TextReader(new InputStreamResource(file.getInputStream()));
        return textReader.get();
    }

    /**
     * 将数据库中的产品及规格信息自动同步到向量数据库
     *
     * @return 同步结果
     */
    public String syncDatabaseToVectorStore() {
        try {
            // 1. 获取所有上架产品
            List<Product> productList = productService.list(new LambdaQueryWrapper<Product>()
                    .eq(Product::getStatus, 1));

            if (productList.isEmpty()) {
                return "数据库中没有上架商品";
            }

            List<Document> documents = new ArrayList<>();

            for (Product product : productList) {
                // 2. 获取该产品下的所有 SKU
                List<SkuStock> skus = skuStockService.list(new LambdaQueryWrapper<SkuStock>()
                        .eq(SkuStock::getProductId, product.getId()));

                // 3. 构建结构化描述文本
                String productText = String.format(
                    "商品名称: %s\n基础价格: ￥%s\n商品描述: %s\n%s",
                    product.getName(),
                    product.getPrice(),
                    product.getDescription(),
                    buildSkuDescription(skus)
                );

                // 4. 创建文档对象，并设置固定 ID 以实现"存在即更新，不存在则插入"
                // 使用 product_ 前缀加上商品 ID，确保唯一且可追溯
                Document document = new Document("doc_prod_" + product.getId(), productText, new java.util.HashMap<>());
                document.getMetadata().put("type", "product");
                document.getMetadata().put("id", product.getId());

                documents.add(document);
            }

            // 5. 更新向量数据库
            vectorStore.add(documents);

            return "成功同步 " + documents.size() + " 件商品的详细信息到 AI 知识库";
        } catch (Exception e) {
            log.error("数据库同步到 RAG 失败", e);
            return "同步失败: " + e.getMessage();
        }
    }

    /**
     * 构建SKU描述文本
     */
    private String buildSkuDescription(List<SkuStock> skus) {
        if (skus.isEmpty()) {
            return "规格信息: 本品为标准规格，暂无其他可选属性。\n";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("可选规格与价格详情:\n");
        for (SkuStock sku : skus) {
            sb.append("- 价格: ￥").append(sku.getPrice());
            if (sku.getSpec() != null) {
                // 优化：尝试将 JSON 格式的规格转为易读文字
                String specText = sku.getSpec()
                        .replace("[", "").replace("]", "")
                        .replace("{\"key\":", "").replace("\"value\":", "")
                        .replace("\"", "").replace("}", "").replace("{", "");
                sb.append(" | 规格: ").append(specText);
            }
            sb.append(" | 库存状态: ").append(sku.getStock() > 0 ? "有货" : "售罄");
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * 聊天
     *
     * @param systemPrompt 系统提示词
     * @param userQuestion 用户问题
     * @param chatId 会话ID
     * @return AI 回答
     * 原始的 RAG 确实和 Memory 有冲突，所以必须引入“查询重写（Query Rewriting）”来调和。
     * 查询重写（Query Rewriting）就是连接 Memory 和 RAG 的桥梁。
     * 第一步（利用 Memory）： 先让一个轻量级 AI 看一眼历史记录（Memory）和当前问题（“多少钱”）。 AI 说：“哦，根据上文，他其实想问的是‘可颂多少钱’。” -> 这就是重写。
     * 第二步（服务 RAG）： 拿着这个补全后的完美句子“可颂多少钱”，去检索器里搜。 这时候检索器就能精准捞出“可颂”的文档了。
     */
    public String chat(String systemPrompt, String userQuestion, Long chatId) {
        String conversationId = String.valueOf(chatId);

        String searchKey = userQuestion;

        // 1. 查询重写：尝试结合历史上下文优化搜索关键词
        try {
            List<Message> history = chatMemory.get(conversationId);

            if (history != null && !history.isEmpty() && userQuestion.length() < 10) {
                StringBuilder historyText = new StringBuilder();
                // 只取最近几条历史，避免太长
                int startIndex = Math.max(0, history.size() - 4);
                for (int i = startIndex; i < history.size(); i++) {
                    Message msg = history.get(i);
                    historyText.append(msg.getMessageType()).append(": ").append(msg.toString()).append("\n");
                }

                String rewritePrompt = "你是一个搜索查询优化助手。请根据以下历史对话，将用户的【最新问题】改写为一个指代明确、完整的搜索关键词，以便在商品知识库中检索。\n" +
                        "规则：\n" +
                        "1. 如果最新问题包含代词（如“它”、“这个”、“多少钱”），请根据历史对话还原指代对象（如“可颂多少钱”）。\n" +
                        "2. 这是一个商品搜索场景，请确保关键词包含商品名称。\n" +
                        "3. 直接返回改写后的句子，不要包含任何解释、标点符号或其他文字。\n\n" +
                        "历史对话:\n" + historyText +
                        "用户最新问题: " + userQuestion + "\n\n" +
                        "改写结果:";

                String rewritten = simpleChatClient.prompt()
                        .user(rewritePrompt)
                        .call()
                        .content();

                if (rewritten != null) {
                    rewritten = rewritten.replace("改写结果:", "").trim();
                    if (!rewritten.isEmpty() && rewritten.length() < 30) { // 简单校验
                        searchKey = rewritten;
                        log.info("Query Rewrite Success: '{}' -> '{}'", userQuestion, searchKey);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Query rewrite failed: " + e.getMessage());
        }

        // 2. 手动检索：使用优化后的关键词 (searchKey) 去向量库搜索
        // 修改为 topK(1)，确保只返回最匹配的一条商品信息，避免返回无关干扰项
        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(searchKey)
                        .topK(2) // 只取最相似的1条，解决多商品混淆问题
                        .similarityThreshold(0.5) // 过滤掉相关性太低的结果
                        .build());

        String context = documents.stream()
                .map(Document::getText)
                .reduce((a, b) -> a + "\n\n" + b)
                .orElse("暂无相关信息");

        // 3. 构建最终 Prompt：强制要求 AI 基于 searchKey 回答
        String finalSystemPrompt = systemPrompt +
                "\n\n【知识库参考资料 (RAG Context)】:\n" + context +
                "\n\n================ 操作规则 ================" +
                "\n1. 你的任务是根据参考资料回答用户问题，并协助下单。" +
                "\n2. 【RAG 优先】回答商品价格、口味时，必须基于知识库资料。" +
                "\n3. 【下单检测】当用户想要下单时：" +
                "\n   - 第一步：检查参考资料中该商品是否有‘规格’（如冷/热，大/中杯）。" +
                "\n   - 第二步（规格缺失）：如果需要选规格但用户没说，请**追问用户**，不要调用工具。" +
                "\n   - 第三步（信息完整）：如果商品无规格，或用户已明确规格，请调用工具 `createOrderCard`。" +
                "\n4. 【输出格式】如果成功调用了工具，请将工具返回的 JSON 结果放在 Markdown 代码块中，格式如下：" +
                "\n   ```json" +
                "\n   { 工具返回的JSON数据 }" +
                "\n   ```" +
                "\n   并在后面附带一句：“已为您生成订单，请点击上方卡片支付。”";

        // 4. 发送给 AI：使用 searchKey (重写后的明确问题) 作为用户输入
        return chatClient.prompt()
                .system(finalSystemPrompt)
                .user(userQuestion) // <--- 关键修改：让 AI 看到明确的 "可颂多少钱"
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }


    /**
     * 获取指定会话的聊天记录
     *
     * @param chatId 会话ID
     * @return 历史消息列表
     */
    public List<Map<String, String>> getHistory(Long chatId) {
        String conversationId = String.valueOf(chatId);
        List<Message> messages = chatMemory.get(conversationId);

        if (messages == null) {
            return new ArrayList<>();
        }

        // 将 Message 对象转换为前端友好的 Map 结构
        return messages.stream().map(msg -> {
            Map<String, String> map = new HashMap<>();
            // 区分角色：USER (用户) 或 ASSISTANT (AI)
            map.put("role", msg.getMessageType() == MessageType.USER ? "user" : "assistant");
            // 获取内容，虽然之前 getContent 有争议，但在 Service 内部处理时我们尽量使用 toString 解析或 content
            // 这里我们使用 toString 并在 Controller 层做更细致的处理，或者如果版本允许直接用 getContent
            // 为了稳妥，这里先简单通过 toString 提取或者假设 getContent 可用
            // 注意：由于之前遇到 getContent 问题，这里我们可以做一个简单的 toString 提取逻辑作为保底
            String content = msg.toString();
            // 简单的正则提取或者清洗逻辑，实际项目中建议排查依赖版本直接用 getContent()
            // 临时方案：如果 toString 是 AssistantMessage [textContent=...] 格式
            if (content.contains("textContent=")) {
                int start = content.indexOf("textContent=") + 12;
                int end = content.indexOf(", metadata=");
                if (end > start) {
                    content = content.substring(start, end);
                }
            } else if (content.contains("content='")) {
                int start = content.indexOf("content='") + 9;
                int end = content.lastIndexOf("'"); // 简单处理，可能有风险
                if (end > start) {
                    content = content.substring(start, end);
                }
            }

            map.put("content", content);
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 清除指定会话的聊天记录
     * @param chatId 会话ID
     */
    public void clearHistory(Long chatId) {
        String conversationId = String.valueOf(chatId);
        chatMemory.clear(conversationId);
        log.info("Cleared chat history for conversationId: {}", conversationId);
    }
}