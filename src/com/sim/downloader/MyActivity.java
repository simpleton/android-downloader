package com.sim.downloader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import com.sim.downloader.intentservice.DownloadFileService;
import com.sim.downloader.intentservice.DownloadReceiver;

public class MyActivity extends Activity {

    private static final String DOWNLOAD_URL = "http://gdown.baidu.com/data/wisegame/784dcbdc9afeed75/baiduditu_523.apk";
    private DownloadReceiver downloadReceiver;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
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
    }
}
