package com.example.rn;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.example.study01.AIUITest;
import com.example.study01.IatTest;
import com.example.study01.MainActivity;
import com.example.study01.TtsTest;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import javax.annotation.Nonnull;

public class FunctionModule extends ReactContextBaseJavaModule {

    private ReactContext mContext;


    public FunctionModule(@Nonnull ReactApplicationContext reactContext) {
        super(reactContext);
        mContext = reactContext;
    }

    @Nonnull
    @Override
    public String getName() {
        return "Voice";
    }


    /**
     *  AIUI
     * @param callback
     */
    @ReactMethod
    public void getAIUI(final Callback callback){

        new Thread(new Runnable() {
            @Override
            public void run() {
                callback.invoke(new AIUITest(mContext).recording());
            }
        }).start();
    }

    /**
     *  语音听写
     * @param callback
     */
    @ReactMethod
    public void getIat(final Callback callback){

        new Thread(new Runnable() {
            @Override
            public void run() {
                callback.invoke(new IatTest(mContext).writing());
            }
        }).start();
    }

    /**
     * 语音合成
     * @param str
     */
    @ReactMethod
    public void getTta(String str){
        new TtsTest(mContext).TtsMethod(str);
    }

    /**
     * 返回Android原生界面
     */
    @ReactMethod
    public void getAndroid(){
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

}
