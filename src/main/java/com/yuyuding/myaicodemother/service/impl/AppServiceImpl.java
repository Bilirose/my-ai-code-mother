package com.yuyuding.myaicodemother.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yuyuding.myaicodemother.constant.AppConstant;
import com.yuyuding.myaicodemother.core.AiCodeGeneratorFacade;
import com.yuyuding.myaicodemother.core.parser.CodeParserExecutor;
import com.yuyuding.myaicodemother.core.saver.CodeFileSaverExecutor;
import com.yuyuding.myaicodemother.exception.BusinessException;
import com.yuyuding.myaicodemother.exception.ErrorCode;
import com.yuyuding.myaicodemother.exception.ThrowUtils;
import com.yuyuding.myaicodemother.mapper.AppMapper;
import com.yuyuding.myaicodemother.model.dto.app.AppAddRequest;
import com.yuyuding.myaicodemother.model.dto.app.AppQueryRequest;
import com.yuyuding.myaicodemother.model.dto.app.AppUpdateRequest;
import com.yuyuding.myaicodemother.model.entity.App;
import com.yuyuding.myaicodemother.model.entity.User;
import com.yuyuding.myaicodemother.model.enums.ChatHistoryMessageEnum;
import com.yuyuding.myaicodemother.model.enums.CodeGenTypeEnum;
import com.yuyuding.myaicodemother.model.vo.AppVO;
import com.yuyuding.myaicodemother.model.vo.UserVO;
import com.yuyuding.myaicodemother.service.AppService;
import com.yuyuding.myaicodemother.service.ChatHistoryService;
import com.yuyuding.myaicodemother.service.UserService;
import jakarta.annotation.Resource;
import org.jspecify.annotations.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用表 服务层实现。
 *
 * @author yuyuding
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Override
    public long createApp(AppAddRequest appAddRequest, User loginUser) {
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "initPrompt 不能为空");
        App app = new App();
        app.setInitPrompt(initPrompt);
        String appName = appAddRequest.getAppName();
        if (StrUtil.isBlank(appName)) {
            appName = AppConstant.DEFAULT_APP_NAME;
        }
        app.setAppName(appName);
        app.setUserId(loginUser.getId());
        app.setPriority(0);
        app.setCodeGenType(CodeGenTypeEnum.MULTI_FILE.getValue());
        validApp(app, true);
        boolean result = this.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return app.getId();
    }

    @Override
    public boolean updateMyApp(AppUpdateRequest appUpdateRequest, User loginUser) {
        ThrowUtils.throwIf(appUpdateRequest == null || appUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        Long id = appUpdateRequest.getId();
        String appName = StrUtil.isBlank(appUpdateRequest.getAppName()) ? AppConstant.DEFAULT_APP_NAME : appUpdateRequest.getAppName();
        ThrowUtils.throwIf(StrUtil.isBlank(appName), ErrorCode.PARAMS_ERROR, "应用名称不能为空");
        App oldApp = this.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        checkAppAuth(loginUser, oldApp);
        App app = new App();
        app.setId(id);
        app.setAppName(appName);
        app.setEditTime(LocalDateTime.now());
        boolean result = this.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return true;
    }

    @Override
    public boolean deleteMyApp(long id, User loginUser) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        App oldApp = this.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        checkAppAuth(loginUser, oldApp);
        boolean result = this.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return true;
    }

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) return null;
        AppVO appVO = new AppVO();
        BeanUtils.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = appVO.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) return null;
        // 批量获取用户信息
        Set<Long> userIds = appList.stream().map(App::getUserId).collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream().collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = new AppVO();
            BeanUtils.copyProperties(app, appVO);
            Long userId = appVO.getUserId();
            if (userId != null) {
                appVO.setUser(userVOMap.get(userId));
            }
            return appVO;
        }).collect(Collectors.toList());
    }

    @Override
    public void validApp(App app, boolean add) {
        ThrowUtils.throwIf(app == null, ErrorCode.PARAMS_ERROR);
        if (add) {
            ThrowUtils.throwIf(StrUtil.isBlank(app.getInitPrompt()), ErrorCode.PARAMS_ERROR, "initPrompt 不能为空");
        }
    }

    @Override
    public void checkAppAuth(User loginUser, App app) {
        ThrowUtils.throwIf(loginUser == null || app == null, ErrorCode.PARAMS_ERROR);
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作该应用");
        }
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();

        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "asc".equals(sortOrder));
    }

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId <= 0, ErrorCode.PARAMS_ERROR, "应用 id 错误");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "提示词不能为空");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 权限校验，仅本人可以和自己的应用对话
        checkAppAuth(loginUser, app);
        // 4. 获取应用的代码生成类型
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getByValue(codeGenType);
        ThrowUtils.throwIf(codeGenTypeEnum == null, ErrorCode.PARAMS_ERROR, "代码生成类型错误");
        // 5. 保存用户消息到数据库中
        boolean addChatUserMessageResult = chatHistoryService.addChatMessage(appId, message, ChatHistoryMessageEnum.USER.getValue(), loginUser.getId());
        ThrowUtils.throwIf(!addChatUserMessageResult, ErrorCode.OPERATION_ERROR, "保存用户消息失败");
        // 6. 调用 AI 生成代码
        Flux<String> contentFlux = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
        // 7. 保存ai输出内容到对话历史中
        // 字符串拼接，用于当流式返回所有的代码之后，再保存代码
        StringBuilder aiResponseBuilder = new StringBuilder();
        return contentFlux.map(chunk -> {
            // 实时收集代码片段
            aiResponseBuilder.append(chunk);
            return chunk;
        }).doOnComplete(() -> {
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

    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1. 校验参数
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 id 错误");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(ObjUtil.isNull(app), ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 权限校验，仅本人可以部署自己的应用
        checkAppAuth(loginUser, app);
        // 4. 检查是否已有 deployKey
        String deployKey = app.getDeployKey();
        // 如果没有，则生成 6 位 deployKey（字母 + 数字）
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        // 5. 获取代码生成类型，获取原始代码生成路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 6. 检查路径是否存在
        File sourceDir = new File(sourceDirPath);
        ThrowUtils.throwIf(!sourceDir.exists() || !sourceDir.isDirectory(),
                ErrorCode.NOT_FOUND_ERROR, "应用代码路径不存在，请先生成应用");
        // 7. 复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署应用失败：" + e.getMessage());
        }
        // 8. 更新数据库
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");
        // 9. 返回可访问的 URL 地址
        return String.format("%s/%s", AppConstant.CODE_DEPLOY_HOST, deployKey);
    }

    /**
     * 删除应用时，同时删除对话历史
     *
     * @param id
     * @return
     */
    @Override
    public boolean removeById(@NonNull Serializable id) {
        long appId = Long.parseLong(id.toString());
        if (appId <= 0) return false;
        // 先删除关联的对话历史
        try {
            chatHistoryService.deleteById(appId);
        } catch (Exception e) {
            log.error("删除应用历史记录失败：{}", e.getMessage());
        }
        // 删除应用
        return super.removeById(appId);
    }
}
