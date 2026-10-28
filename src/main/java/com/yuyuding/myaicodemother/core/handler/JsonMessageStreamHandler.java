package com.yuyuding.myaicodemother.core.handler;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yuyuding.myaicodemother.ai.model.message.*;
import com.yuyuding.myaicodemother.exception.ErrorCode;
import com.yuyuding.myaicodemother.exception.ThrowUtils;
import com.yuyuding.myaicodemother.model.entity.User;
import com.yuyuding.myaicodemother.model.enums.ChatHistoryMessageEnum;
import com.yuyuding.myaicodemother.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * 处理 JSON 消息流（VUE_PROJECT），包含工具调用信息
 */
@Slf4j
@Component
public class JsonMessageStreamHandler {

    public Flux<String> handle(Flux<String> originalFlux,
                               ChatHistoryService chatHistoryService,
                               long appId, User loginUser) {
        // 收集数据用于生成后端记忆格式
        StringBuilder chatHistoryStringBuilder = new StringBuilder();
        // 用于跟踪已经见过的工具ID，判断是否是第一次调用
        Set<String> seenToolIds = new HashSet<>();
        return originalFlux.map(chunk -> {
                    // 解析每个 JSON 消息块
                    return handleJsonMessageChunk(chunk, chatHistoryStringBuilder, seenToolIds);
                })
                // 过滤掉空字符串
                .filter(StrUtil::isNotEmpty)
                .doOnComplete(() -> {
                    // 流式响应完成后，添加 AI 消息到对话历史
                    String aiResponse = chatHistoryStringBuilder.toString();
                    boolean addChatAiMessageResult = chatHistoryService.addChatMessage(appId, aiResponse, ChatHistoryMessageEnum.AI.getValue(), loginUser.getId());
                    ThrowUtils.throwIf(!addChatAiMessageResult, ErrorCode.OPERATION_ERROR, "保存AI输出内容失败");
                }).doOnError(error -> {
                    // 如果 AI 回复失败，也需要保存记录到数据库中
                    String errorMessage = "AI 回复失败：" + error.getMessage();
                    boolean addChatAiMessageResult = chatHistoryService.addChatMessage(appId, errorMessage, ChatHistoryMessageEnum.AI.getValue(), loginUser.getId());
                    ThrowUtils.throwIf(!addChatAiMessageResult, ErrorCode.OPERATION_ERROR, "保存AI输出内容失败");
                });
    }

    /**
     * 解析并收集 TokenStream 数据
     *
     * @param chunk
     * @param chatHistoryStringBuilder
     * @param seenToolIds
     * @return
     */
    private String handleJsonMessageChunk(String chunk, StringBuilder chatHistoryStringBuilder, Set<String> seenToolIds) {
        // 解析 JSON
        StreamMessage streamMessage = JSONUtil.toBean(chunk, StreamMessage.class);
        StreamMessageTypeEnum typeEnum = StreamMessageTypeEnum.getByValue(streamMessage.getType());
        switch (typeEnum) {
            case AI_RESPONSE -> {
                AiResponseMessage aiMessage = JSONUtil.toBean(chunk, AiResponseMessage.class);
                String data = aiMessage.getData();
                // 直接拼接响应
                chatHistoryStringBuilder.append(data);
                return data;
            }
            case TOOL_REQUEST -> {
                ToolRequestMessage toolRequestMessage = JSONUtil.toBean(chunk, ToolRequestMessage.class);
                String toolId = toolRequestMessage.getId();
                if (toolId != null && !seenToolIds.contains(toolId)) {
                    // 如果是第一次调用该工具，则添加工具调用信息
                    seenToolIds.add(toolId);
                    return "\n\n[工具选择] 写入文件\n\n";
                } else {
                    return "";
                }
            }
            case TOOL_EXECUTED -> {
                ToolExecutedMessage toolExecutedMessage = JSONUtil.toBean(chunk, ToolExecutedMessage.class);
                JSONObject jsonObject = JSONUtil.parseObj(toolExecutedMessage.getArguments());
                String relativeFilePath = jsonObject.getStr("relativeFilePath");
                String fileSuffix = FileUtil.getSuffix(relativeFilePath);
                String content = jsonObject.getStr("content");
                String result = String.format("""
                        [工具调用] 写入文件 %s
                        ```%s
                        %s
                        ```
                        """, relativeFilePath, fileSuffix, content);
                // 输出前端和要持久化的内容
                String output = String.format("\n\n%s\n\n", result);
                chatHistoryStringBuilder.append(result);
                return output;
            }
            default -> {
                log.error("不支持的信息类型：{}", typeEnum);
                return "";
            }
        }
    }
}
