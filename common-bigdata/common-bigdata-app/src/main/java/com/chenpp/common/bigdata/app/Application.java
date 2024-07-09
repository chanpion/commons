package com.chenpp.common.bigdata.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author April.Chen
 * @date 2024/7/2 18:03
 */
@SpringBootApplication(scanBasePackages = "com.chenpp.common.bigdata")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
