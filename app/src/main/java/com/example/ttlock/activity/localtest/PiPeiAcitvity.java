package com.example.ttlock.activity.localtest;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ttlock.R;
import com.example.ttlock.activity.BaseActivity;
import com.example.ttlock.config.GlobalConfig;
import com.example.ttlock.net.ResponseService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.ttlock.config.GlobalConfig.AUDIO_FORMAT;
import static com.example.ttlock.config.GlobalConfig.CHANNEL_CONFIG;
import static com.example.ttlock.config.GlobalConfig.SAMPLE_RATE_INHZ;

public class PiPeiAcitvity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "VoiceRegisterActivity";

    private static final int MY_PERMISSIONS_REQUEST = 1001;
    private List<File> mReocrdFileArray = new ArrayList<>();
    private String mVPRGroupIds = GlobalConfig.VPR_GROUPID;



    //private Button mBtnPlay;

    private String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private List<String> mPermissionList = new ArrayList<>();
    private boolean isRecording;
    private AudioRecord audioRecord;
    private AudioTrack audioTrack;
    private String x;
    private double i;
    private String  [] str = {"点击开始录音","点击停止录音",
            "正在进行匹配，请稍等"};
    String[] fileNames = {"vpr1.pcm"};
    private TextView tvtagpipei;
    private long stime;
    private int count;
    private  TextView tvtimerpipei;
    private  TextView tvwhitchvoicetotext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pi_pei_acitvity);

        tvtagpipei = findViewById(R.id.tvtagpipei);
        tvtimerpipei = findViewById(R.id.tvtimerpipei);
        tvwhitchvoicetotext = findViewById(R.id.tvwhitchpipei);
        LinearLayout tvconfirmpipei = findViewById(R.id.tvconfirmpipei);
        tvconfirmpipei.setOnClickListener(this);


    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvconfirmpipei:
                String text = tvtagpipei.getText().toString().trim();
                if (text.equals(str[0])){
                    Message message = new Message();
                    stime = System.currentTimeMillis();
                    tvtagpipei.setText(str[1]);
                    Toast.makeText(this, str[0], Toast.LENGTH_SHORT).show();
                    startRecord(fileNames[0]);
                    handler.sendMessage(message);
                }
                if (text.equals(str[1])){
                    tvtagpipei.setText(str[0]);
                    stopRecord();
                    Toast.makeText(this, str[1], Toast.LENGTH_SHORT).show();
                    registerVoice();
                }
                break;

            default:
                toast("请重新上传一次音频");
                break;
        }
    }

    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            updateTextView();
        }
    };
    Runnable update_runnable = new Runnable() {
        public void run() {
            updateTextView();
        }
    };
    private void updateTextView() {
        if (isRecording){
//           long duration = (int)((System.currentTimeMillis() - stime)/1000);
            handler.postDelayed(update_runnable, 1000);
            tvtimerpipei.setText((++count)+"秒钟");
        }else {
            tvtimerpipei.setText("");
        }

    }
    public void startRecord(String voiceName) {
        count=0;
        /**
         * 参数1：采样率
         * 参数2：声道
         * 参数3：量化精度
         * 获取缓冲区大小
         */
        final int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
        /**
         * 参数1：麦克风
         * 参数2：采样率
         * 参数3：声道
         * 参数4：量化精度
         * 参数5：缓冲区大小
         */
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_INHZ,
                CHANNEL_CONFIG, AUDIO_FORMAT, minBufferSize);

        final byte data[] = new byte[minBufferSize];
        final File file = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), voiceName);
        if (file.exists()) {
            file.delete();
        }

        audioRecord.startRecording();

        isRecording = true;


        new Thread(new Runnable() {
            @Override
            public void run() {

                FileOutputStream os = null;
                try {
                    os = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                if (null != os) {
                    while (isRecording) {
                        int read = audioRecord.read(data, 0, minBufferSize);
                        // 如果读取音频数据没有出现错误，就将数据写入到文件
                        if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                            try {
                                os.write(data);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void stopRecord() {
        isRecording = false;
        // 释放资源
        if (null != audioRecord) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
            //recordingThread = null;
        }
    }

    private void stopPlay() {
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void registerVoice() {
        final ProgressDialog dialog = ProgressDialog.show(this,"声纹匹配","声纹匹配中...");
        new AsyncTask<Void,String,String>() {
            @Override
            protected String doInBackground(Void ...voids) {
                String json = "";
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    String sdPath = getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath();
                    //File file = new File(sdPath+"/Android/data/com.example.ttlock/Music");
                    String filePath = "";

                    for(int i = 0;i<1;i++) {
                        filePath = sdPath + "/" + fileNames[i];
                        File file = new File(filePath);
                        mReocrdFileArray.add(file);
                    }
                    try {
                        json = ResponseService.verifyVoice(mReocrdFileArray);
                        if(json == null || json.trim().equals("")) {
                            toast("服务器正在维护，请稍后再试");
                            Intent intent = new Intent(PiPeiAcitvity.this, PiPeiAcitvity.class);
                            startActivity(intent);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(PiPeiAcitvity.this, "SD卡没有挂载", Toast.LENGTH_SHORT).show();
                }
                return json;
            }

            @Override
            protected void onPostExecute(String json) {
                super.onPostExecute(json);
                dialog.dismiss();
                String msg = "服务器正在维护，请稍后再试";
                if(!json.trim().equals("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        String code = jsonObject.getString("code");
                        if(code != null) {
                            if(code.equals("200")) {
                                msg = "声纹验证完成！result:"+jsonObject.getString("result")+ "score:"+jsonObject.getString("score");;
                            } else {
                                msg = "result:"+jsonObject.getString("result")+
                                        "score:"+jsonObject.getString("score")+
                                        "code:"+code;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                toast(msg);
            }
        }.execute();
    }
}
