package com.fruitbasket.webbrowser.activities;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.fruitbasket.webbrowser.CameraSurfaceView;
import com.fruitbasket.webbrowser.R;
import com.fruitbasket.webbrowser.messages.MeasurementStepMessage;
import com.fruitbasket.webbrowser.messages.MessageHUB;
import com.fruitbasket.webbrowser.messages.MessageListener;
import com.fruitbasket.webbrowser.slider_widget.Item;
import com.fruitbasket.webbrowser.slider_widget.MyAdapter;
import com.fruitbasket.webbrowser.utils.ActivityCollector;
import com.fruitbasket.webbrowser.utils.BaseActivity;
import com.fruitbasket.webbrowser.utils.JsObject;
import com.fruitbasket.webbrowser.utils.LogUtil;


public class MainActivity extends BaseActivity implements MessageListener, SensorEventListener,
        AdapterView.OnItemClickListener{
    private static final String TAG = "MainActivity";

    public static final String CAM_SIZE_WIDTH = "intent_cam_size_width";
    public static final String CAM_SIZE_HEIGHT = "intent_cam_size_height";
    public static final String AVG_NUM = "intent_avg_num";
    public static final String PROBANT_NAME = "intent_probant_name";
    private static final int BRIGHTNESS_FACTOR_DEFAULT = 1;
    private static final int FONT_SIZE_FACTOR_DEFAULT = 1;

    private HorizontalScrollView part0;
    private LinearLayout part1;
    private LinearLayout part2;


    //UI控件部分
    // 相机预览控件
    private CameraSurfaceView _mySurfaceView;
    Camera _cam;

    TextView _currentDistanceView;  //当前距离
    Button _calibrateButton;  //校准按钮

    private Button goTo;  //去到按钮
    private EditText url;  //url编辑栏

    private EditText fontSize;  //字体大小
    private Button fontSizeOk;  //字体大小确定

    private EditText sizeFactor;  //尺寸大小
    private Button sizeFactorOk;  //尺寸大小确定

    private TextView sizeView;  //

    private EditText brightness;  //亮度
    private Button brightnessOk;  //亮度确定

    private EditText brightnessFactor;  //亮度因子
    private Button brightnessFactorOk;  //亮度因子确定

    private TextView backgroundBrightness;  //背景亮度
    private TextView brightnessView;  //背景视图

    private EditText etEyeDistance;
    private TextView tvAngle;
    private TextView tvMoveDistance;//in pixel
    private TextView tvEyeDistance;//in pixel

    private WebView webView;

    private SensorManager sensorManager;
    private Sensor sensor;
    private double lb;

    //WindowManager.LayoutParams用于向WindowManager描述Window的管理策略
    private WindowManager.LayoutParams layoutParams;
    private int count = 0;

    private float _currentDevicePosition;
    private int _cameraHeight;
    private int _cameraWidth;
    private int _avgNum;
    private float distToFace;
    private float brightnessValue;///环境光亮度值
    private int fontSizeFactor = FONT_SIZE_FACTOR_DEFAULT;///后期可取消这两个值
    private int bFactor = BRIGHTNESS_FACTOR_DEFAULT;///

    private float lastDistToFace = -1f;
    private int lastRealX = 0;

    private final static DecimalFormat _decimalFormater = new DecimalFormat("0.0");
    //像素密度参考值，每英寸包含160个像素点
    final int refDpi = 160;
    //2018/12/06
    //参考字体大小
    final int refFontSize = 20;


    //************************************************************************8
    private DrawerLayout drawer_layout;
    private ListView list_drawer;
    private ArrayList<Item> menuLists;
    private MyAdapter<Item> myAdapter = null;

    private ListView mLv;
    private SearchView mSearchView;
    private SearchView.SearchAutoComplete mSearchAutoComplete;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        Log.i(TAG, "onCreate(Bundle)");
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        //修改状态栏颜色
        translucentStatusBar();
        setContentView(R.layout.activity_main);

        setToolbar();
        initViews();

        //加入权限动态申请方可正常启动 否则闪退
        permissionsCtrl();

        //layoutParams用于向WindowManager描述Window的管理策略
        layoutParams = getWindow().getAttributes();
        //获取手机传感器管理器
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //查看手机传感器
        //listSensor();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");

        //SENSOR_DELAY_FASTEST 最灵敏
        //SENSOR_DELAY_GAME 游戏的时候用这个
        //SENSOR_DELAY_NORMAL 比较慢
        //SENSOR_DELAY_UI 最慢的

        // 注册传感器监听器
        // 第一个参数是Listener，第二个参数是所得传感器类型，第三个参数值获取传感器信息的频率
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        // 注册事件监听器
        MessageHUB.get().registerListener(this);

        // 打开摄像头，0-后置主摄像头，1-前置主摄像头
        _cam = Camera.open(1);
        // 获取相机设置参数类
        Camera.Parameters param = _cam.getParameters();

        /*
        Find the best suitable camera picture size for your device. Competent
        research has shown that a smaller size gets better results up to a
        certain point.
        http://ieeexplore.ieee.org/xpl/login.jsp?tp=&arnumber=6825217&url=http%3A%2F%2Fieeexplore.ieee.org%2Fiel7%2F6816619%2F6825201%2F06825217.pdf%3Farnumber%3D6825217
         */
        /*List<Size> pSize = param.getSupportedPictureSizes();
        double deviceRatio = (double) this.getResources().getDisplayMetrics().widthPixels
                / (double) this.getResources().getDisplayMetrics().heightPixels;

        Size bestSize = pSize.get(0);
        double bestRation = (double) bestSize.width / (double) bestSize.height;

        for (Size size : pSize) {
            double sizeRatio = (double) size.width / (double) size.height;
            if (Math.abs(deviceRatio - bestRation) > Math.abs(deviceRatio
                    - sizeRatio)) {
                bestSize = size;
                bestRation = sizeRatio;
            }
        }
        _cameraHeight = bestSize.height;
        _cameraWidth = bestSize.width;

        Log.d("PInfo", _cameraWidth + " x " + _cameraHeight);

        param.setPreviewSize(_cameraWidth, _cameraHeight);*/

        //设置相机参数
        _cam.setParameters(param);
        _mySurfaceView.setCamera(_cam);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
        MessageHUB.get().unregisterListener(this);
        resetCam();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop()");
        sensorManager.unregisterListener(this, sensor);
    }

    @Override
    public void onMessage(final int messageID, final Object message) {
        Log.i(TAG, "onMessage(int,Object)");
        switch (messageID) {
            case MessageHUB.MEASUREMENT_STEP:
                /*count++;
                if(count>=15) {
                    updateUI((MeasurementStepMessage) message);
                    count=0;
                }*/
                updateUI((MeasurementStepMessage) message);
                break;

            case MessageHUB.DONE_CALIBRATION:
                _calibrateButton.setBackgroundResource(R.drawable.green_button);
                //part1.setVisibility(View.INVISIBLE);
                part0.setVisibility(View.VISIBLE);
                part1.setVisibility(View.GONE);
                part2.setVisibility(View.VISIBLE);
                //part2.setVisibility(View.INVISIBLE);
                break;

            default:
                break;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.i(TAG, "onSensorChanged(SensorEvent)");
        lb = event.values[0];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.i(TAG, "onAccuracyChanged(Sensor,int)");
    }

    /**
     * 设置布局等界面参数
     */
    private void mSetLayouts(){
        part0 = (HorizontalScrollView) findViewById(R.id.part0);
        part1 = (LinearLayout) findViewById(R.id.part1);
        part2 = (LinearLayout) findViewById(R.id.part2);

        _mySurfaceView = (CameraSurfaceView) findViewById(R.id.surface_camera);
        //设置相机预览窗口宽高
        RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(
                (int) (1.00 * this.getResources().getDisplayMetrics().widthPixels),
                (int) (0.8 * this.getResources().getDisplayMetrics().heightPixels));

        // 设置相机预览页面和外部控件的距离
        layout.setMargins(0, 0, 0, (int) (0.01 * this.getResources()
                .getDisplayMetrics().heightPixels));

        _mySurfaceView.setLayoutParams(layout);
        _currentDistanceView = (TextView) findViewById(R.id.currentDistance);
        _calibrateButton = (Button) findViewById(R.id.calibrateButton);
    }

    /**
     * 2018/12/06
     */
    private void mFindViewsById(){
        View.OnClickListener listener = new MyOnclickListener();
        //sizeView = (TextView) findViewById(R.id.size_view);
        backgroundBrightness = (TextView) findViewById(R.id.background_brightness);
        brightnessView = (TextView) findViewById(R.id.brightness_view);

        etEyeDistance = (EditText) findViewById(R.id.et_eye_distance);
        tvMoveDistance = (TextView) findViewById(R.id.tv_move_distance);
        tvEyeDistance = (TextView) findViewById(R.id.tv_eye_distance);
        tvAngle = (TextView) findViewById(R.id.tv_angle);
    }

    //初始化webView网页
    private void initWebView(){
        webView = (WebView) findViewById(R.id.web_view);
        fillWebViewWithUrl(webView);
        //fillWebViewWithText(webView);//文本填充网页
    }
    //url填充webView
    private void fillWebViewWithUrl(WebView webView){
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new JsObject(MainActivity.this), "injectedObject");
        webView.loadUrl("https://www.v2ex.com/t/350509#reply106");
    }
    //文本填充webView
    private void fillWebViewWithText(WebView webView){
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<title>Welcome !</title>");
        sb.append("</head>");
        sb.append("<body>");
        sb.append("<p style=\"font-family:Calibri\">");
        sb.append("The Bible (from Koine Greek τὰ βιβλία, tà biblía, \"the books\"[1]) is a collection of sacred texts or scriptures that Jews and Christians consider to be a product of divine inspiration and a record of the relationship between God and humans.\n" +
                "Many different authors contributed to the Bible. And what is regarded as canonical text differs depending on traditions and groups; a number of Bible canons have evolved, with overlapping and diverging contents.[2] The Christian Old Testament overlaps with the Hebrew Bible and the Greek Septuagint; the Hebrew Bible is known in Judaism as the Tanakh. The New Testament is a collection of writings by early Christians, believed to be mostly Jewish disciples of Christ, written in first-century Koine Greek. These early Christian Greek writings consist of narratives, letters, and apocalyptic writings. Among Christian denominations there is some disagreement about the contents of the canon, primarily the Apocrypha, a list of works that are regarded with varying levels of respect.\n" +
                "Attitudes towards the Bible also differ amongst Christian groups. Roman Catholics, Anglicans and Eastern Orthodox Christians stress the harmony and importance of the Bible and sacred tradition, while Protestant churches focus on the idea of sola scriptura, or scripture alone. This concept arose during the Protestant Reformation, and many denominations today support the use of the Bible as the only source of Christian teaching.\n" +
                "With estimated total sales of over 5 billion copies, the Bible is widely considered to be the best-selling book of all time.[3][4] It has estimated annual sales of 100 million copies,[5][6] and has been a major influence on literature and history, especially in the West where the Gutenberg Bible was the first mass-printed book. The Bible was the first book ever printed using movable type.");
        sb.append("</p>");
        sb.append("</body>");
        sb.append("</html>");
        webView.loadData(sb.toString(), "text/html", "utf-8");
    }

    //初始化布局和组件
    private void initViews() {
        //设置滑动菜单
        mSetDrawerLayout();

        //part1初始化和获取控件
        mSetLayouts();

        //part2初始化和获取控件
        mFindViewsById();

        //初始化webView网页
        initWebView();
    }

    /**
     * @param brightness 亮度
     */
    private void setBrightness(float brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = brightness;
        getWindow().setAttributes(lp);
    }

    /**
     * Sets the current eye distance to the calibration point.
     *
     * @param v
     */
    public void pressedCalibrate(final View v) {
        LogUtil.d(TAG, "pressedCalibrate is Clicked.");
        if (!_mySurfaceView.isCalibrated()) {
            _calibrateButton.setBackgroundResource(R.drawable.yellow_button);
            _mySurfaceView.calibrate();
        }
    }

    public void pressedReset(final View v) {
        LogUtil.d(TAG, "pressedReset: is Clicked.");
        if (_mySurfaceView.isCalibrated()) {
            _calibrateButton.setBackgroundResource(R.drawable.red_button);
            _mySurfaceView.reset();
        }
    }

    //开启中眼点
    public void onShowMiddlePoint(final View view) {
        // Is the toggle on?
        boolean on = ((Switch) view).isChecked();
        LogUtil.d(TAG,"onShowMiddlePoint is Clicked. on = "+ on);
        _mySurfaceView.showMiddleEye(on);
    }

    //开启眼球点
    public void onShowEyePoints(final View view) {
        // Is the toggle on? 检查开关按钮是否开启
        boolean on = ((Switch) view).isChecked();
        LogUtil.d(TAG,"onShowEyePoints is Clicked. on = " + on);
        _mySurfaceView.showEyePoints(on);
    }

    /**
     * 2018/12/06
     * 获取理想的字体大小
     * @param message 传入message参数 获取人脸到屏幕的距离
     * @return 理想字体的大小
     */
    public float getIdealTextSize(final MeasurementStepMessage message){
        LogUtil.ObjectValue("getIdealTextSize", message);

        //对应公式4-12
        float fontRatio = message.getDistToFace() / 29.7f;
        float idealTextSize = fontRatio * refFontSize;
        return idealTextSize;
    }

    /**
     * 2018/12/06
     * 获得当前设备的一个DisplayMetrics对象
     * @return DisplayMetrics 对象
     */
    public DisplayMetrics getDisplayMetrics(){
        LogUtil.d(TAG,"getDisplayMetrics()");
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        return metrics;
    }

    /**
     * 2018/12/06
     * get the screen size
     * 获取屏幕对角线尺寸 单位 inch
     * @return 屏幕对角线长度 单位 inch
     */
    public double getScreenSizeInch(){
        LogUtil.d(TAG,"getScreenSizeInch()");
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);

        //dm: density(显示屏的逻辑密度)
        //参考dpi = 160  dpi(dot per inch):屏幕像素密度，每英寸多少像素
        //final int refDpi = 160;
        double dm = metrics.density * refDpi;
        //接下来的代码是获得屏幕尺寸后计算屏幕的实际大小
        //利用勾股定理求得屏幕的对角线长度 单位Inch
        //metrics.widthPixels：宽度上像素点个数
        //y = metrics.widthPixels/dm = 屏幕的物理宽度 下面的y同理
        double x = Math.pow(metrics.widthPixels / dm, 2);
        double y = Math.pow(metrics.heightPixels / dm, 2);
        final double screenInch = Math.sqrt(x + y);

        return screenInch;
    }

    /**
     * 2018/12/07
     * 获取最佳亮度值 LP
     * 论文公式 4-3
     * @param message MeasurementStepMessage
     * @return
     */
    public double getOptimalBrightness(MeasurementStepMessage message){
        LogUtil.ObjectValue(TAG+".getOptimalBrightness()",message);

        DisplayMetrics metrics = getDisplayMetrics();
        final double screenSizeInch = getScreenSizeInch();
        double heightPixel = metrics.heightPixels;
        double widthPixel = metrics.widthPixels;

        //论文公式4-3   vD0：对应论文中的MaxD
        //疑问：widthPixel?? 与论文不对应
        final double vD0 = screenSizeInch / (Math.sqrt(Math.pow((1.0 * widthPixel / heightPixel), 2) + 1) * widthPixel * Math.tan(1 / 60.0 * Math.PI / 180));
        //获取的人脸到设备的距离
        double vDp = message.getDistToFace();

        //l0 目标亮度值
        //l0 ??? 为什么等于lb+30
        double l0 = lb + 30;
        //计算最佳亮度 公式4-7
        double lP = lb + Math.pow((vDp / vD0), 2) * (l0 - lb);

        return lP;
    }

    public int getOptimalFontSize(MeasurementStepMessage message){
        LogUtil.ObjectValue(TAG+".getOptimalFontSize",message);

        DisplayMetrics metrics = getDisplayMetrics();
        int fontSize = (int) (5.5 * message.getDistToFace() * metrics.densityDpi / (6000 * 2.54 * 0.45));
        return fontSize;
    }

    /**
     * Update the UI params.
     * 此方法需要被重构
     * 重构思路
     * 先拆解成多个方法 把以下任务分开
     * <p>
     * 1 计算屏幕的实际大小可以作为初始化的内容一开始就计算出来 以后直接使用
     * 2 计算maxD的方法
     * 3 计算目标亮度的方法
     *
     * @param message
     */
    public void updateUI(final MeasurementStepMessage message) {
        LogUtil.ObjectValue(TAG+".updateUI", message);

        _currentDistanceView.setText("当前距离:" + _decimalFormater.format(message.getDistToFace()) + " cm");
        _currentDistanceView.setTextColor(Color.rgb(0, 0, 0));
        //设置理想字体大小
        _currentDistanceView.setTextSize(getIdealTextSize(message));
        /*
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        //接下来的代码是获得屏幕尺寸后计算屏幕的实际大小

        //dm: density(显示屏的逻辑密度)
        //参考dpi = 160
        final int refDpi = 160;
        double dm = metrics.density * refDpi;

        //利用勾股定理求得屏幕的对角线长度 单位Inch
        //y = metrics.widthPixels/dm = 屏幕的物理宽度 下面的y同理
        double x = Math.pow(metrics.widthPixels / dm, 2);
        double y = Math.pow(metrics.heightPixels / dm, 2);*/
        /*
        DisplayMetrics metrics = getDisplayMetrics();
        final double screenSizeInch = getScreenSizeInch();
        double heightPixel = metrics.heightPixels;
        double widthPixel = metrics.widthPixels;

        //论文公式4-3   vD0：对应论文中的MaxD
        //疑问：widthPixel?? 与论文不对应
        final double vD0 = screenSizeInch / (Math.sqrt(Math.pow((1.0 * widthPixel / heightPixel), 2) + 1) * widthPixel * Math.tan(1 / 60.0 * Math.PI / 180));
        //获取的人脸到设备的距离
        double vDp = message.getDistToFace();

        //l0 目标亮度值
        //l0 ??? 为什么等于lb+30
        double l0 = lb + 30;
        //计算最佳亮度 公式4-7
        */

        //屏幕亮度调整到最佳亮度
        double lP = getOptimalBrightness(message);
        layoutParams.screenBrightness = (float) lP / 255 >= 1 ? 1 : (float) lP / 255;
        getWindow().setAttributes(layoutParams);

        //更改网页显示效果
        /**
         * 调整字体大小的代码部分
         * @param webView 判断webView是否为空
         */
        if (webView != null) {
            //根据距离经过计算设置字体大小
            //算法5-3 44页??
            //公式不一致
            int fontSize = getOptimalFontSize(message);
            if (fontSize > 0) {
                //改变网页内容 字体大小和前后景反差颜色
                changeFontSizeAndContrast(fontSize);
                //ui提示字体改变
                Toast.makeText(MainActivity.this, "fontsize: " + fontSize, Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d(TAG, "webView is null");
        }

        /**计算角度
         *
         */
        double angle = 0;
        if (lastDistToFace > 0) {
            String string;
            if (TextUtils.isEmpty(string = etEyeDistance.getText().toString().trim()) == false) {

                float eyeDistance = Float.valueOf(string);
                // 公式4-15?
                angle = Math.atan(eyeDistance * Math.abs(message.getRealX() - lastRealX) / (message.getHalfEyeDist() * 2) / message.getDistToFace());

                Log.d(TAG, "message.getRealX()= " + message.getRealX());
                Log.d(TAG, "lastRealX= " + lastRealX);
                Log.d(TAG, "eye distance(p)= " + message.getHalfEyeDist() * 2);
                Log.d(TAG, "distance to face(cm)= " + message.getDistToFace());
                tvMoveDistance.setText("move dist(p): " + Math.abs(message.getRealX() - lastRealX));
                tvEyeDistance.setText("eye dist(p): " + message.getHalfEyeDist() * 2);
            }
        }
        lastDistToFace = message.getDistToFace();
        lastRealX = message.getRealX();

        Log.i(TAG, "angle= " + angle);
        tvAngle.setText("angle: " + String.valueOf(angle));
    }

    private void resetCam() {
        LogUtil.d(TAG, "resetCam()");
        _mySurfaceView.reset();
        _cam.stopPreview();
        _cam.setPreviewCallback(null);
        _cam.release();
    }

    /**
     * 改变网页的背景和前景。bg和fg代表背景和前景色，可以是英文颜色名，也可以是rgb值（#RRGGBB）
     * 未被使用
     *
     * @param bg background
     * @param fg foreground
     */
    private void changeFgAndBg(String bg, String fg) {
        Log.d(TAG, "changeFgAndBg(): bg=" + bg +",fg="+fg);

        webView.getSettings().setJavaScriptEnabled(true);
        String js = "javascript:" +
                "var sheet = document.getElementsByTagName('style');"+
                "if(sheet.length==0) sheet =document.createElement('style');"+
                "else sheet = document.getElementsByTagName('style')[0];"+
                "sheet.innerHTML='* { color : " + fg + " !important;background: " + bg + "!important}';"+
                "document.body.appendChild(sheet);";
        webView.loadUrl(js);
    }

    /**
     * 改变网页的字体大小
     * 未被使用
     *
     * @param fontsize
     */
    private void changeFontSize(int fontsize) {
        Log.d(TAG, "changeFontSize(): fontsize= " + fontsize);

        webView.getSettings().setJavaScriptEnabled(true);
        String js = "javascript:(function(){ var css = '* { font-size : " + fontsize + "px !important ; }';var style = document.getElementsByTagName('style');if(style.length==0){style = document.createElement('style');}else{style = document.getElementsByTagName('style')[0];}        if (style.styleSheet){ style.style.styleSheet.cssText=css;}else{style.appendChild(document.createTextNode(css));} document.getElementsByTagName('head')[0].appendChild(style);})()";
        //String js ="javascript:"+"var sheet = document.getElementsByTagName('style');if(sheet.length==0) sheet =document.createElement('style');else sheet = document.getElementsByTagName('style')[0];sheet.innerHTML='* { font-size : "+fontsize+"px !important;}；document.body.appendChild(sheet)';document.body.appendChild(sheet);";
        webView.loadUrl(js);
    }

    /*
    这个方法使用js代码改变了字体和背景
    依据传入的字体大小改变网页前景和背景颜色
     */
    private void changeFontSizeAndContrast(int fontsize) {
        LogUtil.d(TAG, "changeFontSizeAndContrast(): fontsize= " + fontsize);

        //公式4-19 由字体换算出对比度阈值
        String js = "javascript:(function(){  var contrast = -0.0425*" + fontsize + "+0.85; " +
                "    var body = document.getElementsByTagName('body')[0]; " +
                "var bgL =0;" +
                "    var eps = 1; " +
                "    var fgr,fgg,fgb,result,fgl; " +
                "    console.log('bgL'+bgL); " +
                "    for(var b = 0;b<=255;b++){ " +
                "        for (var r =0;r<=255;r++){ " +
                "            for(var  g= 0; g<=255;g++){ " +
                //公式4-17 根据RGB值计算相对色彩亮度
                "                fgL =0.2126*r+0.7152*g+0.0722*b; " +
                "                result =Math.abs(contrast-Math.abs(fgL/255)); " +
                //获得最小的eps
                "                if(result<eps){ " +
                "                    eps = result; " +
                "                    fgr = r; " +
                "                    fgg = g; " +
                "                    fgb = b; " +
                "                } " +
                "            } " +
                "        } " +
                "    };" +

                "var sheet = document.getElementsByTagName('style'); " +
                "    if(sheet.length==0)  " +
                "    sheet =document.createElement('style'); " +
                "    else  " +
                "    sheet = document.getElementsByTagName('style')[0]; " +
                "    sheet.innerHTML=' * { color : rgb('+fgr+','+fgg+','+fgb+') !important; background: black !important;font-size: " + fontsize + "px !important}';  " +
                "    document.body.appendChild(sheet);alert('finish');}" +
                " )()";
        webView.loadUrl(js);
    }

    //按钮点击监听类: 按钮响应---暂未监听按钮
    private class MyOnclickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case 1:
                default:
            }
        }
    }

    private class MySensorEventListener implements SensorEventListener {

        //当传感器的值发生变化时，调用OnSensorChanged().
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float[] values = sensorEvent.values;
            switch (sensorEvent.sensor.getType()) {
                //动态更新光线信息
                case Sensor.TYPE_LIGHT:
                    brightnessValue = values[0];
                    break;
            }
        }

        //当传感器的精度发生变化时会调用OnAccuracyChanged()方法
        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }

    }

//**************************************************************************************************************
    //设置toolbar
    private void setToolbar(){
        //使用ToolBar控件替代ActionBar控件
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSearchAutoComplete.isShown()) {
                    try {
                        mSearchAutoComplete.setText("");//清除文本
                        //利用反射调用收起SearchView的onCloseClicked()方法
                        Method method = mSearchView.getClass().getDeclaredMethod("onCloseClicked");
                        method.setAccessible(true);
                        method.invoke(mSearchView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    finish();
                }
            }
        });
    }

    //加载toolbar.xml菜单文件
    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);

        //通过MenuItem得到SearchView
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchAutoComplete = (SearchView.SearchAutoComplete) mSearchView.findViewById(R.id.search_src_text);
        //mSearchView.setMaxWidth(500);//设置最大宽度
        //设置自定义提交按钮
        mSearchView.setSubmitButtonEnabled(true);
        ImageView iv_submit = (ImageView) mSearchView.findViewById(R.id.search_go_btn);
        iv_submit.setImageResource(R.mipmap.submit_search);
        //设置输入框提示语
        mSearchView.setQueryHint("请输入搜索内容...");
        //设置触发查询的最少字符数（默认2个字符才会触发查询）
        mSearchAutoComplete.setThreshold(0);

        //设置输入框提示文字样式
        mSearchAutoComplete.setHintTextColor(getResources().getColor(android.R.color.darker_gray));
        mSearchAutoComplete.setTextColor(getResources().getColor(android.R.color.background_light));
        mSearchAutoComplete.setTextSize(14);

        //设置搜索框有字时显示叉叉，无字时隐藏叉叉
        mSearchView.onActionViewExpanded();
        mSearchView.setIconified(true);

        //监听SearchView的内容
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //输入完成后，点击回车或是完成键
            @Override
            public boolean onQueryTextSubmit(String s) {
                LogUtil.d(TAG,"searchInput:" + s);
                String urlString = s.trim();
                LogUtil.d(TAG, "onClick: webView = null?"+ (webView == null));
                if (webView != null) {
                    if (TextUtils.isEmpty(urlString) == false && urlString.startsWith("www.")) {
                        LogUtil.d(TAG,"It is legal urlString");
                        webView.loadUrl("http://" + urlString);
                    } else {
                        fillWebViewWithText(webView);
                    }
                } else {
                    Log.e(TAG, "Go ot error");
                }
                return true;
            }

            //查询文本框有变化时事件
            @Override
            public boolean onQueryTextChange(String s) {
                LogUtil.d(TAG,"searchChange:" + s);
                //Cursor cursor = TextUtils.isEmpty(s) ? null : queryData(s);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);

    }

    //实现5.0以上状态栏透明(默认状态是半透明)
    private void translucentStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
            //int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            //decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(ContextCompat.
                    getColor(getApplicationContext(), R.color.colorPrimary)
            );
        }
    }

    //权限控制
    private void permissionsCtrl(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "onCreate: 权限控制");
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{ Manifest.permission. CAMERA }, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    LogUtil.d(TAG, "onRequestPermissionsResult: 权限不足");
                    ActivityCollector.finishAll();
                }
                break;
            default:
        }
    }

    // 查看手机传感器
    private void listSensor(){
        List<Sensor> listSensor = sensorManager.getSensorList(Sensor.TYPE_ALL);
        int i = 1;
        for (Sensor sensor : listSensor) {
            Log.d("sensor " + i, sensor.getName());
            i++;
        }
    }

    //处理toolbar上各个按钮的点击事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                drawer_layout.openDrawer(GravityCompat.END);
                break;
            default:
        }
        return true;
    }

    private void mSetDrawerLayout(){
        drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        list_drawer = (ListView) findViewById(R.id.list_drawer);
        menuLists = new ArrayList<Item>();
        menuLists.add(new Item(R.mipmap.iv_menu_fontsize,"字体大小"));
        menuLists.add(new Item(R.mipmap.iv_menu_size,"尺寸因子"));
        menuLists.add(new Item(R.mipmap.iv_menu_brightness,"亮        度"));
        menuLists.add(new Item(R.mipmap.iv_menu_brightness_factor,"亮度因子"));
        myAdapter = new MyAdapter<Item>(menuLists,R.layout.item_list) {
            @Override
            public void bindView(ViewHolder holder, Item obj) {
                holder.setImageResource(R.id.img_icon,obj.getIconId());
                holder.setText(R.id.txt_content, obj.getIconName());
            }
        };
        list_drawer.setAdapter(myAdapter);
        list_drawer.setOnItemClickListener(this);
    }

    // 滑动菜单按钮点击事件
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //去吃字符串中间空格
        final String editItem = menuLists.get(position).getIconName().trim().replaceAll(" ","");
        LogUtil.d(TAG,"Click " +  editItem);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        final AlertDialog dialog = builder.create();
        //设置对话框标题：修改项
        dialog.setTitle(editItem);
        final View dialogView = View.inflate(MainActivity.this, R.layout.dialog_settings, null);
        final EditText etInput = (EditText) dialogView.findViewById(R.id.etInput);
        SpannableString s = new SpannableString("请输入" + editItem);//这里输入自己想要的提示文字
        etInput.setHint(s);
        //设置对话框布局
        dialog.setView(dialogView);
        dialog.show();
        Button btnOk = (Button) dialogView.findViewById(R.id.ok1);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取输入框输入的值
                EditText etInput =  (EditText) dialogView.findViewById(R.id.etInput);
                final String inputValue = etInput.getText().toString().trim();
                if (TextUtils.isEmpty(inputValue) ) {
                    Toast.makeText(MainActivity.this, editItem + "不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                LogUtil.d(TAG,"Click "+ editItem + "value =" + inputValue);
                if(editItem.equals("字体大小")){
                    LogUtil.d(TAG,"Click "+ editItem + "value =" + inputValue);
                    if (inputValue != null && webView != null) {
                        //设置WebView中加载页面字体变焦百分比，默认100，整型数。
                        WebSettings settings = webView.getSettings();
                        settings.setTextZoom(Integer.parseInt(inputValue));
                    } else {
                        LogUtil.e(TAG, "zoom error");
                    }
                }
                else if(editItem.equals("尺寸因子")){
                    if (inputValue != null) {
                        LogUtil.d(TAG,"Click "+ editItem + " value =" + inputValue);
                        fontSizeFactor = Integer.parseInt(inputValue);
                    }
                }
                else if(editItem.equals("亮        度")){
                    LogUtil.d(TAG,"Click "+ editItem + "value =" + inputValue);
                    setBrightness(Float.parseFloat(inputValue));
                }else{
                    LogUtil.d(TAG,"Click "+ editItem + "value " + inputValue);
                    bFactor = Integer.parseInt(inputValue);
                }
                dialog.dismiss();
            }
        });
        //关闭滑动菜单
        drawer_layout.closeDrawer(list_drawer);
    }
}