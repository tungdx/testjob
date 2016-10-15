package vn.john.testjob;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ToggleButton;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback, MediaRecorder.OnInfoListener {
    private static final int REQUEST_PERMISSION = 100;

    private MediaRecorder recorder;
    private SurfaceHolder holder;
    private boolean recording = false;
    private File recordFile;
    long lastTimeFileReached = 0;

    private ToggleButton toggleButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recorder = new MediaRecorder();

        SurfaceView cameraView = (SurfaceView) findViewById(R.id.surface);
        initHolder(cameraView);
        toggleButton = (ToggleButton) findViewById(R.id.control);
        toggleButton.setOnClickListener(this);
    }

    private void initHolder(SurfaceView surfaceView) {
        holder = surfaceView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    private void initRecorder() {
        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

        CamcorderProfile cpHigh = CamcorderProfile
                .get(CamcorderProfile.QUALITY_HIGH);
        recorder.setProfile(cpHigh);
        recordFile = Utils.getVideoFile();
        recorder.setOutputFile(recordFile.getPath());
        recorder.setMaxFileSize(Config.FILE_SIZE_LIMIT);
        recorder.setOnInfoListener(this);
    }

    private void prepareRecorder() {
        recorder.setPreviewDisplay(holder.getSurface());
        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.control:
                if (!validateExternalStorage()) {
                    return;
                }
                if (recording) {
                    recorder.stop();
                    recorder.reset();
                    recording = false;

                } else {
                    if (hasPermissions()) {
                        initRecorder();
                        prepareRecorder();
                        recording = true;
                        recorder.start();
                    } else {
                        toggleButton.setChecked(false);
                        requestPermissions();
                    }
                }
                break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            if (recording) {
                recorder.stop();
                recording = false;
            }
            recorder.release();
        } catch (Exception ignored) {

        }
        finish();
    }

    private boolean validateExternalStorage() {
        if (!Utils.hasExternalStorage()) {
            showAlertDialog(getString(R.string.sd_required));
            return false;
        }
        return true;
    }

    private void showAlertDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setPositiveButton(getString(R.string.ok), null);
        builder.show();
    }

    private boolean hasPermissions() {
        int camera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int recordAudio = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return camera == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED && recordAudio == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                REQUEST_PERMISSION);
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        if (MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED == what) {
            long current = System.currentTimeMillis();
            long threshold = current - lastTimeFileReached;
            if (threshold < 5 * 1000) {
                return;
            }
            lastTimeFileReached = current;
            Log.i("Record", "file size reached");
            recorder.stop();
            recorder.reset();
            initRecorder();
            prepareRecorder();
            recorder.start();
            Utils.startUploadService(getApplicationContext(), true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.startUploadService(getApplicationContext(), false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (hasPermissions() && toggleButton != null) {
            toggleButton.setChecked(true);
            initRecorder();
            prepareRecorder();
            recording = true;
            recorder.start();
        }
    }
}