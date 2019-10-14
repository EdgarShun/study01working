package com.example.rn;

import androidx.annotation.Nullable;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;

public class MainReactActivity extends ReactActivity {

    @Nullable
    @Override
    protected String getMainComponentName() {
        return "study01";
    }

    @Override
    protected ReactActivityDelegate createReactActivityDelegate() {
        return new ReactActivityDelegate(this,getMainComponentName());
    }
}
