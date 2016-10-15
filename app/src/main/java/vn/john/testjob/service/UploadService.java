package vn.john.testjob.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;

import vn.john.testjob.Config;
import vn.john.testjob.Utils;
import vn.john.testjob.auth.AuthenCallback;
import vn.john.testjob.auth.Authenticator;
import vn.john.testjob.auth.FirebaseAuthen;

/**
 * Created by TUNGDX on 10/15/2016.
 */
public class UploadService extends Service {
    private static final String TAG = "Upload service";
    public static final String CHECK_VIDEO_LENGTH_EXTRA = "check_video_length_extra";

    private Uploader uploader;
    private Authenticator authenticator;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final File videoFolder = Utils.getRootFolder();
        final boolean checkVideoLength = intent == null || intent.getBooleanExtra(CHECK_VIDEO_LENGTH_EXTRA, true);
        if (!videoFolder.exists() && !Utils.hasEnoughPermissions(getApplicationContext()) && Utils.hasWifiConnection(getApplicationContext())) {
            return 0;
        }
        getAuthenticator().authen(new AuthenCallback() {
            @Override
            public void onSucceed() {
                performUploadTask(videoFolder, checkVideoLength);
            }

            @Override
            public void onFailed() {
                //ignored at current stage
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    private void performUploadTask(File videoFolder, boolean checkVideoLength) {
        Log.i(TAG, "Upload Task is running");
        File[] videosFile = videoFolder.listFiles();
        for (File video : videosFile) {
            if (!video.getName().endsWith(Config.FILE_TYPE)) {
                return;
            }
            if (checkVideoLength && video.length() < Config.FILE_SIZE_LIMIT - Config.FILE_SIZE_LIMIT * 0.1) {
                return;
            }
            //upload file
            getUploader().upload(video, new UploadCallback() {
                @Override
                public void onSucceed(File file) {
                    Log.i(TAG, String.format("Upload file %s done", file.getName()));
                    //delete file to save memory
                    file.delete();
                }

                @Override
                public void onFailed(File file) {
                    Log.e(TAG, String.format("Upload file %s failed", file.getName()));
                    //ignored at current stage
                }
            });
        }
    }

    private Uploader getUploader() {
        if (uploader == null) {
            uploader = new FirebaseUploader();
        }
        return uploader;
    }

    private Authenticator getAuthenticator() {
        if (authenticator == null) {
            authenticator = new FirebaseAuthen();
        }
        return authenticator;
    }
}