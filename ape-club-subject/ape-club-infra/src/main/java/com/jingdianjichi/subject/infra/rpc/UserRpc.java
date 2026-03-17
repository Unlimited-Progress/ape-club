package com.jingdianjichi.subject.infra.rpc;

import com.jingdianjichi.auth.api.UserFeignService;
import com.jingdianjichi.auth.entity.AuthUserDTO;
import com.jingdianjichi.auth.entity.Result;
import com.jingdianjichi.subject.infra.entity.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class UserRpc {

    @Resource
    private UserFeignService userFeignService;

    public UserInfo getUserInfo(String username) {
        UserInfo userInfo = buildFallbackUserInfo(username);
        try {
            AuthUserDTO authUserDTO = new AuthUserDTO();
            authUserDTO.setUserName(username);
            Result<AuthUserDTO> result = userFeignService.getUserInfo(authUserDTO);
            if (result == null || !Boolean.TRUE.equals(result.getSuccess()) || result.getData() == null) {
                return userInfo;
            }
            BeanUtils.copyProperties(result.getData(), userInfo);
            if (isBlank(userInfo.getUserName())) {
                userInfo.setUserName(username);
            }
            if (isBlank(userInfo.getNickName())) {
                userInfo.setNickName(userInfo.getUserName());
            }
            return userInfo;
        } catch (Exception e) {
            log.error("subject.UserRpc.getUserInfo.error:{}", e.getMessage(), e);
            return userInfo;
        }
    }

    private UserInfo buildFallbackUserInfo(String username) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserName(username);
        userInfo.setNickName(username);
        return userInfo;
    }

    private boolean isBlank(String val) {
        return val == null || val.trim().isEmpty();
    }
}
