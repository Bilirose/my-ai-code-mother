package com.yuyuding.myaicodemother.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 账号
     */
    public String userAccount;

    /**
     * 密码
     */
    public String userPassword;

    /**
     * 确认密码
     */
    public String checkPassword;
}
