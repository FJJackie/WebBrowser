package com.fruitbasket.webbrowser.utils;

import android.util.Log;

import java.lang.reflect.Field;

/**
  * 日志定制工具，可以随时屏蔽日志，
 * 将level 设置为NOTHING即可屏蔽所有日志
* */
public class LogUtil {
    public static final  int VALUE = 0;
    public static final int VERBOSE = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARN = 4;
    public static final int ERROR = 5;
    public static final int NOTHING = 6;
    public static final int level = VALUE;

    public static void v(String tag,String msg){
        if(level <= VERBOSE){
            Log.v(tag,msg);
        }
    }
    public static void d(String tag,String msg){
        if(level <= DEBUG){
            Log.d(tag,msg);
        }
    }
    public static void i(String tag,String msg){
        if(level <= INFO){
            Log.i(tag,msg);
        }
    }
    public static void w(String tag,String msg){
        if(level <= WARN){
            Log.w(tag,msg);
        }
    }
    public static void e(String tag,String msg){
        if(level <= ERROR){
            Log.e(tag,msg);
        }
    }

    //同个反射获取对象内容
    public static void ObjectValue(String tag,final Object object) {
        String result = "";
        if(object == null){
            return;
        }
        Class<? extends Object> rtClass = object.getClass();
        Field[] fields = rtClass.getDeclaredFields();//获取所有属性
        for (Field field : fields) {
            //获取是否可访问
            boolean flag = field.isAccessible();
            try {
                //设置该属性总是可访问
                field.setAccessible(true);
                result += field.getName()+"="+field.get(object) + ", ";
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            //还原可访问权限
            field.setAccessible(flag);
        }
        //打印对象内容日志
        if(level <= VALUE){
            Log.d(tag, "ObjectValue: "+result);
        }
    }
}
