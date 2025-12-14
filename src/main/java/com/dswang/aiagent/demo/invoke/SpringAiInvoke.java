package com.dswang.aiagent.demo.invoke;/**
 * @author Mr.Wang
 * @create 2025-05-24-14:10
 */

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 *@ClassName SpringAiInvoke
 *@Description TODO
 *@Author Mr.Wang
 *@Date 2025/5/24 14:10
 *@Version 1.0
 */
//CommandLineRunner表示项目运行时执行一次run方法
@Component
public class SpringAiInvoke implements CommandLineRunner {
    @Resource
    private ChatModel dashscopeChatModel;

    @Override
    public void run(String... args) throws Exception {
        AssistantMessage message = dashscopeChatModel.call(new Prompt("你好,你只能说今天天气很好"))
                .getResult()
                .getOutput();
        System.out.println(message.getText());
    }
}


