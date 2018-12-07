package me.sahidur.foodiography.main.utils;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.errorhandler.CustomActivityOnCrash;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import java.lang.ref.WeakReference;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;


public class AppController extends Application {

    private static AppController mInstance;
    private RequestQueue mRequestQueue;
    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mOttoBus = new Bus(ThreadEnforcer.ANY);

        if (!FirebaseApp.getApps(this).isEmpty()) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }

        mInstance = this;

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/QuattrocentoSans-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        CustomActivityOnCrash.install(this);
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public RequestQueue getReqQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToReqQueue(Request<T> req, String tag) {

        getReqQueue().add(req);
    }

    public <T> void addToReqQueue(Request<T> req) {

        getReqQueue().add(req);
    }

    public void cancelPendingReq(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    private Bus mOttoBus;
    private WeakReference<OkHttpClient> mOkHttpClientWeakReference;

    public static AppController getApplication(Context context) {
        if (context instanceof AppController) {
            return (AppController) context;
        }
        return (AppController) context.getApplicationContext();
    }

    public OkHttpClient getOkHttpClient() {
        if (mOkHttpClientWeakReference == null || mOkHttpClientWeakReference.get() == null) {
            mOkHttpClientWeakReference = new WeakReference<>(new OkHttpClient());
        }
        return mOkHttpClientWeakReference.get();
    }

    public void registerOttoBus(Object object) {
        mOttoBus.register(object);
    }

    public void unregisterOttoBus(Object object) {
        mOttoBus.unregister(object);
    }

    public void sendOttoEvent(Object event) {
        mOttoBus.post(event);
    }

}
