package com.mconstant.android.sensorcollector;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends AppCompatActivity implements DataApi.DataListener, MessageApi.MessageListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    private static final String DEBUG_LOG_KEY = "mobile_main_activity";

    private static final int STATE_CONNECTING = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_COLLECTING = 1;
    private static final int STATE_WAITING = 2;

    private static final String PATH_START_SERVICE = "/start_service";
    private static final String PATH_STOP_SERVICE = "/stop_service";
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

    private TextView mCurrentStateTextview;
    private Button mToggleStateBtn;
    private int mState;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(DEBUG_LOG_KEY, "Activity Created");

        mCurrentStateTextview = (TextView) findViewById(R.id.current_state_textview);
        mToggleStateBtn = (Button) findViewById(R.id.toggle_state_btn);

        updateState(STATE_CONNECTING);

        mToggleStateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mState == STATE_IDLE) {
                    sendMessage(PATH_UPDATE_STATE_1);
                    updateState(STATE_COLLECTING);
                } else {
                    sendMessage(PATH_UPDATE_STATE_0);
                    updateState(STATE_IDLE);
                }
            }
        });

        //Build GoogleApiClient with Wearable API
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Connect Google Api Client
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();

        sendMessage(PATH_START_SERVICE);
    }

    @Override
    protected void onPause() {
        Log.d(DEBUG_LOG_KEY, "Activity Paused");
        sendMessage(PATH_STOP_SERVICE);

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();

        super.onDestroy();
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
                        Log.d(DEBUG_LOG_KEY, "Message sent to " + node.getDisplayName() + "(" + path + ")");
                    }
                }
            }).start();
        }
    }

    private void updateState(int state) {
        switch (state) {
            case STATE_CONNECTING:
                //State: Disconnected from Google Play Services
                mCurrentStateTextview.setText(R.string.state_connecting);
                mToggleStateBtn.setText(R.string.btn_state_connecting);
                mToggleStateBtn.setEnabled(false);

                break;

            case STATE_IDLE:
                //State: Idle
                mCurrentStateTextview.setText(R.string.state_0);
                mToggleStateBtn.setText(R.string.btn_state_0);
                mToggleStateBtn.setEnabled(true);

                break;

            case STATE_COLLECTING:
                //State: Collecting data from watch
                mCurrentStateTextview.setText(R.string.state_1);
                mToggleStateBtn.setText(R.string.btn_state_1);
                mToggleStateBtn.setEnabled(true);

                break;

            case STATE_WAITING:
                //State: Waiting to collect data
                mCurrentStateTextview.setText(R.string.state_2);
                mToggleStateBtn.setText(R.string.btn_state_2);
                mToggleStateBtn.setEnabled(true);

                break;
        }

        mState = state;
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(DEBUG_LOG_KEY, "Message Received (" + messageEvent.getPath() + ")");
        String path = messageEvent.getPath();
        if (path.equals(PATH_CURRENT_STATE_0)) {
            updateState(0);
        } else if (path.equals(PATH_CURRENT_STATE_1)) {
            updateState(1);
        } else if (path.equals(PATH_CURRENT_STATE_2)) {
            updateState(2);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(DEBUG_LOG_KEY, "Google Api Client connected to Google Play Services");
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        sendMessage(PATH_START_SERVICE);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(DEBUG_LOG_KEY, "Google Api Client connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(DEBUG_LOG_KEY, "Google Api Client connection failed");
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().equals(ACCELEROMETER_DATA_ITEM)) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    float xAcceleration = dataMap.getFloat(ACCELEROMETER_X_KEY);
                    float yAcceleration = dataMap.getFloat(ACCELEROMETER_Y_KEY);
                    float zAcceleration = dataMap.getFloat(ACCELEROMETER_Z_KEY);
                    long timestamp = dataMap.getLong(TIMESTAMP_KEY);
                    Log.d(DEBUG_LOG_KEY, "X: " + xAcceleration + ", Y: " + yAcceleration + ", Z: " + zAcceleration);
                }
            }
        }
    }
}
