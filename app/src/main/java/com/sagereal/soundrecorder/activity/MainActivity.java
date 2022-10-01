package com.sagereal.soundrecorder.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.sagereal.soundrecorder.R;
import com.sagereal.soundrecorder.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {

    ActivityMainBinding mBind;
    private boolean isRecord = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());

        //设置标题
        setSupportActionBar(mBind.toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(getString(R.string.app_name));
        }
        click();
    }

    private void click() {
        mBind.record.setOnClickListener(view -> {
            if (isRecord) {
                mBind.record.setImageResource(R.drawable.ic_record);
                isRecord = false;
            } else {
                mBind.record.setImageResource(R.drawable.ic_stop);
                isRecord = true;
            }
        });

        mBind.items.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, RecordListActivity.class)));

        mBind.settings.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)));
    }
}