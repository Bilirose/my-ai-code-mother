package com.yuyuding.myaicodemother;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.yuyuding.myaicodemother.mapper")
public class MyAiCodeMotherApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyAiCodeMotherApplication.class, args);
    }

}
