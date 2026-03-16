package com.jingdianjichi.oss.config;

import com.jingdianjichi.oss.adapter.StorageAdapter;
import com.jingdianjichi.oss.adapter.AliStorageAdapter;
import com.jingdianjichi.oss.adapter.MinioStorageAdapter;
import com.jingdianjichi.oss.constant.OssType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件存储config
 *
 * @author: ChickenWing
 * @date: 2023/10/14
 */
@Configuration
@RefreshScope
@EnableAutoConfiguration
/*@EnableAutoConfiguration是Spring Boot中的一个注解，它的作用是开启自动配置功能。
在Spring Boot中，自动配置是一种根据应用程序的依赖关系和类路径中的jar包自动配置Spring应用程序的方式。
通过使用@EnableAutoConfiguration注解，Spring Boot会自动扫描项目中的类路径、配置文件和其他因素，
然后根据这些信息自动配置Spring应用程序。

具体来说，当一个Spring Boot应用程序启动时，如果类路径中存在特定的jar包（如数据库驱动、Web服务器等），
Spring Boot会自动配置相应的组件，例如数据源、事务管理器、视图解析器等。这样，开发者无需手动配置这些组件，
只需添加相应的依赖即可。*/
public class StorageConfig {

    @Value("${storage.service.type}")
    private String storageType;

    @Bean
    @RefreshScope
        /*功能介绍：
    @RefreshScope允许在运行时刷新Spring应用上下文中的Bean及其依赖项，
    而无需重启JVM或整个Spring Boot应用。这对于需要动态更新配置的场景非常有用，如微服务架构中的配置中心变更。
    使用场景：
    当使用@Value注解获取配置属性值时，通过添加@RefreshScope注解，
    可以在配置属性发生变化时，通过发送POST请求到/actuator/refresh端点来刷新配置，从而实现配置属性的动态更新。*/
    public StorageAdapter storageService() {
        if (OssType.MINIO.equals(storageType)) {
            return new MinioStorageAdapter();
        } else if (OssType.ALI_OSS.equals(storageType)) {
            return new AliStorageAdapter();
        } else {
            throw new IllegalArgumentException(OssType.CANNOT_FIND_OBJECT_STORAGE_HANDLER);
        }
    }

}
