package com.mconstant.android.sensorcollector;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class StopDataCollectionService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {

        Intent intent1 = new Intent(this, DataCollectionService.class);
        stopService(intent);
        stopSelf();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
