package vn.john.testjob.service;

import android.app.Service;
import android.content.Intent;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import vn.john.testjob.Utils;

/**
 * Created by TUNGDX on 10/15/2016.
 */
public class RecordService extends Service implements MediaRecorder.OnInfoListener {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    MediaRecorder recorder;
    File file;
    boolean recording;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        recorder = new MediaRecorder();
        initRecorder();
        if (!Utils.hasExternalStorage()) {
            return 0;
        }
        prepareRecorder();
        recorder.start();
        return super.onStartCommand(intent, flags, startId);
    }

    private void initRecorder() {
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

        CamcorderProfile cpHigh = CamcorderProfile
                .get(CamcorderProfile.QUALITY_HIGH);
        recorder.setProfile(cpHigh);
        file = Utils.getVideoFile();
        recorder.setOutputFile(file.getPath());
        recorder.setMaxFileSize(1 * 1000000); // 20 megabytes
        recorder.setOnInfoListener(this);
    }

    private void prepareRecorder() {
        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        if (MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED == what) {
            Log.i("Record", "file size reached");
            recorder.stop();
            recorder.reset();
            initRecorder();
            prepareRecorder();
            recorder.start();
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    File f = file;
//                    Utils.moveFileToUploadFolder2(file);
//                }
//            }).start();
//            Utils.startUploadService(getApplicationContext());
        }
    }
}
