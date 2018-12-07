package me.sahidur.foodiography.main.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;

import me.sahidur.foodiography.R;

import java.util.Locale;


public class SplashActivity extends AppCompatActivity {

    private final String TAG = "SplashScreen:";

    boolean firstTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLanguage(this, "en");

        SharedPreferences settings = getSharedPreferences("firstTime", MODE_PRIVATE);
        firstTime = settings.getBoolean("first_launch", true); //0 is the default valu

        setupWindowAnimations();

        Intent intent;
        if(firstTime){
            intent = new Intent(this, IntroActivity.class);
        }else{
            intent = new Intent(this, SignInActivity.class);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this);
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }

        finish();
    }

    private void setupWindowAnimations() {
        Explode slide = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            slide = new Explode();
            slide.setDuration(getResources().getInteger(R.integer.anim_duration_long));
            getWindow().setExitTransition(slide);
        }
    }

    @SuppressWarnings("deprecation")
    public Locale getSystemLocaleLegacy(Configuration config) {
        return config.locale;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public Locale getSystemLocale(Configuration config) {
        return config.getLocales().get(0);
    }

    @SuppressWarnings("deprecation")
    public void setSystemLocaleLegacy(Configuration config, Locale locale) {
        config.locale = locale;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public void setSystemLocale(Configuration config, Locale locale) {
        config.setLocale(locale);
    }

    public void setLanguage(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setSystemLocale(config, locale);
        } else {
            setSystemLocaleLegacy(config, locale);
        }
        context.getApplicationContext().getResources().updateConfiguration(config,
                context.getResources().getDisplayMetrics());
    }

    private static final int RP_ACCESS_STORAGE = 2;

    public void cekStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
            // cek apakah perlu menampilkan info kenapa membutuhkan access fine location
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) && ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                @SuppressLint("InlinedApi") String[] perm = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, perm,
                        RP_ACCESS_STORAGE);
            } else {
                // request permission untuk access fine location
                @SuppressLint("InlinedApi") String[] perm = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, perm,
                        RP_ACCESS_STORAGE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RP_ACCESS_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    this.recreate();
                } else {
                    cekStoragePermission();
                }
                return;
        }
    }
}