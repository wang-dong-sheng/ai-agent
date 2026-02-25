package com.dswang.aiagent.controller;

import com.dswang.aiagent.agent.YuManus;
import com.dswang.aiagent.app.LoveApp;
import com.dswang.aiagent.chatMemory.PostgresChatMemory;
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

    @Resource
    private PostgresChatMemory postgresChatMemory;

    @GetMapping("/love_app/chat/sync")
    public String doChatWithLoveAppSync(@RequestParam("message") String message, 
                                       @RequestParam("chatId") String chatId, 
                                       @RequestParam("userId") Long userId) {
        try {
            postgresChatMemory.setCurrentUserId(userId);
            return loveApp.doChat(message, chatId, userId);
        } finally {
            postgresChatMemory.clearCurrentUserId();
        }
    }

    @GetMapping(value = "/love_app/chat/tool/sse")
    public Flux<ServerSentEvent<String>> doChatWithLoveAppSSE(@RequestParam("message") String message,
                                                             @RequestParam("chatId") String chatId,
                                                             @RequestParam("userId") Long userId) {
        postgresChatMemory.setConversationUserId(chatId, userId);
        return loveApp.doChatWithToolsStream(message, chatId, userId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build())
                .doFinally(signalType -> {
                    postgresChatMemory.clearConversationUserId(chatId);
                });
    }

//    @GetMapping(value = "/love_app/chat/agent/sse")
//    public Flux<ServerSentEvent<String>> doChatWithRagStream(@RequestParam("message") String message,
//                                                            @RequestParam("chatId") String chatId,
//                                                            @RequestParam("userId") Long userId) {
//        postgresChatMemory.setConversationUserId(chatId, userId);
//        return loveApp.doChatWithRagStream(message, chatId, userId)
//                .map(chunk -> ServerSentEvent.<String>builder()
//                        .data(chunk)
//                        .build())
//                .doFinally(signalType -> {
//                    postgresChatMemory.clearConversationUserId(chatId);
//                });
//    }
//
    @GetMapping(value = "/love_app/chat/agent/sse")
    public Flux<ServerSentEvent<String>> doChatWithAgent(@RequestParam("message") String message,
                                                        @RequestParam("chatId") String chatId,
                                                        @RequestParam("userId") Long userId) {
        postgresChatMemory.setCurrentUserId(userId);
        return yuManus.runAsync(message)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build())
                .doFinally(signalType -> {
                    postgresChatMemory.clearCurrentUserId();
                });
    }

    @GetMapping(value = "/test")
    public String test() {
        return "Test successful!";
    }


}
