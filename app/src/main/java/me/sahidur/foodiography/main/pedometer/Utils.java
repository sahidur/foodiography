package me.sahidur.foodiography.main.pedometer;

public class Utils {
    private static Utils instance = null;

    public static Utils getInstance() {
        if (instance == null) {
            instance = new Utils();
        }
        return instance;
    }
}
