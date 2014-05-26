package com.sim.downloader.IntentService;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.sim.downloader.MyActivity;
import com.sim.downloader.R;

/**
 * Created by simsun on 2014/5/26.
 */
public class DownloadReceiver extends ResultReceiver {
    private static final String TAG = DownloadReceiver.class.getSimpleName();
    public static final int UPDATE_PROGRESS_CODE = 0x1231;
    public static final String PROGRESS = "progress";

    private final NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;

    public DownloadReceiver(Handler handler, Context context) {
        super(handler);
        mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle("Something Download")
                .setContentText("Download in progress")
                .setSmallIcon(R.drawable.ic_launcher);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,new Intent(context, MyActivity.class), 0);
        mBuilder.setContentIntent(contentIntent);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);
        if (resultCode == UPDATE_PROGRESS_CODE) {
            int progress = resultData.getInt(PROGRESS);
            Log.d(TAG, "onReceiveResult" + progress);
            if (progress < 100) {
                mBuilder.setProgress(100, progress, false)
                        .setContentText("Download in progress");
            } else {
                mBuilder.setProgress(0, 0, false)
                        .setContentText("Download Complete");
            }
            mNotifyManager.notify(UPDATE_PROGRESS_CODE, mBuilder.build());
        }
    }
}
