package com.jingdianjichi.club.gateway.auth;

import cn.dev33.satoken.stp.StpInterface;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.google.gson.Gson;

import com.jingdianjichi.club.gateway.Redis.RedisUtil;


import org.springframework.stereotype.Component;


import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 自定义权限验证接口扩展
 * 
 * 该类实现了Sa-Token框架的StpInterface接口，用于提供自定义的权限和角色验证逻辑。
 * Sa-Token是一个轻量级Java权限认证框架，通过实现此接口可以自定义权限数据来源。
 * 
 * 在本系统中，权限和角色信息存储在Redis中，该类负责从Redis中获取用户的权限和角色列表，
 * 供Sa-Token框架进行权限验证使用。
 *
 * @author: ChickenWing
 * @date: 2023/10/28
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    /**
     * 注入Redis工具类，用于操作Redis数据库
     * Redis中存储了用户的权限和角色信息，格式为JSON字符串
     */
    @Resource
    private RedisUtil redisUtil;

    /**
     * 权限信息在Redis中的键前缀
     * 完整的权限键格式为：auth.permission.{loginId}
     * 例如：auth.permission.1001
     */
    private String authPermissionPrefix = "auth.permission";

    /**
     * 角色信息在Redis中的键前缀
     * 完整的角色键格式为：auth.role.{loginId}
     * 例如：auth.role.1001
     */
    private String authRolePrefix = "auth.role";

    /**
     * 获取指定用户的权限列表
     * 
     * 当Sa-Token框架需要验证用户权限时，会调用此方法获取用户的权限列表。
     * 该方法从Redis中获取用户的权限信息，并返回权限标识符列表。
     * 
     * @param loginId 用户登录ID，通常是用户的唯一标识
     * @param loginType 登录类型，用于区分不同登录方式（本系统中未使用）
     * @return 用户的权限标识符列表，如["user:add", "user:edit", "subject:view"]等
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 调用通用方法，使用权限前缀获取权限列表
        return getAuth(loginId.toString(), authPermissionPrefix);
    }

    /**
     * 获取指定用户的角色列表
     * 
     * 当Sa-Token框架需要验证用户角色时，会调用此方法获取用户的角色列表。
     * 该方法从Redis中获取用户的角色信息，并返回角色标识符列表。
     * 
     * @param loginId 用户登录ID，通常是用户的唯一标识
     * @param loginType 登录类型，用于区分不同登录方式（本系统中未使用）
     * @return 用户角色标识符列表，如["admin", "teacher", "student"]等
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 调用通用方法，使用角色前缀获取角色列表
        return getAuth(loginId.toString(), authRolePrefix);
    }

    /**
     * 从Redis中获取用户权限或角色信息的通用方法
     * 
     * 该方法是一个通用的权限获取方法，根据传入的前缀不同，可以获取权限或角色信息。
     * 它首先构建完整的Redis键，然后从Redis中获取对应的JSON字符串，
     * 最后将JSON字符串解析为Java列表对象并返回。
     * 
     * @param loginId 用户登录ID，用于构建Redis键
     * @param prefix 权限或角色的前缀，用于区分获取的是权限还是角色信息
     * @return 权限或角色标识符列表，如果Redis中不存在对应数据则返回空列表
     */
    private List<String> getAuth(String loginId, String prefix) {
        // 构建完整的Redis键，格式为：{prefix}.{loginId}
        String authKey = redisUtil.buildKey(prefix, loginId.toString());
        // 从Redis中获取权限或角色信息的JSON字符串
        String authValue = redisUtil.get(authKey);
        
        // 如果Redis中不存在对应的数据，返回空列表
        if (StringUtils.isBlank(authValue)) {
            return Collections.emptyList();
        }

        List<Object> authList = new Gson().fromJson(authValue, List.class);
        if (authList == null || authList.isEmpty()) {
            return Collections.emptyList();
        }
        String keyName = authPermissionPrefix.equals(prefix) ? "permissionKey" : "roleKey";
        return authList.stream()
                .map(item -> extractAuthValue(item, keyName))
                .filter(value -> !StringUtils.isBlank(value))
                .collect(Collectors.toList());
    }

    private String extractAuthValue(Object authObj, String keyName) {
        if (authObj instanceof String) {
            return authObj.toString();
        }
        if (authObj instanceof Map) {
            Object authValue = ((Map<?, ?>) authObj).get(keyName);
            if (authValue != null) {
                return authValue.toString();
            }
        }
        return null;
    }
}
