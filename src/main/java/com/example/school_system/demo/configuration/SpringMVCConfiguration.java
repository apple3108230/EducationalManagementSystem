package com.example.school_system.demo.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * 此类是用于重写处理静态资源。
 * 由于使用了SpringMVC，则是默认使用了@EnableWebMVC,此注解是用于启动SpringMVC特性。
 *
 * 但因为使用了@EnableWebMVC，那么就会将静态资源定位于src/main/webapp，所以需要重写WebMvcConfigurerAdapter中的addResourceHandlers方法，
 * 新增一个handler去访问项目中的静态资源。
 *
 * 浏览器是不让直接访问磁盘中的内容，所以是无法使用绝对路径进行访问！
 *
 */
@Configuration
public class SpringMVCConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    private StudentInterceptor studentInterceptor;

    @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("templates/img/**")
                .addResourceLocations("classpath:/templates/img/");
        registry.addResourceHandler("templates/js/**")
                .addResourceLocations("classpath:/templates/js/");
        registry.addResourceHandler("templates/css/**")
                .addResourceLocations("classpath:/templates/css/");
        registry.addResourceHandler("templates/font/**")
                .addResourceLocations("classpath:/templates/font/");
        registry.addResourceHandler("templates/pdf/student-status-msg/student_img/**")
                .addResourceLocations("classpath:/templates/pdf/student-status-msg/student_img/");
        super.addResourceHandlers(registry);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(studentInterceptor).addPathPatterns("/to/home/student_status_msg");
    }
}
