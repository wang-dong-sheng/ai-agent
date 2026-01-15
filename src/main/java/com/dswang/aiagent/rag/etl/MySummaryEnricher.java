package com.dswang.aiagent.rag.etl;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.SummaryMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class MySummaryEnricher {

    private final SummaryMetadataEnricher enricher;

    MySummaryEnricher(SummaryMetadataEnricher enricher) {
        this.enricher = enricher;
    }

     public List<Document> enrichDocuments(List<Document> documents,Map<String,String> addMeteData) {
         documents.forEach(document -> addMeteData(document,addMeteData));
        return this.enricher.apply(documents);
    }

    public void addMeteData(Document document,Map<String,String> addMeteData){
        Map<String, Object> metadata = document.getMetadata();
        metadata.putAll(addMeteData);
    }
}