package com.sim.downloader;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.sim.downloader.downloadmanager.FileDownloadManager;
import com.sim.downloader.intentservice.DownloadFileService;
import com.sim.downloader.intentservice.DownloadReceiver;

public class MyActivity extends Activity {
    private static final String TAG = MyActivity.class.getSimpleName();
    private static final String DOWNLOAD_URL = "http://gdown.baidu.com/data/wisegame/784dcbdc9afeed75/baiduditu_523.apk";
    private DownloadReceiver downloadReceiver;
    private TextView downloadManagerTv;
    private DownloadManager downloadManager;
    private long downloadFileId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        Button btnIntentService = (Button) findViewById(R.id.test_intent_service);
        btnIntentService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyActivity.this, DownloadFileService.class);
                intent.putExtra("url", DOWNLOAD_URL);
                downloadReceiver = new DownloadReceiver(new Handler(), MyActivity.this);
                intent.putExtra("receiver", downloadReceiver);
                startService(intent);
            }
        });

        Button btnFileDownloadManager = (Button) findViewById(R.id.fileDownloadManager);
        btnFileDownloadManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadFileId = FileDownloadManager.getInstacen().downloadFile(MyActivity.this, DOWNLOAD_URL);
                pollFileDownloadManagerProgress(3000);
            }
        });

        downloadManagerTv = (TextView) findViewById(R.id.downloadManager_text);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FileDownloadManager.getInstacen().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FileDownloadManager.getInstacen().unregister(this);
    }
    int lastSize;
    public void pollFileDownloadManagerProgress(final long period) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int current = queryDownloadManagerProcess(downloadFileId);
                if (current != lastSize) {
                    pollFileDownloadManagerProgress(period);
                }
                lastSize = current;
            }
        }, period);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public int queryDownloadManagerProcess(long id) {
        DownloadManager.Query q = new DownloadManager.Query();
        q.setFilterById(id);
        Cursor cursor = null;
        int bytesDownloaded;
        try {
            cursor = downloadManager.query(q);
            cursor.moveToFirst();
            bytesDownloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            Log.d(TAG, "queryDownloadManagerProcess :" + bytesDownloaded);
        } finally {
            if (cursor != null && !cursor.isClosed())
                cursor.close();
        }
        return bytesDownloaded;
    }
}
