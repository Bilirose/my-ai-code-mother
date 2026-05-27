package com.yuyuding.myaicodemother.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import com.mybatisflex.core.paginate.Page;
import com.yuyuding.myaicodemother.annotation.AuthCheck;
import com.yuyuding.myaicodemother.common.BaseResponse;
import com.yuyuding.myaicodemother.common.DeleteRequest;
import com.yuyuding.myaicodemother.common.ResultUtils;
import com.yuyuding.myaicodemother.constant.AppConstant;
import com.yuyuding.myaicodemother.constant.UserConstant;
import com.yuyuding.myaicodemother.exception.ErrorCode;
import com.yuyuding.myaicodemother.exception.ThrowUtils;
import com.yuyuding.myaicodemother.model.dto.AppAddRequest;
import com.yuyuding.myaicodemother.model.dto.AppAdminUpdateRequest;
import com.yuyuding.myaicodemother.model.dto.AppQueryRequest;
import com.yuyuding.myaicodemother.model.dto.AppUpdateRequest;
import com.yuyuding.myaicodemother.model.entity.App;
import com.yuyuding.myaicodemother.model.entity.User;
import com.yuyuding.myaicodemother.model.vo.AppVO;
import com.yuyuding.myaicodemother.service.AppService;
import com.yuyuding.myaicodemother.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 应用表 控制层。
 *
 * @author yuyuding
 */
@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    // region 用户接口

    /**
     * 创建应用
     *
     * @param appAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjUtil.isNull(appAddRequest), ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        long appId = appService.createApp(appAddRequest, loginUser);
        return ResultUtils.success(appId);
    }

    /**
     * 更新自己的应用
     *
     * @param appUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateMyApp(@RequestBody AppUpdateRequest appUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(ObjUtil.isNull(appUpdateRequest), ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = appService.updateMyApp(appUpdateRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 删除自己的应用
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteMyApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = appService.deleteMyApp(deleteRequest.getId(), loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 查看应用详情
     *
     * @param id
     * @return
     */
    @PostMapping("/get/vo")
    public BaseResponse<AppVO> getAppVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(appService.getAppVO(app));
    }

    /**
     * 分页查询自己的应用列表
     *
     * @param appQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<AppVO>> listMyAppVOByPage(@RequestBody AppQueryRequest appQueryRequest,
                                                       HttpServletRequest request) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > AppConstant.MAX_PAGE_SIZE, ErrorCode.PARAMS_ERROR, "每页最多 20 个");
        User loginUser = userService.getLoginUser(request);
        Page<AppVO> appVOPage = appService.listMyAppVOByPage(appQueryRequest.getAppName(), pageNum, pageSize, loginUser);
        return ResultUtils.success(appVOPage);
    }

    /**
     * 分页查询精选的应用列表
     *
     * @param appQueryRequest
     * @return
     */
    @PostMapping("/good/list/page/vo")
    public BaseResponse<Page<AppVO>> listGoodAppVOByPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > AppConstant.MAX_PAGE_SIZE, ErrorCode.PARAMS_ERROR, "每页最多 20 个");
        Page<AppVO> appVOPage = appService.listGoodAppVOByPage(appQueryRequest.getAppName(), pageNum, pageSize);
        return ResultUtils.success(appVOPage);
    }

    // endregion

    // region 管理员接口

    /**
     * 根据 id 删除任意应用
     *
     * @param deleteRequest
     * @return
     */
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteAppByAdmin(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR);
        boolean result = appService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 更新任意应用
     *
     * @param appAdminUpdateRequest
     * @return
     */
    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateAppByAdmin(@RequestBody AppAdminUpdateRequest appAdminUpdateRequest) {
        ThrowUtils.throwIf(appAdminUpdateRequest == null || appAdminUpdateRequest.getId() == null,
                ErrorCode.PARAMS_ERROR);
        App oldApp = appService.getById(appAdminUpdateRequest.getId());
        ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
        App app = new App();
        BeanUtil.copyProperties(appAdminUpdateRequest, app);
        boolean result = appService.updateById(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(result);
    }

    /**
     * 分页查询应用列表
     *
     * @param appQueryRequest
     * @return
     */
    @PostMapping("/admin/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<App>> listAppByPage(@RequestBody AppQueryRequest appQueryRequest) {
        ThrowUtils.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = appQueryRequest.getPageNum();
        long pageSize = appQueryRequest.getPageSize();
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize),
                appService.getQueryWrapper(appQueryRequest));
        return ResultUtils.success(appPage);
    }

    /**
     * 根据 id 查看应用详情
     *
     * @param id
     * @return
     */
    @PostMapping("/admin/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<App> getAppById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(app);
    }

    // endregion
}
