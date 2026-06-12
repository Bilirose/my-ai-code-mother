package com.yuyuding.myaicodemother.constant;

/**
 * 应用常量
 */
public interface AppConstant {

    /**
     * 用户分页每页最大数量
     */
    int MAX_PAGE_SIZE = 20;

    /**
     * 精选应用优先级
     */
    int GOOD_APP_PRIORITY = 99;

    /**
     * 默认应用优先级
     */
    int DEFAULT_APP_PRIORITY = 99;

    /**
     * 默认应用名称
     */
    String DEFAULT_APP_NAME = "未命名应用";

    /**
     * 应用生成根目录
     */
    String CODE_OUTPUT_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 应用部署根目录
     */
    String CODE_DEPLOY_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_deploy";

    /**
     * 应用部署域名
     */
    String CODE_DEPLOY_HOST = "http://localhost:8080";
}
