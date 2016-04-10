package com.mconstant.android.sensorcollector;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.sql.Timestamp;

public class DataCollectionService extends Service implements SensorEventListener, MessageApi.MessageListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    private static final String DEBUG_LOG_KEY = "data_collection_service";

    private static final int STATE_IDLE = 0;
    private static final int STATE_COLLECTING = 1;
    private static final int STATE_WAITING = 2;

    private static final String PATH_REQUEST_STATE = "/request_state";
    private static final String PATH_UPDATE_STATE_0 = "/update_0";
    private static final String PATH_UPDATE_STATE_1 = "/update_1";
    private static final String PATH_UPDATE_STATE_2 = "/update_2";
    private static final String PATH_CURRENT_STATE_0 = "/state_0";
    private static final String PATH_CURRENT_STATE_1 = "/state_1";
    private static final String PATH_CURRENT_STATE_2 = "/state_2";

    private static final String TIMESTAMP_KEY = "timestamp";
    private static final String ACCELEROMETER_DATA_ITEM = "/accelerometer";
    private static final String ACCELEROMETER_X_KEY = "x_acceleration";
    private static final String ACCELEROMETER_Y_KEY = "y_acceleration";
    private static final String ACCELEROMETER_Z_KEY = "z_acceleration";

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private GoogleApiClient mGoogleApiClient;
    private int mState;
    private CountDownTimer mCollectionTimer = new CountDownTimer(2000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            updateStateAndNotify(STATE_WAITING);
        }
    };
    private CountDownTimer mBreakTimer = new CountDownTimer(10000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            updateStateAndNotify(STATE_COLLECTING);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int startID, int flags) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(DEBUG_LOG_KEY, "Service Created");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .build();
        mGoogleApiClient.connect();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public void onDestroy() {
        Log.d(DEBUG_LOG_KEY, "Service Destroyed");

        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    private void updateState(int state) {
        switch (state) {
            case STATE_IDLE:
                mSensorManager.unregisterListener(this);
                mCollectionTimer.cancel();
                mBreakTimer.cancel();
                break;
            case STATE_COLLECTING:
                mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                mCollectionTimer.start();
                break;
            case STATE_WAITING:
                mSensorManager.unregisterListener(this);
                mBreakTimer.start();
                break;
        }
        mState = state;
    }

    private void updateStateAndNotify(int state) {
        updateState(state);
        sendMessage(getStatePath());
    }

    private String getStatePath() {
        switch (mState) {
            case STATE_IDLE:
                return PATH_CURRENT_STATE_0;
            case STATE_COLLECTING:
                return PATH_CURRENT_STATE_1;
            case STATE_WAITING:
                return PATH_CURRENT_STATE_2;
            default:
                return PATH_CURRENT_STATE_0;
        }
    }

    private void sendMessage(final String path) {
        Log.d(DEBUG_LOG_KEY, "Attempting to send message");
        if (mGoogleApiClient.isConnected()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                    for (Node node : nodes.getNodes()) {
                        Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path, path.getBytes()).await();
                        Log.d(DEBUG_LOG_KEY, "Message sent to " + node.getDisplayName());
                    }
                }
            }).start();
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(DEBUG_LOG_KEY, "Message Received (" + messageEvent.getPath() + ")");
        String path = messageEvent.getPath();
        switch (path) {
            case PATH_UPDATE_STATE_0:
                updateState(STATE_IDLE);
                break;
            case PATH_UPDATE_STATE_1:
                updateState(STATE_COLLECTING);
                break;
            case PATH_REQUEST_STATE:
                sendMessage(getStatePath());
                break;
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(DEBUG_LOG_KEY, "GoogleApiClient Connected");
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        updateStateAndNotify(STATE_IDLE);
    }

    @Override
    public void onConnectionSuspended(int cause) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(DEBUG_LOG_KEY, "Connection Failed");
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        float xAcceleration = event.values[0];
        float yAcceleration = event.values[1];
        float zAcceleration = event.values[2];
        long time = new Timestamp(System.currentTimeMillis()).getTime();

        sendAccelerometerData(xAcceleration, yAcceleration, zAcceleration, time);
    }

    private void sendAccelerometerData(float xAcc, float yAcc, float zAcc, long time) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(ACCELEROMETER_DATA_ITEM);

        putDataMapRequest.getDataMap().putFloat(ACCELEROMETER_X_KEY, xAcc);
        putDataMapRequest.getDataMap().putFloat(ACCELEROMETER_Y_KEY, yAcc);
        putDataMapRequest.getDataMap().putFloat(ACCELEROMETER_Z_KEY, yAcc);
        putDataMapRequest.getDataMap().putLong(TIMESTAMP_KEY, time);

        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {

                    }
                });
    }
}
