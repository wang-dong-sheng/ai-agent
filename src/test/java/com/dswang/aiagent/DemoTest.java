package com.dswang.aiagent;

import com.dswang.aiagent.app.LoveApp;
import com.dswang.aiagent.rag.LoveAppDocumentLoader;
import com.dswang.aiagent.rag.demo.MultiQueryExpanderDemo;
import com.dswang.aiagent.rag.etl.MyKeywordEnricher;
import com.dswang.aiagent.rag.etl.MySummaryEnricher;
import com.dswang.aiagent.rag.etl.MyTokenTextSplitter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.rag.Query;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@Slf4j
public class DemoTest {
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
    @Resource
    private MultiQueryExpanderDemo multiQueryExpanderDemo;

    @Resource(name = "pgVectorVectorStore")
    private VectorStore vectorStore;

    @Test
    void testDemo(){
        List<Query> relation = multiQueryExpanderDemo.expand("如何维护一段关系");
        log.info("{}",relation);
    }
}
