package com.coffee.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
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
import java.util.List;

/**
 * AI 服务类：负责与向量数据库交互
 */
@Service
@Slf4j
public class CoffeeAiService {


    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    // 增加一个简单的 ChatClient，用于非 RAG 任务（如查询重写）
    private final ChatClient simpleChatClient;
    private final ProductService productService;
    private final SkuStockService skuStockService;
    private final ChatMemory chatMemory; // 保存引用以便手动访问

    // 文本切分器，用于将长文档切分成小块，提高 RAG 效果
    private final TokenTextSplitter textSplitter = new TokenTextSplitter();

    public CoffeeAiService(VectorStore vectorStore,
                           ChatClient.Builder chatClientBuilder,
                           ProductService productService,
                           SkuStockService skuStockService,
                           ChatMemory chatMemory) {
        this.vectorStore = vectorStore;
        this.productService = productService;
        this.skuStockService = skuStockService;
        this.chatMemory = chatMemory;

        // 1. 初始化简单的 ChatClient (用于查询重写)
        this.simpleChatClient = chatClientBuilder.build();

        // 2. 初始化 RAG ChatClient
        VectorStoreDocumentRetriever vectorStoreDocumentRetriever = VectorStoreDocumentRetriever
                .builder()
                .vectorStore(vectorStore)
                .topK(3)
                .similarityThreshold(0.5)
                .build();
        
        // 注意：这里我们移除了 RetrievalAugmentationAdvisor 的默认配置，
        // 改为在 chat 方法中动态构建，以便支持 rewrite 后的查询
        this.chatClient = chatClientBuilder
                .defaultAdvisors(PromptChatMemoryAdvisor.builder(chatMemory).build())
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
                StringBuilder sb = new StringBuilder();
                sb.append("商品名称: ").append(product.getName()).append("\n");
                sb.append("基础价格: ￥").append(product.getPrice()).append("\n");
                sb.append("商品描述: ").append(product.getDescription()).append("\n");

                if (!skus.isEmpty()) {
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
                } else {
                    sb.append("规格信息: 本品为标准规格，暂无其他可选属性。\n");
                }

                // 4. 创建文档对象，并设置固定 ID 以实现“存在即更新，不存在则插入”
                // 使用 product_ 前缀加上商品 ID，确保唯一且可追溯
                Document document = new Document("doc_prod_" + product.getId(), sb.toString(), new java.util.HashMap<>());
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
     * 聊天
     *
     * @param systemPrompt
     * @param userQuestion
     * @return
     */
    public String chat(String systemPrompt, String userQuestion, Long chatId) {
        String conversationId = String.valueOf(chatId);

        String searchKey = userQuestion;

        try {
            // 修正：ChatMemory.get 只接受 conversationId
            List<org.springframework.ai.chat.messages.Message> history = chatMemory.get(conversationId);
            
            if (history != null && !history.isEmpty() && userQuestion.length() < 10) {
                StringBuilder historyText = new StringBuilder();
                for (org.springframework.ai.chat.messages.Message msg : history) {
                   historyText.append(msg.getMessageType()).append(": ").append(msg.toString()).append("\n");
                }
                
                // 优化 Prompt，强制要求简洁
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
                
                // 清理可能的多余字符
                if (rewritten != null) {
                    rewritten = rewritten.replace("改写结果:", "").trim();
                    if (!rewritten.isEmpty()) {
                        searchKey = rewritten;
                        log.info("Query Rewrite Success: '{}' -> '{}'", userQuestion, searchKey);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Query rewrite failed: " + e.getMessage());
        }

        // 修正：SearchRequest 构建方式，使用 builder
        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder().query(searchKey).topK(3).build());

        String context = documents.stream()
                .map(doc -> doc.getText()) 
                .reduce((a, b) -> a + "\n\n" + b)
                .orElse("暂无相关信息");

        String finalSystemPrompt = systemPrompt + "\n\n【参考资料】:\n" + context + "\n\n请根据参考资料和历史对话回答问题。";

        return chatClient.prompt()
                .system(finalSystemPrompt)
                .user(userQuestion)
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }
}
