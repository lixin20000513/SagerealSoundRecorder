package com.sagereal.soundrecorder.activity;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sagereal.soundrecorder.R;
import com.sagereal.soundrecorder.constant.Constants;
import com.sagereal.soundrecorder.util.DialogUtil;
import com.sagereal.soundrecorder.util.PermissionUtil;
import com.sagereal.soundrecorder.util.StartSystemPageUtil;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WelcomeActivity extends AppCompatActivity {

    //动态获取权限
    String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        getPermission();
    }

    private void getPermission() {
        PermissionUtil.getInstance().OnRequestPermission(this, permissions, onPermissionListener);
    }

    private void goMainActivity() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                finish();
            }
        };
        Timer timer = new Timer();
        timer.schedule(timerTask, Constants.DELAY_TIME);
    }

    PermissionUtil.OnPermissionCallbackListener onPermissionListener = new PermissionUtil.OnPermissionCallbackListener() {
        @Override
        public void onGranted() {
            //跳转到主界面
            goMainActivity();
        }

        @Override
        public void OnDenied(List<String> deniedPermissions) {
            //提示用户手动设置权限
            PermissionUtil.getInstance().showDialogTipUserGotoAppSetting(WelcomeActivity.this);
        }
    };
}
