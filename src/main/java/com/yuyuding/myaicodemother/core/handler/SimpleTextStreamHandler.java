package com.yuyuding.myaicodemother.core.handler;

import com.yuyuding.myaicodemother.exception.ErrorCode;
import com.yuyuding.myaicodemother.exception.ThrowUtils;
import com.yuyuding.myaicodemother.model.entity.User;
import com.yuyuding.myaicodemother.model.enums.ChatHistoryMessageEnum;
import com.yuyuding.myaicodemother.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 处理简单文本流（HTML,MULTI_FILE）
 */
@Slf4j
@Component
public class SimpleTextStreamHandler {

    /**
     * 处理 HTML,MULTI_FILE 流式响应
     * 直接收集完整的文本响应
     *
     * @param originalFlux
     * @param chatHistoryService
     * @param appId
     * @param loginUser
     * @return
     */
    public Flux<String> handle(Flux<String> originalFlux,
                               ChatHistoryService chatHistoryService,
                               long appId, User loginUser) {
        StringBuilder aiResponseBuilder = new StringBuilder();
        return originalFlux.map(chunk -> {
            // 收集 AI 的响应内容
            aiResponseBuilder.append(chunk);
            return chunk;
        }).doOnComplete(() -> {
            // 流式响应完成后，添加 AI 消息到对话历史
            String aiResponse = aiResponseBuilder.toString();
            boolean addChatAiMessageResult = chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageEnum.AI.getValue(), loginUser.getId());
            ThrowUtils.throwIf(!addChatAiMessageResult, ErrorCode.OPERATION_ERROR, "保存AI输出内容失败");
        }).doOnError(error -> {
            // 如果 AI 回复失败，也需要保存记录到数据库中
            String errorMessage = "AI 回复失败：" + error.getMessage();
            boolean addChatAiMessageResult = chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageEnum.AI.getValue(), loginUser.getId());
            ThrowUtils.throwIf(!addChatAiMessageResult, ErrorCode.OPERATION_ERROR, "保存AI输出内容失败");
        });
    }
}
