package com.jingdianjichi.auth.infra.config;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

/**
 * MyBatis-Plus SQL日志拦截器
 * 
 * 该类实现了InnerInterceptor接口，用于拦截MyBatis的SQL执行过程，
 * 在SQL执行前记录完整的SQL语句和参数信息，便于调试和问题排查
 * 
 * @author 系统生成
 * @date 2023/11/01
 */
public class MybatisPlusAllSqlLog implements InnerInterceptor {
    /**
     * 日志记录器，使用"sys-sql"作为名称，便于单独配置SQL日志级别
     */
    public static final Logger log = LoggerFactory.getLogger("sys-sql");

    /**
     * 查询操作前置拦截方法
     * 
     * 在MyBatis执行查询操作前被调用，用于记录查询SQL的详细信息
     * 
     * @param executor 执行器实例
     * @param ms 映射语句对象，包含SQL语句信息
     * @param parameter SQL参数对象
     * @param rowBounds 行边界限制
     * @param resultHandler 结果处理器
     * @param boundSql 绑定SQL对象，包含SQL语句和参数映射
     * @throws SQLException 如果发生SQL异常
     */
    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        logInfo(boundSql, ms, parameter);
    }

    /**
     * 更新操作前置拦截方法
     * 
     * 在MyBatis执行更新操作（INSERT、UPDATE、DELETE）前被调用，用于记录更新SQL的详细信息
     * 
     * @param executor 执行器实例
     * @param ms 映射语句对象，包含SQL语句信息
     * @param parameter SQL参数对象
     * @throws SQLException 如果发生SQL异常
     */
    @Override
    public void beforeUpdate(Executor executor, MappedStatement ms, Object parameter) throws SQLException {
        // 从映射语句中获取绑定SQL对象
        BoundSql boundSql = ms.getBoundSql(parameter);
        logInfo(boundSql, ms, parameter);
    }

    /**
     * 记录SQL信息的核心方法
     * 
     * 提取并记录SQL语句的详细信息，包括参数、SQL ID和完整的SQL语句
     * 
     * @param boundSql 绑定SQL对象，包含SQL语句和参数映射
     * @param ms 映射语句对象
     * @param parameter SQL参数对象
     */
    private static void logInfo(BoundSql boundSql, MappedStatement ms, Object parameter) {
        try {
            // 记录原始参数对象
            log.info("parameter = " + parameter);
            // 获取到节点的id,即sql语句的id，通常为Mapper接口的全限定名+方法名
            String sqlId = ms.getId();
            log.info("sqlId = " + sqlId);
            // 获取节点的配置信息
            Configuration configuration = ms.getConfiguration();
            // 获取到最终的sql语句（已替换参数占位符）
            String sql = getSql(configuration, boundSql, sqlId);
            log.info("完整的sql:{}", sql);
        } catch (Exception e) {
            log.error("异常:{}", e.getLocalizedMessage(), e);
        }
    }

    /**
     * 封装SQL语句
     * 
     * 将SQL ID和SQL语句组合成完整格式，便于追踪SQL来源
     * 
     * @param configuration MyBatis配置对象
     * @param boundSql 绑定SQL对象
     * @param sqlId SQL语句ID
     * @return 格式化后的SQL字符串，格式为"sqlId: sqlStatement"
     */
    public static String getSql(Configuration configuration, BoundSql boundSql, String sqlId) {
        return sqlId + ":" + showSql(configuration, boundSql);
    }

    /**
     * 显示完整SQL语句
     * 
     * 将MyBatis中的占位符(?)替换为实际的参数值，生成可执行的完整SQL语句
     * 
     * @param configuration MyBatis配置对象
     * @param boundSql 绑定SQL对象
     * @return 替换参数后的完整SQL语句
     */
    public static String showSql(Configuration configuration, BoundSql boundSql) {
        // 获取参数对象
        Object parameterObject = boundSql.getParameterObject();
        // 获取参数映射列表
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        // 将sql语句中多个空格都用一个空格代替，使输出更整洁
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
        
        // 如果存在参数映射且参数对象不为空，则进行参数替换
        if (!CollectionUtils.isEmpty(parameterMappings) && parameterObject != null) {
            // 获取类型处理器注册器，类型处理器的功能是进行java类型和数据库类型的转换
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            
            // 如果根据parameterObject.getClass()可以找到对应的类型，则直接替换
            // 这种情况通常适用于简单类型参数，如String、Integer等
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceFirst("\\?",
                        Matcher.quoteReplacement(getParameterValue(parameterObject)));
            } else {
                // MetaObject主要是封装了originalObject对象，提供了get和set的方法用于获取和设置originalObject的属性值
                // 主要支持对JavaBean、Collection、Map三种类型对象的操作
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                
                // 遍历所有参数映射，逐个替换占位符
                for (ParameterMapping parameterMapping : parameterMappings) {
                    String propertyName = parameterMapping.getProperty();
                    
                    // 如果metaObject包含该属性的getter方法，则获取属性值
                    if (metaObject.hasGetter(propertyName)) {
                        Object obj = metaObject.getValue(propertyName);
                        sql = sql.replaceFirst("\\?",
                                Matcher.quoteReplacement(getParameterValue(obj)));
                    } 
                    // 如果是动态SQL中的额外参数
                    else if (boundSql.hasAdditionalParameter(propertyName)) {
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        sql = sql.replaceFirst("\\?",
                                Matcher.quoteReplacement(getParameterValue(obj)));
                    } 
                    // 参数缺失的情况，打印出缺失提示，防止错位
                    else {
                        sql = sql.replaceFirst("\\?", "缺失");
                    }
                }
            }
        }
        return sql;
    }

    /**
     * 获取参数值的字符串表示
     * 
     * 根据参数类型进行格式化处理：
     * - 字符串类型：添加单引号
     * - 日期类型：格式化为本地日期时间字符串并添加单引号
     * - 其他类型：直接转换为字符串
     * - null值：返回空字符串
     * 
     * @param obj 参数对象
     * @return 格式化后的参数值字符串
     */
    private static String getParameterValue(Object obj) {
        String value;
        if (obj instanceof String) {
            // 字符串类型添加单引号
            value = "'" + obj.toString() + "'";
        } else if (obj instanceof Date) {
            // 日期类型格式化为本地日期时间格式并添加单引号
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT,
                    DateFormat.DEFAULT, Locale.CHINA);
            value = "'" + formatter.format(new Date()) + "'";
        } else {
            // 其他类型直接转换，null值转为空字符串
            if (obj != null) {
                value = obj.toString();
            } else {
                value = "";
            }
        }
        return value;
    }
}