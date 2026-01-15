package com.dswang.aiagent.rag.etl;


import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.KeywordMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MyKeywordEnricher {

    private final ChatModel chatModel;

    public MyKeywordEnricher(ChatModel dashscopeChatModel) {
        this.chatModel = dashscopeChatModel;
    }

    public List<Document> enrichDocuments(List<Document> documents) {
        //为每个文档提炼五个关键词
        KeywordMetadataEnricher enricher = new KeywordMetadataEnricher(chatModel,5);

        return enricher.apply(documents);
    }
}