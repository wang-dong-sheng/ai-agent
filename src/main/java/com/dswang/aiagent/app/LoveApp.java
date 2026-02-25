package com.dswang.aiagent.app;/**
 * @author Mr.Wang
 * @create 2025-05-24-16:31
 */

import com.dswang.aiagent.advisor.MyLoggerAdvisor;
import com.dswang.aiagent.chatMemory.PostgresChatMemory;
import com.dswang.aiagent.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * @ClassName LoveApp
 * @Description TODO
 * @Author Mr.Wang
 * @Date 2025/5/24 16:31
 * @Version 1.0
 */
@Component
@Slf4j
public class LoveApp {


    private final ChatClient chatClient;
    private final ChatClient.Builder chatClientBuilder;
    @Resource(name = "pgVectorVectorStore")
    private VectorStore loveAppVectorStore;
    @Resource
    private ToolCallback[] allTools;


    @Resource
    private QueryRewriter queryRewriter;

//
//    private MysqlChatMemory mysqlChatMemory;    @Resource

    @Resource
    private Advisor loveAppRagCloudAdvisor;

    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    public LoveApp(ChatModel dashscopeChatModel, PostgresChatMemory postgresChatMemory) {
        // 使用PostgreSQL数据库持久化聊天记录
        // 如果需要切换回文件存储，可以取消下面的注释，并注释掉postgresChatMemory相关代码
        // String fileDir = System.getProperty("user.dir")+"/temp/chat-memory";
        // FileBasedChatMemory fileBasedChatMemory = new FileBasedChatMemory(fileDir);
        
        chatClientBuilder = ChatClient.builder(dashscopeChatModel)
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

    //快速定义一个类
    record LoveReport(String message, List<String> suggestions){}
    /**
     * 演示结构化输出
     * @param message
     * @param chatId
     * @return
     */
    public LoveReport doChatWithReport(String message, String chatId, Long userId) {
        LoveReport loveReport = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(LoveReport.class);
        log.info("loveReport: {}", loveReport);
        return loveReport;
    }

    public Flux<String> doChatStreamWithReport(String message, String chatId, Long userId) {
        Flux<String> content = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .stream()
                .content();
        return content;
    }


    public String doChat(String message, String chatId, Long userId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 2))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    public String doChatWithRag(String message, String chatId, Long userId) {

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


    public String doChatWithRagRAA(String message, String chatId, Long userId) {
        // 使用本地 pgVector 向量数据库做 RAG，检索 top 5 相关文档
        //使用大模型对用户问题进行改写
        Advisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .queryTransformers(RewriteQueryTransformer.builder()
                        .chatClientBuilder(chatClientBuilder.build().mutate())
                        .build())
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.50)
                        .vectorStore(loveAppVectorStore)
                        .build())
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .allowEmptyContext(true)
                        .build())
                .build();


        String answer = chatClient.prompt()
                .advisors(retrievalAugmentationAdvisor)
                .user(message)
                .call()
                .content();



        return answer;
    }

    public Flux<String> doChatWithToolsStream(String message, String chatId, Long userId) {
        String TOOLS_GUARDRAIL = """
                你可以调用工具来完成任务，但必须遵守以下规则：
                - 最多调用工具 3 次（总次数，不是每个工具）。
                - 调用工具时分析用户语言，采用最核心的工具最先调用原则
                - 工具返回后必须先总结要点，再给出最终可执行的答案；不要重复搜索/抓取同一件事。
                - 如果工具连续返回错误、或信息不足以继续：直接给出不依赖工具的备选方案，并调用 doTerminate 结束。
                """;
        Flux<String> content = chatClient
                .prompt()
                .system(TOOLS_GUARDRAIL)
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 2))
                .advisors(new MyLoggerAdvisor())
                .tools(allTools)
                .stream()
                .content();
//        String content = response.getResult().getOutput().getText();
//        log.info("content: {}", content);
        return content;
    }
    public String doChatWithTools(String message, String chatId, Long userId) {
        String TOOLS_GUARDRAIL = """
                你可以调用工具来完成任务，但必须遵守以下规则：
                - 最多调用工具 3 次（总次数，不是每个工具）。
                - 调用工具时分析用户语言，采用最核心的工具最先调用原则
                - 工具返回后必须先总结要点，再给出最终可执行的答案；不要重复搜索/抓取同一件事。
                - 如果工具连续返回错误、或信息不足以继续：直接给出不依赖工具的备选方案，并调用 doTerminate 结束。
                """;
        ChatResponse response = chatClient
                .prompt()
                .system(TOOLS_GUARDRAIL)
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 2))
                .advisors(new MyLoggerAdvisor())
                .tools(allTools)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }
    @Resource
    private ToolCallbackProvider toolCallbackProvider;
    public String doChatWithMCP(String message, String chatId, Long userId) {
        String TOOLS_GUARDRAIL = """
                你可以调用工具来完成任务，但必须遵守以下规则：
                - 最多调用工具 3 次（总次数，不是每个工具）。
                - 调用工具时分析用户语言，采用最核心的工具最先调用原则
                - 工具返回后必须先总结要点，再给出最终可执行的答案；不要重复搜索/抓取同一件事。
                - 如果工具连续返回错误、或信息不足以继续：直接给出不依赖工具的备选方案，并调用 doTerminate 结束。
                """;
        ChatResponse response = chatClient
                .prompt()
//                .system(TOOLS_GUARDRAIL)
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 2))
                .advisors(new MyLoggerAdvisor())
                .tools(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

}


