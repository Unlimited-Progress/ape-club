package com.jingdianjichi.club.gateway.Redis;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * redis的config处理
 *
 * 因为java自带的序列化json可读性很差，所以要自定义redistemplate，并打上bean注解覆盖默认的bean
 */
@Configuration
public class RedisConfig {


    @Bean
//    RedisTemplate<String, Object>key是string，value值不确定
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        //序列化器
        RedisSerializer<String> redisSerializer = new StringRedisSerializer();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(redisSerializer);
//        setHashKeySerializer和setHashValueSerializer是用于设置Redis哈希类型的键和值的序列化器。
//        在Redis中，哈希类型是一种存储结构，它将字符串映射到字符串值。当你需要将一个Java对象存储到Redis的哈希结构中时，
//        你需要将这些对象的字段序列化为字符串，以便它们可以被存储在Redis中。
        redisTemplate.setHashKeySerializer(redisSerializer);
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer());
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer());
        return redisTemplate;
    }

//    有时候RedisTemplate<String, Object>中的value是一个对象，需要对它序列号，就不是一个字符串了
//    所以需要jackson序列化
    private Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer() {
        Jackson2JsonRedisSerializer<Object> jsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
//        对属性进行操作
        ObjectMapper objectMapper = new ObjectMapper();
//        方法规则用哪些来进行序列化，all下的any都要序列化
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        jsonRedisSerializer.setObjectMapper(objectMapper);
        return jsonRedisSerializer;
    }

}
