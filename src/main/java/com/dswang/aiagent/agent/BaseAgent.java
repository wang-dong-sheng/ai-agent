package com.dswang.aiagent.agent;

import com.dswang.aiagent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.internal.StringUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * 抽象基础代理类，用于管理代理状态和执行流程。  
 *   
 * 提供状态转换、内存管理和基于步骤的执行循环的基础功能。  
 * 子类必须实现step方法。  
 */  
@Data
@Slf4j
public abstract class BaseAgent {  
  
    // 核心属性  
    private String name;  
  
    // 提示  
    private String systemPrompt;  
    private String nextStepPrompt;  
  
    // 状态  
    private AgentState state = AgentState.IDLE;
  
    // 执行控制  
    private int maxSteps = 5;
    private int currentStep = 0;  
  
    // LLM  
    private ChatClient chatClient;
  
    // Memory（需要自主维护会话上下文）  
    private List<Message> messageList = new ArrayList<>();
  
    /**  
     * 运行代理  
     *  
     * @param userPrompt 用户提示词  
     * @return 执行结果  
     */  
    public String run(String userPrompt) {  
        if (this.state != AgentState.IDLE) {  
            throw new RuntimeException("Cannot run agent from state: " + this.state);  
        }  
        if (StringUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");  
        }  
        // 更改状态  
        state = AgentState.RUNNING;  
        // 记录消息上下文  
        messageList.add(new UserMessage(userPrompt));
        // 保存结果列表  
        List<String> results = new ArrayList<>();
        try {  
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {  
                int stepNumber = i + 1;  
                currentStep = stepNumber;  
                log.info("Executing step " + stepNumber + "/" + maxSteps);  
                // 单步执行  
                String stepResult = step();  
                String result = "Step " + stepNumber + ": " + stepResult;  
                results.add(result);  
            }  
            // 检查是否超出步骤限制  
            if (currentStep >= maxSteps) {  
                state = AgentState.FINISHED;  
                results.add("Terminated: Reached max steps (" + maxSteps + ")");  
            }  
            return String.join("\n", results);  
        } catch (Exception e) {  
            state = AgentState.ERROR;  
            log.error("Error executing agent", e);  
            return "执行错误" + e.getMessage();  
        } finally {  
            // 清理资源  
            this.cleanup();  
        }  
    }

    public Flux<String> runAsync(String userPrompt) {
        // 参数校验
//




        // 初始化状态
        state = AgentState.RUNNING;
        messageList.add(new UserMessage(userPrompt));

        // 创建一个 Flux 流，用于逐个发射步骤结果
        return Flux.range(0, maxSteps)
                .takeWhile(i -> state != AgentState.FINISHED) // 控制循环条件
                .concatMap(i -> {
                    int stepNumber = i + 1;
                    currentStep = stepNumber;
                    log.info("Executing step " + stepNumber + "/" + maxSteps);

                    // 执行单步逻辑并返回结果
                    return Mono.fromCallable(() -> step())
                            .map(stepResult -> "Step " + stepNumber + ": " + stepResult)
                            .doOnNext(result -> {
                                // 可选：在这里添加中间结果的处理逻辑
                            })
                            .onErrorResume(e -> {
                                // 异常处理
                                state = AgentState.ERROR;
                                log.error("Error executing agent", e);
                                return Mono.just("执行错误: " + e.getMessage());
                            });
                })
                .doFinally(signalType -> {
                    // 最终清理资源
                    cleanup();
                    if (currentStep >= maxSteps) {
                        state = AgentState.FINISHED;
                    }
                });
    }


    /**  
     * 执行单个步骤  
     *  
     * @return 步骤执行结果  
     */  
    public abstract String step();  
  
    /**  
     * 清理资源  
     */  
    protected void cleanup() {  
        // 重置状态为 IDLE，以便代理可以再次执行
        this.state = AgentState.IDLE;
        // 清空消息列表，以便下次执行时使用新的上下文
        this.messageList.clear();
        // 重置当前步骤计数
        this.currentStep = 0;
        // 子类可以重写此方法来清理资源  
    }  
}
