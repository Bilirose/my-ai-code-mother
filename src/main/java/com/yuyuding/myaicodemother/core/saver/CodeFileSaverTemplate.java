package com.yuyuding.myaicodemother.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.yuyuding.myaicodemother.exception.BusinessException;
import com.yuyuding.myaicodemother.exception.ErrorCode;
import com.yuyuding.myaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

public abstract class CodeFileSaverTemplate<T> {

    /**
     * 文件保存的根目录
     */
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 模版方法：保存代码的标准流程
     *
     * @param result 代码结果对象
     * @return 保存的文件
     */
    public final File saveCode(T result) {
        // 1. 验证输入
        validateInput(result);
        // 2. 构建唯一目录
        String baseDirPath = buildUniqueDir();
        // 3. 保存文件（具体实现交给子类）
        saveFiles(result, baseDirPath);
        // 4. 返回保存的文件
        return new File(baseDirPath);
    }

    /**
     * 验证子类参数（可由子类覆盖）
     *
     * @param result 代码结果对象
     */
    protected void validateInput(T result) {
        if (result == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码结果对象不能为空");
        }
    }

    /**
     * 构建文件的唯一路径（业务类型 + 雪花 ID）tmp/code_output/codeType_snowflakeId
     *
     * @return 目录路径
     */
    protected String buildUniqueDir() {
        String codeType = getCodeGenType().getValue();
        String uniqueName = StrUtil.format("{}_{}", codeType + "_", IdUtil.getSnowflakeNextIdStr());
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 保存单个文件
     *
     * @param dirPath
     * @param fileName
     * @param content
     */
    public final void writeToFile(String dirPath, String fileName, String content) {
        if(StrUtil.isNotBlank( content)){
            String filePath = dirPath + File.separator + fileName;
            FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
        }
    }

    /**
     * 保存文件（由子类实现）
     *
     * @param result      代码结果对象
     * @param baseDirPath 文件保存的根目录
     */
    protected abstract void saveFiles(T result, String baseDirPath);

    /**
     * 获取代码生成类型
     *
     * @return 代码生成类型
     */
    protected abstract CodeGenTypeEnum getCodeGenType();
}
