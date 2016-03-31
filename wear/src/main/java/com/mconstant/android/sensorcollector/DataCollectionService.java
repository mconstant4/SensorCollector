package com.mconstant.android.sensorcollector;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by mconstant on 3/30/16.
 */
public class DataCollectionService extends Service implements MessageApi.MessageListener, DataApi.DataListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String DEBUG_LOG_KEY = "data_collection";

    private static final String PATH_START_SERVICE = "/start_service";
    private static final String PATH_STOP_SERVICE = "/stop_service";
    private static final String PATH_REQUEST_STATE = "/request_state";
    private static final String PATH_NOTIFY_STATE_0 = "/notify_state_0";
    private static final String PATH_NOTIFY_STATE_1 = "/notify_state_1";
    private static final String PATH_NOTIFY_STATE_2 = "/notify_state_2";
    private static final String PATH_NEW_STATE_0 = "/new_state_0";
    private static final String PATH_NEW_STATE_1 = "/new_state_1";
    private static final String PATH_NEW_STATE_2 = "/new_state_2";
    private static final String PATH_SERVICE_STOPPED = "/service_stopped";

    private int mState;
    private GoogleApiClient mGoogleApiClient;


    @Override
    public void onCreate() {
        super.onCreate();

        int notificationId = 001;
        // Build intent for notification content
        Intent viewIntent = new Intent(this, StopDataCollectionService.class);
        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(this, 0, viewIntent, 0);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.close_button)
                        .setContentTitle("Service Running")
                        .setContentText("Open to stop service")
                        .setOngoing(true)
                        .setContentIntent(viewPendingIntent);

        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        // Build the notification and issues it with notification manager.
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.cancel(001);
        mGoogleApiClient.disconnect();
    }

    /**
     * onStartCommand method is called when DataCollectionManagerService starts it.
     *
     * @param intent Intent passed by DataCollectionManagerService to start this service
     * @param flags Integer either 0 (normal start), START_FLAG_REDELIVERY, or START_FLAG_RETRY
     * @param startID Integer uniquely identifying the specific request to start
     * @return START_STICKYbecause this service should be restarted as soon as possible if
     * the OS destroys it.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .build();
        mGoogleApiClient.connect();

        return START_STICKY;
    }

    /**
     * onBind method is required by Service class.
     * @param intent Intent passed to bind with this service
     * @return NULL since no components are meant to bind to this service
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String getStatePath() {
        switch (mState) {
            case 0:
                return PATH_NOTIFY_STATE_0;
            case 1:
                return PATH_NOTIFY_STATE_1;
            case 2:
                return PATH_NEW_STATE_2;
            default:
                return null;
        }
    }

    /**
     * notifyState method is called when this service wants to notify all nodes of a state change.
     * Usually called when node sends message requesting the current state of this service or
     * this service changes its state.
     */
    private void notifyState() {
        sendMessage(getStatePath());
    }

    /**
     * updateState method is called when this state should be changed locally.
     * @param state State to changeto
     */
    private void updateState(int state) {
        mState = state;
    }

    /**
     * updateStateAndNotify method is called when the service changes state. It calls updateState
     * and then calls notifyState.
     * @param state
     */
    private void updateStateAndNotify(int state) {
        updateState(state);
        notifyState();
    }

    /*
    MessageApi.MessageListener Callback methods
     */

    /**
     * onMessageReceived method is called when this service receives a new message. This Application
     * uses messages to communicate state changes.
     * @param messageEvent MessageEvent containing the message path, source node ID, and data
     */
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        if (path.equals(PATH_REQUEST_STATE)) {
            sendMessage(getStatePath());
        } else if (path.equals(PATH_NOTIFY_STATE_0)) {
            updateState(0);
        } else if (path.equals(PATH_NOTIFY_STATE_1)) {
            updateState(1);
        } else if (path.equals(PATH_NOTIFY_STATE_2)) {
            updateState(2);
        }
    }

    /**
     * sendMessage method sends a message to all connected nodes with the specified path and
     * payload.
     * @param path String used by MessageListeners in this Application to determine what the
     *             message is for (e.g. notify components of state change, request
     *             current state, etc).
     */
    private void sendMessage(final String path) {
        if (mGoogleApiClient.isConnected()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                    for(Node node : nodes.getNodes()) {
                        MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path, path.getBytes()).await();
                        if(result.getStatus().isSuccess()){
                            Log.d(DEBUG_LOG_KEY, "Message sent to: " + node.getDisplayName());
                        } else {
                            Log.d(DEBUG_LOG_KEY, "Error sending message");
                        }
                    }
                }
            }).start();
        }
    }

    /*
    DataApi.DataListener Callback methods
     */

    /**
     * onDataChanged method is called when the Service is notified of a set of DataItems have
     * been changed or deleted. This Application uses DataItems to transfer Sensor data.
     * @param dataEvents Data structure holding references to a set of DataEvents
     */
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

    }

    /*
    GoogleApiClient Callback methods
     */

    /**
     * onConnected method is called when the GoogleApiClient is connected to Google Play Services
     * @param connectionHint Bundle that is always null in this Application
     */
    @Override
    public void onConnected(Bundle connectionHint) {

    }

    /**
     * onConnectionSuspended method is called when GoogleApiClient's connection to
     * Google Play Services is temporally interrupted
     * @param cause Integer representing the cause of the interruption
     */
    @Override
    public void onConnectionSuspended(int cause) {

    }

    /**
     * onConnectionFailed method is called when GoogleApiClient fails to connect with
     * Google Play Services. If this happens, this Activity can not communicate with the
     * DataCollectionService.
     * @param result ConnectionResult containing the error. This can be used toresolve the
     *               issue, however it is not used in this Activity.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {

    }
}
