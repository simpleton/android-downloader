package com.sim.downloader.IntentService;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.sim.downloader.IntentService.DownloadReceiver;
import com.sim.downloader.MyActivity;
import com.sim.downloader.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class DownloadFileService extends IntentService {
    private static final String TAG = DownloadFileService.class.getSimpleName();
    public static final String BUNDLE_URL = "url";
    public static final String BUNDLE_RECEIVER = "receiver";
    private ResultReceiver receiver;

    public DownloadFileService() {
        super("com.sim.DownloadFileService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("Something Download")
                .setContentText("Download in progress")
                .setSmallIcon(R.drawable.ic_launcher);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,new Intent(this, MyActivity.class), 0);
        mBuilder.setContentIntent(contentIntent);
        startForeground(DownloadReceiver.UPDATE_PROGRESS_CODE, mBuilder.build());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        String downloadUrls = bundle.getString(BUNDLE_URL);
        receiver = (ResultReceiver) intent.getParcelableExtra(BUNDLE_RECEIVER);

        File dirs = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download");
        if (!dirs.exists()) {
            dirs.mkdirs();
        }
        File file = new File(dirs, "file11.apk");

        downloadFile(downloadUrls, file);
    }

    private int downloadLength;
    private int lastProgress;
    private void downloadFile(String downloadUrl, File file) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "can not fond output folder");
            e.printStackTrace();
            return;
        }
        InputStream ips = null;
        try {
            URL url = new URL(downloadUrl);
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setRequestMethod("GET");
            huc.setReadTimeout(10000);
            huc.setConnectTimeout(3000);
            int fileLength = Integer.valueOf(huc.getHeaderField("Content-Length"));
            ips = huc.getInputStream();
            int responseCode = huc.getResponseCode();
            if (responseCode == 200) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = ips.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                    downloadLength = downloadLength + len;
                    Bundle resultData = new Bundle();
                    int currentProgress = downloadLength * 100 / fileLength;
                    if (currentProgress > lastProgress) {
                        resultData.putInt(DownloadReceiver.PROGRESS, currentProgress);
                        receiver.send(DownloadReceiver.UPDATE_PROGRESS_CODE, resultData);
                        lastProgress = currentProgress;
                    }
                }
            } else {
                Log.e(TAG, "Server return code:" + responseCode);
            }

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fos.getFD().sync();
                if (fos != null) {
                    fos.close();
                }
                if (ips != null) {
                    ips.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
