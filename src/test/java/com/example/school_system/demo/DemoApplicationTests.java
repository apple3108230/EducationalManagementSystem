package com.example.school_system.demo;


import com.example.school_system.demo.PageCutter.PageInfo;
import com.example.school_system.demo.controller.AdminController;
import com.example.school_system.demo.controller.TeacherController;
import com.example.school_system.demo.dao.*;
import com.example.school_system.demo.pojo.*;
import com.example.school_system.demo.service.*;
import com.example.school_system.demo.utils.RarUtil;
import com.example.school_system.demo.utils.RedisUtil;
import com.example.school_system.demo.utils.TimeUtil;
import com.example.school_system.demo.utils.ZipUtil;
import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.rarfile.FileHeader;
import com.github.pagehelper.PageHelper;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class DemoApplicationTests {

    @Autowired
    private StudentPersonalMessageDao studentPersonalMessageDao;
    @Autowired
    private AcademyDao academyDao;

    @Test
    public void contextLoads() throws MessagingException {
    }

    @Before
    public void before(){

    }

    @Test
    public void test() throws Exception {
        if(!RedisUtil.redisConnectionIsExist()){
            RedisUtil.autoOpenRedis();
        }
    }
}

