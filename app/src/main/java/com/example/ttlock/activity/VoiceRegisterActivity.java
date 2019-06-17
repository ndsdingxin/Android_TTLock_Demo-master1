package com.example.ttlock.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ttlock.MyApplication;
import com.example.ttlock.R;
import com.example.ttlock.config.GlobalConfig;
import com.example.ttlock.net.ResponseService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.ttlock.config.GlobalConfig.AUDIO_FORMAT;
import static com.example.ttlock.config.GlobalConfig.CHANNEL_CONFIG;
import static com.example.ttlock.config.GlobalConfig.SAMPLE_RATE_INHZ;

public class VoiceRegisterActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "VoiceRegisterActivity";

    private static final int MY_PERMISSIONS_REQUEST = 1001;
    private List<File> mReocrdFileArray = new ArrayList<>();
    private String mVPRGroupIds = GlobalConfig.VPR_GROUPID;


    private ImageView mBtnControl;

    private TextView tv_tip;
    private TextView tv_random;
    //private Button mBtnPlay;

    private String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private List<String> mPermissionList = new ArrayList<>();
    private boolean isRecording;
    private AudioRecord audioRecord;
    private AudioTrack audioTrack;
    private FileInputStream fileInputStream;
    private String x;
    private double i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        MyApplication.addActivity(this);
        setContentView(R.layout.activity_voice_register);



        mBtnControl = findViewById(R.id.btn_control);
        mBtnControl.setOnClickListener(this);
        tv_tip = findViewById(R.id.tv_tip);
        tv_random = findViewById(R.id.tv_random);



        checkPermissions();
    }

    public void rand(){
        double rd;  //随机数
        long  sws;  //生成的10位数
        do {
            rd = Math.random();
            sws = (long) (rd*100000000l);
        } while (rd < 0.1);
        tv_random.setText(sws + "");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_control:
                ImageView button = (ImageView) v;


                if (tv_tip.getText().toString().equals("点击开始录音(再次点击停止录音)")) {
                    tv_tip.setText("点击停止第1条录音");
                    rand();

                    Toast.makeText(this, "开始第1条录音", Toast.LENGTH_SHORT).show();
                    startRecord("vpr1");
                    return;
                }
                if(tv_tip.getText().toString().equals("点击停止第1条录音")) {
                    rand();
                    tv_tip.setText("点击开始第2条录音");
                    Toast.makeText(this, "停止第1条录音", Toast.LENGTH_SHORT).show();
                    stopRecord();
                    return;
                }
                if(tv_tip.getText().toString().equals("点击开始第2条录音")) {
                    tv_tip.setText("点击停止第2条录音");
                    startRecord("vpr2");
                    Toast.makeText(this, "开始第2条录音", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(tv_tip.getText().toString().equals("点击停止第2条录音")) {
                    rand();
                    tv_tip.setText("点击开始第3条录音");
                    stopRecord();
                    Toast.makeText(this, "停止第2条录音", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(tv_tip.getText().toString().equals("点击开始第3条录音")) {
                    tv_tip.setText("点击停止第3条录音");
                    startRecord("vpr3");
                    Toast.makeText(this, "开始第3条录音", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(tv_tip.getText().toString().equals("点击停止第3条录音")) {
                    tv_tip.setText("点击开始录音(再次点击停止录音)");
                    stopRecord();
                    Toast.makeText(this, "正在进行注册，请稍等", Toast.LENGTH_SHORT).show();
                    registerVoice();
                    return;
                }
                break;
            default:
                break;
        }
    }

    public void startRecord(String voiceName) {
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
        final File file = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), voiceName+".pcm");
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

    private void checkPermissions() {
        // Marshmallow开始才用申请运行时权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(this, permissions[i]) !=
                        PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permissions[i]);
                }
            }
            if (!mPermissionList.isEmpty()) {
                String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);
                ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST);
            }
        }
    }


    private void registerVoice() {
        final ProgressDialog dialog = ProgressDialog.show(this,"声纹上传","声纹上传中...");
        new AsyncTask<Void,String,String>() {
            @Override
            protected String doInBackground(Void ...voids) {
                String json = "";
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    String[] fileNames = {"vpr1.pcm","vpr2.pcm","vpr3.pcm"};
                    String sdPath = getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath();
                    //File file = new File(sdPath+"/Android/data/com.example.ttlock/Music");
                    String filePath = "";

                    for(int i = 0;i<3;i++) {
                        filePath = sdPath + "/" + fileNames[i];
                        File file = new File(filePath);
                        mReocrdFileArray.add(file);
                    }
                    try {
                        json = ResponseService.registerVoice(mVPRGroupIds, mReocrdFileArray);
                        if(json == null || json.trim().equals("")) {
                            toast("服务器正在维护，请稍后再试");
                            Intent intent = new Intent(VoiceRegisterActivity.this,VoiceVerifyActivity.class);
                            startActivity(intent);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(VoiceRegisterActivity.this, "SD卡没有挂载", Toast.LENGTH_SHORT).show();
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
                                msg = "声纹注册完成！请进行声纹验证！";
                            } else {
                                msg = jsonObject.getString("error");
                            }
                            Intent intent = new Intent(VoiceRegisterActivity.this,VoiceVerifyActivity.class);
                            startActivity(intent);
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
