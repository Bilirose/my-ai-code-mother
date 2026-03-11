package com.yuyuding.myaicodemother.common;

import lombok.Data;

/**
 * 请求封装类
 */
@Data
public class PageRequest {

    /**
     * 当前页码
     */
    private int pageNum = 1;

    /**
     * 页面大小
     */
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序方式
     */
    private String sortOrder = "desc";

}
