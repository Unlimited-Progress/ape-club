package com.jingdianjichi.subject.infra.rpc;

import com.jingdianjichi.auth.api.UserFeignService;
import com.jingdianjichi.auth.entity.AuthUserDTO;
import com.jingdianjichi.auth.entity.Result;
import com.jingdianjichi.subject.infra.entity.UserInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.events.Event;

import javax.annotation.Resource;

@Component
public class UserRpc {

    @Resource
    private UserFeignService userFeignService;

    public UserInfo getUserInfo(String username){
        AuthUserDTO authUserDTO = new AuthUserDTO();
        authUserDTO.setUserName(username);
        Result<AuthUserDTO> result = userFeignService.getUserInfo(authUserDTO);
        UserInfo userInfo = new UserInfo();
        if (!result.getSuccess()){
            return userInfo;
        }
        AuthUserDTO data = result.getData();
        BeanUtils.copyProperties(data,userInfo);
        return userInfo;

    }

}
