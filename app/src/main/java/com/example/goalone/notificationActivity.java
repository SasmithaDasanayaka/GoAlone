package com.example.goalone;

import static android.media.RingtoneManager.TYPE_NOTIFICATION;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class notificationActivity extends AppCompatActivity {

    private final static String default_notification_channel_id = "default";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        Button btnCreateNotification = findViewById(R.id. btnCreateNotification );
        btnCreateNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                MediaPlayer mp = MediaPlayer.create(getApplicationContext(), notificationSound);
                mp.start();
                NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(notificationActivity.this, default_notification_channel_id)
                        .setSmallIcon(R.drawable. ic_launcher_foreground )
                        .setContentTitle( "Test" )
                        .setContentText( "Hello! This is my first push notification" );
                NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE );
                mNotificationManager.notify(( int ) System. currentTimeMillis () ,
                        mBuilder.build());
            }
        });
    }
}