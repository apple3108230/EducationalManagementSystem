package com.example.school_system.demo;

import com.example.school_system.demo.aop.LogAspect;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Repository;
import javax.annotation.PostConstruct;

@SpringBootApplication
@MapperScan(basePackages = "com.example.school_system.demo.*",annotationClass = Repository.class)
public class DemoApplication {

    @Autowired
    private RedisTemplate redisTemplate;

    @PostConstruct
    public void init(){
        initRedisTemplate();
    }

    @Bean(name="logAspect")
    public LogAspect intiLogAspect(){
        return new LogAspect();
    }

    private void initRedisTemplate(){
        RedisSerializer serializer=redisTemplate.getStringSerializer();
        redisTemplate.setKeySerializer(serializer);
        redisTemplate.setHashValueSerializer(serializer);
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}

