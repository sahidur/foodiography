package me.sahidur.foodiography.main.pedometer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import me.sahidur.foodiography.R;

public class Pedometer extends AppCompatActivity {
    private static final String TAG = "Pedometer";

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private CoordinatorLayout rootLayout;
    private NavigationView mNavigationView;
    private Toolbar mToolbar;
    private TextView mStepValueView;
    private TextView mPaceValueView;
    private TextView mDistanceValueView;
    private TextView mSpeedValueView;
    private TextView mCaloriesValueView;

    private int mStepValue;
    private int mPaceValue;
    private float mDistanceValue;
    private float mSpeedValue;
    private int mCaloriesValue;

    private boolean isPedometerService = false; // set to true when PedometerService is running

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "[ACTIVITY] onCreate");
        super.onCreate(savedInstanceState);

        mStepValue = 0;
        mPaceValue = 0;

        setContentView(R.layout.activity_pedometer);
        initInstances();
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "[ACTIVITY] onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "[ACTIVITY] onResume");
        super.onResume();

        startPedometerService();
        bindPedometerService();

        // TODO: use a listener or something
        if(!PedometerSettings.getIsMetric()) {
            TextView mDistanceUnitView  = (TextView) findViewById(R.id.distance_unit);
            TextView mSpeedUnitView     = (TextView) findViewById(R.id.speed_unit);
            mDistanceUnitView.setText(R.string.distance_unit_imperial);
            mSpeedUnitView.setText(R.string.speed_unit_imperial);
        }
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "[ACTIVITY] onPause");

        if (!PedometerSettings.getShouldRunInBackground()) {
            unbindPedometerService();
            stopPedometerService();
        }

        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "[ACTIVITY] onStop");
        stopPedometerService();
        super.onStop();
    }

    protected void onDestroy() {
        Log.i(TAG, "[ACTIVITY] onDestroy");

        // clear things up on destroy if enable background running
        if(PedometerSettings.getShouldRunInBackground()) {
            unbindPedometerService();
            stopPedometerService();
        }

        super.onDestroy();
    }

    protected void onRestart() {
        Log.i(TAG, "[ACTIVITY] onRestart");
        super.onRestart();
    }

    private void initInstances() {
        mToolbar           = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mStepValueView     = (TextView) findViewById(R.id.step_value);
        mPaceValueView     = (TextView) findViewById(R.id.pace_value);
        mDistanceValueView = (TextView) findViewById(R.id.distance_value);
        mSpeedValueView    = (TextView) findViewById(R.id.speed_value);
        mCaloriesValueView = (TextView) findViewById(R.id.calories_value);
        rootLayout         = (CoordinatorLayout) findViewById(R.id.rootLayout);



        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }


    private PedometerService mPedometerService;

    private ServiceConnection mPedometerConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mPedometerService = ((PedometerService.PedometerBinder)service).getService();
            mPedometerService.registerCallback(mCallback);
        }

        public void onServiceDisconnected(ComponentName className) {
            mPedometerService = null;
        }
    };

    private void startPedometerService() {
        if (!isPedometerService) {
            Log.v(TAG, "[PedometerService] Start");
            isPedometerService = true;
            startService(new Intent(Pedometer.this, PedometerService.class));
        }
    }

    private void bindPedometerService() {
        Log.i(TAG, "[PedometerService] Bind");
        bindService(
                new Intent(Pedometer.this, PedometerService.class),
                mPedometerConnection,
                Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
    }

    private void unbindPedometerService() {
        Log.i(TAG, "[PedometerService] Unbind");
        unbindService(mPedometerConnection);
    }

    private void stopPedometerService() {
        Log.i(TAG, "[PedometerService] Stop");
        if (mPedometerService != null) {
            Log.i(TAG, "[PedometerService] stopService");
            stopService(new Intent(Pedometer.this, PedometerService.class));
            isPedometerService = false;
        }
    }


    private void shareData () {
        Intent mSharingIntent = new Intent(Intent.ACTION_SEND);
        String mShareBody = new StringBuilder()
                .append("My pedometer data\n")
                .append("Steps: ").append(mStepValue).append("\n")
                .append("Calories: ").append(mCaloriesValue).append(" kJ\n")
                .append("Distance: ").append(mDistanceValue).append(" km\n")
                .append("Current Speed: ").append(mSpeedValue).append(" km/h\n")
                .append("Pace: ").append(mPaceValue).append(" steps/min\n")
                .toString();
        mSharingIntent.setType("text/plain");
        mSharingIntent.putExtra(
                android.content.Intent.EXTRA_SUBJECT, "Pedometer");
        mSharingIntent.putExtra(
                android.content.Intent.EXTRA_TEXT, mShareBody);
        startActivity(Intent.createChooser(mSharingIntent, "Share via"));
    }

    // TODO: unite all into 1 type of message
    private PedometerService.ICallback mCallback = new PedometerService.ICallback() {
        public void stepsChanged(int value) {
            mHandler.sendMessage(mHandler.obtainMessage(STEPS_MSG, value, 0));
        }
        public void paceChanged(int value) {
            mHandler.sendMessage(mHandler.obtainMessage(PACE_MSG, value, 0));
        }
        public void distanceChanged(float value) {
            mHandler.sendMessage(mHandler.obtainMessage(DISTANCE_MSG, (int)(value*1000), 0));
        }
        public void speedChanged(float value) {
            mHandler.sendMessage(mHandler.obtainMessage(SPEED_MSG, (int)(value*1000), 0));
        }
        public void caloriesChanged(float value) {
            mHandler.sendMessage(mHandler.obtainMessage(CALORIES_MSG, (int)(value), 0));
        }
    };

    private static final int STEPS_MSG = 1;
    private static final int PACE_MSG = 2;
    private static final int DISTANCE_MSG = 3;
    private static final int SPEED_MSG = 4;
    private static final int CALORIES_MSG = 5;

    private Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case STEPS_MSG:
                    mStepValue = (int)msg.arg1;
                    mStepValueView.setText("" + mStepValue);
                    break;
                case PACE_MSG:
                    mPaceValue = msg.arg1;
                    if (mPaceValue <= 0) {
                        mPaceValueView.setText("0");
                    }
                    else {
                        mPaceValueView.setText("" + (int)mPaceValue);
                    }
                    break;
                case DISTANCE_MSG:
                    mDistanceValue = ((int)msg.arg1)/1000f;
                    if (mDistanceValue <= 0) {
                        mDistanceValueView.setText("0");
                    }
                    else {
                        mDistanceValueView.setText(
                                ("" + (mDistanceValue + 0.000001f)).substring(0, 5)
                        );
                    }
                    break;
                case SPEED_MSG:
                    mSpeedValue = ((int)msg.arg1)/1000f;
                    if (mSpeedValue <= 0) {
                        mSpeedValueView.setText("0");
                    }
                    else {
                        mSpeedValueView.setText(
                                ("" + (mSpeedValue + 0.000001f)).substring(0, 4)
                        );
                    }
                    break;
                case CALORIES_MSG:
                    mCaloriesValue = msg.arg1;
                    if (mCaloriesValue <= 0) {
                        mCaloriesValueView.setText("0");
                    }
                    else {
                        mCaloriesValueView.setText("" + (int)mCaloriesValue);
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }

    };
}