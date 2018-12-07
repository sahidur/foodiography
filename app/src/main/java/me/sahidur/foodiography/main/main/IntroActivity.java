package me.sahidur.foodiography.main.main;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import me.sahidur.foodiography.R;
import com.github.paolorotolo.appintro.AppIntro2;



public class IntroActivity extends AppIntro2 {
    @Override
    public void init(Bundle savedInstanceState) {
        addSlide(IntroFragment.newInstance(R.layout.main_intro_1));
        addSlide(IntroFragment.newInstance(R.layout.main_intro_2));
        addSlide(IntroFragment.newInstance(R.layout.main_intro_3));
        addSlide(IntroFragment.newInstance(R.layout.main_intro_4));
        addSlide(IntroFragment.newInstance(R.layout.main_intro_5));
        // Show and Hide Skip and Done buttons
        showStatusBar(false);
        //showSkipButton(false);

        setZoomAnimation();

        askForPermissions(new String[]{Manifest.permission.CAMERA}, 2);
        askForPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
        askForPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 4);
    }

    @Override
    public void onNextPressed() {

    }

    @Override
    public void onDonePressed() {
        Intent i = new Intent(getApplicationContext(), SignInActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onSlideChanged() {

    }

}
