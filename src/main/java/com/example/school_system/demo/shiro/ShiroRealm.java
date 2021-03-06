package com.example.school_system.demo.shiro;

import com.example.school_system.demo.pojo.User;
import com.example.school_system.demo.service.BaseService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class ShiroRealm extends AuthorizingRealm {

    @Autowired
    private BaseService baseService;

    //用户认证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        //UsernamePasswordToken用于存放提交的登录信息
        UsernamePasswordToken token= (UsernamePasswordToken) authenticationToken;
        User user= baseService.getUserByUserName(token.getUsername());
        if(user==null){
            return null;
        }
        //返回认证信息 使用用户名作为salt
        else{
            String hashPwd=user.getPassword();
            ByteSource salt=ByteSource.Util.bytes(user.getUsername());
            //user.getPassword是从数据库中获取的，getName获取的是当前realm的名字
            SimpleAuthenticationInfo simpleAuthenticationInfo=new SimpleAuthenticationInfo(user,hashPwd,salt,getName());
            return simpleAuthenticationInfo;
        }

    }

    //授权认证
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        User user= (User) principalCollection.fromRealm(getName()).iterator().next();
        if(baseService.getUserByUserName(user.getUsername())!=null){
            SimpleAuthorizationInfo info=new SimpleAuthorizationInfo();
            String[] roles=user.getRole().split(",");
            List<String> roleList=new ArrayList<>();
            for(int i=0;i<roles.length;i++){
                roleList.add(roles[i]);
            }
            info.addRoles(roleList);
            return info;
        }
        return null;
    }
}
