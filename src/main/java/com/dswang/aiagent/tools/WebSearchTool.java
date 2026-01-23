package com.dswang.aiagent.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 网页搜索工具
 */
@Slf4j
public class WebSearchTool {

    // SearchAPI 的搜索接口地址
    private static final String SEARCH_API_URL = "https://www.searchapi.io/api/v1/search";

    private final String apiKey;

    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }

    @Tool(description = "Search for information from Baidu Search Engine", returnDirect = true)
    public String searchWeb(
            @ToolParam(description = "Search query keyword") String query) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("q", query);
        paramMap.put("api_key", apiKey);
        paramMap.put("engine", "baidu");
        try {
            String response = HttpUtil.get(SEARCH_API_URL, paramMap);
            JSONObject jsonObject = JSONUtil.parseObj(response);
            JSONArray organicResults = jsonObject.getJSONArray("organic_results");

            if (organicResults == null || organicResults.isEmpty()) {
                return JSONUtil.toJsonStr(Map.of(
                        "query", query,
                        "engine", "baidu",
                        "results", List.of(),
                        "note", "No organic_results returned by provider"
                ));
            }

            int limit = Math.min(5, organicResults.size());
            List<Object> objects = organicResults.subList(0, limit);

            // 只返回模型真正需要的字段，避免返回整段 JSON 导致上下文爆炸
            List<Map<String, Object>> results = objects.stream().map(obj -> {
                JSONObject o = (JSONObject) obj;
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("title", o.getStr("title"));
                item.put("link", o.getStr("link"));
                item.put("snippet", o.getStr("snippet"));
                return item;
            }).collect(Collectors.toList());

            String result = JSONUtil.toJsonStr(Map.of(
                    "query", query,
                    "engine", "baidu",
                    "results", results
            ));
            log.info("7.web search success");
            return result;
        } catch (Exception e) {
            // 返回结构化错误，方便模型停止重试并给出兜底方案
            return JSONUtil.toJsonStr(Map.of(
                    "query", query,
                    "engine", "baidu",
                    "error", "Error searching Baidu",
                    "message", String.valueOf(e.getMessage())
            ));
        }
    }
}
