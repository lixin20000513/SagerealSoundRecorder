package com.sagereal.soundrecorder.activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.sagereal.soundrecorder.R;
import com.sagereal.soundrecorder.application.Application;
import com.sagereal.soundrecorder.constant.Constants;
import com.sagereal.soundrecorder.databinding.ActivityMainBinding;
import com.sagereal.soundrecorder.service.RecorderService;
import com.zlw.main.recorderlib.RecordManager;
import com.zlw.main.recorderlib.recorder.RecordConfig;

import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    ActivityMainBinding mBind;
    SharedPreferences sharedPreferences;
    private boolean isRecord = true;
    private Boolean isRename = true;
    private String recordMode;
    private SimpleDateFormat calSdf;
    private int time;
    final RecordManager recordManager = RecordManager.getInstance();
    private RecorderService recorderService;

    RecorderService.OnRefreshUIThreadListener refreshUIListener = new RecorderService.OnRefreshUIThreadListener() {
        @Override
        public void OnRefresh(String time) {
            mBind.time.setText(time);
        }
    };

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RecorderService.RecorderBinder binder = (RecorderService.RecorderBinder) service;
            recorderService = binder.getService();
            recorderService.setOnRefreshUIThreadListener(refreshUIListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };




    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        Intent intent = new Intent(this, RecorderService.class);
        bindService(intent,connection,BIND_AUTO_CREATE);


        /**
         * @deprecated 初始化录音管理器
         * 参数1： Application 实例
         * 参数2： 是否打印日志
         */
        //RecordManager.getInstance().init(Application.getInstance(), false);

        //设置时间格式
        //calSdf = new SimpleDateFormat("HH:mm:ss");


        //设置标题
        setSupportActionBar(mBind.toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(getString(R.string.app_name));
        }

        //初始化
        click();
    }


//    /**
//     * 设置更新Activity的UI界面的回调接口
//     */
//    public interface OnRefreshUIThreadListener {
//        void OnRefresh(String time);
//    }
//
//    private OnRefreshUIThreadListener onRefreshUIThreadListener;
//
//    public void setOnRefreshUIThreadListener(OnRefreshUIThreadListener onRefreshUIThreadListener) {
//        this.onRefreshUIThreadListener = onRefreshUIThreadListener;
//    }
//
//    Handler handler = new Handler(new Handler.Callback() {
//        @Override
//        public boolean handleMessage(@NonNull Message message) {
//            time += 1000;
//            if (onRefreshUIThreadListener != null) {
//                String timeStr = callTime(time);
//                onRefreshUIThreadListener.OnRefresh(timeStr);
//                mBind.time.setText(timeStr);
//            }
//            return false;
//        }
//    });
//
//    /**
//     * 计算时间为指定格式
//     */
//    private String callTime(int mSecond) {
//        mSecond = 8 * 60 * 60 * 1000;
//        return calSdf.format(new Date(mSecond));
//    }
//
//    /**
//     * 开启子线程，实时获取录音时间，反馈给主线程
//     */
//    Thread thread = new Thread(new Runnable() {
//        @Override
//        public void run() {
//            handler.sendEmptyMessage(0);
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//    });

    /**
     * 获取录音参数
     */
    private void getRecordInfo() {
        //获取设置
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
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
                recorderService.startRecord();
                //startRecord();
                //音频可视化数据监听
                recordManager.setRecordFftDataListener(data -> mBind.audioView.setWaveData(data));
                isRecord = false;
            } else {
                //停止录音
                mBind.record.setImageResource(R.drawable.ic_stop);
                recorderService.stopRecord();
                //重置录音时间
                mBind.time.setText(Constants.DEFAULT_TIME);
                //stopRecord();
                isRecord = true;
            }
        });

        //录音列表
        mBind.items.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, RecordListActivity.class)));

        //设置
        mBind.settings.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));
    }

//    /**
//     * 开始录音
//     */
//    private void startRecord() {
//        //获取录音参数
//        getRecordInfo();
//        //设置录音模式
//        switch (recordMode) {
//            case "AAC":
//                RecordManager.getInstance().changeFormat(RecordConfig.RecordFormat.PCM);
//                break;
//            case "WAV":
//                RecordManager.getInstance().changeFormat(RecordConfig.RecordFormat.WAV);
//                break;
//            case "MP3":
//                RecordManager.getInstance().changeFormat(RecordConfig.RecordFormat.MP3);
//                break;
//            default:
//                break;
//        }
//        //开始录音
//        RecordManager.getInstance().start();
//        thread.start();
//    }
//
//    /**
//     * 停止录音
//     */
//    private void stopRecord() {
//        RecordManager.getInstance().stop();
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解绑服务
        if (connection != null) {
            unbindService(connection);
        }
    }

}