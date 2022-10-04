package com.sagereal.soundrecorder.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.sagereal.soundrecorder.R;
import com.sagereal.soundrecorder.databinding.ActivityMainBinding;
import com.sagereal.soundrecorder.entity.AudioFile;
import com.sagereal.soundrecorder.service.RecorderService;
import com.sagereal.soundrecorder.util.FileUtils;
import com.sagereal.soundrecorder.util.PCMToAACUtil;
import com.zlw.main.recorderlib.RecordManager;
import com.zlw.main.recorderlib.recorder.listener.RecordResultListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    ActivityMainBinding mBind;
    SharedPreferences mSharedPreferences;
    private boolean isRecord = false;
    private SimpleDateFormat calSdf;
    private int time;
    final RecordManager recordManager = RecordManager.getInstance();
    private RecorderService recorderService;
    private Dialog mRenameDialog;
    private final byte[] resetByte = new byte[]{};
    private final String resetTime = "00:00:00";

    RecorderService.OnRefreshUIThreadListener refreshUIListener = new RecorderService.OnRefreshUIThreadListener() {
        @Override
        public void OnRefresh(String time) {
            mBind.time.setText(time);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void clearUI() {
            mBind.time.setText("00:00:00");
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
        bindService(intent, connection, BIND_AUTO_CREATE);
        //设置标题
        setSupportActionBar(mBind.toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(getString(R.string.app_name));
        }
        mRenameDialog = new Dialog(this, R.style.dialogStyle);
        mRenameDialog.setCancelable(false);
        mRenameDialog.setCanceledOnTouchOutside(false);
        Window window = mRenameDialog.getWindow();
        window.setGravity(Gravity.BOTTOM);      //位于底部
        window.setWindowAnimations(R.style.dialog_share);    //弹出动画
        View inflate = View.inflate(this, R.layout.dialog_rename, null);
        window.setContentView(inflate);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        //初始化
        click();
    }

    private void click() {
        mBind.record.setOnClickListener(view -> {
            if (!isRecord) {
                //开始录音
                mBind.record.setImageResource(R.drawable.ic_stop);
                recorderService.startRecord();
                //音频可视化数据监听
                recordManager.setRecordFftDataListener(data -> mBind.audioView.setWaveData(data));

                recordManager.setRecordResultListener(new RecordResultListener() {
                    @Override
                    public void onResult(File result) {
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                        boolean isRename = sharedPreferences.getBoolean("prompt_rename", true);
                        String path = result.getAbsolutePath();
                        String[] names = path.split("/");
                        String nameWithFormat = names[names.length - 1];
                        String name = nameWithFormat.split("\\.")[0];
                        if (isRename) {
                            if (mRenameDialog != null) {
                                EditText edtName = mRenameDialog.findViewById(R.id.name);
                                edtName.setText(name);
                                TextView commit = mRenameDialog.findViewById(R.id.commit);
                                TextView delete = mRenameDialog.findViewById(R.id.delete);
                                commit.setOnClickListener(view1 -> {
                                    String newName = edtName.getText().toString().trim();
                                    FileUtils.renameFile(result, newName);
                                    mBind.time.setText(resetTime);
                                    mBind.audioView.setWaveData(resetByte);
                                    mRenameDialog.dismiss();
                                    Toast.makeText(MainActivity.this,
                                            String.format("录音创建成功\n%s", newName), Toast.LENGTH_SHORT).show();
                                });
                                delete.setOnClickListener(view1 -> {
                                    FileUtils.deleteFile(result);

                                    mBind.time.setText(resetTime);
                                    mBind.audioView.setWaveData(resetByte);
                                    mRenameDialog.dismiss();
                                });
                                if (!mRenameDialog.isShowing()) {
                                    mRenameDialog.show();
                                }
                            }
                        } else {
                            Toast.makeText(MainActivity.this,
                                    String.format("录音创建成功\n%s", name), Toast.LENGTH_SHORT).show();
                            mBind.time.setText(resetTime);
                            mBind.audioView.setWaveData(resetByte);
                        }
                    }
                });
                isRecord = true;
            } else {
                //停止录音
                mBind.record.setImageResource(R.drawable.ic_record);
                recorderService.stopRecord();
                isRecord = false;
            }
        });

        //录音列表
        mBind.items.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, RecordListActivity.class)));

        //设置
        mBind.settings.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解绑服务
        if (connection != null) {
            unbindService(connection);
        }
    }

}