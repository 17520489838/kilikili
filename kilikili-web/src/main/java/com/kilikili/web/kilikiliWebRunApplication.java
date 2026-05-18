package com.kilikili.web;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.kilikili")
@MapperScan("com.kilikili.mappers")
public class kilikiliWebRunApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(kilikiliWebRunApplication.class, args);
    }
}
