package me.sahidur.foodiography.main.about;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import me.sahidur.foodiography.BuildConfig;
import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.utils.AboutPageUtils;
import me.sahidur.foodiography.main.utils.Constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class AboutActivity extends AppCompatActivity {

    @Bind(R.id.btnEmail)
    RelativeLayout btnEmail;
    @Bind(R.id.btnFacebook)
    RelativeLayout btnFacebook;
    @Bind(R.id.btnTwitter)
    RelativeLayout btnTwitter;
    @Bind(R.id.btnInstagram)
    RelativeLayout btnInstagram;
    @Bind(R.id.btnPlaystore)
    RelativeLayout btnPlaystore;
    @Bind(R.id.btnWebsite)
    RelativeLayout btnWebsite;
    @Bind(R.id.btnYoutube)
    RelativeLayout btnYoutube;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.textView3)
    TextView versionText;

    boolean clicked = false;

    private FirebaseDatabase mFDatabase = FirebaseDatabase.getInstance();

    private String twitter_id, instagram_id, email_id, facebook_id, youtube_id, url, app_description;

    private String TAG = "AboutActivity:";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_about);

        ButterKnife.bind(this);

        fetchAppConfig();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setTitle(getString(R.string.str_about) + " " + getString(R.string.app_name));

        versionText.setText(getString(R.string.str_version) + " " + BuildConfig.VERSION_NAME);

    }

    @OnClick(R.id.btnEmail)
    public void email() {
        btnEmail.startAnimation(AnimationUtils.loadAnimation(this, R.anim.alpha_button));
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email_id});
        startActivity(intent);
    }

    @OnClick(R.id.btnFacebook)
    public void facebook() {
        btnFacebook.startAnimation(AnimationUtils.loadAnimation(this, R.anim.alpha_button));
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        if (AboutPageUtils.isAppInstalled(this, "com.facebook.katana")) {
            intent.setPackage("com.facebook.katana");
            int versionCode = 0;
            try {
                versionCode = this.getPackageManager().getPackageInfo("com.facebook.katana", 0).versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            if (versionCode >= 3002850) {
                Uri uri = Uri.parse("fb://facewebmodal/f?href=" + "http://m.facebook.com/" + facebook_id);
                intent.setData(uri);
            } else {
                Uri uri = Uri.parse("fb://page/" + facebook_id);
                intent.setData(uri);
            }
        } else {
            intent.setData(Uri.parse("http://m.facebook.com/" + facebook_id));
        }
        startActivity(intent);
    }

    @OnClick(R.id.btnTwitter)
    public void twitter() {
        btnTwitter.startAnimation(AnimationUtils.loadAnimation(this, R.anim.alpha_button));
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);

        if (AboutPageUtils.isAppInstalled(this, "com.twitter.android")) {
            intent.setPackage("com.twitter.android");
            intent.setData(Uri.parse(String.format("twitter://user?screen_name=" + twitter_id)));
        } else {
            intent.setData(Uri.parse(String.format("http://twitter.com/intent/user?screen_name=" + twitter_id)));
        }
        startActivity(intent);
    }

    @OnClick(R.id.btnInstagram)
    public void instagram() {
        btnInstagram.startAnimation(AnimationUtils.loadAnimation(this, R.anim.alpha_button));
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http://instagram.com/_u/" + instagram_id));

        if (AboutPageUtils.isAppInstalled(this, "com.instagram.android")) {
            intent.setPackage("com.instagram.android");
        }
        startActivity(intent);
    }

    @OnClick(R.id.btnPlaystore)
    public void playstore() {
        btnPlaystore.startAnimation(AnimationUtils.loadAnimation(this, R.anim.alpha_button));
        Uri uri = Uri.parse("market://details?id="+getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(goToMarket);
    }

    @OnClick(R.id.btnYoutube)
    public void youtube() {
        btnYoutube.startAnimation(AnimationUtils.loadAnimation(this, R.anim.alpha_button));
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(String.format("http://youtube.com/user/" + youtube_id)));

        if (AboutPageUtils.isAppInstalled(this, "com.google.android.youtube")) {
            intent.setPackage("com.google.android.youtube");
        }
        startActivity(intent);
    }

    @OnClick(R.id.btnWebsite)
    public void website() {
        btnWebsite.startAnimation(AnimationUtils.loadAnimation(this, R.anim.alpha_button));
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        Uri uri = Uri.parse(url);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(browserIntent);
    }


    private void fetchAppConfig() {


        DatabaseReference configRef = mFDatabase.getReference("config").child("about_config");
        configRef.keepSynced(true);
        configRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snap : dataSnapshot.getChildren()){
                        if(snap.getKey().equalsIgnoreCase(Constants.APP_DESCRIPTION)){
                            app_description = snap.getValue(String.class);
                            //txtAppDescription.setText(app_description);
                        }else if(snap.getKey().equalsIgnoreCase(Constants.APP_EMAIL)){
                            email_id = snap.getValue(String.class);
                        }else if(snap.getKey().equalsIgnoreCase(Constants.APP_FACEBOOK)){
                            facebook_id = snap.getValue(String.class);
                        }else if(snap.getKey().equalsIgnoreCase(Constants.APP_INSTAGRAM)){
                            instagram_id = snap.getValue(String.class);
                        }else if(snap.getKey().equalsIgnoreCase(Constants.APP_TWITTER)){
                            twitter_id = snap.getValue(String.class);
                        }else if(snap.getKey().equalsIgnoreCase(Constants.APP_WEBSITE)){
                            url = snap.getValue(String.class);
                        }else if(snap.getKey().equalsIgnoreCase(Constants.APP_YOUTUBE)){
                            youtube_id = snap.getValue(String.class);
                        }
                    }
                }else{
                    facebook_id = getString(R.string.facebook_id);
                    app_description = getString(R.string.app_description);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(Constants.TAG_PARENT, TAG+":"+databaseError.getMessage());
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
