package com.yuyuding.myaicodemother.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.yuyuding.myaicodemother.model.dto.app.AppAddRequest;
import com.yuyuding.myaicodemother.model.dto.app.AppQueryRequest;
import com.yuyuding.myaicodemother.model.dto.app.AppUpdateRequest;
import com.yuyuding.myaicodemother.model.entity.App;
import com.yuyuding.myaicodemother.model.entity.User;
import com.yuyuding.myaicodemother.model.vo.AppVO;

import java.util.List;

/**
 * 应用表 服务层。
 *
 * @author yuyuding
 */
public interface AppService extends IService<App> {

    /**
     * 创建应用
     *
     * @param appAddRequest
     * @param loginUser
     * @return 新应用 id
     */
    long createApp(AppAddRequest appAddRequest, User loginUser);

    /**
     * 更新自己的应用
     *
     * @param appUpdateRequest
     * @param loginUser
     * @return
     */
    boolean updateMyApp(AppUpdateRequest appUpdateRequest, User loginUser);

    /**
     * 删除自己的应用
     *
     * @param id
     * @param loginUser
     * @return
     */
    boolean deleteMyApp(long id, User loginUser);

    /**
     * 获取应用视图对象
     *
     * @param app
     * @return
     */
    AppVO getAppVO(App app);

    /**
     * 获取应用视图对象列表
     *
     * @param appList
     * @return
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 校验应用
     *
     * @param app
     * @param add
     */
    void validApp(App app, boolean add);

    /**
     * 校验应用操作权限（仅本人可操作）
     *
     * @param loginUser
     * @param app
     */
    void checkAppAuth(User loginUser, App app);

    /**
     * 根据查询条件构造查询参数（管理员）
     *
     * @param appQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);
}
