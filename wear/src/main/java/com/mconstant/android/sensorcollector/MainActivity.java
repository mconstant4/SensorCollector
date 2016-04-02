package com.mconstant.android.sensorcollector;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends Activity implements MessageApi.MessageListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private static final String DEBUG_LOG_KEY = "wear_main_activity";

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

    /**
     * Textview displaying the current state of this activity
     */
    private TextView mCurrentStateTextview;

    /**
     * Button that toggles state of service
     */
    private Button mToggleStateBtn;

    /**
     * Integer that stores the current state of the DataCollectionService
     */
    private int mState;

    /**
     * GoogleApiClient used to communicate with Google Play Services. This Activity uses it to
     * send messages and DataItems.
     */
    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        Bind member variables to their corresponding View
         */
        mCurrentStateTextview = (TextView) findViewById(R.id.current_state_textview);
        mToggleStateBtn = (Button) findViewById(R.id.toggle_state_btn);

        mToggleStateBtn.setOnClickListener(new View.OnClickListener() {
            /**
             * This button toggles the state of the DataCollectionService when it is clicked.
             *
             * If the current state is STATE_CONNECTING, this button is disabled
             * If the current state is STATE_IDLE, this button changes state to STATE_COLLECTING
             * If the current state is STATE_COLLECTING, this button changes state to STATE_WAITING
             * If the current state is STATE_WAITING, this button changes state to CTATE_COLLECTING
             * @param v View of button that was clicked.
             */
            @Override
            public void onClick(View v) {
                if (mState == 0) {
                    updateStateAndNotify(1);
                } else {
                    updateStateAndNotify(0);
                }
            }
        });

        /*
        Add Wearable Api to GoogleApiClient
         */
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    /**
     * When this component is resumed, it connects the GoogleApiClient to Google Play Services.
     */
    @Override
    protected void onResume() {
        super.onResume();

        mGoogleApiClient.connect();
    }

    /**
     * When this component is paused, it disconnects from Google Play Services.
     */
    @Override
    protected void onPause() {
        super.onPause();

        Wearable.MessageApi.removeListener(mGoogleApiClient, this);

        mGoogleApiClient.unregisterConnectionCallbacks(this);
        mGoogleApiClient.unregisterConnectionFailedListener(this);
        mGoogleApiClient.disconnect();
    }


    /**
     * requestState method sends a message requesting the current state of DataCollectionService.
     * Usually, this is used when GoogleApiClient has connected to Google Play Services.
     */
    private void requestState() {
        sendMessage(PATH_REQUEST_STATE);
    }

    /**
     * updateState method updates the state locally on this component. Usually, this called
     * when the component receives a message with path PATH_NOTIFY_STATE.
     * @param state Integer to change the current state (mState) to.
     */
    private String updateState(int state) {
        String statePath;
        switch (state) {
            case -1:
                //Connecting State
                mToggleStateBtn.setText(R.string.btn_state_connecting);
                mToggleStateBtn.setEnabled(false);

                mCurrentStateTextview.setText(R.string.state_connecting);

                statePath = null;
                break;
            case 0:
                //Idle State
                mToggleStateBtn.setText(R.string.btn_state_0);
                mToggleStateBtn.setEnabled(true);

                mCurrentStateTextview.setText(R.string.state_0);

                statePath = PATH_NOTIFY_STATE_0;
                break;
            case 1:
                //Collecting State
                mToggleStateBtn.setText(R.string.btn_state_1);
                mToggleStateBtn.setEnabled(true);

                mCurrentStateTextview.setText(R.string.state_1);

                statePath = PATH_NOTIFY_STATE_1;
                break;
            case 2:
                //Idle State
                mToggleStateBtn.setText(R.string.btn_state_2);
                mToggleStateBtn.setEnabled(true);

                mCurrentStateTextview.setText(R.string.state_2);

                statePath = PATH_NOTIFY_STATE_2;
                break;
            default:
                statePath = null;
                break;
        }
        mState = state;

        return statePath;
    }

    /**
     * updateStateAndNotify method calls updateState and then sends a message notifying
     * DataCollectionService of state change. This method is usually called when the user toggles
     * the state using the toggle state button.
     * @param state
     */
    private void updateStateAndNotify(int state) {
        sendMessage(updateState(state));
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
                        Log.d(DEBUG_LOG_KEY, "Found node " + node.getDisplayName());
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
    MessageApi.MessageListener Callback methods
     */

    /**
     * onMessageReceived method is called when thisnode receives a new message. This Application
     * uses messages to communicate state changes.
     * @param messageEvent MessageEvent containing the message path, source node ID, and data
     */
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        if (path.equals(PATH_NOTIFY_STATE_0)) {
            updateState(0);
        } else if (path.equals(PATH_NOTIFY_STATE_1)) {
            updateState(1);
        } else if (path.equals(PATH_NOTIFY_STATE_2)) {
            updateState(2);
        } else if (path.equals(PATH_SERVICE_STOPPED)) {
            //Service has been stopped
        }
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
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        sendMessage(PATH_REQUEST_STATE);
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
