package com.fruitbasket.webbrowser.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;

import com.fruitbasket.webbrowser.R;
import com.fruitbasket.webbrowser.utils.Base64Utils;
import com.fruitbasket.webbrowser.utils.BaseActivity;
import com.fruitbasket.webbrowser.utils.SharedPreferencesUtils;
import com.fruitbasket.webbrowser.widget.LoadingDialog;

/**
 * 登录界面
 */

public class WelcomeActivity extends BaseActivity {


    //登录状态提示信息
    private static String tipMsg;

    //显示正在加载的对话框
    private LoadingDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        //隐藏标题栏
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }

        delayed();

    }

    private void delayed() {
        //子线程进行延时3s
        showLoading();//显示加载框
        Thread loginRunnable = new Thread() {
            @Override
            public void run() {
                super.run();
                //睡眠
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                showToast("欢迎使用");
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                finish();//关闭页面
                hideLoading();//隐藏加载框
            }
        };
        loginRunnable.start();
    }

    public void showLoading() {
        if (mLoadingDialog == null) {
            mLoadingDialog = new LoadingDialog(this, getString(R.string.welcome), false);
        }
        mLoadingDialog.show();
    }

    /**
     * 隐藏加载的进度框
     */
    public void hideLoading() {
        if (mLoadingDialog != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLoadingDialog.hide();
                }
            });
        }
    }


    /**
     * 页面销毁前回调的方法
     */
    protected void onDestroy() {
        if (mLoadingDialog != null) {
            mLoadingDialog.cancel();
            mLoadingDialog = null;
        }
        super.onDestroy();
    }


    public void showToast(String msg) {
        tipMsg = msg;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(WelcomeActivity.this, tipMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
