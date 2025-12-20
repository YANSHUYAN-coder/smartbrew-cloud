package com.coffee.ai.service;


import com.coffee.system.domain.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 服务类：负责与向量数据库交互
 */
@Service
@Slf4j
public class CoffeeAiService {


    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public CoffeeAiService(VectorStore vectorStore, ChatClient.Builder ChatClientBuilder) {
        this.vectorStore = vectorStore;
        VectorStoreDocumentRetriever vectorStoreDocumentRetriever = VectorStoreDocumentRetriever
                .builder()
                .vectorStore(vectorStore)
                .topK(3)
                .similarityThreshold(0.5)
                .build();
        RetrievalAugmentationAdvisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor
                .builder()
                .documentRetriever(vectorStoreDocumentRetriever)
                .build();
        this.chatClient = ChatClientBuilder
                .defaultAdvisors(retrievalAugmentationAdvisor)
                .build();
    }


    public String importData() {
        try {
            // 读取classPath下的QA.csv文件
            ClassPathResource resource = new ClassPathResource("qa_data.csv");
            InputStreamReader reader = new InputStreamReader(resource.getInputStream());

            // 使用Apache CSV工具类读取文件
            CSVParser csvParser = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(reader);
            List<Document> documents = new ArrayList<>();
            for (CSVRecord record : csvParser) {
                // 获取问题和回答字段
                String question = record.get(0); // 第一列：问题
                String answer = record.get(1);

                // 将问题和回答构建Document对象
                String content = "问题: " + question + "\n答案: " + answer;

                // 创建Document对象
                Document document = new Document(content);

                // 添加到文档列表中
                documents.add(document);
            }

            // 关闭解析器
            csvParser.close();

            // 将文档列表添加到向量数据库中
            vectorStore.add(documents);

            return "成功导入 " + documents.size() + " 条数据到向量数据库";
        } catch (IOException e) {
            e.printStackTrace();
            return "导入数据失败: " + e.getMessage();
        }
    }

    // 相似度搜索
    public List<Document> search(String query) {
        SearchRequest searchRequest = SearchRequest.builder()
                // 返回前 2 条结果
                .topK(2)
                .query(query)
                .build();
        return vectorStore.similaritySearch(searchRequest);
    }

    /**
     * 聊天
     * @param systemPrompt
     * @param userQuestion
     * @return
     */
    public String chat(String systemPrompt, String userQuestion) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(userQuestion)
                .call()
                .content();
    }
}