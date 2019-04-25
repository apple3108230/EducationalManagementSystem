package com.example.school_system.demo.PageCutter;

import lombok.Data;

@Data
/**
 * @deprecated
 */
public class PageInfo {

    //sql总查询结果条数
    private Integer total;
    //每页显示条数
    private Integer pageSize;
    //总页数
    private Integer totalPage;
    //当前页码
    private Integer pageNum;
    //是否使用分页插件
    private Boolean usePageCutter;
    //当前开启检查页面是否合法
    private Boolean CheckPage;

}
