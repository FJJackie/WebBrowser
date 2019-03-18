package com.fruitbasket.webbrowser.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.fruitbasket.webbrowser.R;
import com.fruitbasket.webbrowser.utils.BaseActivity;

/**
 * 注册活动
 */
public class RegisterActivity extends BaseActivity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {

    //布局内的控件
    private ImageView back_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //隐藏标题栏
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }

        initViews();
        setupEvents();
    }


    /**
     * 获取布局 控件
     * */
    private void initViews() {
        back_login = (ImageView) findViewById(R.id.back_login);
    }


    private void setupEvents() {
        back_login.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_login:
                Log.d(TAG, "onClick: back_login");
                RegisterActivity.this.finish();
                break;
        }
    }
    /**
     * CheckBox点击时的回调方法 ,不管是勾选还是取消勾选都会得到回调
     *
     * @param buttonView 按钮对象
     * @param isChecked  按钮的状态
     */
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }
}
