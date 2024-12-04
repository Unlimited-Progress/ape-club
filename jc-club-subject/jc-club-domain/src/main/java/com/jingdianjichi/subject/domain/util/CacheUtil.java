package com.jingdianjichi.subject.domain.util;

import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 缓存工具类
 */

@Component
public class CacheUtil<K,V> {

//    使用Google Guava库创建一个本地缓存
    private Cache<String,String> localCache =
            CacheBuilder.newBuilder()
//                    这里的5000指的是缓存可以容纳的最大键值对数量
                    .maximumSize(5000)
//    expireAfterWrite(10, TimeUnit.SECONDS)：设置缓存中的元素在写入后的10秒钟后过期。过期的元素会在下次访问时被移除。
                    .expireAfterWrite(10, TimeUnit.SECONDS)
                    .build();


//    cacheKey：缓存键，用于在缓存中查找对应的数据。
//    clazz：泛型类，表示要获取的数据类型。
    public List<V> getResult(String cacheKey, Class<V> clazz,
                             Function<String,List<V>> function){
        List<V> resultList = new LinkedList<>();
//        首先尝试从名为localCache的缓存中获取cacheKey对应的值，将其赋值给content
//        我们在使用LoadingCache类的时候，builder中会传入一个CacheLoader，这个load方法是用来从别的地方取值保存在内存中的。
//        使用get时，如果内存中没有值，会自动调用load方法，如果load方法返回的是null，那么get会抛出异常。
//        使用getIfPresent时，如果内存中没有值，不会调用load方法，而是直接返回null。
        String content = localCache.getIfPresent(cacheKey);
        if (StringUtils.isNotBlank(content)){
//            将content解析为clazz类型的对象列表，并赋值给resultList。
            resultList = JSON.parseArray(content,clazz);
        }else {
//            如果content为空（即缓存中无数据），则调用传入的function函数，传入cacheKey作为参数，获取结果列表。
            resultList = function.apply(cacheKey);
            if (!CollectionUtils.isEmpty(resultList)){
//                如果获取到的结果列表不为空，则将结果列表转换为JSON字符串，并将其存入localCache缓存中，以便下次使用
                localCache.put(cacheKey,JSON.toJSONString(resultList));
            }
        }
        return resultList;

    }


    public Map<K, V> getMapResult(String cacheKey, Class<V> clazz,
                                  Function<String, Map<K, V>> function) {
        return new HashMap<>();
    }

}
