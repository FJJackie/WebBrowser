package com.fruitbasket.webbrowser.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.fruitbasket.webbrowser.R;
import com.fruitbasket.webbrowser.utils.BaseActivity;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

/**
 * 注册活动
 */
public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    private TimerTask tt;
    private Timer tm;

    //布局内的控件
    private ImageView btn_back_login;
    private EditText et_phonenum;
    private Button btn_get_validate;
    private EditText et_validate;
    private Button btn_register;

    private int TIME = 60;//倒计时60s这里应该多设置些因为mob后台需要60s,我们前端会有差异的建议设置90，100或者120
    public String country="86";//这是中国区号，如果需要其他国家列表，可以使用getSupportedCountries();获得国家区号
    private String phone;
    private static final int CODE_REPEAT = 1; //重新发送
    Handler hd = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == CODE_REPEAT) {
                btn_get_validate.setEnabled(true);
                btn_register.setEnabled(true);
                tm.cancel();//取消任务
                tt.cancel();//取消任务
                TIME = 60;//时间重置
                btn_get_validate.setText("重新发送验证码");
            }else {
                btn_get_validate.setText(TIME + "s后重新发送");
            }
        }
    };
    //回调
    EventHandler eh=new EventHandler(){
        @Override
        public void afterEvent(int event, int result, Object data) {
            if (result == SMSSDK.RESULT_COMPLETE) {
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                    toast("验证成功");
                }else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE){       //获取验证码成功
                    toast("获取验证码成功");
                }else if (event ==SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES){//如果你调用了获取国家区号类表会在这里回调
                    //返回支持发送验证码的国家列表
                }
            }else{//错误等在这里（包括验证失败）
                //错误码请参照http://wiki.mob.com/android-api-错误码参考/这里我就不再继续写了
                ((Throwable)data).printStackTrace();
                String str = data.toString();
                toast(str);
            }
        }
    };
    //toast的一个小方法
    private void toast(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RegisterActivity.this, str, Toast.LENGTH_SHORT).show();
            }
        });
    }

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

        //配置app你的 SMSSDK.initSDK(this, "您的appkey", "您的appsecret");
        SMSSDK.initSDK(this, "2a84eb8a5da16", "2a84eb8a5da16");
        SMSSDK.registerEventHandler(eh); //注册短信回调（记得销毁，避免泄露内存）
        initViews();
        setupEvents();
    }


    /**
     * 获取布局 控件
     * */
    private void initViews() {
        btn_back_login = (ImageView) findViewById(R.id.btn_back_login);
        et_phonenum = (EditText) findViewById(R.id.et_phonenum);
        btn_get_validate = (Button) findViewById(R.id.btn_get_validate);
        et_validate = (EditText) findViewById(R.id.et_validate);
        btn_register = (Button) findViewById(R.id.btn_register);
        btn_get_validate.setOnClickListener(this);
        btn_register.setOnClickListener(this);
    }


    private void setupEvents() {
        btn_back_login.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back_login:
                Log.d(TAG, "onClick: back_login");
                RegisterActivity.this.finish();
            case R.id.btn_get_validate:
                phone = et_phonenum.getText().toString().trim().replaceAll("/s","");
                if (!TextUtils.isEmpty(phone)) {
                    //定义需要匹配的正则表达式的规则
                    String REGEX_MOBILE_SIMPLE = "^[1](([3][0-9])|([4][5,7,9])|([5][4,6,9])|([6][6])|([7][3,5,6,7,8])|([8][0-9])|([9][8,9]))[0-9]{8}$";
                    //把正则表达式的规则编译成模板
                    Pattern pattern = Pattern.compile(REGEX_MOBILE_SIMPLE);
                    //把需要匹配的字符给模板匹配，获得匹配器
                    Matcher matcher = pattern.matcher(phone);
                    // 通过匹配器查找是否有该字符，不可重复调用重复调用matcher.find()
                    if (matcher.find()) {//匹配手机号是否存在
                        alterWarning();
                    } else {
                        toast("手机号格式错误");
                    }
                } else {
                    toast("请先输入手机号");
                }
                break;
            case R.id.btn_register:
                //获得用户输入的验证码
                String code = et_validate.getText().toString().replaceAll("/s","");
                if (!TextUtils.isEmpty(code)) {//判断验证码是否为空
                    //验证
                    SMSSDK.submitVerificationCode( country,  phone,  code);
                }else{//如果用户输入的内容为空，提醒用户
                    toast("请输入验证码后再提交");
                }
                break;
        }
    }


    //弹窗确认下发
    private void alterWarning() {
        // 2. 通过sdk发送短信验证
        AlertDialog.Builder builder = new AlertDialog.Builder(this);  //先得到构造器
        builder.setTitle("提示"); //设置标题
        builder.setMessage("我们将要发送到" + phone + "验证"); //设置内容
        builder.setIcon(R.mipmap.ic_launcher);//设置图标，图片id即可
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            //设置确定按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); //关闭dialog
                // 2. 通过sdk发送短信验证（请求获取短信验证码，在监听（eh）中返回）
                SMSSDK.getVerificationCode(country, phone);
                //做倒计时操作
                Toast.makeText(RegisterActivity.this, "已发送" + which, Toast.LENGTH_SHORT).show();
                btn_get_validate.setEnabled(false);
                btn_register.setEnabled(true);
                tm = new Timer();
                tt = new TimerTask() {
                    @Override
                    public void run() {
                        hd.sendEmptyMessage(TIME--);
                    }
                };
                tm.schedule(tt,0,1000);
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() { //设置取消按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Toast.makeText(RegisterActivity.this, "已取消" + which, Toast.LENGTH_SHORT).show();
            }
        });
        //参数都设置完成了，创建并显示出来
        builder.create().show();
    }

    //销毁短信注册
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销回调接口registerEventHandler必须和unregisterEventHandler配套使用，否则可能造成内存泄漏。
        SMSSDK.unregisterEventHandler(eh);
    }

}


