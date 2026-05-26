package com.yuyuding.myaicodemother.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * 多文件代码返回结果
 */
@Description("生成多个代码文件的结果")
@Data
public class MultiFileCodeResult {

    /**
     * html代码
     */
    @Description("HTML 代码")
    private String htmlCode;

    /**
     * css代码
     */
    @Description("CSS 样式代码")
    private String cssCode;

    /**
     * js代码
     */
    @Description("JS 代码")
    private String jsCode;

    /**
     * 描述
     */
    @Description("生成代码的描述")
    private String description;
}
