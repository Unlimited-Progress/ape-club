package com.jingdianjichi.auth.application.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jingdianjichi.auth.application.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

/**
 * MVC全局配置类
 * <p>
 * 该类继承自WebMvcConfigurationSupport，用于自定义Spring MVC的配置，
 * 包括消息转换器和拦截器的配置。通过重写父类方法，实现对HTTP请求
 * 和响应的处理方式以及请求拦截规则的定制。
 * </p>
 *
 * @author: ChickenWing
 * @date: 2023/10/7
 */
@Configuration
public class GlobalConfig extends WebMvcConfigurationSupport {

    /**
     * 配置HTTP消息转换器
     * <p>
     * 消息转换器负责将HTTP请求体转换为Java对象，以及将Java对象转换为HTTP响应体。
     * 此方法重写了父类的configureMessageConverters方法，添加了自定义的JSON转换器。
     * </p>
     * 
     * @param converters 消息转换器列表，Spring MVC维护的转换器集合
     *                  包含了系统中所有可用的HTTP消息转换器，用于处理不同类型的请求和响应
     */
    @Override
    protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 调用父类方法，执行默认的消息转换器配置
        super.configureMessageConverters(converters);
        // 添加自定义的Jackson JSON转换器到转换器列表中
        converters.add(mappingJackson2HttpMessageConverter());
    }

    /**
     * 配置拦截器
     * <p>
     * 拦截器用于在请求处理前后执行特定逻辑，如身份验证、日志记录等。
     * 此方法重写了父类的addInterceptors方法，添加了登录验证拦截器。
     * </p>
     * 
     * @param registry 拦截器注册表，Spring提供的拦截器管理器
     *                用于注册和管理应用程序中的所有拦截器，可以配置拦截路径和排除路径
     */
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        // 向拦截器注册表中添加登录拦截器
        // registry是InterceptorRegistry类型的对象，提供了注册拦截器的方法
        registry.addInterceptor(new LoginInterceptor())
                // 配置拦截器应用的路径模式，/**表示拦截所有请求
                .addPathPatterns("/**")
                // 配置不拦截的路径，/user/doLogin路径下的请求不会被拦截
                .excludePathPatterns("/user/doLogin");
    }

    /**
     * 自定义MappingJackson2HttpMessageConverter
     * <p>
     * 创建并配置一个自定义的Jackson JSON消息转换器，用于处理HTTP请求和响应中的JSON数据。
     * 通过ObjectMapper的配置，实现了空值忽略和空字段可返回的功能。
     * </p>
     * 
     * @return 配置好的MappingJackson2HttpMessageConverter实例
     */
    private MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        // 创建ObjectMapper实例，用于JSON序列化和反序列化
        ObjectMapper objectMapper = new ObjectMapper();
        // 配置序列化时不因空Bean而失败，允许返回空对象
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 设置序列化包含策略，忽略null值的字段，不序列化到JSON中
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 创建并返回配置好的消息转换器
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }
}
