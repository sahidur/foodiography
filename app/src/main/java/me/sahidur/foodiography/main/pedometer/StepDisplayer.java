package me.sahidur.foodiography.main.pedometer;

import java.util.ArrayList;

/**
 * Counts steps provided by StepDetector and passes the current
 * step count to the activity.
 */
public class StepDisplayer implements StepListener {

    private int mCount = 0;

    public StepDisplayer() {
        notifyListener();
    }

    public void setSteps(int steps) {
        mCount = steps;
        notifyListener();
    }

    public void onStep() {
        mCount ++;
        notifyListener();
    }

    /* Listeners and notification */
    /* Listener is an interface, with two method, stepChanged(), and passValue()
     * Every notifier, or something that sends message, will implement a list of Listener,
     * Then, add listeners when needed. Then, the listener will implemented stepChanged themselves.
     * */

    public interface Listener {
        void stepsChanged(int value);
        void passValue();
    }
    private ArrayList<Listener> mListeners = new ArrayList<>();

    public void addListener(Listener listener) {
        mListeners.add(listener);
    }

    public void notifyListener() {
        for (Listener listener : mListeners) {
            listener.stepsChanged(mCount);
        }
    }
}