package com.dswang.aiagent.tools;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.Map;

/**
 * 网页抓取工具
 */
@Slf4j
public class WebScrapingTool {

    @Tool(description = "Scrape the content of a web page")
    public String scrapeWebPage(@ToolParam(description = "URL of the web page to scrape") String url) {
        try {
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (compatible; ai-agent/1.0)")
                    .timeout(15_000)
                    .get();
            log.info("6.Scraped web page successfully");
            // 只返回可读文本并截断，避免把整页 HTML 塞进上下文导致模型反复调用工具
            String title = document.title();
            String text = document.body() == null ? "" : document.body().text();
            text = normalize(text);
            int maxChars = 8000;
            if (text.length() > maxChars) {
                text = text.substring(0, maxChars) + "...(truncated)";
            }
            return cn.hutool.json.JSONUtil.toJsonStr(Map.of(
                    "url", url,
                    "title", title,
                    "content_text", text
            ));
        } catch (Exception e) {
            return cn.hutool.json.JSONUtil.toJsonStr(Map.of(
                    "url", url,
                    "error", "Error scraping web page",
                    "message", String.valueOf(e.getMessage())
            ));
        }
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return s.replaceAll("\\s+", " ").trim();
    }
}
