package com.mconstant.android.sensorcollector;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;

public class MainActivity extends AppCompatActivity implements MessageApi.MessageListener, DataApi.DataListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

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

            }
        });
    }

    /**
     * requestState method sends a message requesting the current state of DataCollectionService.
     * Usually, this is used when GoogleApiClient has connected to Google Play Services.
     */
    private void requestState() {

    }

    /**
     * updateState method updates the state locally on this component. Usually, this called
     * when the component receives a message with path PATH_NOTIFY_STATE.
     * @param state Integer to change the current state (mState) to.
     */
    private void updateState(int state) {

    }

    /**
     * updateStateAndNotify method calls updateState and then sends a message notifying
     * DataCollectionService of state change. This method is usually called when the user toggles
     * the state using the toggle state button.
     * @param state
     */
    private void updateStateAndNotify(int state) {

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


    }

    /*
    DataApi.DataListener Callback methods
     */

    /**
     * onDataChanged method is called when the Activity is notified of a set of DataItems have
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
