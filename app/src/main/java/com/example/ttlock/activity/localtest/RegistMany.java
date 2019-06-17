package com.example.ttlock.activity.localtest;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ttlock.R;
import com.example.ttlock.activity.BaseActivity;
import com.example.ttlock.activity.VoiceVerifyActivity;
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

import butterknife.OnClick;

import static com.example.ttlock.config.GlobalConfig.AUDIO_FORMAT;
import static com.example.ttlock.config.GlobalConfig.CHANNEL_CONFIG;
import static com.example.ttlock.config.GlobalConfig.SAMPLE_RATE_INHZ;

public class RegistMany extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "VoiceRegisterActivity";

    private static final int MY_PERMISSIONS_REQUEST = 1001;
    private List<File> mReocrdFileArray = new ArrayList<>();
    private String mVPRGroupIds = GlobalConfig.VPR_GROUPID;



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
    private String x;
    private double i;
    private TextView tvwhitchregistermany;
    private String  [] str = {"点击开始录音","停止第一次录音",
            "开始第二次录音","停止第二次录音"
            ,"开始第三次次录音","停止第三次录音","开始第四次录音",
            "停止第四次录音","开始第五次录音","停止第五次次录音","正在进行注册，请稍等"};

    String[] fileNames = {"vpr1.pcm","vpr2.pcm","vpr3.pcm","vpr4.pcm","vpr5.pcm"};
    private TextView tvtimeregistermany;
    private TextView tvtagregistermany;
    private long stime;
    private int count;
   private LinearLayout lvconfirmregistermany ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist_many);

        tvwhitchregistermany = findViewById(R.id.tvwhitchregistermany);
        tv_random = findViewById(R.id.tv_random);
        tvtimeregistermany = findViewById(R.id.tvtimeregistermany);
        tvtagregistermany = findViewById(R.id.tvtagregistermany);

        lvconfirmregistermany = findViewById(R.id.lvconfirmregistermany);

        lvconfirmregistermany.setOnClickListener(this);

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

    @OnClick(R.id.lvconfirmregistermany)
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.lvconfirmregistermany:
                String text = tvtagregistermany.getText().toString().trim();
                if (text.equals(str[0])){
                    Message message = new Message();
                    stime = System.currentTimeMillis();
                    tvtagregistermany.setText(str[1]);
                    rand();
                    Toast.makeText(this, "开始第1条录音", Toast.LENGTH_SHORT).show();
                    startRecord(fileNames[0]);
                    handler.sendMessage(message);
                }else if (text.equals(str[2])){
                    Message message = new Message();
                    stime = System.currentTimeMillis();
                    tvtagregistermany.setText(str[3]);
                    startRecord(fileNames[1]);
                    Toast.makeText(this, str[2], Toast.LENGTH_SHORT).show();
                    handler.sendMessage(message);
                }else if (text.equals(str[4])){
                    Message message = new Message();
                    stime = System.currentTimeMillis();
                    tvtagregistermany.setText(str[5]);
                    startRecord(fileNames[2]);
                    Toast.makeText(this, str[4], Toast.LENGTH_SHORT).show();
                    handler.sendMessage(message);
                }else if (text.equals(str[6])){
                    Message message = new Message();
                    stime = System.currentTimeMillis();
                    tvtagregistermany.setText(str[7]);
                    startRecord(fileNames[3]);
                    Toast.makeText(this, str[6], Toast.LENGTH_SHORT).show();
                    handler.sendMessage(message);
                }else if (text.equals(str[8])){
                    Message message = new Message();
                    stime = System.currentTimeMillis();
                    tvtagregistermany.setText(str[9]);
                    startRecord(fileNames[4]);
                    Toast.makeText(this, str[8], Toast.LENGTH_SHORT).show();
                    handler.sendMessage(message);
                }

                if (text.equals(str[1])){
                    rand();
                    tvtagregistermany.setText(str[2]);
                    Toast.makeText(this, str[1], Toast.LENGTH_SHORT).show();
                    stopRecord();
                }else if (text.equals(str[3])){
                    rand();
                    tvtagregistermany.setText(str[4]);
                    Toast.makeText(this, str[3], Toast.LENGTH_SHORT).show();
                    stopRecord();
                }else if (text.equals(str[5])){
                    rand();
                    tvtagregistermany.setText(str[6]);
                    Toast.makeText(this, str[5], Toast.LENGTH_SHORT).show();
                    stopRecord();
                }else if (text.equals(str[7])){
                    rand();
                    tvtagregistermany.setText(str[8]);
                    Toast.makeText(this, str[7], Toast.LENGTH_SHORT).show();
                    stopRecord();
                }else if (text.equals(str[9])){
                    tvtagregistermany.setText(str[0]);
                    stopRecord();
                    Toast.makeText(this, str[9], Toast.LENGTH_SHORT).show();
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
            tvtimeregistermany.setText((++count)+"秒钟");
        }else {
            tvtimeregistermany.setText("");
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

    @SuppressLint("StaticFieldLeak")
    private void registerVoice() {
        final ProgressDialog dialog = ProgressDialog.show(this,"声纹上传","声纹上传中...");
        new AsyncTask<Void,String,String>() {
            @Override
            protected String doInBackground(Void ...voids) {
                String json = "";
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    String sdPath = getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath();
                    //File file = new File(sdPath+"/Android/data/com.example.ttlock/Music");
                    String filePath = "";

                    for(int i = 0;i<5;i++) {
                        filePath = sdPath + "/" + fileNames[i];
                        File file = new File(filePath);
                        mReocrdFileArray.add(file);
                    }
                    try {
                        json = ResponseService.registerVoice(mVPRGroupIds, mReocrdFileArray);
                        if(json == null || json.trim().equals("")) {
                            toast("服务器正在维护，请稍后再试");
                            Intent intent = new Intent(RegistMany.this, PiPeiAcitvity.class);
                            startActivity(intent);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(RegistMany.this, "SD卡没有挂载", Toast.LENGTH_SHORT).show();
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
                            Intent intent = new Intent(RegistMany.this,VoiceVerifyActivity.class);
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
