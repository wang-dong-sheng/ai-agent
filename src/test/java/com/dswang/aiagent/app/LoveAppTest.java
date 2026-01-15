package com.dswang.aiagent.app;

import com.dswang.aiagent.rag.LoveAppDocumentLoader;
import com.dswang.aiagent.rag.etl.MyKeywordEnricher;
import com.dswang.aiagent.rag.etl.MySummaryEnricher;
import com.dswang.aiagent.rag.etl.MyTokenTextSplitter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * @author Mr.Wang
 * @create 2025-05-24-16:48
 */
@SpringBootTest
@Slf4j
//@MapperScan("com.dswang.aiagent.mapper")  // 扫描生成的mapper
class LoveAppTest {
    @Resource
    private LoveApp loveApp;

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;
    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;
    @Resource
    private MyKeywordEnricher myKeywordEnricher;
    @Resource
    private MySummaryEnricher mySummaryEnricher;

    @Resource(name = "pgVectorVectorStore")
    private VectorStore vectorStore;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Test
    void doChat() {
        String appId = UUID.randomUUID().toString();
//        第一轮
        String massage="我是张三";
        String answer = loveApp.doChat(massage, appId);
        log.info("第一轮回答:{}",answer);
//        第二轮
        String massage2="我的爱人是李四";
        String answer2 = loveApp.doChat(massage2, appId);
        log.info("第二轮回答:{}",answer2);
//        第三轮
        String massage3="我的另一半是什么来着？刚给你说过，帮我回忆一下";
        String answer3 = loveApp.doChat(massage3, appId);
        log.info("第三轮回答:{}",answer3);


    }

    @Test
    void doChatWithReport(){
        String appId = UUID.randomUUID().toString();
//        第一轮
        String massage="你好我是张三，我想让我的另一半李四更爱我，我该怎么办";
        LoveApp.LoveReport loveReport = loveApp.doChatWithReport(massage, appId);
        Assertions.assertNotNull(loveReport);
    }


    @Test
    void doChatStreamWithReport(){
        String appId = UUID.randomUUID().toString();
//        第一轮
        String massage="你好我是张三，我想让我的另一半李四更爱我，我该怎么办";
        StringBuilder result = new StringBuilder();
        Flux<String> stringFlux = loveApp.doChatStreamWithReport(massage, appId);
        stringFlux.doOnNext(chunk -> {
                    log.info(chunk); // 实时输出
                    result.append(chunk);
                })
                .blockLast(); // 等待流完成

        log.info("完整流式输出: {}", result.toString());
//        Assertions.assertNotNull(loveReport);
    }

    @Test
    void doChatWithRag() {
        String appId = UUID.randomUUID().toString();
//        第一轮
        String massage="我是已婚人士，我应该怎么处理好伴侣关系，我该怎么办";
        String s = loveApp.doChatWithRag(massage, appId);
        Assertions.assertNotNull(s);

    }

    @Test
    void loadDocumentsToVectorStore() {
        try {

            // 1. E、加载 document 文件夹中的所有 md 文档
            log.info("开始加载文档...");
            List<Document> documents = loveAppDocumentLoader.loadDocument();
            Assertions.assertNotNull(documents);
            Assertions.assertFalse(documents.isEmpty());
            log.info("成功加载 {} 个文档片段", documents.size());


            //2. T 转换
            log.info("开始转换文档...");
            // 文档分割
            List<Document> transDocumentList = myTokenTextSplitter.splitDocuments(documents);
            //文档转换增强
            List<Document> enrichDocuments = myKeywordEnricher.enrichDocuments(transDocumentList);

            HashMap<String, String> addMeteData = new HashMap<>();
            addMeteData.put("userId", "123456789");
            List<Document> enrichDocuments1 = mySummaryEnricher.enrichDocuments(transDocumentList,addMeteData);

            // 2. 将文档添加到向量数据库
            log.info("开始将文档存入向量数据库...");
            log.info("向量存储类型: {}", vectorStore.getClass().getName());
            try {
                // 注意：add() 方法会为每个文档生成嵌入向量，这可能需要一些时间
                log.info("正在添加文档并生成向量嵌入（这可能需要一些时间）...");
                vectorStore.add(enrichDocuments1);
                log.info("add() 方法调用完成，已添加 {} 个文档", enrichDocuments1.size());
            } catch (Exception e) {
                log.error("添加文档到向量数据库时发生异常", e);
                log.error("异常详情: {}", e.getMessage(), e);
                throw e;
            }

            // 5. 验证文档是否成功存入（直接查询数据库）
            log.info("开始验证文档是否成功存入向量数据库...");

            log.info("文档ETL完成，已成功存入向量数据库");
        } catch (Exception e) {
            log.error("文档ETL过程中发生异常", e);
            throw e;
        }
    }

    @Test
    void testSearch() {
        SearchRequest request = SearchRequest.builder()
                .query("怎么维护一段关系")
                .topK(5)                  // 返回最相似的5个结果
//                .similarityThreshold(0.7) // 相似度阈值，0.0-1.0之间
                .filterExpression("userId == '123456789'")  // 过滤表达式
                .build();

        List<Document> results = vectorStore.similaritySearch(request);
        log.info("搜索结果: {}", results);

    }
}