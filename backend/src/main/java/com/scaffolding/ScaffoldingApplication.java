package com.scaffolding;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 脚手架后端启动类
 * 
 * @author scaffolding
 */
@SpringBootApplication
@MapperScan("com.scaffolding.mapper")
public class ScaffoldingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScaffoldingApplication.class, args);
    }
}
