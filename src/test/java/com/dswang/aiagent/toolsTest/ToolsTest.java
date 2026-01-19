package com.dswang.aiagent.toolsTest;

import com.dswang.aiagent.app.LoveApp;
import com.dswang.aiagent.rag.LoveAppDocumentLoader;
import com.dswang.aiagent.rag.etl.MyKeywordEnricher;
import com.dswang.aiagent.rag.etl.MySummaryEnricher;
import com.dswang.aiagent.rag.etl.MyTokenTextSplitter;
import com.dswang.aiagent.tools.*;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Mr.Wang
 * @create 2025-06-29-22:16
 */
@SpringBootTest
class ToolsTest {
    @Resource
    private LoveApp loveApp;
    @Value("${search-api.api-key}")
    private String apiKey;

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


    @Test
    void ReadFile() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        String s = fileOperationTool.readFile("测试.txt");
        System.out.println(s);
    }

    @Test
    void WriteFile() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        fileOperationTool.writeFile("测试.txt", "测试内容");
        System.out.println("写入成功");
    }

    @Test
    void WebSearch() {
        WebSearchTool webSearchTool = new WebSearchTool(apiKey);
        String s = webSearchTool.searchWeb("今天上海的天气情况");
        System.out.println(s);
    }
    @Test
    void webScraping() {
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        String s = webScrapingTool.scrapeWebPage("https://www.baidu.com");
        System.out.println(s);
    }
    @Test
    void terminalOperationToolTest(){
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        String s = terminalOperationTool.executeTerminalCommand("dir");
        System.out.println(s);
    }
    @Test
    void downloadFile() {
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        String s = resourceDownloadTool.downloadResource("https://www.baidu.com/img/bd_logo1.png", "bd_logo1.png");
        System.out.println(s);
    }
    @Test
    void pdfTest(){
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        String s = pdfGenerationTool.generatePDF("测试.pdf", "小测工具pdf");
        System.out.println(s);
    }

}