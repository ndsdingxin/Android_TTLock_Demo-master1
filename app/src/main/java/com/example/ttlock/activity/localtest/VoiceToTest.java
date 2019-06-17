package com.example.ttlock.activity.localtest;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ttlock.R;
import com.example.ttlock.activity.BaseActivity;

public class VoiceToTest extends BaseActivity implements View.OnClickListener {
    private TextView tvwhitchvoicetotext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_to_test);

        tvwhitchvoicetotext = findViewById(R.id.tvwhitchvoicetotext);

        LinearLayout tvconfirmregistermany = findViewById(R.id.tvconfirmvoicetotext);

        tvconfirmregistermany.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvconfirmvoicetotext:
                toast("测试11111");
                break;
        }
    }
}
