package com.dswang.aiagent.app;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * @author Mr.Wang
 * @create 2025-05-24-16:48
 */
@SpringBootTest
@Slf4j
@MapperScan("com.dswang.aiagent.mapper")  // 扫描生成的mapper
class LoveAppTest {
    @Resource
    private LoveApp loveApp;

    @Test
    void doChat() {
        String appId = UUID.randomUUID().toString();
//        第一轮
        String massage="我是张三";
        String answer = loveApp.doChat(massage, appId);
        log.info("第一轮回答:{}",answer);
//        第二轮
        String massage2="我的爱人是李四";
        String answer2 = loveApp.doChat(massage2, appId);
        log.info("第二轮回答:{}",answer2);
//        第三轮
        String massage3="我的另一半是什么来着？刚给你说过，帮我回忆一下";
        String answer3 = loveApp.doChat(massage3, appId);
        log.info("第三轮回答:{}",answer3);


    }

    @Test
    void doChatWithReport(){
        String appId = UUID.randomUUID().toString();
//        第一轮
        String massage="你好我是张三，我想让我的另一半李四更爱我，我该怎么办";
        LoveApp.LoveReport loveReport = loveApp.doChatWithReport(massage, appId);
        Assertions.assertNotNull(loveReport);
    }


    @Test
    void doChatStreamWithReport(){
        String appId = UUID.randomUUID().toString();
//        第一轮
        String massage="你好我是张三，我想让我的另一半李四更爱我，我该怎么办";
        StringBuilder result = new StringBuilder();
        Flux<String> stringFlux = loveApp.doChatStreamWithReport(massage, appId);
        stringFlux.doOnNext(chunk -> {
                    log.info(chunk); // 实时输出
                    result.append(chunk);
                })
                .blockLast(); // 等待流完成

        log.info("完整流式输出: {}", result.toString());
//        Assertions.assertNotNull(loveReport);
    }

    @Test
    void doChatWithRag() {
        String appId = UUID.randomUUID().toString();
//        第一轮
        String massage="我是已婚人士，我应该怎么处理好伴侣关系，我该怎么办";
        String s = loveApp.doChatWithRag(massage, appId);
        Assertions.assertNotNull(s);

    }
}