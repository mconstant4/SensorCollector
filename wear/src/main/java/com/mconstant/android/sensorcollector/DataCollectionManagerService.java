package com.mconstant.android.sensorcollector;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by mconstant on 3/30/16.
 */
public class DataCollectionManagerService extends WearableListenerService {

    /**
     * isServiceRunning method checks if DataCollectionService is running and returns true if
     * it is, false otherwise.
     * @return boolean, true if DataCollectionService is running
     */
    private boolean isServiceRunning() {

        return false;
    }

    /**
     * onMessageReceived is called when component receives a message. In this service, messages are
     * used to start and stop DataCollectionService.
     * @param messageEvent MessageEvent containing message path, source node ID, and data.
     */
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

    }
}
