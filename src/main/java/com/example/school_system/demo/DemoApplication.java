package com.example.school_system.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Repository;

@SpringBootApplication
@MapperScan(basePackages = "com.example.school_system.demo.*",annotationClass = Repository.class)
//@ImportResource(value = {"classpath:shiro-application.xml"})
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}

