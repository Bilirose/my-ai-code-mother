package com.yuyuding.myaicodemother.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * html 代码返回结果
 */
@Description("生成 HTML 代码文件的结果")
@Data
public class HtmlCodeResult {

    /**
     * html 代码
     */
    @Description("生成的 HTML 源码")
    private String htmlCode;

    /**
     * 描述
     */
    @Description("生成的 HTML 源码的描述")
    private String description;
}
