package com.dswang.aiagent.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.transformer.SummaryMetadataEnricher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
class EnricherConfig {

    @Bean
    public SummaryMetadataEnricher summaryMetadata(ChatModel dashscopeChatModel) {
        //使用 AI来根据文档内容进行文档增强
        return new SummaryMetadataEnricher(dashscopeChatModel,
            List.of(SummaryMetadataEnricher.SummaryType.PREVIOUS, SummaryMetadataEnricher.SummaryType.CURRENT, SummaryMetadataEnricher.SummaryType.NEXT));
    }
}