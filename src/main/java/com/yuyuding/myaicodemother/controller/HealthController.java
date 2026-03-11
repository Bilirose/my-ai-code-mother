package com.yuyuding.myaicodemother.controller;

import com.yuyuding.myaicodemother.common.BaseResponse;
import com.yuyuding.myaicodemother.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping("/")
    public BaseResponse<String> health() {
        return ResultUtils.success("healthy!");
    }
}
