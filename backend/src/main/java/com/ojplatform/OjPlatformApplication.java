package com.ojplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 后端应用启动类。
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.ojplatform.mapper")
public class OjPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(OjPlatformApplication.class, args);
    }
}
