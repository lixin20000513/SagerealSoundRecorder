package com.sagereal.soundrecorder.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sagereal.soundrecorder.R;
import com.sagereal.soundrecorder.entity.AudioFile;

import java.util.ArrayList;
import java.util.List;

public class RecordListAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private List<AudioFile> mAudioFileList = new ArrayList<>();
    private ItemClickListener mItemClickListener;

    public RecordListAdapter(Context context) {
        this.layoutInflater = LayoutInflater.from(context);
    }

    public void setAudioFileList(List<AudioFile> mAudioFileList) {
        this.mAudioFileList = mAudioFileList;
    }

    public void setItemClickListener(ItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    @Override
    public int getCount() {
        Log.e("TAG", "getCount: " + mAudioFileList.size());
        return mAudioFileList.size();
    }

    @Override
    public Object getItem(int position) {
        return mAudioFileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = layoutInflater.inflate(R.layout.audio_listview_item, viewGroup, false);
        }
        Log.e("TAG", "getView: " + view);
        view.findViewById(R.id.list_item).setOnClickListener(v -> {
            if (mItemClickListener != null) {
                mItemClickListener.onClick(position);
            }
        });
        view.findViewById(R.id.list_item).setOnLongClickListener(v -> {
            if (mItemClickListener != null) {
                mItemClickListener.onLongClick(position);
            }
            return true;
        });
        TextView tvName = view.findViewById(R.id.file_name);
        TextView tvSize = view.findViewById(R.id.file_size);
        tvName.setText(mAudioFileList.get(position).getFileName());
        tvSize.setText(mAudioFileList.get(position).getFileSize());
        return view;
    }

    public interface ItemClickListener {

        void onClick(int position);

        //长按事件
        void onLongClick(int position);
    }
}
