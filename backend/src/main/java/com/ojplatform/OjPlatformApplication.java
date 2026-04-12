package com.ojplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI 智能 OJ 学习平台 - 启动类
 */
@SpringBootApplication
@MapperScan("com.ojplatform.mapper")
public class OjPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(OjPlatformApplication.class, args);
    }
}
