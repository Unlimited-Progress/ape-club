package com.jingdianjichi.auth.infra.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis配置类
 * 
 * 该类用于配置MyBatis-Plus的相关设置，包括拦截器等组件
 * 通过@Configuration注解标识为Spring配置类，@Bean注解将方法返回对象注册为Spring Bean
 * 
 * @author 系统生成
 * @date 2023/11/01
 */
@Configuration
public class MybatisConfiguration {

    /**
     * 配置MyBatis-Plus拦截器
     * 
     * 创建MybatisPlusInterceptor实例并添加自定义的SQL日志拦截器
     * 这样可以在SQL执行前后进行额外的处理，如日志记录、性能监控等
     * 
     * @return 配置好的MybatisPlusInterceptor实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        // 创建MyBatis-Plus拦截器实例
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        // 添加自定义的SQL日志拦截器，用于记录完整的SQL语句
        mybatisPlusInterceptor.addInnerInterceptor(new MybatisPlusAllSqlLog());
        return mybatisPlusInterceptor;
    }

}
