package com.yuyuding.myaicodemother.service.impl;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yuyuding.myaicodemother.constant.UserConstant;
import com.yuyuding.myaicodemother.exception.ErrorCode;
import com.yuyuding.myaicodemother.exception.ThrowUtils;
import com.yuyuding.myaicodemother.model.dto.chathistory.ChatHistoryQueryRequest;
import com.yuyuding.myaicodemother.model.entity.App;
import com.yuyuding.myaicodemother.model.entity.ChatHistory;
import com.yuyuding.myaicodemother.mapper.ChatHistoryMapper;
import com.yuyuding.myaicodemother.model.entity.User;
import com.yuyuding.myaicodemother.model.enums.ChatHistoryMessageEnum;
import com.yuyuding.myaicodemother.service.AppService;
import com.yuyuding.myaicodemother.service.ChatHistoryService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 对话历史 服务层实现。
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Resource
    @Lazy
    private AppService appService;

    @Override
    public boolean addChatMessage(Long appId, String message, String messageType, Long userId) {
        // 基础校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 id 错误");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户 id 错误");
        // 验证消息类型是否有效
        ChatHistoryMessageEnum messageTypeEnum = ChatHistoryMessageEnum.getByValue(messageType);
        ThrowUtils.throwIf(messageTypeEnum == null, ErrorCode.PARAMS_ERROR, "消息类型错误");
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .message(message)
                .messageType(messageType)
                .userId(userId)
                .build();
        return this.save(chatHistory);
    }

    @Override
    public boolean deleteById(Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "id 不能为空");
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("id", id);
        return this.remove(queryWrapper);
    }

    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest 查询条件
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (chatHistoryQueryRequest == null) return queryWrapper;
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq("id", id)
                .like("message", message)
                .eq("messageType", messageType)
                .eq("appId", appId)
                .eq("userId", userId);
        // 游标查询逻辑 - 只使用 createTime 作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt("createTime", lastCreateTime);
        }
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, sortOrder.equals("asc"));
        } else {
            // 默认按照时间降序
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize, LocalDateTime localDateTime, User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 id 错误");
        ThrowUtils.throwIf(pageSize <= 0, ErrorCode.PARAMS_ERROR, "分页大小错误");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 检验权限，只有创建者和管理员可以查看
        App app = appService.getById(appId);
        ThrowUtils.throwIf(ObjUtil.isNull(app), ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        ThrowUtils.throwIf(!isCreator && !isAdmin, ErrorCode.NO_AUTH_ERROR, "无访问权限");
        // 构造查询条件
        ChatHistoryQueryRequest chatHistoryQueryRequest = ChatHistoryQueryRequest.builder()
                .appId(appId)
                .lastCreateTime(localDateTime)
                .build();
        QueryWrapper queryWrapper = this.getQueryWrapper(chatHistoryQueryRequest);
        // 查询数据
        return this.page(Page.of(1, pageSize), queryWrapper);
    }
}
