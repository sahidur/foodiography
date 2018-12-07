package me.sahidur.foodiography.main.pedometer;

import android.content.SharedPreferences;

/**
 * Created by Brian on 2015/9/9.
 * A wrapper for SharedPreference, providing various convenient methods.
 */
public class PedometerSettings {
    private static SharedPreferences mPedometerSettings;
    private static SharedPreferences.Editor editor;

    public PedometerSettings(SharedPreferences mSharedPreferences) {
        mPedometerSettings = mSharedPreferences;
        editor = mPedometerSettings.edit();
    }

    public static SharedPreferences getmPedometerSettings() {
        return mPedometerSettings;
    }

    public static boolean getIsMetric() {
        try {
            String str = mPedometerSettings.getString("is_metric", "Metric");
            return str.equals("Metric");
        }
        catch (Exception e) {
            return true;
        }
    }

    public static void setIsMetric(boolean value) {
        editor.putBoolean("is_metric", value);
        editor.commit();
    }

    public static float getStepLength() {
        try {
            return Float.valueOf(mPedometerSettings.getString("step_length", "20").trim());
        }
        catch (NumberFormatException e) {
            // TODO: reset value, & notify user somehow
            return 20f;
        }
    }

    public static void setStepLength(float length) {
        editor.putFloat("step_length", length);
        editor.commit();
    }

    public static float getBodyWeight() {
        try {
            return Float.valueOf(mPedometerSettings.getString("body_weight", "50").trim());
        }
        catch (NumberFormatException e) {
            // TODO: reset value, & notify user somehow
            return 50f;
        }
    }

    public static void setBodyWeight(float weight) {
        editor.putFloat("body_weight", weight);
        editor.commit();
    }

    public static boolean getShouldRunInBackground() {
        try {
            return Boolean.valueOf(mPedometerSettings.getBoolean("should_run_in_background", true));
        }
        catch (NullPointerException e) {
            return true;
        }
    }

    public static void setShouldRunInBackground(boolean value) {
        editor.putBoolean("should_run_in_background", value);
        editor.commit();
    }

    public static int getHistoryWaypointLimit() {
        try {
            return Integer.valueOf(mPedometerSettings.getString("history_waypoint_limit", "100"));
        }
        catch (NullPointerException e) {
            return 0;
        }
    }

    public static void setHistoryWaypointLimit(int limit) {
        editor.putInt("history_waypoint_limit", limit);
        editor.commit();
    }

    public static void setDesiredPace(int pace) {
        editor.putFloat("desire_pace", pace);
        editor.commit();
    }

    // Values: 1.97  2.96  4.44  6.66  10.00  15.00  22.50  33.75  50.62
    public static float getSensitivity() {
        try {
            float base = 2;
            float raw = Float.valueOf(mPedometerSettings.getFloat("sensitivity", 0.5f));
            return 48*(1-raw) + base; // TODO: refactor this to remove the constant
        }
        catch (NullPointerException e) {
            return 22.50f;
        }
    }

    public static void setSensitivity(float sensitivity) {
        editor.putFloat("sensitivity", sensitivity);
        editor.commit();
    }
}
