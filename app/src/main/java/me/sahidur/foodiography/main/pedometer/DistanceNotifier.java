package me.sahidur.foodiography.main.pedometer;

/**
 * Calculates and displays the distance walked.
 */
public class DistanceNotifier implements StepListener {

    public interface Listener {
        public void valueChanged(float value);
        public void passValue();
    }
    private Listener mListener;

    float mDistance = 0;

    boolean mIsMetric = PedometerSettings.getIsMetric();
    float mStepLength = PedometerSettings.getStepLength();

    public DistanceNotifier(Listener listener) {
        mListener = listener;
    }
    public void setDistance(float distance) {
        mDistance = distance;
        notifyListener();
    }

    public void onStep() {
        if (mIsMetric) {
            mDistance += (float)(// kilometers
                    mStepLength // centimeters
                            / 100000.0); // centimeters/kilometer
        }
        else {
            mDistance += (float)(// miles
                    mStepLength // inches
                            / 63360.0); // inches/mile
        }

        notifyListener();
    }

    private void notifyListener() {
        mListener.valueChanged(mDistance);
    }

    public void passValue() {
        // Callback of StepListener - Not implemented
    }
}