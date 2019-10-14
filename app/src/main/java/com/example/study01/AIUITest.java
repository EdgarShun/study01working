package com.example.study01;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.aiui.AIUIAgent;
import com.iflytek.aiui.AIUIConstant;
import com.iflytek.aiui.AIUIEvent;
import com.iflytek.aiui.AIUIListener;
import com.iflytek.aiui.AIUIMessage;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class AIUITest {
    //定义上下文对象
    public Context context;

    public AIUITest(Context context){
        this.context = context;
    }

    private AIUIAgent mAIUIAgent = null;

    //交互状态
    private int mAIUIState = AIUIConstant.STATE_IDLE;

    private static String TAG = "AIUITest";

    //AIUI返回来的数据
    StringBuffer stringBuffer = new StringBuffer();

    //开始语音听写，然后文字理解
    public String recording(String string) {

        if (checkAIUIAgent()) {
            startTextNlp(string);
//            startVoiceNlp();


            //每次显示新的结果前，把之前的结果内容清零
            stringBuffer.delete(0, stringBuffer.length());

            int i = 0;
            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i+=0.1;
            } while (TextUtils.isEmpty(stringBuffer.toString()));
            return stringBuffer.toString();
        }else{
            return "创建AIUIAgent失败";
        }
    }

    /**
     *
     * @param
     */
    public String recording() {

        if (checkAIUIAgent()) {
            startVoiceNlp();
            //每次显示新的结果前，把之前的结果内容清零
            stringBuffer.delete(0, stringBuffer.length());
            int i = 0;
            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i+=0.1;
            } while (TextUtils.isEmpty(stringBuffer.toString()));
            return stringBuffer.toString();
        }else{
            return "创建AIUIAgent失败";
        }
    }

    /*
    开始语音理解
    本人先做一个实验
    简单的固定的把值传给AIUI
    看能不能互相交互
     */

    //文字理解
    private void startTextNlp(String string) {


        Log.i(TAG, string);

        // 先发送唤醒消息，改变AIUI内部状态，只有唤醒状态才能接收文本输入
        if (AIUIConstant.STATE_WORKING != mAIUIState)
        {

            AIUIMessage wakeupMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
            if (wakeupMsg != null) {
//                Log.i(TAG, "唤醒了AIUI");
            } else {
//                Log.i(TAG, "AIUI没有唤醒");
            }
            mAIUIAgent.sendMessage(wakeupMsg);
        }

        try {
            // 在输入参数中设置tag，则对应结果中也将携带该tag，可用于关联输入输出
            String params = "data_type=text,tag=text-tag";
            byte[] textData = string.getBytes("utf-8");

            AIUIMessage write = new AIUIMessage(AIUIConstant.CMD_WRITE, 0, 0, params, textData);
            mAIUIAgent.sendMessage(write);


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    //检查AIUIAgent时候存在
    private boolean checkAIUIAgent() {

        if (null == mAIUIAgent) {
            //创建AIUIAgent
            mAIUIAgent = AIUIAgent.createAgent(context, getAIUIParams(), mAIUIListener);
//            stringBuffer.append("创建aiuiagent成功");
        }
        if( null == mAIUIAgent ){
            final String strErrorTip = "创建 AIUI Agent 失败！";
            Log.i(TAG,strErrorTip );
//            this.stringBuffer.append(strErrorTip);
        }
        return null != mAIUIAgent;
    }


    private String getAIUIParams() {
        String params = "";
        AssetManager assetManager = context.getResources().getAssets();
        try {
            InputStream ins = assetManager.open("cfg/aiui_phone.cfg");
            byte[] buffer = new byte[ins.available()];
            ins.read(buffer);
            ins.close();

            params = new String(buffer);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return params;
    }


    //AIUI事件监听器
    private AIUIListener mAIUIListener = new AIUIListener() {

        @Override
        public void onEvent(AIUIEvent event) {

            switch (event.eventType){
                case AIUIConstant.EVENT_WAKEUP:
                    Log.i( TAG,  "on event: "+ event.eventType );
                    Log.i(TAG,"进入识别状态");

                    break;
                case AIUIConstant.EVENT_RESULT:
                    //结果事件

                    Log.i(TAG, "结果事件on event: " + event.eventType);

                    try {

                        JSONObject bizParamJson = new JSONObject(event.info);
                        JSONObject data = bizParamJson.getJSONArray("data").getJSONObject(0);
                        JSONObject params = data.getJSONObject("params");
                        JSONObject content = data.getJSONArray("content").getJSONObject(0);

                        if (content.has("cnt_id")) {
                            String cnt_id = content.getString("cnt_id");
                            // JSONObject cntJson = new JSONObject(new String(event.data.getByteArray(cnt_id), "utf-8"));

                            String cntStr = new String(event.data.getByteArray(cnt_id), "utf-8");


                            JSONObject cntJson = new JSONObject(cntStr);

                            // mNlpText.append(cntJson.toString());

                            String sub = params.optString("sub");

                            JSONObject result = cntJson.optJSONObject("intent");


                            //为什么我的sub就一直是iat，而不是nlp？
                            //是听写结果，而不是语义结果，这是为什么？
                            //全景模式加_box


                            if ("nlp".equals(sub) && result.length() > 2) {
                                // 解析得到语义结果
                                String str = "";


                                //在线语义结果
                                if (result.optInt("rc") == 0) {
                                    JSONObject answer = result.optJSONObject("answer");
                                    if (answer != null) {
                                        str = answer.optString("text");
                                    }
                                } else {
                                    str = "rc4，无法识别";
                                }
                                if (!TextUtils.isEmpty(str)) {
                                    stringBuffer.append("\n");
                                    stringBuffer.append(str);
                                }
                            }
                        }

                    } catch (Throwable e) {
                        e.printStackTrace();

                    }
                    break;

                case AIUIConstant.EVENT_ERROR:
                    Log.i( TAG,  "on event: "+ event.eventType );
                    stringBuffer.append("\n");
                    stringBuffer.append("错误:"+event.arg1+"\n"+event.info);
                    break;

                case AIUIConstant.EVENT_VAD:
                    if (AIUIConstant.VAD_BOS == event.arg1) {
                        //找到语音前端点
                        Log.i(TAG,"找到vad_bos");
                    } else if (AIUIConstant.VAD_EOS == event.arg1) {
                        //找到语音后端点
                        Log.i(TAG,"找到vad_eos");
                    } else {
                        Log.i(TAG,"" + event.arg2);
                    }
                    break;

                case AIUIConstant.EVENT_START_RECORD:
                    //开始录音事件
                    Log.i( TAG,  "on event: "+ event.eventType );
//                    showTip("开始录音");
                    Log.i(TAG,"开始录音");
                    break;

                case AIUIConstant.EVENT_STOP_RECORD:
                    //停止录音事件
                    Log.i( TAG,  "on event: "+ event.eventType );
                    Log.i(TAG,"停止录音");
                    break;

                //状态事件
                case AIUIConstant.EVENT_STATE:
                    mAIUIState = event.arg1;

                    if (AIUIConstant.STATE_IDLE == mAIUIState) {
                        // 闲置状态，AIUI未开启
//                        showTip("STATE_IDLE");
                        Log.i(TAG,"STATE_IDLE");
                    } else if (AIUIConstant.STATE_READY == mAIUIState) {
                        // AIUI已就绪，等待唤醒
//                        showTip("STATE_READY");
                        Log.i(TAG,"STATE_READY");
                    } else if (AIUIConstant.STATE_WORKING == mAIUIState) {
                        // AIUI工作中，可进行交互
//                        showTip("STATE_WORKING");
                        Log.i(TAG,"STATE_WORKING");
                    }
                    break;

                default:
                    break;
            }
        }
    };

    //录音理解
    private void startVoiceNlp(){
        Log.i( TAG, "start voice nlp" );
//        mNlpText.setText("");
        //我是想把stringBuffer归置为零


        // 先发送唤醒消息，改变AIUI内部状态，只有唤醒状态才能接收语音输入
        // 默认为oneshot 模式，即一次唤醒后就进入休眠，如果语音唤醒后，需要进行文本语义，请将改段逻辑copy至startTextNlp()开头处
        if( AIUIConstant.STATE_WORKING != 	this.mAIUIState ){
            AIUIMessage wakeupMsg = new AIUIMessage(AIUIConstant.CMD_WAKEUP, 0, 0, "", null);
            mAIUIAgent.sendMessage(wakeupMsg);
        }

        // 打开AIUI内部录音机，开始录音
        String params = "sample_rate=16000,data_type=audio";
        AIUIMessage writeMsg = new AIUIMessage( AIUIConstant.CMD_START_RECORD, 0, 0, params, null );
        mAIUIAgent.sendMessage(writeMsg);
    }


    public void destory(){
        if (null != this.mAIUIAgent){
            AIUIMessage stopMsg = new AIUIMessage(AIUIConstant.CMD_STOP,0,0,null,null);
            mAIUIAgent.sendMessage(stopMsg);

            this.mAIUIAgent.destroy();
            this.mAIUIAgent = null;
        }
    }

}
