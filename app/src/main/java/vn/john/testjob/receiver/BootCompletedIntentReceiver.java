package vn.john.testjob.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import vn.john.testjob.service.RecorderService;

/**
 * Created by TUNGDX on 10/16/2016.
 */
public class BootCompletedIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Record", "Reboot received");
        Intent i = new Intent(context, RecorderService.class);
        context.startService(i);
    }
}
