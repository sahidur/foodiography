package me.sahidur.foodiography.main.pedometer;

/**
 * Interface implemented by classes that can handle notifications about steps.
 * These classes can be passed to StepDetector.
 */
public interface StepListener {
    void onStep();
}