package com.dswang.aiagent.demo.invoke;/**
 * @author Mr.Wang
 * @create 2025-05-24-14:19
 */

import dev.langchain4j.community.model.dashscope.QwenChatModel;

/**
 *@ClassName Langchain4jAiInvoke
 *@Description TODO
 *@Author Mr.Wang
 *@Date 2025/5/24 14:19
 *@Version 1.0
 */
public class Langchain4jAiInvoke {
    public static void main(String[] args) {
        QwenChatModel qwenChatModel = QwenChatModel.builder()
                .apiKey(TestApiKey.API_KEY)
                .modelName("qwen-max")
                .build();
        String test = qwenChatModel.chat("你只能说hello");
        System.out.println(test);
    }
}


