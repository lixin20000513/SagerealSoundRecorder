package com.sagereal.soundrecorder.util;


import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.os.Build;
import androidx.fragment.app.FragmentActivity;
import com.permissionx.guolindev.PermissionX;
import com.sagereal.soundrecorder.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionUtil {
    private static PermissionUtil checkPermissionUtil;
    private PermissionUtil() {}
    Dialog permissionDialog;
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

    public void OnRequestPermission(FragmentActivity context, String[] permissions, OnPermissionCallbackListener onPermissionCallbackListener) {
        mOnPermissionCallbackListener = onPermissionCallbackListener;
        //判断手机版本6.0以上需要申请权限
        if(Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            PermissionX.init(context).permissions(permissions).request((allGranted, grantedList, deniedList) -> {
                if (allGranted) {
                    mOnPermissionCallbackListener.onGranted();
                } else {
                    mOnPermissionCallbackListener.OnDenied(deniedList);
                }
            });
        } else if (Build.VERSION.SDK_INT >=  Build.VERSION_CODES.R) {
            //获取文件管理权限
            List<String> newPermissions = new ArrayList<>(Arrays.asList(permissions));
            newPermissions.add(0, Manifest.permission.MANAGE_EXTERNAL_STORAGE);
            PermissionX.init(context).permissions(newPermissions).onExplainRequestReason((scope, deniedList) -> {
                String msg = "需要获取该权限才能使用此app";
                scope.showRequestReasonDialog(deniedList, msg, "允许", "拒绝");
            }).request((allGranted, grantedList, deniedList) -> {
                if (allGranted) {
                    mOnPermissionCallbackListener.onGranted();
                } else {
                    mOnPermissionCallbackListener.OnDenied(deniedList);
                }
            });
        }
    }

    /**
     * 提示用户去手动设置权限
     */
    public void showDialogTipUserGotoAppSetting(Activity context) {
        permissionDialog = DialogUtil.getNormalDialog(context,context.getString(R.string.permission_not_available),
                context.getString(R.string.jump_save_permission),
                context.getString(R.string.cancel),
                () -> {
                    permissionDialog.dismiss();
                    context.finish();
                },
                context.getString(R.string.confirm),
                () -> {
                    //跳转到设置界面
                    StartSystemPageUtil.goToAppSetting(context);
                    permissionDialog.dismiss();
                    context.finish();
                });
        if (!permissionDialog.isShowing()) {
            permissionDialog.show();
        }
    }


}
