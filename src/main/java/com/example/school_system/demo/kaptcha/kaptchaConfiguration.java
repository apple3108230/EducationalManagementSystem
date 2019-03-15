package com.example.school_system.demo.kaptcha;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class kaptchaConfiguration {
    @Bean
    public DefaultKaptcha getDefaultKaptcha(){
        DefaultKaptcha defaultKaptcha=new DefaultKaptcha();
        Properties properties=new Properties();
        properties.setProperty("kaptcha.session.key","code");
        //阴影实现类
        properties.setProperty("kaptcha.obscurificator.impl","com.google.code.kaptcha.impl.ShadowGimpy");
        //验证码有无边框
        properties.setProperty("kaptcha.border","yes");
        //边框颜色
        properties.setProperty("kaptcha.border.color","white");
        properties.setProperty("kaptcha.image.width","200");
        properties.setProperty("kaptcha.image.height","50");
        //验证码长度
        properties.setProperty("kaptcha.textproduct.char.length","4");
        //验证码字符集
        properties.setProperty("kaptcha.textproducer.char.string","0123456789ABCEFGHIJKLMNOPQRSTUVWXYZ");
        //验证码干扰颜色
        properties.setProperty("kaptcha.noise.color","black");
        //字符之间的间隙
        properties.setProperty("kaptcha.textproduct.char.space","3");
        properties.setProperty("kaptcha.textproduct.font.size","40");
        properties.setProperty("kaptcha.textproducer.font.names","宋体");
        properties.setProperty("kaptcha.textproduct.font.color","black");
        //渐变开始颜色
        properties.setProperty("kaptcha.background.clear.from","185,55,210");
        //渐变结束颜色
        properties.setProperty("kaptcha.background.clear.to","white");
        Config config=new Config(properties);
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }
}
