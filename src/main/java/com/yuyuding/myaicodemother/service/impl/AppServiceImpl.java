package com.yuyuding.myaicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yuyuding.myaicodemother.constant.AppConstant;
import com.yuyuding.myaicodemother.exception.BusinessException;
import com.yuyuding.myaicodemother.exception.ErrorCode;
import com.yuyuding.myaicodemother.exception.ThrowUtils;
import com.yuyuding.myaicodemother.mapper.AppMapper;
import com.yuyuding.myaicodemother.model.dto.AppAddRequest;
import com.yuyuding.myaicodemother.model.dto.AppQueryRequest;
import com.yuyuding.myaicodemother.model.dto.AppUpdateRequest;
import com.yuyuding.myaicodemother.model.entity.App;
import com.yuyuding.myaicodemother.model.entity.User;
import com.yuyuding.myaicodemother.model.vo.AppVO;
import com.yuyuding.myaicodemother.service.AppService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 应用表 服务层实现。
 *
 * @author yuyuding
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

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
        validApp(app, true);
        boolean result = this.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return app.getId();
    }

    @Override
    public boolean updateMyApp(AppUpdateRequest appUpdateRequest, User loginUser) {
        ThrowUtils.throwIf(appUpdateRequest == null || appUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        Long id = appUpdateRequest.getId();
        String appName = appUpdateRequest.getAppName();
        ThrowUtils.throwIf(StrUtil.isBlank(appName), ErrorCode.PARAMS_ERROR, "应用名称不能为空");
        App oldApp = this.getById(id);
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        checkAppAuth(loginUser, oldApp);
        App app = new App();
        app.setId(id);
        app.setAppName(appName);
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
        return getAppVO(app, false);
    }

    @Override
    public AppVO getAppVO(App app, boolean hideInitPrompt) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        if (hideInitPrompt) {
            appVO.setInitPrompt(null);
        }
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        return getAppVOList(appList, false);
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList, boolean hideInitPrompt) {
        if (CollUtil.isEmpty(appList)) {
            return List.of();
        }
        return appList.stream()
                .map(app -> getAppVO(app, hideInitPrompt))
                .collect(Collectors.toList());
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
    public QueryWrapper getMyAppQueryWrapper(String appName, Long userId) {
        return QueryWrapper.create()
                .eq("userId", userId)
                .like("appName", appName)
                .orderBy("createTime", false);
    }

    @Override
    public QueryWrapper getGoodAppQueryWrapper(String appName) {
        return QueryWrapper.create()
                .gt("priority", 0)
                .like("appName", appName)
                .orderBy("priority", false)
                .orderBy("createTime", false);
    }

    @Override
    public Page<AppVO> listMyAppVOByPage(String appName, long pageNum, long pageSize, User loginUser) {
        QueryWrapper queryWrapper = getMyAppQueryWrapper(appName, loginUser.getId());
        Page<App> appPage = this.page(Page.of(pageNum, pageSize), queryWrapper);
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        appVOPage.setRecords(getAppVOList(appPage.getRecords()));
        return appVOPage;
    }

    @Override
    public Page<AppVO> listGoodAppVOByPage(String appName, long pageNum, long pageSize) {
        QueryWrapper queryWrapper = getGoodAppQueryWrapper(appName);
        Page<App> appPage = this.page(Page.of(pageNum, pageSize), queryWrapper);
        Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        appVOPage.setRecords(getAppVOList(appPage.getRecords(), true));
        return appVOPage;
    }
}
