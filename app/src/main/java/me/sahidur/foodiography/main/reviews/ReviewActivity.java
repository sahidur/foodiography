package me.sahidur.foodiography.main.reviews;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.models.UserRateModel;
import me.sahidur.foodiography.main.utils.Constants;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class ReviewActivity extends AppCompatActivity {

    @Bind(R.id.recycleComments)
    RecyclerView vRecycleViewComments;

    @Bind(R.id.textEmpty)
    LinearLayout vTextEmpty;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    FirebaseDatabase mFDatabase = FirebaseDatabase.getInstance();

    List<UserRateModel> mUserRateModelList;
    ReviewAdapter mReviewAdapter;

    String sPlaceId;

    String TAG = "ReviewActivity:";
    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private InterstitialAd mInterstitialAd;

    boolean enable_ads_interstitial;

    void loadConfig(){
        DatabaseReference configRef = mFDatabase.getReference("config").child("ads_config").child(Constants.ENABLE_ADS_INTERSTITIAL);
        configRef.keepSynced(true);
        configRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                enable_ads_interstitial = dataSnapshot.getValue(Boolean.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(Constants.TAG_PARENT, TAG + ":"+databaseError.getMessage());
            }
        });
    }

    public void loadInterstitial(boolean isEnable) {
        if(isEnable){
            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
            AdRequest adRequest = new AdRequest.Builder()
                    //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .build();
            mInterstitialAd.loadAd(adRequest);
            mInterstitialAd.setAdListener(new AdListener() {
                public void onAdLoaded() {
                    // Call displayInterstitial() function
                    displayInterstitial();
                }
            });
        }
    }

    public void displayInterstitial() {
        // If Ads are loaded, show Interstitial else show nothing.
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reviews_review_activity);

        ButterKnife.bind(this);

        loadConfig();

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUserRateModelList = new ArrayList<>();
        mReviewAdapter = new ReviewAdapter(this, mUserRateModelList);

        sPlaceId = getIntent().getStringExtra("placeId");

        getSupportActionBar().setTitle(getIntent().getStringExtra("placeName"));

        getUserRateCommentsUid(sPlaceId);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        vRecycleViewComments.setLayoutManager(mLayoutManager);
        vRecycleViewComments.setItemAnimator(new DefaultItemAnimator());
        vRecycleViewComments.setAdapter(mReviewAdapter);

        loadInterstitial(enable_ads_interstitial);
    }

    void getUserRateCommentsUid(final String placeId) {

        DatabaseReference rateRef = mFDatabase.getReference("rate");
        rateRef.keepSynced(true);
        rateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUserRateModelList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    getUserRateComments(postSnapshot.getKey(), placeId);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(Constants.TAG_PARENT, TAG + "getUserRateComments:" + mUserRateModelList.toString());
            }
        });
    }

    void getUserRateComments(final String userid, final String placeId) {

        DatabaseReference rateRef = mFDatabase.getReference("rate").child(userid);

        rateRef.keepSynced(true);

        rateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    UserRateModel person = postSnapshot.getValue(UserRateModel.class);
                    if (postSnapshot.getKey().equals(placeId)) {
                        person.setPlaceId(postSnapshot.getKey());
                        person.setUid(userid);
                        mUserRateModelList.add(person);
                    }
                }
                sortListByDate();
                if (mUserRateModelList.size() > 0) {
                    vTextEmpty.setVisibility(View.GONE);
                }
                mReviewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(Constants.TAG_PARENT, TAG + "getUserRateComments:" + mUserRateModelList.toString());
            }
        });
    }

    public void sortListByDate() {

        Collections.sort(mUserRateModelList, new Comparator<UserRateModel>() {
            public int compare(UserRateModel o1, UserRateModel o2) {
                Date date = null;
                Date date2 = null;
                try {
                    date = formatter.parse(o1.getTimestamp());
                    date2 = formatter.parse(o2.getTimestamp());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (date == null || date2 == null)
                    return 0;
                return date2.compareTo(date);
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