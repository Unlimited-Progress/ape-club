package com.jingdianjichi.auth.application.context;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录上下文持有者
 * <p>
 * 该类使用ThreadLocal技术，为每个线程维护独立的登录上下文信息，
 * 实现了线程安全的用户登录状态管理。通过InheritableThreadLocal，
 * 子线程可以继承父线程的登录上下文，适用于异步任务场景。
 * </p>
 *
 * @author: ChickenWing
 * @date: 2023/11/26
 */
public class LoginContextHolder {

    /**
     * 使用InheritableThreadLocal存储线程本地变量
     * InheritableThreadLocal的特点是子线程可以继承父线程的变量值
     * 这样在异步任务中也能获取到登录用户的上下文信息
     */
    private static final InheritableThreadLocal<Map<String, Object>> THREAD_LOCAL
            = new InheritableThreadLocal<>();

    /**
     * 向当前线程的上下文中设置键值对
     *
     * @param key   键名
     * @param val   键值
     */
    public static void set(String key, Object val) {
        // 获取当前线程的ThreadLocalMap，如果不存在则创建
        Map<String, Object> map = getThreadLocalMap();
        // 设置键值对
        map.put(key, val);
    }

    /**
     * 从当前线程的上下文中获取指定键的值
     *
     * @param key   键名
     * @return      对应的值，如果不存在则返回null
     */
    public static Object get(String key){
        // 获取当前线程的ThreadLocalMap
        Map<String, Object> threadLocalMap = getThreadLocalMap();
        // 返回指定键的值
        return threadLocalMap.get(key);
    }

    /**
     * 获取当前线程上下文中的登录用户ID
     *
     * @return  登录用户ID字符串，如果未登录则返回null
     */
    public static String getLoginId(){
        // 从ThreadLocalMap中获取loginId
        return (String) getThreadLocalMap().get("loginId");
    }

    /**
     * 清除当前线程的ThreadLocal变量
     * 在请求处理完成后应该调用此方法，防止内存泄漏
     */
    public static void remove(){
        THREAD_LOCAL.remove();
    }

    /**
     * 获取当前线程的ThreadLocalMap，如果不存在则创建一个新的
     * 使用ConcurrentHashMap保证线程安全
     *
     * @return  当前线程的ThreadLocalMap
     */
    public static Map<String, Object> getThreadLocalMap() {
        // 获取当前线程的ThreadLocalMap
        Map<String, Object> map = THREAD_LOCAL.get();
        // 如果为null，则创建一个新的ConcurrentHashMap并设置到ThreadLocal中
        if (Objects.isNull(map)) {
            map = new ConcurrentHashMap<>();
            THREAD_LOCAL.set(map);
        }
        return map;
    }
}
