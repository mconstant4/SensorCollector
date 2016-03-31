package com.mconstant.android.sensorcollector;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by mconstant on 3/30/16.
 */
public class DataCollectionManagerService extends WearableListenerService {
    private static final String PATH_START_SERVICE = "/start_service";
    private static final String PATH_STOP_SERVICE = "/stop_service";
    private static final String PATH_SERVICE_STOPPED = "/service_stopped";

    /**
     * isServiceRunning method checks if DataCollectionService is running and returns true if
     * it is, false otherwise.
     * @return boolean, true if DataCollectionService is running
     */
    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("com.mconstant.android.sensorcollector.DataCollectionService".equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * onMessageReceived is called when component receives a message. In this service, messages are
     * used to start and stop DataCollectionService.
     * @param messageEvent MessageEvent containing message path, source node ID, and data.
     */
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        if (path.equals(PATH_START_SERVICE)) {
            if (!isServiceRunning()) {
                Intent intent = new Intent(this, DataCollectionService.class);
                startService(intent);
            }
        } else if (path.equals(PATH_STOP_SERVICE)) {
            if (isServiceRunning()) {
                Intent intent = new Intent(this, StopDataCollectionService.class);
                startService(intent);
            }
        }
    }
}
