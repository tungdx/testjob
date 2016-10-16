package vn.john.testjob;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import vn.john.testjob.service.RecorderService;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 100;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkOverLayPermission();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkOverLayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.enable_drawover_permisison));
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        startActivity(intent);
                    }
                });
                builder.setCancelable(false);
                builder.show();
            }
        }
    }

    private void startRecorderService() {
        Intent intent = new Intent(MainActivity.this, RecorderService.class);
        startService(intent);
    }

    public void onStartRecord(View view) {
        if (!validateExternalStorage()) {
            return;
        }
        if (Utils.hasEnoughPermissions(this)) {
            startRecorderService();
        } else {
            requestPermissions();
        }
    }

    public void onStopRecord(View view) {
        Intent intent = new Intent(MainActivity.this, RecorderService.class);
        stopService(intent);
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

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                REQUEST_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Utils.hasEnoughPermissions(this)) {
            startRecorderService();
        }
    }
}