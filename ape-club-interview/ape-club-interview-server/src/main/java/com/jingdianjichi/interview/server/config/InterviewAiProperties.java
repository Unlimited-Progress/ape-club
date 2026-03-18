package com.jingdianjichi.interview.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "interview.ai")
public class InterviewAiProperties {

    /**
     * 大模型服务地址
     */
    private String endpoint;

    /**
     * 模型名称
     */
    private String model;

    /**
     * API Key，推荐通过环境变量注入
     */
    private String apiKey;

    /**
     * AI 模式默认生成题目数量
     */
    private Integer questionCount = 8;

    /**
     * AI 请求并行度
     */
    private Integer parallelism = 3;

    private Prompt prompt = new Prompt();

    @Data
    public static class Prompt {

        private String questionSystem;

        private String questionUserTemplate;

        private String scoreSystem;

        private String scoreUserTemplate;
    }
}
