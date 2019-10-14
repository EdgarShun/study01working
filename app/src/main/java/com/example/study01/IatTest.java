package com.example.study01;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;


/**
 *  语音听写
 */
public class IatTest {

    private Context context;

    StringBuffer buffer = new StringBuffer();

    // 语音听写对象
    private SpeechRecognizer mIat;

    private Toast mToast;

    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;

    private String resultType = "json";

    private SharedPreferences mSharedPreferences;

    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResult = new LinkedHashMap<String, String>();

    private final String TAG = "IatTest";

    /**
     * 构造方法
     * @param context
     */
    public IatTest(Context context) {
        this.context = context;
    }


    int ret = 0;//函数调用返回值


    public String writing() {

        // 初始化识别无UI识别对象
        // 使用SpeechRecognizer对象，可根据回调消息自定义界面；
        mIat = SpeechRecognizer.createRecognizer(context, mInitListener);

        mSharedPreferences = context.getSharedPreferences("wjs.xml", Activity.MODE_PRIVATE);

        buffer.setLength(0);

        if (null == mIat) {

            return "创建对象失败，请确认 libmsc.so 放置正确，且有调用 createUtility 进行初始化";

        } else {
            //设置参数
            setParam();
            // 不显示听写对话框
            ret = mIat.startListening(mRecognizeListener);
            if (ret != ErrorCode.SUCCESS) {
//                showTip("听写失败,错误码：" + ret+",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
            } else {
//                showTip("请开始说话…");
            }

            /**
             * 因为AIUI各项功能都是耗时的，所以每次我在主界面从后台取值因为延迟导致取不到值，这在我做RN集成的时候就已经发现
             * 所以，我就把这个类变成一个耗时的动作，用多线程的方式，改变UI取值
             *
             */
            int i = 0;
            do {
                try {
                    Thread.sleep(i);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i+=0.1;
            }while (TextUtils.isEmpty(buffer.toString()));
            return buffer.toString();
        }
    }

    /**
     * 设置参数
     */
    private void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎。TYPE_LOCAL表示本地，TYPE_CLOUD表示云端，TYPE_MIX 表示混合
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, resultType);

        String lag = mSharedPreferences.getString("iat_language_preference", "mandarin");

        if (lag.equals("en_us")) {  // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
            mIat.setParameter(SpeechConstant.ACCENT, null);
        } else {
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);
        }

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));
        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));
        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));
        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/recognize.wav");

    }


    /**
     * 初始化监听器
     */
    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Log.d(TAG,"初始化失败，错误码：" + code);
            }
        }
    };


    /**
     * 听写监听器
     */
    private RecognizerListener mRecognizeListener = new RecognizerListener() {
        @Override
        public void onVolumeChanged(int i, byte[] bytes) {

        }

        @Override
        public void onBeginOfSpeech() {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onResult(RecognizerResult results, boolean b) {

            if (resultType.equals("json")) {
                printResult(results);

            } else if (resultType.equals("plain")) {
                buffer.append(results.getResultString());
            }

        }

        @Override
        public void onError(SpeechError speechError) {

        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };

    /**
     * @param results
     */
    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResult.put(sn, text);

        for (String key : mIatResult.keySet()) {
            buffer.append(mIatResult.get(key));
        }
    }

    /**
     * 暂停录音
     */
    private void stop(){
        if (null != mIat){
            mIat.stopListening();
        }
    }

    /**
     * 取消录音
     */
    private void cancle(){
        mIat.cancel();
    }
}
