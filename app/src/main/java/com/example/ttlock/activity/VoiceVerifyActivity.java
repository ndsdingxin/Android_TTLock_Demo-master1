package com.example.ttlock.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.face.FaceEngine;
import com.example.ttlock.MyApplication;
import com.example.ttlock.R;
import com.example.ttlock.config.GlobalConfig;
import com.example.ttlock.dao.DbService;
import com.example.ttlock.enumtype.Operation;
import com.example.ttlock.model.BleSession;
import com.example.ttlock.model.Key;
import com.example.ttlock.model.KeyObj;
import com.example.ttlock.net.ResponseService;
import com.example.ttlock.sp.MyPreference;
import com.google.gson.reflect.TypeToken;
import com.ttlock.bl.sdk.util.GsonUtil;
import com.ttlock.bl.sdk.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.ttlock.MyApplication.mTTLockAPI;
import static com.example.ttlock.config.GlobalConfig.AUDIO_FORMAT;
import static com.example.ttlock.config.GlobalConfig.CHANNEL_CONFIG;
import static com.example.ttlock.config.GlobalConfig.SAMPLE_RATE_INHZ;

public class VoiceVerifyActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "VoiceVerifyActivity";
    private static final int MY_PERMISSIONS_REQUEST = 1001;

    Button btn_verify;
    TextView btn_register;
    TextView btn_camera;
    private Uri imageUri;
    private TextView tv_random;

    private List<Key> keys;

    public static Key mKey;

    private BleSession bleSession;
    private int openid;

    private String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };

    private List<String> mPermissionList = new ArrayList<>();
    private boolean isRecording;
    private AudioRecord audioRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        MyApplication.addActivity(this);
        setContentView(R.layout.activity_voice_verify);

        btn_verify = findViewById(R.id.btn_verify);
        btn_register =findViewById(R.id.btn_register);
        btn_verify.setOnClickListener(this);
        btn_register.setOnClickListener(this);
        tv_random = findViewById(R.id.tv_random_verify);
        init();

        activeEngine();
        bleSession = MyApplication.bleSession;
        openid = MyPreference.getOpenid(this, MyPreference.OPEN_ID);
    }

    public void activeEngine() {
        FaceEngine faceEngine = new FaceEngine();
        int activeCode = faceEngine.active(MyApplication.mContext, GlobalConfig.APP_ID, GlobalConfig.SDK_KEY);
        Log.d(TAG, "activeEngine: "+activeCode);
    }

    private void init() {

        //turn on bluetooth


//        accessToken = MyPreference.getStr(this, MyPreference.ACCESS_TOKEN);
        keys = new ArrayList<>();
        syncData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogUtil.d("", DBG);
        syncData();
    }

    @Override
    @RequiresPermission(Manifest.permission.BLUETOOTH)
    public void onResume() {
        super.onResume();
        LogUtil.d("", DBG);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.d("stop bluetooth service", DBG);
        MyApplication.mTTLockAPI.stopBleService(this);
    }

    private void syncData() {
        showProgressDialog();
        new AsyncTask<Void,String,String>() {

            @Override
            protected String doInBackground(Void... params) {
                //you can synchronizes all key datas when lastUpdateDate is 0
                String json = ResponseService.syncData(0);
                LogUtil.d("json:" + json, DBG);
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    if(jsonObject.has("errcode")) {
                        toast(jsonObject.getString("description"));
                        Intent intent = new Intent(VoiceVerifyActivity.this, AuthActivity.class);
                        startActivity(intent);
                        return json;
                    }
                    //use lastUpdateDate you can get the newly added key and data after the time
                    long lastUpdateDate = jsonObject.getLong("lastUpdateDate");
                    String keyList = jsonObject.getString("keyList");
//                    JSONArray jsonArray = jsonObject.getJSONArray("keyList");
                    keys.clear();
                    ArrayList<KeyObj> list = GsonUtil.toObject(keyList, new TypeToken<ArrayList<KeyObj>>(){});
                    keys.addAll(convert2DbModel(list));
//                    for(int i = 0;i<jsonArray.length();i++) {
//                        jsonObject = (JSONObject) jsonArray.get(i);
//                        int keyId = jsonObject.getInt("keyId");
//                        int lockId = jsonObject.getInt("lockId");
//                        String userType = jsonObject.getString("userType");
//                        String keyStatus = jsonObject.getString("keyStatus");
//                        String lockName = jsonObject.getString("lockName");
//                        String lockAlias = jsonObject.getString("lockAlias");
//                        String lockKey = jsonObject.getString("lockKey");
//                        String lockMac = jsonObject.getString("lockMac");
//                        int lockFlagPos = jsonObject.getInt("lockFlagPos");
//                        String adminPwd = "";
//                        if (jsonObject.has("adminPwd"))
//                            adminPwd = jsonObject.getString("adminPwd");
//                        String noKeyPwd = "";
//                        if (jsonObject.has("noKeyPwd"))
//                            noKeyPwd = jsonObject.getString("noKeyPwd");
//                        String deletePwd = "";
//                        if (jsonObject.has("deletePwd"))
//                            deletePwd = jsonObject.getString("deletePwd");
//                        int electricQuantity = jsonObject.getInt("electricQuantity");
//                        String aesKeyStr = jsonObject.getString("aesKeyStr");
//                        String lockVersion = jsonObject.getString("lockVersion");
//                        long startDate = jsonObject.getLong("startDate");
//                        long endDate = jsonObject.getLong("endDate");
//                        String remarks = jsonObject.getString("remarks");
//                        int timezoneRawOffset = jsonObject.getInt("timezoneRawOffset");
//
//                        Key key = new Key();
//                        key.setKeyId(keyId);
//                        key.setLockId(lockId);
//                        key.setAdminPs(adminPwd);
//                        key.setAdminKeyboardPwd(noKeyPwd);
//                        key.setDeletePwd(deletePwd);
//                        key.setKeyStatus(keyStatus);
//                        key.setAdmin("110301".equals(userType));
//                        key.setAccessToken(MyPreference.getStr(MainActivity.this, MyPreference.ACCESS_TOKEN));
//                        key.setLockFlagPos(lockFlagPos);
//                        key.setLockId(lockId);
//                        key.setKeyId(keyId);
//                        key.setLockMac(lockMac);
//                        key.setLockName(lockName);
//                        key.setLockAlias(lockAlias);
//                        key.setUnlockKey(lockKey);
//                        key.setLockVersion(lockVersion);
//                        key.setBattery(electricQuantity);
//                        key.setStartDate(startDate);
//                        key.setEndDate(endDate);
//                        key.setAesKeystr(aesKeyStr);
//                        key.setTimezoneRawOffset(timezoneRawOffset);
//                        keys.add(key);
////                        DbService.saveKey(key);
//                    }

                    //clear local keys and save new keys
                    DbService.deleteAllKey();
                    DbService.saveKeyList(keys);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return json;
            }

            @Override
            protected void onPostExecute(String json) {
                super.onPostExecute(json);
                progressDialog.cancel();

                if(keys!=null && keys.size() > 0) {
                    mKey = keys.get(0);
                }
            }
        }.execute();
    }

    private static ArrayList<Key> convert2DbModel(ArrayList<KeyObj> list){
        ArrayList<Key> keyList = new ArrayList<>();
        if(list != null && list.size() > 0){
            for(KeyObj key : list){
                Key DbKey = new Key();
                DbKey.setUserType(key.userType);
                DbKey.setKeyStatus(key.keyStatus);
                DbKey.setLockId(key.lockId);
                DbKey.setKeyId(key.keyId);
                DbKey.setLockVersion(GsonUtil.toJson(key.lockVersion));
                DbKey.setLockName(key.lockName);
                DbKey.setLockAlias(key.lockAlias);
                DbKey.setLockMac(key.lockMac);
                DbKey.setElectricQuantity(key.electricQuantity);
                DbKey.setLockFlagPos(key.lockFlagPos);
                DbKey.setAdminPwd(key.adminPwd);
                DbKey.setLockKey(key.lockKey);
                DbKey.setNoKeyPwd(key.noKeyPwd);
                DbKey.setDeletePwd(key.deletePwd);
                DbKey.setPwdInfo(key.pwdInfo);
                DbKey.setTimestamp(key.timestamp);
                DbKey.setAesKeyStr(key.aesKeyStr);
                DbKey.setStartDate(key.startDate);
                DbKey.setEndDate(key.endDate);
                DbKey.setSpecialValue(key.specialValue);
                DbKey.setTimezoneRawOffset(key.timezoneRawOffset);
                DbKey.setKeyRight(key.keyRight);
                DbKey.setKeyboardPwdVersion(key.keyboardPwdVersion);
                DbKey.setRemoteEnable(key.remoteEnable);
                DbKey.setRemarks(key.remarks);

                keyList.add(DbKey);
            }
        }
        return keyList;
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
            case R.id.btn_verify:
                Button button = (Button) v;
                if (button.getText().toString().equals("声纹\n验证")) {
                    button.setText("停止\n录音");
                    rand();
                    Toast.makeText(this, "开始录音", Toast.LENGTH_SHORT).show();
                    startRecord("vpr");
                    return;
                } else {
                    button.setText("声纹\n验证");
                    stopRecord();
                    Toast.makeText(this, "停止录音，正在进行验证，请稍等...", Toast.LENGTH_SHORT).show();
                    verifyVoice();
                }
                break;
            case R.id.btn_register:
                Intent intent = new Intent(this, CameraMenuActivity.class);
                startActivity(intent);
                break;
            default:
                break;
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


    public void startRecord(String voiceName) {


        if(requestPermission(Manifest.permission.RECORD_AUDIO)) {


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

    public void verifyVoice() {
        final ProgressDialog dialog = ProgressDialog.show(this,"声纹验证","声纹验证中...");
        new AsyncTask<Void,String,String>(){
            @Override
            protected String doInBackground(Void... voids) {
                String json = "";
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    String fileName = "vpr.pcm";
                    String sdPath = getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath();
                    String filePath = sdPath + "/" + fileName;
                    List<File> list = new ArrayList<>();
                    list.add(new File(filePath));
                    try {
                        json = ResponseService.verifyVoice(list);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(VoiceVerifyActivity.this, "SD卡没有挂载", Toast.LENGTH_SHORT).show();
                }
                return json;
            }

            @Override
            protected void onPostExecute(String json) {
                super.onPostExecute(json);
                dialog.dismiss();


              /*  if(mTTLockAPI.isConnected(mKey.getLockMac())) {//如果锁已经连接上了，你可以立即调用
                    if(mKey.isAdmin()) //如果认证为管理员
                        //管理员开门
                        mTTLockAPI.unlockByAdministrator(null, openid, mKey.getLockVersion(), mKey.getAdminPwd(), mKey.getLockKey(), mKey.getLockFlagPos(), System.currentTimeMillis(), mKey.getAesKeyStr(), mKey.getTimezoneRawOffset());
                    else
                        //电子钥匙开门
                        mTTLockAPI.unlockByUser(null, openid, mKey.getLockVersion(), mKey.getStartDate(), mKey.getEndDate(), mKey.getLockKey(), mKey.getLockFlagPos(), mKey.getAesKeyStr(), mKey.getTimezoneRawOffset());
                } else {//to connect the lock
                    mTTLockAPI.connect(mKey.getLockMac());  //再次链接
                    bleSession.setOperation(Operation.CLICK_UNLOCK);  //设置解锁操作
                    bleSession.setLockmac(mKey.getLockMac());  //设置mac地址
                }*/



                try {
                    JSONObject jsonObject = new JSONObject(json);
                    String code = jsonObject.getString("code");
                    Boolean result = jsonObject.getBoolean("result");
                    if(code != null && code.equals("200")) {
                        if(result) {
                            String score = jsonObject.getString("score");
                            Toast.makeText(VoiceVerifyActivity.this, "声纹验证成功！得分为"+score, Toast.LENGTH_SHORT).show();
                            if(mTTLockAPI.isConnected(mKey.getLockMac())) {//如果锁已经连接上了，你可以立即调用
                                if(mKey.isAdmin()) //如果认证为管理员
                                    //管理员开门
                                    mTTLockAPI.unlockByAdministrator(null, openid, mKey.getLockVersion(), mKey.getAdminPwd(), mKey.getLockKey(), mKey.getLockFlagPos(), System.currentTimeMillis(), mKey.getAesKeyStr(), mKey.getTimezoneRawOffset());
                                else
                                    //电子钥匙开门
                                    mTTLockAPI.unlockByUser(null, openid, mKey.getLockVersion(), mKey.getStartDate(), mKey.getEndDate(), mKey.getLockKey(), mKey.getLockFlagPos(), mKey.getAesKeyStr(), mKey.getTimezoneRawOffset());
                            } else {//to connect the lock
                                mTTLockAPI.connect(mKey.getLockMac());  //再次链接
                                bleSession.setOperation(Operation.CLICK_UNLOCK);  //设置解锁操作
                                bleSession.setLockmac(mKey.getLockMac());  //设置mac地址
                            }
                           /* Intent intent = new Intent(VoiceVerifyActivity.this,MainActivity.class);
                            startActivity(intent);*/
                        } else {
                            String score = jsonObject.getString("score");
                            Toast.makeText(VoiceVerifyActivity.this, "验证失败,得分为"+score+"，请重新录音验证或重新注册", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE,1,1,"声纹注册");
        menu.add(Menu.NONE,2,2,"重新登录");
        menu.add(Menu.NONE,3,3,"人脸注册");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                Intent intent1 = new Intent(this,VoiceRegisterActivity.class);
                startActivity(intent1);
                break;
            case 2:
                Intent intent2 = new Intent(this,AuthActivity.class);
                startActivity(intent2);
                break;
            case 3:
                View view = View.inflate(this, R.layout.dialog_input, null);
                final EditText dialog_name = view.findViewById(R.id.tv_name);
                new AlertDialog.Builder(this)
                    .setView(view)
                    .setTitle("请输入注册名称")
                    .setNegativeButton("取消",null)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String registerName = dialog_name.getText().toString();
                            if(registerName !=null && !registerName.trim().equals("")) {
                                File outputImage = new File(getExternalCacheDir(), registerName+".jpg");
                                try {
                                    if (outputImage.exists()) {
                                        outputImage.delete();
                                    }
                                    outputImage.createNewFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                if (Build.VERSION.SDK_INT < 24) {
                                    imageUri = Uri.fromFile(outputImage);
                                } else {
                                    imageUri = FileProvider.getUriForFile(VoiceVerifyActivity.this, "com.example.ttlock.fileprovider", outputImage);
                                }
                                // 启动相机程序
                                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                                startActivity(intent);
                            } else {
                                toast("请输入注册名称！");
                            }
                        }
                    })
                    .show();

                break;
            default:

        }
        return super.onOptionsItemSelected(item);
    }


}
