package me.sahidur.foodiography.main.pedometer;

/**
 * Calculates and displays pace (steps / minute), handles input of desired pace,
 * notifies user if he/she has to go faster or slower.
 *
 * Uses {@link PaceNotifier}, calculates speed as product of pace and step length.
 *
 */
public class SpeedNotifier implements PaceNotifier.Listener {

    public interface Listener {
        void valueChanged(float value);
        void passValue();
    }
    private Listener mListener;

    float mSpeed = 0;

    boolean mIsMetric = PedometerSettings.getIsMetric();

    float mStepLength = PedometerSettings.getStepLength();

    public SpeedNotifier(Listener listener) {
        mListener = listener;
    }
    public void setSpeed(float speed) {
        mSpeed = speed;
        notifyListener();
    }

    private void notifyListener() {
        mListener.valueChanged(mSpeed);
    }

    public void paceChanged(int value) {
        if (mIsMetric) {
            mSpeed = // kilometers / hour
                    value * mStepLength // centimeters / minute
                            / 100000f * 60f; // centimeters/kilometer
        }
        else {
            mSpeed = // miles / hour
                    value * mStepLength // inches / minute
                            / 63360f * 60f; // inches/mile
        }
        notifyListener();
    }


    public void passValue() {
        // Not used
    }
}