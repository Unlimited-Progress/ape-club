package com.jingdianjichi.club.gateway.auth;

import cn.dev33.satoken.stp.StpInterface;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.google.gson.Gson;

import com.jingdianjichi.club.gateway.Redis.RedisUtil;


import org.springframework.stereotype.Component;


import javax.annotation.Resource;
import java.util.Collections;

import java.util.List;


/**
 * 自定义权限验证接口扩展
 *
 * @author: ChickenWing
 * @date: 2023/10/28
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private RedisUtil redisUtil;

    private String authPermissionPrefix = "auth.permission";

    private String authRolePrefix = "auth.role";

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return getAuth(loginId.toString(), authPermissionPrefix);
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return getAuth(loginId.toString(), authRolePrefix);
    }

    private List<String> getAuth(String loginId, String prefix) {
        String authKey = redisUtil.buildKey(prefix, loginId.toString());
        String authValue = redisUtil.get(authKey);
        if (StringUtils.isBlank(authValue)) {
            return Collections.emptyList();
        }

        // 使用Gson库将JSON字符串转换为Java列表对象
        List<String> authList = new Gson().fromJson(authValue, List.class);
        return authList;
    }
}
