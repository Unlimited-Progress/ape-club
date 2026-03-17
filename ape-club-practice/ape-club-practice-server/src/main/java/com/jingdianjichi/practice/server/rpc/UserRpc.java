package com.jingdianjichi.practice.server.rpc;

import com.jingdianjichi.auth.api.UserFeignService;
import com.jingdianjichi.auth.entity.AuthUserDTO;
import com.jingdianjichi.auth.entity.Result;
import com.jingdianjichi.practice.server.entity.dto.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class UserRpc {

    @Resource
    private UserFeignService userFeignService;

    public UserInfo getUserInfo(String userName) {
        UserInfo userInfo = buildFallbackUserInfo(userName);
        try {
            AuthUserDTO authUserDTO = new AuthUserDTO();
            authUserDTO.setUserName(userName);
            Result<AuthUserDTO> result = userFeignService.getUserInfo(authUserDTO);
            if (result == null || !Boolean.TRUE.equals(result.getSuccess()) || result.getData() == null) {
                return userInfo;
            }
            AuthUserDTO data = result.getData();
            if (!isBlank(data.getUserName())) {
                userInfo.setUserName(data.getUserName());
            }
            if (!isBlank(data.getNickName())) {
                userInfo.setNickName(data.getNickName());
            }
            if (!isBlank(data.getAvatar())) {
                userInfo.setAvatar(data.getAvatar());
            }
            return userInfo;
        } catch (Exception e) {
            log.error("practice.UserRpc.getUserInfo.error:{}", e.getMessage(), e);
            return userInfo;
        }
    }

    private UserInfo buildFallbackUserInfo(String userName) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserName(userName);
        userInfo.setNickName(userName);
        return userInfo;
    }

    private boolean isBlank(String val) {
        return val == null || val.trim().isEmpty();
    }
}
