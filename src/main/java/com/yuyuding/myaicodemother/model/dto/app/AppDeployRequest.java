package com.yuyuding.myaicodemother.model.dto.app;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppDeployRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 应用 ID
     */
    private Long appId;
}

