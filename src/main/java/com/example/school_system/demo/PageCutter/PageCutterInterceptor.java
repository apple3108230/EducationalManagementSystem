package com.example.school_system.demo.PageCutter;

import com.example.school_system.demo.utils.PageCutterUtil;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.util.Properties;

/**
 * @deprecated 当使用foreach标签循环map时会有bug 暂时先使用第三方分页插件
 */
@Intercepts({@Signature(type= StatementHandler.class,method="prepare",args = {Connection.class,Integer.class})})
public class PageCutterInterceptor implements Interceptor {

    private PageInfo pageInfo;

    //若没有填写插件参数 则使用默认值
    final private static Integer DEFAULT_PAGE_NUM=1;
    final private static Integer DEFAULT_PAGE_SIZE=20;
    final private static boolean DEFAULT_CHECK_PAGE=false;
    final private static boolean DEFAULT_USE_PAGE_CUTTER=false;
    Logger logger= LoggerFactory.getLogger(PageCutterInterceptor.class);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        //获取真实的StatementHandler对象
        StatementHandler statementHandler= (StatementHandler) PageCutterUtil.getUnProxyObject(invocation.getTarget());
        MetaObject metaObject= SystemMetaObject.forObject(statementHandler);
        //从StatementHandler中获取boundSql中的sql
        String sql= (String) metaObject.getValue("delegate.boundSql.sql");
        //MappedStatement mappedStatement= (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        //若不是select语句 则不拦截此sql语句
        if(!PageCutterUtil.isSelectSql(sql)){
            return invocation.proceed();
        }
        BoundSql boundSql= (BoundSql) metaObject.getValue("delegate.boundSql");
        Object parameterObject=boundSql.getParameterObject();
        //从parameterObject中获取PageInfo
        PageInfo info=PageCutterUtil.getPageInfoFromParameterObject(parameterObject);
        if(info==null){
            return invocation.proceed();
        }
        boolean usePageCutter=info.getUsePageCutter()==null?pageInfo.getUsePageCutter():DEFAULT_USE_PAGE_CUTTER;
        //若不使用插件 则不拦截此sql语句
        if(usePageCutter==false){
            return invocation.proceed();
        }
        Integer pageNum=info.getPageNum()==null?pageInfo.getPageNum():info.getPageNum();
        int pageSize=info.getPageSize()==null?pageInfo.getPageSize():info.getPageSize();
        Boolean checkPageNum=info.getCheckPage()==null?pageInfo.getCheckPage():DEFAULT_CHECK_PAGE;
        int total=PageCutterUtil.getTotal(invocation,metaObject,boundSql);
        info.setTotal(total);
        //总页数 若有余数 则页数=页数+1
        int totalPage=total%pageSize==0?total/pageSize:total/pageSize+1;
        info.setTotalPage(totalPage);
        //检查查询页码是否合法
        PageCutterUtil.checkPageLegal(checkPageNum,pageNum,totalPage);
        return PageCutterUtil.pageCutterSQL(invocation,metaObject,boundSql,pageNum,pageSize);
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target,this);
    }

    @Override
    public void setProperties(Properties properties) {
        pageInfo=new PageInfo();
        if(properties.getProperty("pageSize")==null){
            pageInfo.setPageSize(DEFAULT_PAGE_SIZE);
        }else{
            pageInfo.setPageSize(Integer.parseInt(properties.getProperty("pageSize")));
        }
        if(properties.getProperty("pageNum")==null){
            pageInfo.setPageNum(DEFAULT_PAGE_NUM);
        }else{
            pageInfo.setPageNum(Integer.parseInt(properties.getProperty("pageNum")));
        }
        if(properties.getProperty("checkPage")==null){
            pageInfo.setCheckPage(DEFAULT_CHECK_PAGE);
        }else{
            pageInfo.setCheckPage(Boolean.valueOf(properties.getProperty("checkPage")));
        }
        if(properties.getProperty("usePageCutter")==null){
            pageInfo.setUsePageCutter(DEFAULT_USE_PAGE_CUTTER);
        }else{
            pageInfo.setUsePageCutter(Boolean.valueOf(properties.getProperty("usePageCutter")));
        }
    }
}
