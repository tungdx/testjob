package vn.john.testjob.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;

import vn.john.testjob.Config;
import vn.john.testjob.R;
import vn.john.testjob.Utils;

public class RecorderService extends Service implements SurfaceHolder.Callback, MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener {
    private static final String TAG = RecorderService.class.getSimpleName();
    /**
     * It is used to check recording status
     */
    public static boolean mRecordingStatus;

    private WindowManager windowManager;
    private SurfaceView surfaceView;
    private MediaRecorder mMediaRecorder = null;

    /**
     * This override method is called when first instance of RecordServices is made and use to create layout for camera video recording.
     */
    @Override
    public void onCreate() {
        if (!Utils.hasEnoughPermissions(getApplicationContext())) {
            stopSelf();
            return;
        }
        try {
            windowManager = (WindowManager) this
                    .getSystemService(Context.WINDOW_SERVICE);
            surfaceView = new SurfaceView(this);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
            layoutParams.height = 1;
            layoutParams.width = 1;
            windowManager.addView(surfaceView, layoutParams);
            surfaceView.getHolder().addCallback(this);

            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setOnInfoListener(this);
            mMediaRecorder.setOnErrorListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mRecordingStatus) {
            showToast(getString(R.string.already_running));
        }
        return super.onStartCommand(intent, flags, startId);
    }


    /**
     * Callback method which gets called when sufaceholder is create.
     * As surfaceHolder is created it initializes MediaRecorder and starts recording front camera video.
     *
     * @param surfaceHolder
     */
    @Override
    public void surfaceCreated(final SurfaceHolder surfaceHolder) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                setupSurface(surfaceHolder);
            }
        }).start();

    }

    /**
     * Initialize MediaRecorder to open and start recording front camera video
     *
     * @param surfaceHolder
     */
    private void setupSurface(SurfaceHolder surfaceHolder) {
        initRecorder();
        startRecorder();
        showToast(getString(R.string.record_running));
    }

    private void initRecorder() {
        mMediaRecorder
                .setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mMediaRecorder
                .setVideoSource(MediaRecorder.VideoSource.DEFAULT);
        CamcorderProfile cpHigh = CamcorderProfile
                .get(CamcorderProfile.QUALITY_HIGH);
        mMediaRecorder.setProfile(cpHigh);
        mMediaRecorder.setOutputFile(Utils.getVideoFile().getPath());
        mMediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());
        mMediaRecorder.setMaxFileSize(Config.FILE_SIZE_LIMIT);
    }

    private void startRecorder() {
        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            mRecordingStatus = true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
            showToast(getString(R.string.record_failed));
        }
    }

    // Stop recording and remove SurfaceView
    @Override
    public void onDestroy() {
        try {
            mRecordingStatus = false;
            if (mMediaRecorder != null) {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                mMediaRecorder.release();
            }
            windowManager.removeView(surfaceView);
            showToast(getString(R.string.record_stopped));
            Utils.startUploadService(getApplicationContext(), false);
        } catch (Exception e) {
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format,
                               int width, int height) {
        Log.i("Record", "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.i("Record", "surfaceDestroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void showToast(final String msg) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RecorderService.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        if (MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED == what) {
            Utils.startUploadService(getApplicationContext(), true);
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            initRecorder();
            startRecorder();
        }
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder = null;
        stopSelf();
    }
}

