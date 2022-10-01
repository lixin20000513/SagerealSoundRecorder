package com.sagereal.soundrecorder.util;


import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtil {
    private static PermissionUtil checkPermissionUtil;
    private PermissionUtil() {}
    //权限请求码
    private final int mRequestCode = 100;
    public interface OnPermissionCallbackListener {
        //权限全部允许
        void onGranted();
        //权限被拒绝
        void OnDenied(List<String> deniedPermissions);
    }
    private OnPermissionCallbackListener mOnPermissionCallbackListener;

    public static PermissionUtil getInstance() {
        if (checkPermissionUtil == null) {
            synchronized (PermissionUtil.class) {
                if (checkPermissionUtil == null) {
                    checkPermissionUtil = new PermissionUtil();
                }
            }
        }
        return checkPermissionUtil;
    }

    public void OnRequestPermission(Activity context, String[] permissions, OnPermissionCallbackListener onPermissionCallbackListener) {
        mOnPermissionCallbackListener = onPermissionCallbackListener;
        //判断手机版本6.0以上需要申请权限
        if(Build.VERSION.SDK_INT >= 23) {
            //创建一个集合，用来存放没有授权的权限
            List<String> mPermissionList = new ArrayList<>();
            //逐个判断权限是否授权
            for (int i = 0; i < permissions.length; i++) {
                int res = ContextCompat.checkSelfPermission(context, permissions[i]);
                if(res != PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permissions[i]);
                }
            }
            //判断集合是否为空，不为空则申请权限
            if(mPermissionList.size() > 0) {
                String[] permissions_arr = mPermissionList.toArray(new String[mPermissionList.size()]);
                ActivityCompat.requestPermissions(context, permissions_arr, mRequestCode);
            }else {
                //权限已经全部授权
                mOnPermissionCallbackListener.onGranted();
            }
        }
    }

    public void onRequestPermissionsResult(Activity context, int requestCode, @NonNull String[] permissions, int[] grantResults) {
        if (requestCode == mRequestCode) {
            List<String> deniedPermissions = new ArrayList<>();
            if (grantResults.length > 0) {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        deniedPermissions.add(permissions[i]);
                    }
                }
            }
            if (deniedPermissions.size() == 0) {
                mOnPermissionCallbackListener.onGranted();
            }else {
                mOnPermissionCallbackListener.OnDenied(deniedPermissions);
            }
        }else {
            //所有权限都已经授权
            mOnPermissionCallbackListener.onGranted();
        }
    }


    /**
     * 提示用户去手动设置权限
     */
    public void showDialogTipUserGotoAppSetting(Activity context) {
        DialogUtil.showNormalDialog(context, "权限不可用", "请在-应用设置-权限-中，允许使用存储权限来保存用户数据", "取消",
                (DialogUtil.OnLeftClickListener) () -> {
                    //取消
                    context.finish();
                }, "确定", (DialogUtil.OnRightClickListener) () -> {
                    //跳转到设置界面
                    StartSystemPageUtil.goToAppSetting(context);
                    context.finish();
                });
    }

}
