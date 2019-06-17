package com.example.ttlock.activity;

import android.Manifest;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.ttlock.MyApplication;
import com.example.ttlock.R;
import com.example.ttlock.net.ResponseService;
import com.example.ttlock.sp.MyPreference;
import com.ttlock.bl.sdk.util.LogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import at.markushi.ui.CircleButton;

public class AuthActivity extends BaseActivity implements View.OnClickListener {

    EditText user;
    EditText pwd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        MyApplication.addActivity(this);
        setContentView(R.layout.activity_auth);
        user = getView(R.id.auth_user);
        pwd = getView(R.id.auth_pwd);
        CircleButton auth = getView(R.id.auth);
        auth.setOnClickListener(this);


        MyApplication.mTTLockAPI.requestBleEnable(this);
        LogUtil.d("start bluetooth service", DBG);
        MyApplication.mTTLockAPI.startBleService(this);
        //It need location permission to start bluetooth scan,or it can not scan device
        if(requestPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            MyApplication.mTTLockAPI.startBTDeviceScan();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        String access_token = MyPreference.getStr(AuthActivity.this, MyPreference.ACCESS_TOKEN);
        String openid = MyPreference.getStr(AuthActivity.this, MyPreference.OPEN_ID);
        //((TextView)getView(R.id.auth_access_token)).setText(access_token);
        //((TextView)getView(R.id.auth_openid)).setText(openid);
    }

    @Override
    public void onClick(View v) {
//        final String username = "ttlock_" + user.getText().toString();
        final String username = user.getText().toString();
        final String password = pwd.getText().toString();
        new AsyncTask<Void, Integer, String>() {

            @Override
            protected String doInBackground(Void... params) {
                if(!username.trim().equals("") && !password.trim().equals("")) {
                    String str =ResponseService.auth(username, password);
                    return str;
                }
                return "";
            }

            @Override
            protected void onPostExecute(String json) {
                String msg = "";
                if(!json.trim().equals("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        if(jsonObject.has("errcode")) {
                            msg = jsonObject.getString("description");
                        }  else {
                                msg = "登录成功";
                                String access_token = jsonObject.getString("access_token");
                                String openid = jsonObject.getString("openid");
                                MyPreference.putStr(AuthActivity.this, MyPreference.ACCESS_TOKEN, access_token);
                                MyPreference.putStr(AuthActivity.this, MyPreference.OPEN_ID, openid);
                                Intent intent = new Intent(AuthActivity.this, VoiceVerifyActivity.class);
                                startActivity(intent);
                                onResume();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    if(username.trim().equals("")) {
                        msg = "请输入用户名";
                    }else if(password.trim().equals("")) {
                        msg ="请输入密码";
                    }
                }
                toast(msg);
            }
        }.execute();
    }

    /*public void register(View view) {
        Intent intent = new Intent(AuthActivity.this,RegisterActivity.class);
        int requestCode = 1;
        startActivityForResult(intent,requestCode);
    }*/

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1 && resultCode == 2) {
            String username = data.getStringExtra("username");
            user.setText(username);
            pwd.setFocusable(true);
            pwd.setFocusableInTouchMode(true);
            pwd.requestFocus();
        }

    }*/
}
