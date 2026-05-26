package com.yuyuding.myaicodemother.model.enums;

import lombok.Getter;

/**
 * 代码生成类型枚举
 */
@Getter
public enum CodeGenTypeEnum {

    HTML("原生 HTML 模式","html"),
    MULTI_FILE("原生多文件模式","multi_file");

    private final String text;
    private final String value;

    CodeGenTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value
     * @return
     */
    public static CodeGenTypeEnum getByValue(String value) {
        if (value == null) {
            return null;
        }
        for (CodeGenTypeEnum item : CodeGenTypeEnum.values()) {
            if (item.getValue().equals(value)) {
                return item;
            }
        }
        return null;
    }
}
