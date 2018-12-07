package me.sahidur.foodiography.main.errorhandler;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import me.sahidur.foodiography.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.crash.FirebaseCrash;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public final class DefaultErrorActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.shared_activity_default_error);
        //Close/restart button logic:
        //If a class if set, use restart.
        //Else, use close and just finish the app.
        //It is recommended that you follow this logic if implementing a custom error activity.
        FirebaseApp.initializeApp(this);
        Button restartButton = (Button) findViewById(R.id.customactivityoncrash_error_activity_restart_button);

        final Class<? extends Activity> restartActivityClass = CustomActivityOnCrash.getRestartActivityClassFromIntent(getIntent());
        final CustomActivityOnCrash.EventListener eventListener = CustomActivityOnCrash.getEventListenerFromIntent(getIntent());

        if (restartActivityClass != null) {
            restartButton.setText(R.string.customactivityoncrash_error_activity_restart_app);
            restartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(DefaultErrorActivity.this, restartActivityClass);
                    CustomActivityOnCrash.restartApplicationWithIntent(DefaultErrorActivity.this, intent, eventListener);
                }
            });
        } else {
            restartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CustomActivityOnCrash.closeApplication(DefaultErrorActivity.this, eventListener);
                }
            });
        }

        Button moreInfoButton = (Button) findViewById(R.id.customactivityoncrash_error_activity_more_info_button);

        if (CustomActivityOnCrash.isShowErrorDetailsFromIntent(getIntent())) {

            moreInfoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //We retrieve all the error data and show it

                    Toast.makeText(DefaultErrorActivity.this, R.string.customactivityoncrash_error_activity_error_details_reported, Toast.LENGTH_LONG).show();
                    FirebaseCrash.report(new Exception(CustomActivityOnCrash.getStackTraceFromIntent(getIntent())));

                }
            });
        } else {
            moreInfoButton.setVisibility(View.GONE);
        }

        int defaultErrorActivityDrawableId = CustomActivityOnCrash.getDefaultErrorActivityDrawableIdFromIntent(getIntent());
        ImageView errorImageView = ((ImageView) findViewById(R.id.customactivityoncrash_error_activity_image));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            errorImageView.setImageDrawable(getResources().getDrawable(defaultErrorActivityDrawableId, getTheme()));
        } else {
            //noinspection deprecation
            errorImageView.setImageDrawable(getResources().getDrawable(defaultErrorActivityDrawableId));
        }
    }

    private void copyErrorToClipboard() {
        String errorInformation =
                CustomActivityOnCrash.getAllErrorDetailsFromIntent(DefaultErrorActivity.this, getIntent());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(getString(R.string.customactivityoncrash_error_activity_error_details_clipboard_label), errorInformation);
            clipboard.setPrimaryClip(clip);
        } else {
            //noinspection deprecation
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            clipboard.setText(errorInformation);
        }
    }
}
