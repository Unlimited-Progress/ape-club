package com.jingdianjichi.subject.infra.config;

import com.jingdianjichi.subject.common.util.LoginUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 填充createBy,createTime等公共字段的拦截器
 *
 * @author: ChickenWing
 * @date: 2024/1/5
 */
@Component
@Slf4j
@Intercepts({@Signature(type = Executor.class, method = "update", args = {
        MappedStatement.class, Object.class
})})
public class MybatisInterceptor implements Interceptor {

    @Override
//    Invocation 是一个接口，它表示一个方法调用或函数调用。
//    在编程中，当一个对象的方法被另一个对象使用时，这种使用过程就被称为 Invocation。
    public Object intercept(Invocation invocation) throws Throwable {
        // 获取 MappedStatement 对象和参数对象
//      MappedStatement 的主要作用是将 SQL 语句与 Java 对象进行映射
//        开发者可以在 XML 文件中定义 SQL 语句和映射关系，然后将这些信息加载到 MappedStatement 对象中。
//        当执行数据库操作时，MyBatis 会根据 MappedStatement 中的信息来生成相应的 SQL 语句并执行
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        // 获取SQL命令类型
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        Object parameter = invocation.getArgs()[1];
        // 如果参数为空，直接执行原方法
        if (parameter == null) {
//            用于触发下一个拦截器或者目标方法的执行，proceed() 方法来继续执行后续的方法调用链。
            return invocation.proceed();
        }
        // 获取当前登录用户的 id
        String loginId = LoginUtil.getLoginId();
        // 如果登录用户 id 为空，直接执行原方法
        if (StringUtils.isBlank(loginId)) {
            return invocation.proceed();
        }
        // 如果 SQL 命令类型是插入或更新，替换实体属性
        if (SqlCommandType.INSERT == sqlCommandType || SqlCommandType.UPDATE == sqlCommandType) {
            replaceEntityProperty(parameter, loginId, sqlCommandType);
        }
        // 执行原方法
        return invocation.proceed();
    }

    private void replaceEntityProperty(Object parameter, String loginId, SqlCommandType sqlCommandType) {
        // 如果参数是 Map 类型，遍历并替换 Map 中的值
        if (parameter instanceof Map) {
            replaceMap((Map) parameter, loginId, sqlCommandType);
        } else {
            // 否则直接替换参数对象的属性值
            replace(parameter, loginId, sqlCommandType);
        }
    }

    private void replaceMap(Map parameter, String loginId, SqlCommandType sqlCommandType) {
        // 遍历 Map 的值，递归调用 replace 方法进行替换
        for (Object val : parameter.values()) {
            replace(val, loginId, sqlCommandType);
        }
    }

    private void replace(Object parameter, String loginId, SqlCommandType sqlCommandType) {
        // 根据 SQL 命令类型，分别处理插入和更新操作
        if (SqlCommandType.INSERT == sqlCommandType) {
            dealInsert(parameter, loginId);
        } else {
            dealUpdate(parameter, loginId);
        }
    }

    private void dealUpdate(Object parameter, String loginId) {
        // 获取所有字段，遍历并设置相应的属性值
        Field[] fields = getAllFields(parameter);
        for (Field field : fields) {
            try {
//                用于设置字段的可访问性。当一个类的字段被声明为私有（private）时，
//                外部类无法直接访问该字段。通过调用 setAccessible(true) 方法，
//                可以打破这种限制，使得外部类能够访问和修改私有字段的值。
                field.setAccessible(true);
                Object o = field.get(parameter);
                if (Objects.nonNull(o)) {
                    field.setAccessible(false);
                    continue;
                }
                if ("updateBy".equals(field.getName())) {
                    field.set(parameter, loginId);
                    field.setAccessible(false);
                } else if ("updateTime".equals(field.getName())) {
                    field.set(parameter, new Date());
                    field.setAccessible(false);
                } else {
                    field.setAccessible(false);
                }
            } catch (Exception e) {
                log.error("dealUpdate.error:{}", e.getMessage(), e);
            }
        }
    }

    private void dealInsert(Object parameter, String loginId) {
        // 获取所有字段，遍历并设置相应的属性值
        Field[] fields = getAllFields(parameter);
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object o = field.get(parameter);
                if (Objects.nonNull(o)) {
                    field.setAccessible(false);
                    continue;
                }
                if ("isDeleted".equals(field.getName())) {
                    field.set(parameter, 0);
                    field.setAccessible(false);
                } else if ("createdBy".equals(field.getName())) {
                    field.set(parameter, loginId);
                    field.setAccessible(false);
                } else if ("createdTime".equals(field.getName())) {
                    field.set(parameter, new Date());
                    field.setAccessible(false);
                } else {
                    field.setAccessible(false);
                }
            } catch (Exception e) {
                log.error("dealInsert.error:{}", e.getMessage(), e);
            }
        }
    }

    private Field[] getAllFields(Object object) {
        // 获取对象的所有字段（包括父类）
        Class<?> clazz = object.getClass();
        List<Field> fieldList = new ArrayList<>();
        while (clazz != null) {
//            clazz.getDeclaredFields() 方法返回当前类的所有声明的字段（包括私有、保护和公有字段）
            fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
//            句将 clazz 更新为当前类的父类，以便在下一次循环迭代时处理父类的字段。这个过程会一直持续到没有更多的父类为止
            clazz = clazz.getSuperclass();
        }
        Field[] fields = new Field[fieldList.size()];
        fieldList.toArray(fields);
        return fields;
    }

    @Override
    public Object plugin(Object target) {
        // 使用插件包装目标对象，返回包装后的对象
        return Plugin.wrap(target, this);
    }


    @Override
    public void setProperties(Properties properties) {
    }

}
