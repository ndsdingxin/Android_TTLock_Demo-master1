package com.example.ttlock.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.ttlock.R;
import com.example.ttlock.activity.localtest.IsGeShiReal;
import com.example.ttlock.activity.localtest.JianSuoActivity;
import com.example.ttlock.activity.localtest.PcmDownloadActivity;
import com.example.ttlock.activity.localtest.PiPeiAcitvity;
import com.example.ttlock.activity.localtest.RegistMany;
import com.example.ttlock.activity.localtest.RegisterSingle;
import com.example.ttlock.activity.localtest.VoiceToTest;

public class CameraMenuActivity extends BaseActivity implements View.OnClickListener{
    private Uri imageUri;
    TextView btn_1;
    TextView btn_2;
    TextView btn_3;
    TextView btn_4;
    TextView btn_5;
    TextView btn_6;
    TextView btn_7;
    TextView btn_8;
    TextView btn_9;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cam);
        btn_1 = findViewById(R.id.btn_1);
        btn_2 = findViewById(R.id.btn_2);
        btn_3 = findViewById(R.id.btn_3);
        btn_4 = findViewById(R.id.btn_4);
        btn_5 = findViewById(R.id.btn_5);
        btn_6 = findViewById(R.id.btn_6);
        btn_7 = findViewById(R.id.btn_7);
        btn_8 = findViewById(R.id.btn_8);
        btn_9 = findViewById(R.id.btn_9);
        /*btn_3 = findViewById(R.id.btn_3);
        btn_4 = findViewById(R.id.btn_4);*/
        btn_1.setOnClickListener(this);
        btn_2.setOnClickListener(this);
        btn_3.setOnClickListener(this);
        btn_4.setOnClickListener(this);
        btn_5.setOnClickListener(this);
        btn_6.setOnClickListener(this);
        btn_7.setOnClickListener(this);
        btn_8.setOnClickListener(this);
        btn_9.setOnClickListener(this);
        /*btn_3.setOnClickListener(this);
        btn_4.setOnClickListener(this);*/
}

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //声纹注册按钮
            case R.id.btn_1:
                Intent intent1 = new Intent(this, VoiceRegisterActivity.class);
                startActivity(intent1);
                break;
                //重新登录按钮
            case R.id.btn_2:
                Intent intent2 = new Intent(this, AuthActivity.class);
                startActivity(intent2);
                break;
            case R.id.btn_3:
                Intent intent3 = new Intent(this, RegistMany.class);
                startActivity(intent3);
                break;
            case R.id.btn_4:
                Intent intent4 = new Intent(this, RegisterSingle.class);
                startActivity(intent4);
                break;
            case R.id.btn_5:
                Intent intent5 = new Intent(this, PiPeiAcitvity.class);
                startActivity(intent5);
                break;
            case R.id.btn_6:
                Intent intent6 = new Intent(this, JianSuoActivity.class);
                startActivity(intent6);
                break;
            case R.id.btn_7:
                Intent intent7 = new Intent(this, IsGeShiReal.class);
                startActivity(intent7);
                break;
            case R.id.btn_8:
                Intent intent8 = new Intent(this, PcmDownloadActivity.class);
                startActivity(intent8);
                break;
            case R.id.btn_9:
                Intent intent9 = new Intent(this, VoiceToTest.class);
                startActivity(intent9);
                break;
                //人脸注册按钮
           /* case R.id.btn_3:
            View view = View.inflate(this, R.layout.dialog_input, null);
            final EditText dialog_name = view.findViewById(R.id.tv_name);
            new AlertDialog.Builder(this)
                    .setView(view)
                    .setTitle("请输入注册名称")
                    .setNegativeButton("取消", null)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String registerName = dialog_name.getText().toString();
                            if (registerName != null && !registerName.trim().equals("")) {
                                File outputImage = new File(getExternalCacheDir(), registerName + ".jpg");
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
                                    imageUri = FileProvider.getUriForFile(CameraMenuActivity.this, "com.example.ttlock.fileprovider", outputImage);
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
            //人脸验证按钮
            case R.id.btn_4:
                Intent intent4 = new Intent(this, FaceMainActivity.class);
                startActivity(intent4);
                break;*/
            default:
                break;
        }

    }
}
