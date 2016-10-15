package vn.john.testjob;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.ContextCompat;

import java.io.File;

import vn.john.testjob.service.UploadService;

public class Utils {

    public static boolean hasExternalStorage() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    public static File getRootFolder() {
        File videoFolder = new File(Environment.getExternalStorageDirectory(), "TestJob");
        if (!videoFolder.exists()) {
            videoFolder.mkdir();
        }
        return videoFolder;
    }

    public static File getUploadFolder() {
        File videoFolder = getRootFolder();
        File uploadFolder = new File(videoFolder, "upload");
        if (!uploadFolder.exists()) {
            uploadFolder.mkdir();
        }
        return uploadFolder;
    }

    public static void moveFileToUploadFolder(File inFile) {
        File to = new File(Utils.getUploadFolder(), inFile.getName());
        inFile.renameTo(to);
    }


    public static File getVideoFile() {
        return new File(getRootFolder(), System.currentTimeMillis() + Config.FILE_TYPE);
    }

    public static boolean hasEnoughPermissions(Context context) {
        int permission = ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasWifiConnection(Context context) {
        ConnectivityManager connManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo current = connManager.getActiveNetworkInfo();
        return current != null && current.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static void startUploadService(Context context, boolean checkVideoLength) {
        Intent intent = new Intent(context, UploadService.class);
        intent.putExtra(UploadService.CHECK_VIDEO_LENGTH_EXTRA, checkVideoLength);
        context.startService(intent);
    }

    public static long getVideoDuration(Context context, String path) {
        MediaPlayer mMediaPlayer = null;
        long duration = 0;
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(context, Uri.parse(path));
            mMediaPlayer.prepare();
            duration = mMediaPlayer.getDuration();
        } catch (Exception e) {
            if (e != null)
                e.printStackTrace();
        } finally {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }
        return duration;
    }
}
