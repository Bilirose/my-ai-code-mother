package com.yuyuding.myaicodemother.model.dto.chathistory;

import com.yuyuding.myaicodemother.common.PageRequest;
import dev.langchain4j.agent.tool.P;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话历史查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class ChatHistoryQueryRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 消息内容
     */
    private String message;

    /**
     * 消息类型
     */
    private String messageType;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 游标查询 - 最后一条记录的创建时间
     * 用于分页查询，获取早于此时间的记录   < lastCreateTime
     */
    private LocalDateTime lastCreateTime;
}
