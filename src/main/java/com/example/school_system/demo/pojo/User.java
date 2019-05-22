package com.example.school_system.demo.pojo;

import lombok.Data;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.ibatis.type.Alias;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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

    public User(Integer id, String username, String password, String role, String permission, String email,String salt) {
        this.id = String.valueOf(id);
        this.username = username;
        this.password = password;
        this.role = role;
        this.permission = permission;
        this.email=email;
        this.salt=salt;
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

    /**
     * 重写equals需要注意等价关系：自反性、对称性、传递性和一致性。除此之外，在与空值对比时要返回false
     * 重写equals方式时还需要重写其hashCode方法，否则相同的对象的hashcode是不一样的，导致HashMap,HashTable,HashSet等Hash类型的集合的部分方法不能正常使用
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(username, user.username) &&
                Objects.equals(password, user.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password);
    }

}
