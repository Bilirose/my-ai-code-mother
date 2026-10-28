package com.yuyuding.myaicodemother.core.handler;

import com.yuyuding.myaicodemother.model.entity.User;
import com.yuyuding.myaicodemother.model.enums.CodeGenTypeEnum;
import com.yuyuding.myaicodemother.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 流处理器执行器
 * 根据代码生成类型创建合适的流处理器
 */
@Slf4j
@Component
public class StreamHandlerExecutor {

    @Resource
    private JsonMessageStreamHandler jsonMessageStreamHandler;

    @Resource
    private SimpleTextStreamHandler simpleTextStreamHandler;

    /**
     * 创建流处理器并处理聊天历史记录
     *
     * @param codeGenType 代码生成类型
     * @param originalFlux 原始数据流
     * @param chatHistoryService 聊天历史记录服务
     * @param appId 应用程序ID
     * @param loginUser 登录用户
     * @return 处理后的数据流
     */
    public Flux<String> doExecute(CodeGenTypeEnum codeGenType,
                                  Flux<String> originalFlux,
                                  ChatHistoryService chatHistoryService,
                                  long appId, User loginUser) {
        return switch (codeGenType) {
            case HTML,MULTI_FILE -> simpleTextStreamHandler.handle(originalFlux, chatHistoryService, appId, loginUser);
            case VUE_PROJECT -> jsonMessageStreamHandler.handle(originalFlux, chatHistoryService, appId, loginUser);
        };
    }
}
