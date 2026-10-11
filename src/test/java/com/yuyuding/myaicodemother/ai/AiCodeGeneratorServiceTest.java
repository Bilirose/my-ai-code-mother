package com.yuyuding.myaicodemother.ai;

import com.yuyuding.myaicodemother.ai.model.HtmlCodeResult;
import com.yuyuding.myaicodemother.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {
        HtmlCodeResult htmlCode = aiCodeGeneratorService.generateHtmlCode("生成一个HTML页面，内容是：<h1>Hello World</h1>");
        Assertions.assertNotNull(htmlCode);
    }

    @Test
    void generateMultiFileCode() {
        MultiFileCodeResult multiFileCode = aiCodeGeneratorService.generateMultiFileCode("生成两个HTML页面，内容是：<h1>Hello World</h1>");
        Assertions.assertNotNull(multiFileCode);
    }

    @Test
    void testChatMemory(){
        HtmlCodeResult htmlCode = aiCodeGeneratorService.generateHtmlCode("生成一个HTML页面，内容是：<h1>Hello World</h1>");
        Assertions.assertNotNull(htmlCode);
        htmlCode = aiCodeGeneratorService.generateHtmlCode("不要生成网页，告诉我你刚刚做了什么");
        Assertions.assertNotNull(htmlCode);
        htmlCode = aiCodeGeneratorService.generateHtmlCode("不要生成网页，告诉我你刚刚做了什么");
        Assertions.assertNotNull(htmlCode);
    }
}