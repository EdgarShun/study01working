package com.example.study01;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.IdentityListener;
import com.iflytek.cloud.IdentityResult;
import com.iflytek.cloud.IdentityVerifier;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class FaceTest extends Activity implements View.OnClickListener {


    private final String TAG = "FaceTest";
    private final int REQUEST_PICTURE_CHOOSE = 1;
    private final int REQUEST_CAMERA_IMAGE = 2;

    private EditText online_authid;
    private Toast mToast;
    private ProgressDialog mProDialog;

    // 拍照得到的照片文件
    private File mPictureFile;

    //采用身份识别接口进行在线人脸识别
    private IdentityVerifier mIdVerifier;

    // 删除模型
    private final static int MODEL_DEL = 1;

    private Bitmap mImage = null;
    private byte[] mImageData = null;
    // authid为6-18个字符长度，用于唯一标识用户
    private String mAuthid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face);

        //初始化界面
        init();

        online_authid = (EditText) findViewById(R.id.online_authid);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        mProDialog = new ProgressDialog(this);
        mProDialog.setCancelable(true);
        mProDialog.setTitle("请稍后");

        mProDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {

                // cancel进度框时,取消正在进行的操作
                if (null != mIdVerifier) {
                    mIdVerifier.cancel();
                }
            }
        });

        mIdVerifier = IdentityVerifier.createVerifier(FaceTest.this, new InitListener() {
            @Override
            public void onInit(int errorCode) {
                if (ErrorCode.SUCCESS == errorCode) {
                    showTip("引擎初始化成功");
                } else {
                    showTip("引擎初始化失败，错误码：" + errorCode+",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
                }
            }
        });
    }



    //初始化界面
    private void init() {
        findViewById(R.id.online_pick).setOnClickListener(FaceTest.this);
        findViewById(R.id.online_reg).setOnClickListener(FaceTest.this);
        findViewById(R.id.online_verify).setOnClickListener(FaceTest.this);
        findViewById(R.id.online_camera).setOnClickListener(FaceTest.this);
        findViewById(R.id.btn_modle_delete).setOnClickListener(FaceTest.this);

    }

    @Override
    public void onClick(View view) {

        int ret = ErrorCode.SUCCESS;
        mAuthid = online_authid.getText().toString().trim();
        if (TextUtils.isEmpty(mAuthid)){
            showTip("请输入用户ID");
        }else{
            online_authid.setEnabled(false);
        }
        switch (view.getId()){
            case R.id.online_pick:
                //挑选照片
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(intent, REQUEST_PICTURE_CHOOSE);
                break;
            case R.id.online_reg:
                //注册
                if (TextUtils.isEmpty(mAuthid)){
                    showTip("authid不能为空");
                    return;
                }
                if (null!=mImageData){
                    mProDialog.setMessage("注册中...");
                    mProDialog.show();
                    // 设置用户标识，格式为6-18个字符（由字母、数字、下划线组成，不得以数字开头，不能包含空格）。
                    // 当不设置时，云端将使用用户设备的设备ID来标识终端用户。
                    // 设置人脸注册参数
                    // 清空参数
                    mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
                    // 设置会话场景
                    mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ifr");
                    // 设置会话类型
                    mIdVerifier.setParameter(SpeechConstant.MFV_SST, "enroll");
                    // 设置用户id
                    mIdVerifier.setParameter(SpeechConstant.AUTH_ID, mAuthid);
                    // 设置监听器，开始会话
                    mIdVerifier.startWorking(mEnrollListener);

                    // 子业务执行参数，若无可以传空字符传
                    StringBuffer params = new StringBuffer();
                    // 向子业务写入数据，人脸数据可以一次写入
                    mIdVerifier.writeData("ifr", params.toString(), mImageData, 0, mImageData.length);
                    // 停止写入
                    mIdVerifier.stopWrite("ifr");
                }else {
                    showTip("请选择图片后再注册...");
                }
                break;
            case R.id.online_verify:
                mAuthid = online_authid.getText().toString().trim();
                if (TextUtils.isEmpty(mAuthid)) {
                    showTip("authid不能为空");
                    return;
                }

                if (null != mImageData) {
                    mProDialog.setMessage("验证中...");
                    mProDialog.show();
                    // 设置人脸验证参数
                    // 清空参数
                    mIdVerifier.setParameter(SpeechConstant.PARAMS, null);
                    // 设置会话场景
                    mIdVerifier.setParameter(SpeechConstant.MFV_SCENES, "ifr");
                    // 设置会话类型
                    mIdVerifier.setParameter(SpeechConstant.MFV_SST, "verify");
                    // 设置验证模式，单一验证模式：sin
                    mIdVerifier.setParameter(SpeechConstant.MFV_VCM, "sin");
                    // 用户id
                    mIdVerifier.setParameter(SpeechConstant.AUTH_ID, mAuthid);
                    // 设置监听器，开始会话
                    mIdVerifier.startWorking(mVerifyListener);

                    // 子业务执行参数，若无可以传空字符传
                    StringBuffer params = new StringBuffer();
                    // 向子业务写入数据，人脸数据可以一次写入
                    mIdVerifier.writeData("ifr", params.toString(), mImageData, 0, mImageData.length);
                    // 停止写入
                    mIdVerifier.stopWrite("ifr");
                } else {
                    showTip("请选择图片后再验证");
                }
                break;
            case R.id.online_camera:
                // 设置相机拍照后照片保存路径
                mPictureFile = new File(Environment.getExternalStorageDirectory(),
                        "picture" + System.currentTimeMillis()/1000 + ".jpg");
                // 启动拍照,并保存到临时文件
                Intent mIntent = new Intent();
                mIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                mIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPictureFile));
                mIntent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
                startActivityForResult(mIntent, REQUEST_CAMERA_IMAGE);
                break;

        }

    }

    public void showTip(String str){
        mToast.setText(str);
        mToast.show();
    }


    /**
     * 人脸注册监听器
     */
    private IdentityListener mEnrollListener = new IdentityListener() {

        @Override
        public void onResult(IdentityResult result, boolean islast) {
            Log.d(TAG, result.getResultString());

            if (null != mProDialog) {
                mProDialog.dismiss();
            }

            try {
                JSONObject object = new JSONObject(result.getResultString());
                int ret = object.getInt("ret");

                if (ErrorCode.SUCCESS == ret) {
                    //你想做什么事情应该就在这里做
                    showTip("注册成功");
                }else {
                    showTip(new SpeechError(ret).getPlainDescription(true));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }

        @Override
        public void onError(SpeechError error) {
            if (null != mProDialog) {
                mProDialog.dismiss();
            }

            showTip(error.getPlainDescription(true));
        }
    };


    /**
     * 人脸验证监听器
     */
    private IdentityListener mVerifyListener = new IdentityListener() {

        @Override
        public void onResult(IdentityResult result, boolean islast) {
            Log.d(TAG, result.getResultString());

            if (null != mProDialog) {
                mProDialog.dismiss();
            }

            try {
                JSONObject object = new JSONObject(result.getResultString());
                Log.d(TAG,"object is: "+object.toString());
                String decision = object.getString("decision");

                if ("accepted".equalsIgnoreCase(decision)) {
                    showTip("通过验证");
                } else {
                    showTip("验证失败");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }

        @Override
        public void onError(SpeechError error) {
            if (null != mProDialog) {
                mProDialog.dismiss();
            }

            showTip(error.getPlainDescription(true));
        }

    };






}
