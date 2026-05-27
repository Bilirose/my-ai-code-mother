package com.yuyuding.myaicodemother.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.yuyuding.myaicodemother.model.dto.AppAddRequest;
import com.yuyuding.myaicodemother.model.dto.AppQueryRequest;
import com.yuyuding.myaicodemother.model.dto.AppUpdateRequest;
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
     * 获取应用视图对象（是否隐藏 initPrompt）
     *
     * @param app
     * @param hideInitPrompt
     * @return
     */
    AppVO getAppVO(App app, boolean hideInitPrompt);

    /**
     * 获取应用视图对象列表
     *
     * @param appList
     * @return
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 获取应用视图对象列表（是否隐藏 initPrompt）
     *
     * @param appList
     * @param hideInitPrompt
     * @return
     */
    List<AppVO> getAppVOList(List<App> appList, boolean hideInitPrompt);

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

    /**
     * 构造当前用户应用列表查询条件
     *
     * @param appName
     * @param userId
     * @return
     */
    QueryWrapper getMyAppQueryWrapper(String appName, Long userId);

    /**
     * 构造精选应用列表查询条件
     *
     * @param appName
     * @return
     */
    QueryWrapper getGoodAppQueryWrapper(String appName);

    /**
     * 分页查询自己的应用列表
     *
     * @param appName
     * @param pageNum
     * @param pageSize
     * @param loginUser
     * @return
     */
    Page<AppVO> listMyAppVOByPage(String appName, long pageNum, long pageSize, User loginUser);

    /**
     * 分页查询精选应用列表
     *
     * @param appName
     * @param pageNum
     * @param pageSize
     * @return
     */
    Page<AppVO> listGoodAppVOByPage(String appName, long pageNum, long pageSize);
}
