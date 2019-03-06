package com.fruitbasket.webbrowser.utils;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * 活动管理器类
 * */
public class ActivityCollector {

    //活动容器
    public static List<Activity> activities = new ArrayList<>();

    public static void addActivity(Activity activity){
        activities.add(activity);
    }

    //移除一个活动
    public static void removeActivity(Activity activity){
        activities.remove(activity);
    }

    //全部销毁活动
    public static void finishAll(){
        for(Activity activity:activities){
            if(!activity.isFinishing()){
                activity.finish();
            }
        }
        activities.clear();
    }
}
