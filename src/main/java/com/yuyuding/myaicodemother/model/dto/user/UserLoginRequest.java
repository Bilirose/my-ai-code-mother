package com.yuyuding.myaicodemother.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求
 *
 * @author yuyuding
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 账号
     */
    public String userAccount;

    /**
     * 密码
     */
    public String userPassword;
}
