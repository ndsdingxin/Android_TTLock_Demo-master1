
package com.example.ttlock.activity.localtest;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.ttlock.R;
import com.example.ttlock.activity.BaseActivity;
import com.example.ttlock.net.ResponseService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PcmDownloadActivity extends BaseActivity implements View.OnClickListener {
    private EditText etwhitchpcmdownload;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pcm_download);
        etwhitchpcmdownload = findViewById(R.id.etwhitchpcmdownload);

        LinearLayout tvconfirmpcmdownload = findViewById(R.id.tvconfirmpcmdownload);

        tvconfirmpcmdownload.setOnClickListener(this);

        etwhitchpcmdownload.setText( getIntent().getStringExtra("code"));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvconfirmpcmdownload:
                String str = etwhitchpcmdownload.getText().toString().trim();

                if (str.matches("^[0-9A-Za-z]{1,}$")){
                    pcmDownloaad(str);
                }else {
                    toast(str);
                }

                break;
        }
    }


    @SuppressLint("StaticFieldLeak")
    private void pcmDownloaad(final String code) {
        final ProgressDialog dialog = ProgressDialog.show(this,"pcm下载","pcm下载...");
        new AsyncTask<Void,String,String>() {
            @Override
            protected String doInBackground(Void ...voids) {
                String json = "";

                    try {
                        json = ResponseService.PcmDownload(code);
                        if(json == null || json.trim().equals("")) {
                            Toast.makeText(PcmDownloadActivity.this,"服务器正在维护，请稍后再试",Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(PcmDownloadActivity.this, PiPeiAcitvity.class);
                            startActivity(intent);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
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

                        byte [] bytes = json.getBytes();

                        FileOutputStream fos  =  new FileOutputStream(new File(Environment.getExternalStorageDirectory()+"/AAAAAAA/"+"fffff.m4a"));
                        fos.write(bytes);
                        fos.flush();
                        fos.close();
                        msg = "下载完成";

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                toast(msg);
            }
        }.execute();
    }
}
