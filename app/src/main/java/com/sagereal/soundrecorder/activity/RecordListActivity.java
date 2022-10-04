package com.sagereal.soundrecorder.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.preference.PreferenceManager;

import com.sagereal.soundrecorder.R;
import com.sagereal.soundrecorder.adapter.RecordListAdapter;
import com.sagereal.soundrecorder.databinding.ActivityRecordListBinding;
import com.sagereal.soundrecorder.entity.AudioFile;
import com.sagereal.soundrecorder.util.DialogUtil;
import com.sagereal.soundrecorder.util.FileUtils;
import com.sagereal.soundrecorder.util.PCMToAACUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class RecordListActivity extends AppCompatActivity {

    public static final String TAG = "RecordListActivity";
    ActivityRecordListBinding mBind;
    List<AudioFile> mAudioList;
    RecordListAdapter mAdapter;
    String directoryFilePath;
    Dialog deleteDialog;
    Dialog playDialog;
    boolean isPlay = false;
    MediaPlayer mMediaPlayer;
    int process = 0;
    AppCompatSeekBar seekBar;
    TextView tvCurrentTime;
    private ListView mListView;

    private final RecordListAdapter.ItemClickListener itemClickListener = new RecordListAdapter.ItemClickListener() {
        @Override
        public void onClick(int position) {
            playAudio(position);
        }

        @Override
        public void onLongClick(int position) {
            deleteAudio(position);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityRecordListBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());
        directoryFilePath = String.format(Locale.getDefault(), "%s/Record",
                Environment.getExternalStorageDirectory().getAbsolutePath());
        mAudioList = FileUtils.getFilesFormDirectory(directoryFilePath);
        //pcm转aac(临时方案)
        mAudioList.forEach( mAudioFile -> {
            if (mAudioFile.getFileName().endsWith(".pcm")) {
                //pcm转aac
                String pcmPath =  Environment.getExternalStorageDirectory() + "/Record/"+mAudioFile.getFileName();
                String aacPath = pcmPath.replace(".pcm", ".aac");
                PCMToAACUtil pcmToAACUtil =  new PCMToAACUtil(aacPath, pcmPath);
                File pcmFile = new File(pcmPath);
                //生成aac文件
                pcmToAACUtil.readInputStream(pcmFile);
                //删除pcm文件
                FileUtils.deleteFile(pcmFile);
                //更新文件名
                mAudioFile.setFileName(mAudioFile.getFileName().replace(".pcm", ".aac"));
            }
        });
        setAdapter();
        //设置返回键
        setSupportActionBar(mBind.toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(getString(R.string.record_list));
            bar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setAdapter() {
        mBind.listItem.setEmptyView(mBind.noRecordFound);
        mAdapter = new RecordListAdapter(this);
        mAdapter.setAudioFileList(mAudioList);
        mAdapter.setItemClickListener(itemClickListener);
        mBind.listItem.setAdapter(mAdapter);
    }

    private void playAudio(int position) {
        Log.e(TAG, "playAudio: 位置======>" + position);

        String filePath = String.format(Locale.CHINA, "%s%s%s",
                directoryFilePath, "/", mAudioList.get(position).getFileName());
        mMediaPlayer = new MediaPlayer();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        try { ///初始化播放器，并开始播放
            mMediaPlayer.reset();
            if (sharedPreferences.getBoolean("earpiece_mode", false)) {
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.setSpeakerphoneOn(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
                    //3.0以上可以直接设置
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                } else {
                    //3.0以下需要通过设置声音路由来模拟
                    audioManager.setMode(AudioManager.MODE_IN_CALL);
                }
                mMediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                        .setLegacyStreamType(AudioManager.STREAM_VOICE_CALL).build());
                //使用完后需要恢复默认设置
                audioManager.setMode(AudioManager.MODE_NORMAL);
            }
            mMediaPlayer.setDataSource(filePath);
            mMediaPlayer.prepare();
            mMediaPlayer.seekTo(process);
            mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                process = 0;
                mediaPlayer.seekTo(process);
                ///将mediaPlayer初始化封装，写在此处即可循环播放
            }
        });

        playDialog = new Dialog(this, R.style.dialogStyle);
        playDialog.setCancelable(false);
        playDialog.setCanceledOnTouchOutside(false);
        Window window = playDialog.getWindow();
        window.setGravity(Gravity.CENTER);//设置dialog显示在中间
        window.setWindowAnimations(R.style.dialog_share);///添加dialog动画
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        View inflate = View.inflate(this, R.layout.dialog_play_audio, null);
        window.setContentView(inflate);
        //设置宽度小于屏幕
        window.setLayout(dm.widthPixels-140, WindowManager.LayoutParams.WRAP_CONTENT);
        TextView tvName = inflate.findViewById(R.id.file_name);
        tvName.setText(mAudioList.get(position).getFileName());
        tvCurrentTime = inflate.findViewById(R.id.current_time);
        TextView tvTotalTime = inflate.findViewById(R.id.total_time);
        int totalTime = mMediaPlayer.getDuration() / 1000;
        tvTotalTime.setText(String.format("%s:%s", totalTime/60, totalTime%60));
        inflate.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
                process = 0;
                playDialog.dismiss();
                isPlay = false;
                deleteAudio(position);
            }
        });
        inflate.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                process = 0;
                playDialog.dismiss();
                isPlay = false;
                if (mMediaPlayer != null) {
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
            }
        });

        seekBar = inflate.findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ///滑动seekbar同时更改mediplayer的播放进度
                process = seekBar.getProgress();
                if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    mMediaPlayer.seekTo(process);
                }
            }
        });
        if (!playDialog.isShowing() && !isPlay) {
            playDialog.show();
            isPlay = true;
        }
        ///handle内部设置延迟, 每0.5秒获取mediaplayer的播放进度，同步显示到seekbar
        handle.sendEmptyMessage(1);
    }

    private void deleteAudio(int position) {
        String name = mAudioList.get(position).getFileName();
        deleteDialog = DialogUtil.getNormalDialog(this, "删除录音",
                String.format("确定删除录音文件:%s", name), "取消",
                () -> deleteDialog.dismiss(), "确定", () -> {
                    String filePath = String.format(Locale.CHINA, "%s%s%s",
                            directoryFilePath, "/", name);
                    FileUtils.deleteFile(new File(filePath));
                    mAudioList.remove(position);
                    setAdapter();
                    deleteDialog.dismiss();
                });
        if (!deleteDialog.isShowing()) {
            deleteDialog.show();
        }
    }

    private final Handler handle = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1 && mMediaPlayer != null && seekBar != null){
                process = mMediaPlayer.getCurrentPosition();
                int total = mMediaPlayer.getDuration();
                seekBar.setMax(total);
                seekBar.setProgress(process);
                int currentTime = process/1000;
                tvCurrentTime.setText(String.format("%s:%s/",currentTime/60,currentTime%60));
                //设置延迟，并且通知本身(假循环)
                handle.sendEmptyMessageDelayed(1, 500);
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
