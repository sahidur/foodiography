package me.sahidur.foodiography.main.pedometer;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import me.sahidur.foodiography.R;

import java.util.LinkedList;
import java.util.List;

/**
 * This is an example of implementing an application service that runs locally
 * in the same process as the application.
 *
 * <p>Notice the use of the {@link NotificationManager} when interesting things
 * happen in the service.  This is generally how background services should
 * interact with the user, rather than doing something more disruptive such as
 * calling startActivity().
 */
public class PedometerService extends Service {
    static private LinkedList<LatLng> mHistoryTrace = new LinkedList<>();
    static private MyLocationData mCurrentLocation = null;
    private SharedPreferences mSharedPreference;
    private PedometerSettings mPedometerSettings;

    private SensorManager mSensorManager;
    private StepDetector mStepDetector;

    private LocationClient mLocationClient = null;
    private BDLocationListener mLocationListener = new LocationListener();

    private int mSteps;
    private int mPace;       // steps / minute
    private float mDistance; // kilometers
    private float mSpeed;    // kilometers / hour
    private float mCalories; // kiloJoule

    /* Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class PedometerBinder extends Binder {
        PedometerService getService() {
            return PedometerService.this;
        }
    }

    @Override
    public void onCreate() {
        StepDisplayer mStepDisplayer;
        PaceNotifier mPaceNotifier;
        DistanceNotifier mDistanceNotifier;
        SpeedNotifier mSpeedNotifier;
        CaloriesNotifier mCaloriesNotifier;

        super.onCreate();

        // Preparing settings for this app
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(this);
        mPedometerSettings = new PedometerSettings(mSharedPreference);

        // Start detecting
        mStepDetector = new StepDetector();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        registerDetector();

        // Register our receiver for the ACTION_SCREEN_OFF action. This will make our receiver
        // code be called whenever the phone enters standby mode.
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mReceiver, filter);

        mStepDisplayer = new StepDisplayer();
        mStepDisplayer.addListener(mStepListener);
        mStepDetector.addStepListener(mStepDisplayer);

        mPaceNotifier     = new PaceNotifier();
        mPaceNotifier.addListener(mPaceListener);
        mStepDetector.addStepListener(mPaceNotifier);

        mDistanceNotifier = new DistanceNotifier(mDistanceListener);
        mStepDetector.addStepListener(mDistanceNotifier);

        mSpeedNotifier    = new SpeedNotifier(mSpeedListener);
        mPaceNotifier.addListener(mSpeedNotifier);

        mCaloriesNotifier = new CaloriesNotifier(mCaloriesListener);
        mStepDetector.addStepListener(mCaloriesNotifier);

        // Location and Baidu Map related stuff
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(mLocationListener);
        initLocation();

        // Tell the user we started.
        Toast.makeText(this, getText(R.string.started), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        // Unregister our receiver.
        unregisterReceiver(mReceiver);
        unregisterDetector();

        // Stop detecting
        mSensorManager.unregisterListener(mStepDetector);

        mLocationClient.stop();

        // Tell the user we stopped.
        Toast.makeText(this, getText(R.string.stopped), Toast.LENGTH_SHORT).show();

        super.onDestroy();
    }

    private void registerDetector() {
        Sensor mSensor;
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(
                mStepDetector,
                mSensor,
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void unregisterDetector() {
        mSensorManager.unregisterListener(mStepDetector);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Receives messages from activity.
     */
    private final IBinder mBinder = new PedometerBinder();

    public interface ICallback {
        void stepsChanged(int value);
        void paceChanged(int value);
        void distanceChanged(float value);
        void speedChanged(float value);
        void caloriesChanged(float value);
    }

    private ICallback mCallback;

    public void registerCallback(ICallback cb) {
        mCallback = cb;
    }

    /**
     * Forwards pace values from PaceNotifier to the activity.
     */
    private StepDisplayer.Listener mStepListener = new StepDisplayer.Listener() {
        public void stepsChanged(int value) {
            mSteps = value;
            passValue();
        }
        public void passValue() {
            if (mCallback != null) {
                mCallback.stepsChanged(mSteps);
            }
        }
    };
    /**
     * Forwards pace values from PaceNotifier to the activity.
     */
    private PaceNotifier.Listener mPaceListener = new PaceNotifier.Listener() {
        public void paceChanged(int value) {
            mPace = value;
            passValue();
        }
        public void passValue() {
            if (mCallback != null) {
                mCallback.paceChanged(mPace);
            }
        }
    };
    /**
     * Forwards distance values from DistanceNotifier to the activity.
     */
    private DistanceNotifier.Listener mDistanceListener = new DistanceNotifier.Listener() {
        public void valueChanged(float value) {
            mDistance = value;
            passValue();
        }
        public void passValue() {
            if (mCallback != null) {
                mCallback.distanceChanged(mDistance);
            }
        }
    };
    /**
     * Forwards speed values from SpeedNotifier to the activity.
     */
    private SpeedNotifier.Listener mSpeedListener = new SpeedNotifier.Listener() {
        public void valueChanged(float value) {
            mSpeed = value;
            passValue();
        }

        public void passValue() {
            if (mCallback != null) {
                mCallback.speedChanged(mSpeed);
            }
        }
    };
    /**
     * Forwards calories values from CaloriesNotifier to the activity.
     */
    private CaloriesNotifier.Listener mCaloriesListener = new CaloriesNotifier.Listener() {
        public void valueChanged(float value) {
            mCalories = value;
            passValue();
        }

        public void passValue() {
            if (mCallback != null) {
                mCallback.caloriesChanged(mCalories);
            }
        }
    };

    // BroadcastReceiver for handling ACTION_SCREEN_OFF.
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Check action just to be on the safe side.
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                // Unregisters the listener and registers it again.
                PedometerService.this.unregisterDetector();
                PedometerService.this.registerDetector();
            }
        }
    };

    public class LocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();

            if(mHistoryTrace.size() >= PedometerSettings.getHistoryWaypointLimit()) {
                mHistoryTrace.remove();
            }
            mHistoryTrace.add(new LatLng(locData.latitude, locData.longitude));
            mCurrentLocation = locData;
        }
    }

    private void initLocation(){
        // update the location every 10000ms
        int span = 10000;

        LocationClientOption option = new LocationClientOption();

        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);

        option.setCoorType("bd09ll");

        option.setScanSpan(span);

        option.setIsNeedAddress(true);

        option.setOpenGps(true);

        option.setLocationNotify(true);

        option.setIsNeedLocationDescribe(true);
        option.setIsNeedLocationPoiList(true);
        option.setIgnoreKillProcess(false);

        option.SetIgnoreCacheException(false);

        option.setEnableSimulateGps(false);
        mLocationClient.setLocOption(option);

        mLocationClient.start();
    }

    static public List<LatLng> getHistoryTrace() {
        return mHistoryTrace;
    }
    static public MyLocationData getCurrentLocation() {
        return mCurrentLocation;
    }
}
