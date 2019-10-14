package com.example.study01;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rn.CustomFunctionPackage;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactRootView;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.shell.MainReactPackage;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.util.ResourceUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class GetUpTest extends Activity implements DefaultHardwareBackBtnHandler {

    private ReactRootView mReactRootView;
    private ReactInstanceManager mReactInstanceManager;

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

    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_getup);

        mReactRootView = new ReactRootView(this);
        mReactInstanceManager = ReactInstanceManager.builder()
                .setApplication(getApplication())
                .setCurrentActivity(this)
                .setBundleAssetName("index.android.bundle")
                .setJSMainModulePath("index")
                .addPackage(new MainReactPackage())
                .addPackage(new CustomFunctionPackage())
                .setUseDeveloperSupport(BuildConfig.DEBUG)
                .setInitialLifecycleState(LifecycleState.RESUMED)
                .build();
        // 注意这里的MyReactNativeApp必须对应“index.js”中的
        // “AppRegistry.registerComponent()”的第一个参数
        mReactRootView.startReactApplication(mReactInstanceManager, "study01", null);

        setContentView(mReactRootView);
        tv=(TextView)findViewById(R.id.tv);

        // 初始化唤醒对象
        mIvw = VoiceWakeuper.createWakeuper(GetUpTest.this, null);
        //非空判断，防止因空指针使程序崩溃
        mIvw = VoiceWakeuper.getWakeuper();
        if(mIvw != null) {
            resultString="";

            // 清空参数
            mIvw.setParameter(SpeechConstant.PARAMS, null);
            // 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
            mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:"+ curThresh);
            // 设置唤醒模式
            mIvw.setParameter(SpeechConstant.IVW_SST, "wakeup");
            // 设置持续进行唤醒
            mIvw.setParameter(SpeechConstant.KEEP_ALIVE, keep_alive);
            // 设置闭环优化网络模式
            mIvw.setParameter(SpeechConstant.IVW_NET_MODE, ivwNetMode);
            // 设置唤醒资源路径
            mIvw.setParameter(SpeechConstant.IVW_RES_PATH, getResource());
            // 设置唤醒录音保存路径，保存最近一分钟的音频
            mIvw.setParameter( SpeechConstant.IVW_AUDIO_PATH, Environment.getExternalStorageDirectory().getPath()+"/msc/ivw.wav" );
            mIvw.setParameter( SpeechConstant.AUDIO_FORMAT, "wav" );
            // 如有需要，设置 NOTIFY_RECORD_DATA 以实时通过 onEvent 返回录音音频流字节
            //mIvw.setParameter( SpeechConstant.NOTIFY_RECORD_DATA, "1" );
            // 启动唤醒
            mIvw.startListening(new MyWakeuperListener());
        }else{
            showTip("唤醒未初始化");
        }
    }

    public void showTip(String str){
        Toast.makeText(GetUpTest.this,str,Toast.LENGTH_SHORT).show();
    }

    /**
     * 获取唤醒词功能
     * @return 返回文件位置
     */
    private String getResource() {
        final String resPath = ResourceUtil.generateResourcePath(GetUpTest.this,ResourceUtil.RESOURCE_TYPE.assets, "ivw/"+getString(R.string.IflytekAPP_id)+".jet");
        return resPath;
    }

    @Override
    public void invokeDefaultOnBackPressed() {
        super.onBackPressed();
    }


    /**
     * 唤醒词监听类
     * @author Administrator
     *
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
                //setRadioEnable(true);
            }
            new TtsTest(GetUpTest.this).TtsMethod(getString(R.string.huihua));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mReactInstanceManager != null) {
            mReactInstanceManager.onHostPause(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mReactInstanceManager != null) {
            mReactInstanceManager.onHostResume(this, this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mReactInstanceManager != null) {
            mReactInstanceManager.onHostDestroy(this);
        }
        if (mReactRootView != null) {
            mReactRootView.unmountReactApplication();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && mReactInstanceManager != null) {
            mReactInstanceManager.showDevOptionsDialog();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

}
