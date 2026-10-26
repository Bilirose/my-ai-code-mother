package com.yuyuding.myaicodemother.core;

import com.yuyuding.myaicodemother.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateAndSaveCode() {
        File file = aiCodeGeneratorFacade.generateAndSaveCode("生成一个HTML页面，内容是：<h1>Hello World</h1>", CodeGenTypeEnum.MULTI_FILE, 1L);
        Assertions.assertNotNull(file);
    }


    @Test
    void generateAndSaveCodeStream() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream("生成一个HTML页面，内容是：<h1>Hello World</h1>", CodeGenTypeEnum.HTML, 1L);
        // 阻塞等待所有数据收集完成
        List<String> result = codeStream.collectList().block();
        // 验证结果
        Assertions.assertNotNull(result);
        // 拼接字符串，得到完整结果
        String wholeContent = String.join("", result);
        Assertions.assertNotNull(wholeContent);
    }

    @Test
    void generateVueProjectCodeStream() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(
                "生成一个简单的日历网站，总代码量不超过200行",
                CodeGenTypeEnum.VUE_PROJECT, 1L);
        // 阻塞等待所有数据收集完成
        List<String> result = codeStream.collectList().block();
        // 验证结果
        Assertions.assertNotNull(result);
        // 拼接字符串，得到完整结果
        String wholeContent = String.join("", result);
        Assertions.assertNotNull(wholeContent);
    }
}