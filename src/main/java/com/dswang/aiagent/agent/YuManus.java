package com.dswang.aiagent.agent;


import com.dswang.aiagent.advisor.MyLoggerAdvisor;
import com.dswang.aiagent.chatMemory.PostgresChatMemory;
import com.dswang.aiagent.rag.QueryRewriter;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Component
public class YuManus extends ToolCallAgent {

    private final PostgresChatMemory postgresChatMemory;
    @Resource(name = "pgVectorVectorStore")
    private VectorStore vectorStore;
    @Resource
    private QueryRewriter queryRewriter;

    private final ChatModel chatModel;
    private ChatClient ragChatClient;

    public YuManus(ToolCallback[] allTools, ChatModel dashscopeChatModel, PostgresChatMemory postgresChatMemory) {
        super(allTools);
        this.postgresChatMemory = postgresChatMemory;
        this.chatModel = dashscopeChatModel;
        this.setName("yuManus");
        String SYSTEM_PROMPT = """
                You are YuManus, an all-capable AI assistant, aimed at solving any task presented by the user.
                You have various tools at your disposal that you can call upon to efficiently complete complex requests.
                You can also retrieve relevant information from a knowledge base to provide accurate and up-to-date answers.
                """;
        SYSTEM_PROMPT+=  """
                你可以调用工具来完成任务，但必须遵守以下规则：
                - 最多调用工具 3 次（总次数，不是每个工具）。
                - 调用工具时分析用户语言，采用最核心的工具最先调用原则
                - 工具返回后必须先总结要点，再给出最终可执行的答案；不要重复搜索/抓取同一件事。
                - 如果工具连续返回错误、或信息不足以继续：直接给出不依赖工具的备选方案，并调用 doTerminate 结束。
                """;
        this.setSystemPrompt(SYSTEM_PROMPT);
        String NEXT_STEP_PROMPT = """
                Based on user needs, proactively select the most appropriate tool or combination of tools.
                For complex tasks, you can break down the problem and use different tools step by step to solve it.
                After using each tool, clearly explain the execution results and suggest the next steps.
                You can also retrieve relevant information from the knowledge base when needed.
                If you want to stop the interaction at any point, use the `terminate` tool/function call.
                """;
        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        this.setMaxSteps(5);
        // 初始化客户端（用于工具调用）
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor(),
                        new MessageChatMemoryAdvisor(postgresChatMemory))
                .build();
        this.setChatClient(chatClient);
    }

    /**
     * 初始化 RAG 专用的 ChatClient
     * 在依赖注入完成后执行
     */
    @PostConstruct
    public void initRagChatClient() {
        if (vectorStore != null && postgresChatMemory != null) {
            // 创建带有 RAG 功能的 ChatClient
            this.ragChatClient = ChatClient.builder(chatModel)
                    .defaultAdvisors(
                            new MyLoggerAdvisor(),
                            new MessageChatMemoryAdvisor(postgresChatMemory)
                    )
                    .build();
        }
    }

    /**
     * 使用 RAG 的聊天方法
     * 会先重写查询，然后从向量库检索相关文档，最后结合上下文回答
     */
    public String chatWithRag(String message, String chatId) {
        // 1. 对用户消息进行重写，专业化，让大模型更容易理解
        String transMessage = queryRewriter.doQueryRewrite(message);

        // 2. 构建向量搜索请求，检索 top 5 相关文档
        SearchRequest searchRequest = SearchRequest.builder()
                .query(transMessage)
                .topK(5)
                .build();

        // 3. 使用 RAG 进行对话
        return ragChatClient
                .prompt()
                .user(transMessage)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new QuestionAnswerAdvisor(vectorStore, searchRequest))
                .call()
                .content();
    }

    /**
     * 使用 RAG 的流式聊天方法
     */
    public reactor.core.publisher.Flux<String> chatWithRagStream(String message, String chatId) {
        // 1. 对用户消息进行重写
        String transMessage = queryRewriter.doQueryRewrite(message);

        // 2. 构建向量搜索请求
        SearchRequest searchRequest = SearchRequest.builder()
                .query(transMessage)
                .topK(5)
                .build();

        // 3. 使用 RAG 进行流式对话
        return ragChatClient
                .prompt()
                .user(transMessage)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new QuestionAnswerAdvisor(vectorStore, searchRequest))
                .stream()
                .content();
    }
}
