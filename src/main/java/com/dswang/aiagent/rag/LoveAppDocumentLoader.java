package com.dswang.aiagent.rag;/**
 * @author Mr.Wang
 * @create 2025-06-29-22:00
 */

import io.swagger.v3.oas.models.info.License;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *@ClassName LoveAppLoader
 *@Description 资源加载器
 *@Author Mr.Wang
 *@Date 2025/6/29 22:00
 *@Version 1.0
 */
@Component
@Slf4j
public class LoveAppDocumentLoader {
    private final ResourcePatternResolver resourcePatternResolver;

    public LoveAppDocumentLoader( ResourcePatternResolver resourcePatternResolver){
        this.resourcePatternResolver=resourcePatternResolver;
    }

    public List<Document> loadDocument(){
        List<Document> allDocument=new ArrayList<>();
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            for (Resource resource : resources) {
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("filename", resource.getFilename())
                        .build();
                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                List<Document> currentDocuments = reader.get();
                allDocument.addAll(currentDocuments);
            }
        } catch (IOException e) {
            log.error("文档加载失败");
        }
        return allDocument;
    }
}


