package com.sagereal.soundrecorder.activity;

import static android.service.controls.ControlsProviderService.TAG;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.sagereal.soundrecorder.R;
import com.sagereal.soundrecorder.constant.Constants;
import com.sagereal.soundrecorder.util.PermissionUtil;

import java.io.File;
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
            }
        };
        Timer timer = new Timer();
        timer.schedule(timerTask, Constants.DELAY_TIME);
    }

    PermissionUtil.OnPermissionCallbackListener onPermissionListener = new PermissionUtil.OnPermissionCallbackListener() {
        @Override
        public void onGranted() {
            //判断是否有有应用文件夹，没有则创建
            createAppDir();
            //跳转到主界面
            goMainActivity();
        }

        @Override
        public void OnDenied(List<String> deniedPermissions) {
            PermissionUtil.getInstance().showDialogTipUserGotoAppSetting(WelcomeActivity.this);
        }
    };

    public void createAppDir() {
        //创建应用文件夹
        File recordDir = new File(getFilesDir(), "record");
        //存放路径
        String path = recordDir.getAbsolutePath();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtil.getInstance().onRequestPermissionsResult(this,requestCode, permissions, grantResults);
    }
}
