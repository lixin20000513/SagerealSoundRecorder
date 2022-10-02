package com.sagereal.soundrecorder.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.sagereal.soundrecorder.application.Application;
import com.zlw.main.recorderlib.RecordManager;
import com.zlw.main.recorderlib.recorder.RecordConfig;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RecorderService extends Service {

    private SimpleDateFormat calSdf;
    private Boolean isAlive;
    SharedPreferences sharedPreferences;
    private Boolean isRename = true;
    private String recordMode;
    private int time = 0;


    @SuppressLint("SimpleDateFormat")
    @Override
    public void onCreate() {
        super.onCreate();

        /**
         * @deprecated 初始化录音管理器
         * 参数1： Application 实例
         * 参数2： 是否打印日志
         */
        RecordManager.getInstance().init(Application.getInstance(), false);

        //设置时间格式
        calSdf = new SimpleDateFormat("HH:mm:ss");
    }

    /**
     * 获取录音参数
     * */
    private void getRecordInfo() {
        //获取设置
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //录音结束后是否重命名(true为重命名，false为不重命名)
        isRename = sharedPreferences.getBoolean("prompt_rename", true);
        //录音模式(默认AAC)
        recordMode = sharedPreferences.getString("recording_format", "AAC");
    }

    /**
     * 设置更新Activity的UI界面的回调接口
     */
    public interface OnRefreshUIThreadListener {
        void OnRefresh(String time);
    }

    private OnRefreshUIThreadListener onRefreshUIThreadListener;

    public void setOnRefreshUIThreadListener(OnRefreshUIThreadListener onRefreshUIThreadListener) {
        this.onRefreshUIThreadListener = onRefreshUIThreadListener;
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            time += 1000;
            if (onRefreshUIThreadListener != null) {
                String timeStr = callTime(time);
                onRefreshUIThreadListener.OnRefresh(timeStr);
            }
            return false;
        }
    });

    /**
     * 计算时间为指定格式
     */
    private String callTime(int mSecond) {
        mSecond = 8 * 60 * 60 * 1000;
        String format = calSdf.format(new Date(mSecond));
        return format;
    }

    /**
     * 开启子线程，实时获取录音时间，反馈给主线程
     */
    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (isAlive) {
                handler.sendEmptyMessage(0);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    /**
     * 开始录音
     * */
    public void startRecord() {
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
        //开启子线程
        thread.start();
        isAlive = true;
    }

    /**
     * 停止录音
     */
    public void stopRecord() {
        RecordManager.getInstance().stop();
        time = 0;
        //停止线程
        isAlive = false;
    }


    @Override
    public IBinder onBind(Intent intent) {
       return new RecorderBinder();
    }

    public class RecorderBinder extends Binder {
        public RecorderService getService() {
            return RecorderService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}