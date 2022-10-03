package com.sagereal.soundrecorder.util;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.sagereal.soundrecorder.entity.AudioFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FileUtils {

    public static final String[] SIZE_TYPE = {"B", "KB", "MB"};
    public static final int SIZE_B = 8;
    //设置可以输出显示0.99M
    public static final int SIZE_JUDGEMENT = 1000;
    public static final int SIZE_UNIT = 1024;
    public static final int SIZE_MB = 8 * 1024 * 1024;

    private final static String TAG = "FileUtils";

    /**
     * 删除文件
     *
     * @param file
     * @return
     */
    public static boolean deleteFile(@NonNull File file) {
        if (!file.exists()) {
            Log.e(TAG, "deleteFile: 文件不存在");
            return true;
        } else {
            if (file.isFile()) {
                return file.delete();
            } else {
                Log.e(TAG, "deleteFile: 需要删除的是文件夹");
                return false;
            }
        }
    }

    public static boolean renameFile(@NonNull File file, String newName) {
        String path = file.getAbsolutePath();
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(newName)) {
            Log.e(TAG, "renameFile: 文件路径或新名称为空");
            return false;
        }
        String parentPath = getParentFilePath(file);
        File newFile = new File(parentPath + newName + "." + getFileFormat(file));
        return file.renameTo(newFile);
    }

    public static List<AudioFile> getFilesFormDirectory(String directoryPath) {
        List<AudioFile> fileList = new ArrayList<>();
        File file = new File(directoryPath);
        File[] tempFiles = file.listFiles();
        if (tempFiles != null && tempFiles.length > 0) {
            for (File tempFile : tempFiles) {
                if (tempFile.isFile()) {
                    AudioFile audioFile = new AudioFile();
                    audioFile.setFileName(tempFile.getName());
                    audioFile.setFileSize(getFileSize(tempFile));
                    fileList.add(audioFile);
                }
            }
        }
        return fileList;
    }

    public static String getParentFilePath(@NonNull File file) {
        String absolutePath = file.getAbsolutePath();
        String[] splitPath = absolutePath.split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < splitPath.length - 1; i++) {
            sb.append(splitPath[i]).append("/");
        }
        return sb.toString();
    }

    private static String getFileFormat(@NonNull File file) {
        String absolutePath = file.getAbsolutePath();
        String[] splitPath = absolutePath.split("\\.");
        return splitPath[splitPath.length - 1];
    }

    private static String getFileSize(@NonNull File file) {
        long sizeByte = file.length();
        sizeByte /= SIZE_B;
        int i = 0;
        while (SIZE_JUDGEMENT <= sizeByte) {
            sizeByte /= SIZE_UNIT;
            i++;
        }
        return String.format(Locale.CHINA, "%d%s", sizeByte, SIZE_TYPE[i]);
    }
}
