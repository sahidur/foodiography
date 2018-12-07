package me.sahidur.foodiography.main.pedometer;

/*
 * Calculates and displays the approximate calories.
 */
public class CaloriesNotifier implements StepListener {

    public interface Listener {
        public void valueChanged(float value);
        public void passValue();
    }
    private Listener mListener;

    private static double METRIC_RUNNING_FACTOR = 1.02784823;
    private static double IMPERIAL_RUNNING_FACTOR = 0.75031498;

    private static double METRIC_WALKING_FACTOR = 0.708;
    private static double IMPERIAL_WALKING_FACTOR = 0.517;

    private double mCalories = 0;

    boolean mIsMetric = PedometerSettings.getIsMetric();
    boolean mIsRunning;
    float mStepLength = PedometerSettings.getStepLength();
    float mBodyWeight = PedometerSettings.getBodyWeight();

    public CaloriesNotifier(Listener listener) {
        mListener = listener;
    }

    public void setCalories(float calories) {
        mCalories = calories;
        notifyListener();
    }

    public void onStep() {
        if (mIsMetric) {
            mCalories +=
                    (mBodyWeight * (mIsRunning ? METRIC_RUNNING_FACTOR : METRIC_WALKING_FACTOR))
                            // Distance:
                            * mStepLength // centimeters
                            / 10000.0; // centimeters/kilometer
        }
        else {
            mCalories +=
                    (mBodyWeight * (mIsRunning ? IMPERIAL_RUNNING_FACTOR : IMPERIAL_WALKING_FACTOR))
                            // Distance:
                            * mStepLength // inches
                            / 63360.0; // inches/mile
        }

        notifyListener();
    }

    private void notifyListener() {
        mListener.valueChanged((float)mCalories);
    }

    public void passValue() {

    }
}