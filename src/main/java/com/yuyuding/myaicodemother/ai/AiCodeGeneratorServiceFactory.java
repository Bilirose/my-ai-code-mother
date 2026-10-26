package com.yuyuding.myaicodemother.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yuyuding.myaicodemother.ai.tools.FileWriteTool;
import com.yuyuding.myaicodemother.exception.BusinessException;
import com.yuyuding.myaicodemother.exception.ErrorCode;
import com.yuyuding.myaicodemother.model.enums.CodeGenTypeEnum;
import com.yuyuding.myaicodemother.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * AI 服务创建工厂
 */
@Configuration
@Slf4j
public class AiCodeGeneratorServiceFactory {

    @Resource
    ChatModel chatModel;

    @Resource
    StreamingChatModel openAiStreamingChatModel;

    @Resource
    StreamingChatModel reasoningStreamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * AI 服务实例缓存
     * 缓存策略：
     * - 最大缓存 1000 个实例
     * - 写入后 30 分钟过期
     * - 访问后 10 分钟过期
     */
        private final Cache<String, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.debug("AI 服务实例被移除，缓存键: {}，原因：{}", key, cause);
            })
            .build();

    /**
     * 根据 appId 获取服务（兼容旧版本）
     *
     * @param appId
     * @return
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId) {
        return getAiCodeGeneratorService(appId, CodeGenTypeEnum.HTML);
    }

    /**
     * 根据 appId 获取服务
     *
     * @param appId
     * @param codeGenType
     * @return
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(long appId, CodeGenTypeEnum codeGenType) {
        String cacheKey = buildCacheKey(appId, codeGenType);
        return serviceCache.get(cacheKey, k -> createAiCodeGeneratorService(appId, codeGenType));
    }

    /**
     * 创建新的 AI 服务实例
     *
     * @param appId
     * @param codeGenType
     * @return
     */
    private AiCodeGeneratorService createAiCodeGeneratorService(Object appId, CodeGenTypeEnum codeGenType) {
        log.info("为 appId: {} 创造新的 AI 服务实例", appId);
        MessageWindowChatMemory messageWindowChatMemory = MessageWindowChatMemory
                .builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .maxMessages(20)
                .build();
        // 从数据库中加载对话历史到记忆中
        chatHistoryService.loadChatHistoryToMemory((Long) appId, messageWindowChatMemory, 20);
        return switch (codeGenType) {
            // Vue 项目生成，使用工具调用和推理模型
            case VUE_PROJECT -> AiServices.builder(AiCodeGeneratorService.class)
                    .chatModel(chatModel)
                    .streamingChatModel(reasoningStreamingChatModel)
                    .chatMemoryProvider(memoryId -> messageWindowChatMemory)
                    .tools(new FileWriteTool())
                    // 处理工具调用幻觉，返回没有该工具
                    .hallucinatedToolNameStrategy(toolExecutionRequest ->
                            ToolExecutionResultMessage.from(toolExecutionRequest,
                                    "Error: there is no tool called " + toolExecutionRequest.name())
                    )
                    .build();
            // HTML, 多文件项目生成，使用流式对话模型
            case HTML, MULTI_FILE -> AiServices.builder(AiCodeGeneratorService.class)
                    .chatModel(chatModel)
                    .streamingChatModel(openAiStreamingChatModel)
                    .chatMemory(messageWindowChatMemory)
                    .build();
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的生成类型：" + codeGenType.getValue());
        };
    }

    /**
     * 创建 AI 代码生成服务
     *
     * @return
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return getAiCodeGeneratorService(0);
    }

    /**
     * 构建缓存键
     *
     * @param appId
     * @param codeGenType
     * @return
     */
    private String buildCacheKey(long appId, CodeGenTypeEnum codeGenType) {
        return appId + "_" + codeGenType;
    }
}
