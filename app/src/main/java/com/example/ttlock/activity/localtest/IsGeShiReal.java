package com.example.ttlock.activity.localtest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ttlock.R;
import com.example.ttlock.net.ResponseService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IsGeShiReal extends Activity implements View.OnClickListener {

    private static final String TAG = "VoiceRegisterActivity";
    private static final int MY_PERMISSIONS_REQUEST = 1001;
    //private String mVPRGroupIds = GlobalConfig.VPR_GROUPID;
    private List<File> mReocrdFileArray = new ArrayList<>();
    private AudioTrack audioTrack;
    private String  [] str = {"点击开始录音","点击停止录音",
            "正在进行注册，请稍等"};
    private TextView tvwhitchisgeshireal;
    private  String strpath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_is_ge_shi_real);
        tvwhitchisgeshireal = findViewById(R.id.tvwhitchisgeshireal);

        LinearLayout tvconfirmisgeshireal = findViewById(R.id.tvconfirmisgeshireal);

        tvconfirmisgeshireal.setOnClickListener(this);
        tvwhitchisgeshireal.setOnClickListener(this);


    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvwhitchisgeshireal:
               fileSelect();
                break;
            case R.id.tvconfirmisgeshireal:
            if (strpath.equals("")){
                Toast.makeText(this,"请选择正确的文件格式重新验证",Toast.LENGTH_LONG).show();
            }else {
                registerVoice(strpath);
            }
                break;
            default:
                Toast.makeText(this,"请选择正确的文件格式重新验证",Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void fileSelect() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //intent.setType(“image/*”);//选择图片
        //intent.setType(“audio/*”); //选择音频
        //intent.setType(“video/*”); //选择视频 （mp4 3gp 是android支持的视频格式）
        //intent.setType(“video/*;image/*”);//同时选择视频和图片
        intent.setType("*/*");//无类型限制
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 获取当前活动的Activity实例
        //判断是否实现返回值接口
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            Uri uri = data.getData();
            strpath = getPath(this,uri);
            tvwhitchisgeshireal.setText(strpath);

        }
    }
    @SuppressLint("StaticFieldLeak")
    private void registerVoice(final String path) {
        final ProgressDialog dialog = ProgressDialog.show(this,"格式验证","格式验证中...");
        new AsyncTask<Void,String,String>() {
            @Override
            protected String doInBackground(Void ...voids) {
                String json = "";
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                  //  String sdPath = getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath();
                    //File file = new File(sdPath+"/Android/data/com.example.ttlock/Music");
                   // String filePath = "";

                 /*  /for(int i = 0;i<1;i++) {
                        filePath = sdPath + "/" + fileNames[i];
                        File file = new File(filePath);
                        mReocrdFileArray.add(file);
                    }*/
                    mReocrdFileArray.add(new File(path));
                    try {
                        json = ResponseService.verifyVoiceFormat(mReocrdFileArray);
                        if(json == null || json.trim().equals("")) {
                            Toast.makeText(IsGeShiReal.this,"服务器正在维护，请稍后再试",Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(IsGeShiReal.this, PiPeiAcitvity.class);
                            startActivity(intent);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                  Toast.makeText(IsGeShiReal.this, "SD卡没有挂载", Toast.LENGTH_SHORT).show();
                }
                return json;
            }

            @Override
            protected void onPostExecute(String json) {
                super.onPostExecute(json);
                dialog.dismiss();
                String msg = "服务器正在维护，请稍后再试";
                String codefromserver="没有获取成功";
                if(!json.trim().equals("")) {
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        String code = jsonObject.getString("code");
                        if(code != null) {
                            if(code.equals("200")) {
                                msg = "语音验证完成！可以用下载吗下载！";
                                codefromserver=jsonObject.getString("error");
                            } else{
                                msg = jsonObject.getString("error");
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Toast.makeText(IsGeShiReal.this, msg, Toast.LENGTH_SHORT).show();
                 Intent intent = new Intent(IsGeShiReal.this, PcmDownloadActivity.class);
                 intent.putExtra("code",codefromserver);
                startActivity(intent);

            }
        }.execute();
    }
    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if(null!=cursor&&cursor.moveToFirst()){;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
            cursor.close();
        }
        return res;
    }



    @SuppressLint("NewApi")
    public String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
