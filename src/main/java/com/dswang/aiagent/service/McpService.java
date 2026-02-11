//package com.dswang.aiagent.service;
//
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.mcp.client.McpClient;
//import org.springframework.ai.mcp.client.function.McpFunctionCallback;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//
//@Service
//public class McpService {
//
//    private final McpClient mcpClient;
//    private final ChatClient chatClient;
//
//    public McpService(McpClient mcpClient, ChatClient.Builder chatClientBuilder) {
//        this.mcpClient = mcpClient;
//        // 注册MCP工具到ChatClient
//        this.chatClient = chatClientBuilder
//                .defaultFunctions(mcpClient.listTools(null)
//                        .tools()
//                        .stream()
//                        .map(tool -> new McpFunctionCallback(mcpClient, tool))
//                        .toArray(McpFunctionCallback[]::new))
//                .build();
//    }
//
//    /**
//     * 使用MCP服务进行查询
//     * @param prompt 查询提示
//     * @return 查询结果
//     */
//    public String queryWithMcp(String prompt) {
//        return chatClient
//                .prompt(prompt)
//                .call().content();
//    }
//
//    /**
//     * 列出所有可用的MCP工具
//     * @return 工具信息
//     */
//    public String listAvailableTools() {
//        var tools = mcpClient.listTools(null).tools();
//        StringBuilder sb = new StringBuilder();
//        sb.append("Available MCP tools:\n");
//        tools.forEach(tool -> {
//            sb.append("- " + tool.name() + ": " + tool.description() + "\n");
//        });
//        return sb.toString();
//    }
//}
