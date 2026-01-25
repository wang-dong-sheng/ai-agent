package com.dswang.aiagent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@MapperScan("com.dswang.aiagent.mapper")
@SpringBootApplication(exclude = {
		org.springframework.ai.autoconfigure.vectorstore.pgvector.PgVectorStoreAutoConfiguration.class
})// 扫描生成的mapper
//@ComponentScan(basePackages = {"com.dswang.aiagent"})  // 扫描所有相关包

public class AiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiAgentApplication.class, args);
    }

}

