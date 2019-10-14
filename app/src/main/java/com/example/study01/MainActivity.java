package com.example.study01;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.util.ResourceUtil;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //唤醒的阈值，就相当于门限值，当用户输入的语音的置信度大于这一个值的时候，才被认定为成功唤醒。
    private int curThresh = 1450;

    //是否持续唤醒
    private String keep_alive = "1";

    private String ivwNetMode = "0";
    // 语音唤醒对象
    private VoiceWakeuper mIvw;
    //存储唤醒词的ID
    private String wordID = "";
    // 唤醒结果内容
    private String resultString;

    private EditText mResultText;

    private EditText mAIUIText;

    private static final int UPDATE_TEXT = 101;
    private static final int UPDATE_AIUI = 102;


    TtsTest tt = new TtsTest(MainActivity.this);
    IatTest it = new IatTest(MainActivity.this);
    AIUITest at = new AIUITest(MainActivity.this);

    private Intent intent;

    private String string = "";

    private String str = "";

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TEXT:

                    mResultText.setText((String) msg.obj);
                    break;
                case UPDATE_AIUI:
                    mAIUIText.setText((String) msg.obj);
                    if (null != tt) {
                        tt.stop();
                    }
                    tt = new TtsTest(MainActivity.this);
                    tt.TtsMethod(str01);
                    switch (mAIUIText.getText().toString().trim()) {
                        case "微信":
                            intent = getPackageManager().getLaunchIntentForPackage("com.tencent.mm");
                            startActivity(intent);
                            break;
                        case "qq":
                            intent = getPackageManager().getLaunchIntentForPackage("com.tencent.mobileqq");
                            startActivity(intent);
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }

    };
    private String str01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化界面
        initLayout();

        requestPermissions();

        mResultText = (EditText) findViewById(R.id.iat_text);

        mAIUIText = (EditText) findViewById(R.id.aiui_text);

        // 初始化唤醒对象
        mIvw = VoiceWakeuper.createWakeuper(MainActivity.this, null);

        //非空判断，防止因空指针使程序崩溃
        mIvw = VoiceWakeuper.getWakeuper();
        if (mIvw != null) {
            resultString = "";
//            tv.setText(resultString);
            // 清空参数
            mIvw.setParameter(SpeechConstant.PARAMS, null);
            // 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
            mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:" + curThresh);
            // 设置唤醒模式
            mIvw.setParameter(SpeechConstant.IVW_SST, "wakeup");
            // 设置持续进行唤醒
            mIvw.setParameter(SpeechConstant.KEEP_ALIVE, keep_alive);
            // 设置闭环优化网络模式
            mIvw.setParameter(SpeechConstant.IVW_NET_MODE, ivwNetMode);
            // 设置唤醒资源路径
            mIvw.setParameter(SpeechConstant.IVW_RES_PATH, getResource());
            // 设置唤醒录音保存路径，保存最近一分钟的音频
            mIvw.setParameter(SpeechConstant.IVW_AUDIO_PATH, Environment.getExternalStorageDirectory().getPath() + "/msc/ivw.wav");
            mIvw.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
            // 如有需要，设置 NOTIFY_RECORD_DATA 以实时通过 onEvent 返回录音音频流字节
            //mIvw.setParameter( SpeechConstant.NOTIFY_RECORD_DATA, "1" );

            // 启动唤醒
            mIvw.startListening(new MyWakeuperListener());
        } else {
            showTip("唤醒未初始化");
        }
        /**
         *  长按事件
         */
        findViewById(R.id.iat_recognize).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                showTip("请长按按钮说话...");
                if( null != at){
                    at.destory();
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        str = it.writing();
                        Message message = new Message();
                        message.what = UPDATE_TEXT;
                        message.obj = str;
                        handler.sendMessage(message);

                        String str01 = at.recording(str);
                        Message message1 = new Message();
                        message1.what = UPDATE_AIUI;
                        message1.obj = str01;
                        handler.sendMessage(message1);

                        if (null != tt) {
                            tt.stop();
                        }
                        tt = new TtsTest(MainActivity.this);
                        tt.TtsMethod(str01);
                    }
                }).start();
                return true;
            }
        });

    }

    /**
     * 初始化界面
     */
    private void initLayout() {

        findViewById(R.id.iat_recognize).setOnClickListener(this);
        findViewById(R.id.iat_stop).setOnClickListener(this);
        findViewById(R.id.iat_understand).setOnClickListener(this);

    }


    /**
     * @param str
     */
    public void showTip(String str) {
        Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
    }


    /**
     * 获取唤醒词功能
     *
     * @return 返回文件位置
     */
    private String getResource() {
        final String resPath = ResourceUtil.generateResourcePath(MainActivity.this, ResourceUtil.RESOURCE_TYPE.assets, "ivw/" + getString(R.string.IflytekAPP_id) + ".jet");
        return resPath;
    }


    /**
     *  单击按钮事件
     *
     * @param view
     */
    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.iat_recognize:
                showTip("请开始说话...");

                if (null != tt) {
                    tt.stop();
                }

                //定时任务，随时刷新后台更新的数据
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        String str01 = at.recording();
                        Message message1 = new Message();
                        message1.what = UPDATE_AIUI;
                        message1.obj = str01;
                        handler.sendMessage(message1);
                    }
                };

                Timer timer = new Timer();
                timer.schedule(task, 500, 500);

                break;

            case R.id.iat_stop: {
                showTip("停止");
                if (null != tt) {
                    tt.stop();
                }
                if(null != at){
                    at.destory();
                }
            }
            break;

            case R.id.iat_understand: {
                string = mResultText.getText().toString();
                if (TextUtils.isEmpty(string)) {
                    showTip("请先说话...");
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        str01 = at.recording(string);
                        Message message1 = new Message();
                        message1.what = UPDATE_AIUI;
                        message1.obj = str01;
                        handler.sendMessage(message1);

                    }
                }).start();

            }
            break;

            default:
                break;
        }

    }

    /**
     * 唤醒词监听类
     *
     * @author Administrator
     */
    private class MyWakeuperListener implements WakeuperListener {
        //开始说话
        @Override
        public void onBeginOfSpeech() {

        }

        //错误码返回
        @Override
        public void onError(SpeechError arg0) {

        }

        @Override
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {

        }

        @Override
        public void onVolumeChanged(int i) {

        }

        @Override
        public void onResult(WakeuperResult result) {

            if (!"1".equalsIgnoreCase(keep_alive)) {
//                setRadioEnable(true);
            }

            tt.TtsMethod(getString(R.string.huihua));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {

                    String str = it.writing();
                    Message message = new Message();
                    message.what = UPDATE_TEXT;
                    message.obj = str;
                    handler.sendMessage(message);

                    String str01 = at.recording(str);
                    Message message1 = new Message();
                    message1.what = UPDATE_AIUI;
                    message1.obj = str01;
                    handler.sendMessage(message1);

                    if (null != tt) {
                        tt.stop();
                    }
                    tt = new TtsTest(MainActivity.this);
                    tt.TtsMethod(str01);
                }
            }).start();
        }
    }

    private void requestPermissions() {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                int permission = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.LOCATION_HARDWARE, Manifest.permission.READ_PHONE_STATE,
                                    Manifest.permission.WRITE_SETTINGS, Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS}, 0x0010);
                }

                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION}, 0x0010);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
