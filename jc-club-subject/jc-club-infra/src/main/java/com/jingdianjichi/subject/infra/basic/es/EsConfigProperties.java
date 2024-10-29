package com.jingdianjichi.subject.infra.basic.es;

import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
//@ConfigurationProperties(prefix = "es.cluster")：
//这个注解表示这个类的属性将从配置文件中读取，配置文件中的键值对应该以es.cluster作为前缀。
@ConfigurationProperties(prefix = "es.cluster")
public class EsConfigProperties {

    private List<EsClusterConfig> esClusterConfigs = new ArrayList<>();

    public List<EsClusterConfig> getEsClusterConfigs(){return esClusterConfigs;}

    public void setEsClusterConfigs(List<EsClusterConfig> esClusterConfigs){this.esClusterConfigs = esClusterConfigs;}


}
