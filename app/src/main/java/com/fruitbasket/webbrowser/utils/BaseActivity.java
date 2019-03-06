package com.fruitbasket.webbrowser.utils;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


/**
* 所有活动的父类
  用途：用于知晓当前活动在哪个活动
  通过日志 BaseActivity:"当前活动名称" 查看
* */
public class BaseActivity extends AppCompatActivity {

    public static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, getClass().getSimpleName());
        ActivityCollector.addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
