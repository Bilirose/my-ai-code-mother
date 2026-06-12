package com.yuyuding.myaicodemother.core.saver;

import com.yuyuding.myaicodemother.ai.model.HtmlCodeResult;
import com.yuyuding.myaicodemother.ai.model.MultiFileCodeResult;
import com.yuyuding.myaicodemother.exception.BusinessException;
import com.yuyuding.myaicodemother.exception.ErrorCode;
import com.yuyuding.myaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * 代码保存执行器
 * 根据代码生成类型执行相应的保存逻辑
 */
public class CodeFileSaverExecutor {

    private static final HtmlCodeFileSaverTemplate htmlCodeFileSaverTemplate = new HtmlCodeFileSaverTemplate();

    private static final MultiFileCodeFileSaverTemplate multiFileCodeFileSaverTemplate = new MultiFileCodeFileSaverTemplate();

    /**
     * 执行代码保存
     *
     * @param codeResult      代码结果对象
     * @param codeGenTypeEnum 代码生成类型
     * @param appId           应用ID
     * @return 保存的文件
     */
    public static File executeSaver(Object codeResult, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        return switch (codeGenTypeEnum) {
            case HTML -> htmlCodeFileSaverTemplate.saveCode((HtmlCodeResult) codeResult, appId);
            case MULTI_FILE -> multiFileCodeFileSaverTemplate.saveCode((MultiFileCodeResult) codeResult, appId);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码保存类型");
        };
    }
}
