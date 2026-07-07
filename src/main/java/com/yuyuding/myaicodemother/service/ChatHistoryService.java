package com.yuyuding.myaicodemother.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.yuyuding.myaicodemother.model.dto.chathistory.ChatHistoryQueryRequest;
import com.yuyuding.myaicodemother.model.entity.ChatHistory;
import com.yuyuding.myaicodemother.model.entity.User;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层。
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 添加对话历史
     *
     * @param appId        应用id
     * @param message      消息内容
     * @param messageType  消息类型
     * @param userId       用户id
     * @return
     */
    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    /**
     * 删除对话历史
     *
     * @param id 消息id
     * @return
     */
    boolean deleteById(Long id);

    /**
     * 获取查询条件
     *
     * @param chatHistoryQueryRequest 查询条件
     * @return
     */
    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    /**
     * 分页查询对话历史
     *
     * @param appId
     * @param pageSize
     * @param localDateTime
     * @param loginUser
     * @return
     */
    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize, LocalDateTime localDateTime, User loginUser);
}
