package com.example.ttlock.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.ttlock.MyApplication;
import com.example.ttlock.R;
import com.example.ttlock.enumtype.Operation;
import com.example.ttlock.model.BleSession;
import com.example.ttlock.model.Key;
import com.example.ttlock.sp.MyPreference;

import static com.example.ttlock.MyApplication.mTTLockAPI;
import static com.example.ttlock.activity.MainActivity.curKey;

/**
 * 自定义操作类 by jiajinlei
 */
public class UserOperateActivity extends BaseActivity {
    private Key mKey;
    private BleSession bleSession;
    private int openid;
    private Button btn_user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_operate);
        //ButterKnife.bind(this);
        mKey = curKey;
        bleSession = MyApplication.bleSession;
        openid = MyPreference.getOpenid(this, MyPreference.OPEN_ID);
        btn_user = findViewById(R.id.btn_user);
        btn_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTTLockAPI.isConnected(mKey.getLockMac())) {//如果锁已经连接上了，你可以立即调用
                    if(!mKey.isAdmin()) //如果认证为管理员
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
            }
        });
    }
}
