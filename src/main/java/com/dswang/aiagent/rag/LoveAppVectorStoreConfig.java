package com.dswang.aiagent.rag;/**
 * @author Mr.Wang
 * @create 2025-06-29-22:37
 */

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 *@ClassName LoveAppVectorStoreConfig
 *@Description 内存向量库工具
 *@Author Mr.Wang
 *@Date 2025/6/29 22:37
 *@Version 1.0
 */
@Configuration
public class LoveAppVectorStoreConfig {
    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;
    @Bean
     VectorStore loveAppVectorStore(EmbeddingModel embeddingModel){
        List<Document> documentList = loveAppDocumentLoader.loadDocument();
        SimpleVectorStore simpleVectorStore= SimpleVectorStore.builder(embeddingModel).build();
        simpleVectorStore.doAdd(documentList);
        return simpleVectorStore;
    }
}


