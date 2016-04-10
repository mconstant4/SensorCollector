package com.mconstant.android.sensorcollector;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by mconstant on 3/30/16.
 */
public class DataCollectionManagerService extends WearableListenerService {
    private static final String DEBUG_LOG_KEY = "data_collection_manager";

    private static final String PATH_START_SERVICE = "/start_service";
    private static final String PATH_STOP_SERVICE = "/stop_service";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(DEBUG_LOG_KEY, "Service Created");
    }

    @Override
    public void onDestroy() {
        Log.d(DEBUG_LOG_KEY, "Service Destroyed");
        super.onDestroy();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Intent intent = new Intent(this, DataCollectionService.class);
        String path = messageEvent.getPath();
        switch (path) {
            case PATH_START_SERVICE:
                if (!isServiceRunning(DataCollectionService.class)) {
                    startService(intent);
                }
                break;
            case PATH_STOP_SERVICE:
                if (isServiceRunning(DataCollectionService.class)) {
                    stopService(intent);
                }
                break;
            default:
                super.onMessageReceived(messageEvent);
                break;
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
