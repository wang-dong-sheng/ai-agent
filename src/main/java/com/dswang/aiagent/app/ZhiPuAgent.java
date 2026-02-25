package com.dswang.aiagent.app;

import com.dswang.aiagent.advisor.MyLoggerAdvisor;
import com.dswang.aiagent.chatMemory.PostgresChatMemory;
import com.dswang.aiagent.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.ai.zhipuai.api.ZhiPuAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;
@Slf4j
@Component
public class ZhiPuAgent {
    private final ChatClient chatClient;
    private final ChatClient.Builder chatClientBuilder;
    @Resource(name = "pgVectorVectorStore")
    private VectorStore loveAppVectorStore;
    @Resource
    private ToolCallback[] allTools;
    @Resource
    private QueryRewriter queryRewriter;
    @Value("${ZHIPUAI_API_KEY}")
    private String apiKey;

    private void test(){
        ZhiPuAiApi zhiPuAiApi = new ZhiPuAiApi(apiKey);

        ZhiPuAiChatModel zhiPuAiChatModel = new ZhiPuAiChatModel(zhiPuAiApi, ZhiPuAiChatOptions.builder()
                .model(ZhiPuAiApi.ChatModel.GLM_4_Air.getValue())
                .temperature(0.4)
                .maxTokens(200)
                .build());


    }

    public ZhiPuAgent(PostgresChatMemory postgresChatMemory) {
        // 使用PostgreSQL数据库持久化聊天记录

        ZhiPuAiApi zhiPuAiApi = new ZhiPuAiApi(apiKey);

        ZhiPuAiChatModel zhiPuAiChatModel = new ZhiPuAiChatModel(zhiPuAiApi, ZhiPuAiChatOptions.builder()
                .model(ZhiPuAiApi.ChatModel.GLM_4_Air.getValue())
                .temperature(0.4)
                .maxTokens(200)
                .build());

        chatClientBuilder = ChatClient.builder(zhiPuAiChatModel)
//                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(postgresChatMemory),
                        // 自定义日志拦截器
                        new MyLoggerAdvisor()
                        // 自定义增强Advisor
                        // new ReReadingAdvisor()
                )
        ;
        chatClient = chatClientBuilder.build();
    }
    public String doChatWithRag(String message, String chatId) {

        //1.对用户消息进行重写，专业化，让大模型更容易理解

        String transMessage = queryRewriter.doQueryRewrite(message);
        // 使用本地 pgVector 向量数据库做 RAG，检索 top 5 相关文档
        SearchRequest searchRequest = SearchRequest.builder()
                .query(transMessage)
                .topK(5)
                .build();

        ChatResponse response = chatClient
                .prompt()
                .user(transMessage)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new MyLoggerAdvisor())
                // 基于本地向量库（pgvector）的 RAG，并指定检索 top 5
                .advisors(new QuestionAnswerAdvisor(loveAppVectorStore, searchRequest))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    public Flux<String> doChatWithRagStream(String message, String chatId, Long userId) {

        //1.对用户消息进行重写，专业化，让大模型更容易理解

        String transMessage = queryRewriter.doQueryRewrite(message);
        // 使用本地 pgVector 向量数据库做 RAG，检索 top 5 相关文档
        SearchRequest searchRequest = SearchRequest.builder()
                .query(transMessage)
                .topK(5)
                .build();

        // 获取 PostgresChatMemory 实例来设置 userId
        return chatClient
                .prompt()
                .user(transMessage)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new MyLoggerAdvisor())
                // 基于本地向量库（pgvector）的 RAG，并指定检索 top 5
                .advisors(new QuestionAnswerAdvisor(loveAppVectorStore, searchRequest))
                .stream()
                .content();

    }

}
