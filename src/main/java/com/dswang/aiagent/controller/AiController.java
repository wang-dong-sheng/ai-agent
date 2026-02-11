package com.dswang.aiagent.controller;

import com.dswang.aiagent.agent.YuManus;
import com.dswang.aiagent.app.LoveApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    @Resource
    private YuManus yuManus;

    @GetMapping("/love_app/chat/sync")
    public String doChatWithLoveAppSync(@RequestParam("message") String message, @RequestParam("chatId") String chatId) {
        return loveApp.doChat(message, chatId);
    }

    @GetMapping(value = "/love_app/chat/tool/sse")
    public Flux<ServerSentEvent<String>> doChatWithLoveAppSSE(@RequestParam("message") String message, @RequestParam("chatId") String chatId) {
        return loveApp.doChatWithToolsStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    @GetMapping(value = "/love_app/chat/rag/sse")
    public Flux<ServerSentEvent<String>> doChatWithRagStream(@RequestParam("message") String message, @RequestParam("chatId") String chatId) {
        return loveApp.doChatWithRagStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    @GetMapping(value = "/love_app/chat/agent/sse")
    public Flux<ServerSentEvent<String>> doChatWithAgent(@RequestParam("message") String message, @RequestParam("chatId") String chatId) {

        return yuManus.runAsync(message).map(chunk -> ServerSentEvent.<String>builder()
                .data(chunk)
                .build());
    }


}
