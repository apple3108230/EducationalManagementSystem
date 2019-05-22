package com.example.school_system.demo.utils;

import org.omg.SendingContext.RunTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Connection;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.awt.*;
import java.io.File;
import java.io.IOException;

@Component
public class RedisUtil {

    @Autowired
    protected static StringRedisTemplate redisTemplate;

    private String redisBatUrl;
    public static Process process;

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
            Jedis jedis= (Jedis) redisTemplate.getConnectionFactory().getConnection().getNativeConnection();
            if(!jedis.isConnected()){
                return false;
            }
        }catch (NullPointerException ex){
            return false;
        }
        return true;
    }

    /**
     * 开启redis服务
     * @throws IOException
     */
    public void autoOpenRedisServer() throws IOException {
        String cmd="cmd.exe   /C   start   "+redisBatUrl;
        Process process=Runtime.getRuntime().exec(cmd);
        this.process=process;
    }

//    public static void main(String[] args){
//        System.out.println(RedisUtil.redisConnectionIsExist());
//    }

}
