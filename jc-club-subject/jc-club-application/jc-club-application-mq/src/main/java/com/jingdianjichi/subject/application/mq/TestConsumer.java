package com.jingdianjichi.subject.application.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RocketMQMessageListener(topic = "test-topic",consumerGroup = "test-consumer")
public class TestConsumer implements RocketMQListener<String> {
    @Override
    public void onMessage(String s) {
        log.info("接受到信息，{}",s);
    }
}
