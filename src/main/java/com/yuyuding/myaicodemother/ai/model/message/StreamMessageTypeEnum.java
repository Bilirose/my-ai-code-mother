package com.yuyuding.myaicodemother.ai.model.message;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 流式消息类型枚举
 */
@Getter
public enum StreamMessageTypeEnum {

    AI_RESPONSE("AI响应", "ai_response"),
    TOOL_REQUEST("工具请求", "tool_request"),
    TOOL_EXECUTED("工具执行结果", "tool_executed");

    private final String name;
    private final String value;

    StreamMessageTypeEnum(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * 根据value获取枚举
     *
     * @param value
     * @return
     */
    public static StreamMessageTypeEnum getByValue(String value) {
        if (ObjUtil.isEmpty(value)) return null;
        for (StreamMessageTypeEnum item : StreamMessageTypeEnum.values()) {
            if (item.getValue().equals(value)) {
                return item;
            }
        }
        return null;
    }
}
