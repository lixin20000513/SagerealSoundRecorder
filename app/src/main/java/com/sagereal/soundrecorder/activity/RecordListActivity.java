package com.sagereal.soundrecorder.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.sagereal.soundrecorder.R;
import com.sagereal.soundrecorder.databinding.ActivityRecordListBinding;

public class RecordListActivity extends AppCompatActivity {

    ActivityRecordListBinding mBind;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBind = ActivityRecordListBinding.inflate(getLayoutInflater());
        setContentView(mBind.getRoot());

        //设置返回键
        setSupportActionBar(mBind.toolbar);
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setTitle(getString(R.string.record_list));
            bar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
