package com.yuyuding.myaicodemother.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 对话历史消息枚举
 */
@Getter
public enum ChatHistoryMessageEnum {

    USER("用户", "user"),
    AI("AI", "ai");

    private final String name;
    private final String value;

    ChatHistoryMessageEnum(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * 根据value获取枚举
     *
     * @param value
     * @return
     */
    public static ChatHistoryMessageEnum getByValue(String value) {
        if (ObjUtil.isEmpty(value)) return null;
        for (ChatHistoryMessageEnum item : ChatHistoryMessageEnum.values()) {
            if (item.getValue().equals(value)) {
                return item;
            }
        }
        return null;
    }
}
