package com.yuyuding.myaicodemother.core;

import com.yuyuding.myaicodemother.ai.model.HtmlCodeResult;
import com.yuyuding.myaicodemother.ai.model.MultiFileCodeResult;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CodeParserTest {

    @Test
    void parseHtmlCode() {
        // 准备测试数据
        String content = """
                Here is some text
                ```html
                <!DOCTYPE html>
                <html>
                <head><title>Test</title></head>
                <body>Hello World</body>
                </html>
                ```
                """;
        HtmlCodeResult result = CodeParser.parseHtmlCode(content);
        assertNotNull(result);
        assertNotNull(result.getHtmlCode());
    }

    @Test
    void parseMultiFileCode() {
        String content = """
                Some intro text
                
                ```html
                <!DOCTYPE html>
                <html><body>HTML Content</body></html>
                ```
                
                ```css
                body { color: red; }
                .container { padding: 10px; }
                ```
                
                ```javascript
                console.log("Hello JS");
                function test() { return true; }
                ```
                
                End text
                """;
        MultiFileCodeResult result = CodeParser.parseMultiFileCode(content);
        assertNotNull(result);
        assertNotNull(result.getHtmlCode());
        assertNotNull(result.getCssCode());
        assertNotNull(result.getJsCode());
    }
}