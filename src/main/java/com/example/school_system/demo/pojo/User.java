package com.example.school_system.demo.pojo;

import lombok.Data;
import org.apache.ibatis.type.Alias;
import java.io.Serializable;

@Data
@Alias("user")
public class User implements Serializable {
    private String id;
    private String username;
    private String password;
    private String role;
    private String permission;
    private String email;
    private String salt;


    public User(String username,String password){
        this.username=username;
        this.password=password;
    }

    public User(String id, String username, String password, String role, String permission, String email,String salt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.permission = permission;
        this.email=email;
        this.salt=salt;
    }
}
