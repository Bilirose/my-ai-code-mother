package com.yuyuding.myaicodemother.controller;

import cn.hutool.core.util.ObjUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.yuyuding.myaicodemother.annotation.AuthCheck;
import com.yuyuding.myaicodemother.common.BaseResponse;
import com.yuyuding.myaicodemother.common.ResultUtils;
import com.yuyuding.myaicodemother.constant.UserConstant;
import com.yuyuding.myaicodemother.exception.ErrorCode;
import com.yuyuding.myaicodemother.exception.ThrowUtils;
import com.yuyuding.myaicodemother.model.dto.chathistory.ChatHistoryQueryRequest;
import com.yuyuding.myaicodemother.model.entity.User;
import com.yuyuding.myaicodemother.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import com.yuyuding.myaicodemother.model.entity.ChatHistory;
import com.yuyuding.myaicodemother.service.ChatHistoryService;

import java.time.LocalDateTime;

/**
 * 对话历史 控制层。
 *
 * @author yuyuding
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;

    /**
     * 分页查询应用的对话历史
     *
     * @param appId          应用id
     * @param pageSize       每页大小
     * @param lastCreateTime 游标时间
     * @param request        请求
     * @return 对话历史分页结果
     */
    @GetMapping("/app/{appId}")
    public BaseResponse<Page<ChatHistory>> listAppChatHistoryByPage(@PathVariable Long appId,
                                                                    @RequestParam(defaultValue = "10") int pageSize,
                                                                    @RequestParam(required = false) LocalDateTime lastCreateTime,
                                                                    HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Page<ChatHistory> chatHistoryPage = chatHistoryService.listAppChatHistoryByPage(appId, pageSize, lastCreateTime, loginUser);
        return ResultUtils.success(chatHistoryPage);
    }

    /**
     * 管理员分页查询对话历史
     *
     * @param chatHistoryQueryRequest 查询参数
     * @return 对话历史分页结果
     */
    @PostMapping("admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistory>> listAppChatHistoryByPageForAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throwIf(ObjUtil.isNull(chatHistoryQueryRequest), ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        int pageNum = chatHistoryQueryRequest.getPageNum();
        ThrowUtils.throwIf(pageNum <= 0, ErrorCode.PARAMS_ERROR, "页码必须大于0");
        int pageSize = chatHistoryQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize <= 0, ErrorCode.PARAMS_ERROR, "页面大小必须大于0");
        // 查询数据
        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        Page<ChatHistory> chatHistoryPage = chatHistoryService.page(Page.of(pageNum, pageSize), queryWrapper);
        return ResultUtils.success(chatHistoryPage);
    }
}
