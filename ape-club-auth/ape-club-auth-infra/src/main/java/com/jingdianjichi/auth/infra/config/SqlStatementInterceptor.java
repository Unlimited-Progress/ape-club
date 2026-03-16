package com.jingdianjichi.auth.infra.config;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * SQL执行性能监控拦截器
 * 
 * 该类实现了MyBatis的Interceptor接口，用于监控SQL执行的性能，
 * 记录SQL执行时间，并根据执行时间长短进行不同级别的日志记录，
 * 便于发现性能问题和优化慢查询
 * 
 * @author 系统生成
 * @date 2023/11/01
 */
@Intercepts({
        // 拦截Executor的update方法，包括INSERT、UPDATE、DELETE操作
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class,
                Object.class}),
        // 拦截Executor的query方法，包括SELECT操作
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class,
                Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})})
public class SqlStatementInterceptor implements Interceptor {

    /**
     * 日志记录器，使用"sys-sql"作为名称，与SQL日志拦截器保持一致
     */
    public static final Logger log = LoggerFactory.getLogger("sys-sql");

    /**
     * 拦截方法核心实现
     * 
     * 在SQL执行前后记录时间戳，计算SQL执行耗时，
     * 并根据耗时长短进行不同级别的日志记录
     * 
     * @param invocation 拦截器调用对象，包含被拦截的方法和参数
     * @return 被拦截方法的执行结果
     * @throws Throwable 如果方法执行过程中抛出异常
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 记录SQL开始执行的时间戳
        long startTime = System.currentTimeMillis();
        try {
            // 执行被拦截的方法，即实际的SQL操作
            return invocation.proceed();
        } finally {
            // 计算SQL执行耗时（毫秒）
            long timeConsuming = System.currentTimeMillis() - startTime;
            // 记录所有SQL的执行时间
            log.info("执行SQL:{}ms", timeConsuming);
            
            // 根据执行时间长短进行不同级别的日志记录
            if (timeConsuming > 999 && timeConsuming < 5000) {
                // 执行时间在1秒到5秒之间，记录警告日志
                log.info("执行SQL大于1s:{}ms", timeConsuming);
            } else if (timeConsuming >= 5000 && timeConsuming < 10000) {
                // 执行时间在5秒到10秒之间，记录警告日志
                log.info("执行SQL大于5s:{}ms", timeConsuming);
            } else if (timeConsuming >= 10000) {
                // 执行时间超过10秒，记录严重警告日志
                log.info("执行SQL大于10s:{}ms", timeConsuming);
            }
        }
    }

    /**
     * 拦截器包装方法
     * 
     * 使用MyBatis提供的Plugin工具类包装目标对象，
     * 使其具备拦截功能
     * 
     * @param target 被拦截的目标对象
     * @return 包装后的代理对象
     */
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    /**
     * 设置拦截器属性
     * 
     * 用于从配置文件中读取拦截器所需的参数
     * 当前实现中未使用任何属性，但保留接口以备将来扩展
     * 
     * @param properties 属性集合
     */
    @Override
    public void setProperties(Properties properties) {
        // 当前实现中不需要设置任何属性
    }
}