package com.mconstant.android.sensorcollector;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;

/**
 * Created by mconstant on 3/30/16.
 */
public class DataCollectionService extends Service implements MessageApi.MessageListener, DataApi.DataListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private int mState;
    private GoogleApiClient mGoogleApiClient;

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

    /**
     * notifyState method is called when this service wants to notify all nodes of a state change.
     * Usually called when node sends message requesting the current state of this service or
     * this service changes its state.
     * @param state State to change to.
     */
    private void notifyState(int state) {

    }

    /**
     * updateState method is called when this state should be changed locally.
     * @param state State to changeto
     */
    private void updateState(int state) {

    }

    /**
     * updateStateAndNotify method is called when the service changes state. It calls updateState
     * and then calls notifyState.
     * @param state
     */
    private void updateStateAndNotify(int state) {

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
