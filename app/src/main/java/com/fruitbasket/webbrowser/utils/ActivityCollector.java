package com.fruitbasket.webbrowser.utils;

import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * 活动管理器类
 * */
public class ActivityCollector {
    public static final String TAG = "ActivityCollector";

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
        LogUtil.d(TAG, "finishAll: 结束所有活动");
        for(Activity activity:activities){
            if(!activity.isFinishing()){
                activity.finish();
            }
        }
        activities.clear();
    }
}
