package com.sim.downloader.downloadmanager;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

/**
 * Created by simsun on 2014/5/27.
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class FileDownloadManager {
    private static final String TAG = FileDownloadManager.class.getSimpleName();
    private long enqueue;
    private DownloadManager dm = null;

    BroadcastReceiver receiver;
    private FileDownloadManager() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (dm == null) return;
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long downloadId = intent.getLongExtra(
                            DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(enqueue);
                    Cursor cursor = null;
                    try {
                        cursor = dm.query(query);
                        if (cursor != null) {
                            if (cursor.moveToFirst()) {
                                int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                                if (DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(columnIndex)) {
                                    String uriString = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                                    Log.e(TAG, "download finished:" + uriString +"\n file path:" + Uri.parse(uriString).getPath());
                                }
                            }
                        }
                    } finally {
                        if (cursor != null && !cursor.isClosed()) {
                            cursor.close();
                        }
                    }
                }
            }
        };
    }
    private static FileDownloadManager fileDownloadManager;

    public static FileDownloadManager getInstacen() {
        if (fileDownloadManager == null) {
            synchronized (FileDownloadManager.class) {
                fileDownloadManager = new FileDownloadManager();
            }
        }
        return fileDownloadManager;
    }

    public void register(Context context) {
        context.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public void unregister(Context context) {
        context.unregisterReceiver(receiver);
    }

    public long downloadFile(Context context ,String url) {
        if (dm == null) {
            dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        }
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        enqueue = dm.enqueue(request);

        return enqueue;
    }

}
