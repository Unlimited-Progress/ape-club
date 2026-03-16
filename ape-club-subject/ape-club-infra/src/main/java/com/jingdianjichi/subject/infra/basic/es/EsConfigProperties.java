package com.jingdianjichi.subject.infra.basic.es;

import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
//@ConfigurationProperties(prefix = "es.cluster")：
//这个注解表示这个类的属性将从配置文件中读取，配置文件中的键值对应该以es.cluster作为前缀。
@ConfigurationProperties(prefix = "es.cluster")
//用于从应用程序的配置文件中读取 Elasticsearch 集群的配置信息
public class EsConfigProperties {

    private List<EsClusterConfig> esClusterConfigs = new ArrayList<>();
//    Spring 的 @ConfigurationProperties 注解依赖于 getter 和 setter 方法来读取和设置属性。
//    Spring 会调用 setter 方法将配置文件中的值注入到相应的字段中，同时通过 getter 方法获取这些值。
    public List<EsClusterConfig> getEsClusterConfigs(){return esClusterConfigs;}

    public void setEsClusterConfigs(List<EsClusterConfig> esClusterConfigs){this.esClusterConfigs = esClusterConfigs;}


}
