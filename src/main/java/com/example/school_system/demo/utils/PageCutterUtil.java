package com.example.school_system.demo.utils;

import com.example.school_system.demo.PageCutter.PageInfo;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PageCutterUtil {

    /**获取真实的非代理对象**/
    public static Object getUnProxyObject(Object target){
        MetaObject metaObject= SystemMetaObject.forObject(target);
        Object obj=null;
        while(metaObject.hasGetter("h")){
            obj=metaObject.getValue("h");
            metaObject=SystemMetaObject.forObject(target);
        }
        if(obj==null){
            return target;
        }
        return null;
    }

    /**判断sql语句是否是select语句**/
    public static boolean isSelectSql(String sql){
        String var1=sql.trim().toLowerCase();
        int index=var1.indexOf("select");
        return index==0;
    }

    /**
     *从parameterObject中获取PageInfo
     * @param paramaterObject
     * @return
     * @throws IntrospectionException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static PageInfo getPageInfoFromParameterObject(Object paramaterObject) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        if(paramaterObject==null){
            return null;
        }
        PageInfo pageInfo=null;
        //若有多个参数或使用了@Param 则是Map类型的
        if(paramaterObject instanceof Map){
            Map<String,Object> map= (Map<String, Object>) paramaterObject;
            Set<String> keySet=map.keySet();
            Iterator<String> iterator=keySet.iterator();
            while(iterator.hasNext()){
                String key=iterator.next();
                Object value=map.get(key);
                if(value instanceof PageInfo){
                    return (PageInfo) value;
                }
            }
            //若参数是PageInfo或继承了PageInfo
        }else if(paramaterObject instanceof PageInfo){
            return (PageInfo)paramaterObject;
            //尝试从POJO中读取分页参数 通过PropertyDescriptor反射获取getter方法
        }else{
            Field[] fields=paramaterObject.getClass().getDeclaredFields();
            for(Field field:fields){
                if(field.getType()==PageInfo.class){
                    //
                    PropertyDescriptor pd=new PropertyDescriptor(field.getName(),paramaterObject.getClass());
                    //获取getter方法
                    Method getter=pd.getReadMethod();
                    return (PageInfo) getter.invoke(paramaterObject);
                }
            }
        }
        return pageInfo;
    }

    //获取总条数
    public static int getTotal(Invocation invocation, MetaObject metaObject, BoundSql boundSql) throws SQLException {
        MappedStatement mappedStatement= (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        Configuration configuration=mappedStatement.getConfiguration();
        String sql= (String) metaObject.getValue("delegate.boundSql.sql");
        String countSql="select count(*) as total from("+sql+") $_total";
        //从invocation中获取Connection
        Connection connection= (Connection) invocation.getArgs()[0];
        PreparedStatement preparedStatement=null;
        int total=0;
        try{
            //从Connection中获取PrepareStatement
            preparedStatement=connection.prepareStatement(countSql);
            //创建一个新的BoundSql 用于查询总条数
            BoundSql countBoundSql=new BoundSql(configuration,countSql,boundSql.getParameterMappings(),boundSql.getParameterObject());
            //创建新的ParameterHandler
            ParameterHandler parameterHandler=new DefaultParameterHandler(mappedStatement,boundSql.getParameterObject(),countBoundSql);
            parameterHandler.setParameters(preparedStatement);
            ResultSet rs=preparedStatement.executeQuery();
            while(rs.next()){
                total=rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            //这里不能关闭connection 否则后面的操作就不能继续进行
            if(preparedStatement!=null){
                preparedStatement.close();
            }
        }
        return total;
    }

    //检查查询页码是否合法
    public static void checkPageLegal(boolean useCheckPage,int pageNum,int totalPage) throws Exception {
        if(useCheckPage){
           if(pageNum>totalPage){
               throw new Exception("查询失败！查询页码大于总页数");
           }
        }
    }

    //修改sql
    public static Object pageCutterSQL(Invocation invocation,MetaObject metaObject,BoundSql boundSql,int pageNum,int pageSize) throws InvocationTargetException, IllegalAccessException, SQLException {
       String sql=boundSql.getSql();
       String pageCutterSql="select * from ("+sql+") $_page_cutter_table limit ?,?";
       metaObject.setValue("delegate.boundSql.sql",pageCutterSql);
       Object statement=invocation.proceed();
       PageCutterUtil pageCutterUtil=new PageCutterUtil();
       pageCutterUtil.preparePageInfo((PreparedStatement) statement,pageNum,pageSize);
       return statement;
    }

    private void preparePageInfo(PreparedStatement preparedStatement,int pageNum,int pageSize) throws SQLException {
        int index=preparedStatement.getParameterMetaData().getParameterCount();
        preparedStatement.setInt(index-1,(pageNum-1)*pageSize);
        preparedStatement.setInt(index,pageSize);
    }
}
