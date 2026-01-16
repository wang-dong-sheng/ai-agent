package com.dswang.aiagent.app;/**
 * @author Mr.Wang
 * @create 2025-05-24-16:31
 */

import com.dswang.aiagent.advisor.MyLoggerAdvisor;
import com.dswang.aiagent.chatMemory.FileBasedChatMemory;
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
    private QueryRewriter queryRewriter;

//
//    private MysqlChatMemory mysqlChatMemory;    @Resource

    @Resource
    private Advisor loveAppRagCloudAdvisor;

    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    public LoveApp(ChatModel dashscopeChatModel) {
//        基于文件的持久化
        String fileDir = System.getProperty("user.dir")+"/temp/chat-memory";
        FileBasedChatMemory fileBasedChatMemory = new FileBasedChatMemory(fileDir);
//        MysqlChatMemory mysqlChatMemory=new MysqlChatMemory();
        chatClientBuilder = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(fileBasedChatMemory),
//                        自定义日志拦截器
                        new MyLoggerAdvisor()
////                        自定义增强Advisor
//                        new ReReadingAdvisor()
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
    public LoveReport doChatWithReport(String message, String chatId) {
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

    public Flux<String> doChatStreamWithReport(String message, String chatId) {
        Flux<String> content = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .stream()
                .content();
        return content;
    }


    public String doChat(String message, String chatId) {
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


    public String doChatWithRagRAA(String message, String chatId) {
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
}


