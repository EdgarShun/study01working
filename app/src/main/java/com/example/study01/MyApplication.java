package com.example.study01;

import android.app.Application;

import com.example.rn.CustomFunctionPackage;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

import java.util.Arrays;
import java.util.List;

public class MyApplication extends Application implements ReactApplication {


    public static AIUITest am;

    @Override
    public void onCreate() {
        initializeIflytek();

        am = new AIUITest(MyApplication.this);
        super.onCreate();

        SoLoader.init(this, /* native exopackage */ false);

    }

    private void initializeIflytek()
    {
        StringBuffer param = new StringBuffer();
        //IflytekAPP_id为我们申请的Appid
        param.append("appid="+getString(R.string.IflytekAPP_id));
        param.append(",");
        // 设置使用v5+
        param.append(SpeechConstant.ENGINE_MODE+"="+ SpeechConstant.MODE_MSC);
        SpeechUtility.createUtility(MyApplication.this, param.toString());
    }



    private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {

        @Override
        public boolean getUseDeveloperSupport() {
            return BuildConfig.DEBUG;
        }

        @Override
        protected List<ReactPackage> getPackages() {
            return Arrays.<ReactPackage>asList(
                    new MainReactPackage(),
                    new CustomFunctionPackage()
            );
        }

        @Override
        protected String getJSMainModuleName() {
            return "index";
        }
    };


    @Override
    public ReactNativeHost getReactNativeHost() {
        return mReactNativeHost;
    }
}
