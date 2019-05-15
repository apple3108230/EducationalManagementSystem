package com.example.school_system.demo.utils;

import org.omg.SendingContext.RunTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;

import java.awt.*;
import java.io.File;
import java.io.IOException;

@Component
public class RedisUtil {

    @Autowired
    public static JedisPool jedisPool;

    private static String redisBatUrl;

    @Value("${redis.bat.url}")
    public void setRedisBatUrl(String url){
        redisBatUrl=url;
    }


    /**
     * 判断是否启动redis服务
     * @return
     */
    public static boolean redisConnectionIsExist(){
        try{
            jedisPool.getResource();
        }catch (NullPointerException ex){
            return false;
        }
        return true;
    }

    /**
     * 开启redis服务
     * @throws IOException
     */
    public static void autoOpenRedis() throws IOException {
        File file=new File(redisBatUrl);
        String cmd="cmd.exe   /C   start   "+redisBatUrl;
        Runtime.getRuntime().exec(cmd);
    }
}
