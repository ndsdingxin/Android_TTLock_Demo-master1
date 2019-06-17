package com.example.ttlock.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.ttlock.R;
import com.example.ttlock.net.ResponseService;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends BaseActivity {

    private EditText rg_username;
    private EditText rg_password;
    private Button rg_submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        rg_username = findViewById(R.id.rg_username);
        rg_password = findViewById(R.id.rg_password);
        rg_submit = findViewById(R.id.rg_submit);
    }

    public void submit(View view) {
        final String username = rg_username.getText().toString();
        final String password = rg_password.getText().toString();
        new AsyncTask<Void, String, String>() {

            @Override
            protected String doInBackground(Void... voids) {
                String json = ResponseService.registerUser(username,password);
                return json;
            }

            @Override
            protected void onPostExecute(String json) {
                String msg = "注册成功！";
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    if(jsonObject.has("errcode")) {
                        msg = jsonObject.getString("description");
                    } else {
                        String username1 = (String) jsonObject.get("username");
                        msg = msg + "用户名为:" + username1;
                        int resultCode = 2;
                        Intent intent = new Intent(RegisterActivity.this,AuthActivity.class);
                        intent.putExtra("username",username1);
                        setResult(resultCode,intent);
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                toast(msg);
            }
        }.execute();
    }
}
