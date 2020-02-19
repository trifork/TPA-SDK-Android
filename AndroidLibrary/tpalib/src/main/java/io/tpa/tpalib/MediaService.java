package io.tpa.tpalib;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import io.tpa.tpalib.android.R;

public class MediaService extends Service {

    private static final int NOTIFICATION_ID = 23943;

    private static boolean running = false;

    static boolean isRunning() {
        return running;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        startForeground();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        running = false;

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startForeground() {
        Context context = getApplicationContext();
        Notification notification = Notifications.createNotification(context, context.getString(R.string.feedback_media_server_title), context.getString(R.string.feedback_media_server_text));

        startForeground(NOTIFICATION_ID, notification);

        running = true;
    }
}
