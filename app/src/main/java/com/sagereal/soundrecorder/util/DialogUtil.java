package com.sagereal.soundrecorder.util;

import android.app.AlertDialog;
import android.content.Context;

public class DialogUtil {

    public interface OnLeftClickListener {
        void onLeftClick();
    }

    public interface OnRightClickListener {
        void onRightClick();
    }

    public static void showNormalDialog(Context context, String title, String msg,
                                        String leftBtn, OnLeftClickListener leftClickListener,
                                        String rightBtn, OnRightClickListener rightClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title).setMessage(msg);
        builder.setNegativeButton(leftBtn, (dialog, which) -> {
            if (leftClickListener != null) {
                leftClickListener.onLeftClick();
            }
        });

        builder.setPositiveButton(rightBtn, (dialog, which) -> {
            if (rightClickListener != null) {
                rightClickListener.onRightClick();
            }
        });
        builder.create().show();
    }

}
