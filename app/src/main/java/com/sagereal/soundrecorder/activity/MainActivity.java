package com.sagereal.soundrecorder.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import com.sagereal.soundrecorder.R;
import com.sagereal.soundrecorder.application.Application;
import com.sagereal.soundrecorder.databinding.ActivityMainBinding;
import com.zlw.main.recorderlib.RecordManager;
import com.zlw.main.recorderlib.recorder.RecordConfig;
import com.zlw.main.recorderlib.recorder.listener.RecordFftDataListener;


public class MainActivity extends AppCompatActivity {

    ActivityMainBinding mBind;
    SharedPreferences sharedPreferences;
    private boolean isRecord = true;
    private Boolean soundSource = false;
    private Boolean isRename = true;
    private String recordMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());

        /**
         * @deprecated 初始化录音管理器
         * 参数1： Application 实例
         * 参数2： 是否打印日志
         */
        RecordManager.getInstance().init(Application.getInstance(), false);


        //设置标题
        setSupportActionBar(mBind.toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(getString(R.string.app_name));
        }

        //初始化
        click();
    }

    /**
     * 获取录音参数
     * */
    private void getRecordInfo() {
        //获取设置
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //音源(false为麦克风，true为听筒)
         soundSource = sharedPreferences.getBoolean("handset_mode", true);
        //录音结束后是否重命名(true为重命名，false为不重命名)
         isRename = sharedPreferences.getBoolean("prompt_rename", true);
        //录音模式(默认AAC)
         recordMode = sharedPreferences.getString("recording_format", "AAC");
    }

    private void click() {
        mBind.record.setOnClickListener(view -> {
            if (isRecord) {
                //开始录音
                mBind.record.setImageResource(R.drawable.ic_record);
                startRecord();
                isRecord = false;
            } else {
                //停止录音
                mBind.record.setImageResource(R.drawable.ic_stop);
                stopRecord();
                isRecord = true;
            }
        });

        //录音列表
        mBind.items.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, RecordListActivity.class)));

        //设置
        mBind.settings.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));
    }

    /**
     * 开始录音
     * */
    private void startRecord() {
        //获取录音参数
        getRecordInfo();
        //设置录音模式
        switch (recordMode) {
            case "AAC":
                RecordManager.getInstance().changeFormat(RecordConfig.RecordFormat.PCM);
                break;
            case "WAV":
                RecordManager.getInstance().changeFormat(RecordConfig.RecordFormat.WAV);
                break;
            case "MP3":
                RecordManager.getInstance().changeFormat(RecordConfig.RecordFormat.MP3);
                break;
            default:
                break;
        }
        //开始录音
        RecordManager.getInstance().start();
    }

    /**
     * 停止录音
     * */
    private void stopRecord() {
        RecordManager.getInstance().stop();
    }

}