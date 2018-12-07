package me.sahidur.foodiography.main.gallery;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.models.GalleryModel;
import me.sahidur.foodiography.main.utils.Config;
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


public class GalleryListActivity extends AppCompatActivity {

    List<GalleryModel> vGaleriList;

    FirebaseDatabase mFDatabase = FirebaseDatabase.getInstance();

    @Bind(R.id.progress_view)
    ProgressBar vProgressView;

    @Bind(R.id.recycler_view)
    RecyclerView vRecyclerView;

    @Bind(R.id.nogallery)
    LinearLayout vNoGallery;

    StaggeredGridLayoutManager mGaggeredGridLayoutManager;
    String TAG = "GalleryListActivity:";
    SimpleDateFormat formatter2 = new SimpleDateFormat("dd-MM-yyyy");


    private GalleryListAdapter mGalleryListAdapter;
    private InterstitialAd mInterstitialAd;

    boolean enable_ads_interstitial = true;

    void loadConfig(){
        DatabaseReference configRef = mFDatabase.getReference("config").child("ads_config").child(Constants.ENABLE_ADS_INTERSTITIAL);
        configRef.keepSynced(true);
        configRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                enable_ads_interstitial = dataSnapshot.getValue(Boolean.class);
                loadInterstitial(enable_ads_interstitial);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(Constants.TAG_PARENT, TAG + ":" +databaseError.getMessage());
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void loadInterstitial(boolean isEnable) {
        if(isEnable){
            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
            AdRequest adRequest = new AdRequest.Builder()
                    // for testing add this .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_activity_gallery);

        ButterKnife.bind(this);

        loadConfig();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getIntent().getStringExtra("placeName"));
        }

        vProgressView.setVisibility(View.GONE);

        vGaleriList = new ArrayList<>();

        loadGalleryUser(getIntent().getStringExtra("placeId"));
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

    public void loadGalleryUser(final String placeId) {
        vProgressView.setVisibility(View.VISIBLE);

        DatabaseReference myRef = mFDatabase.getReference("gallery");
        myRef.keepSynced(true);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                //vGaleriList = new ArrayList<>();

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    loadGallery(placeId, postSnapshot.getKey());
                }
                vProgressView.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.e(Constants.TAG_PARENT, TAG + "loadGalleryUser:firebaseStatus:" + firebaseError.getMessage());
                vProgressView.setVisibility(View.GONE);
            }
        });
    }

    public void sortGaleryByDate(List<GalleryModel> list) {

        Collections.sort(list, new Comparator<GalleryModel>() {
            public int compare(GalleryModel o1, GalleryModel o2) {
                Date date = null;
                Date date2 = null;
                try {
                    date = formatter2.parse(o1.getTimestamp());
                    date2 = formatter2.parse(o2.getTimestamp());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (date == null || date2 == null)
                    return 0;
                return date2.compareTo(date);
            }
        });
    }

    public void updateUI(List<GalleryModel> gList, String placeId) {
        sortGaleryByDate(gList);
        mGalleryListAdapter = new GalleryListAdapter(GalleryListActivity.this, gList, placeId);
        vRecyclerView.setHasFixedSize(true);

        if (Config.getDensityDpi(this) > 7) {
            mGaggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        } else {
            mGaggeredGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        }

        vRecyclerView.setLayoutManager(mGaggeredGridLayoutManager);
        vRecyclerView.setItemAnimator(new DefaultItemAnimator());
        vRecyclerView.setAdapter(mGalleryListAdapter);
    }

    public void loadGallery(final String placeId, final String user) {
        vProgressView.setVisibility(View.VISIBLE);

        DatabaseReference myRef = mFDatabase.getReference("gallery").child(user).child(placeId);
        myRef.keepSynced(true);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {

                    GalleryModel person = postSnapshot.getValue(GalleryModel.class);

                    person.setPlaceId(placeId);
                    person.setImageId(postSnapshot.getKey());
                    person.setUid(user);
                    vGaleriList.add(person);

                }

                if (vGaleriList.size() > 0) {
                    vNoGallery.setVisibility(View.GONE);
                } else {
                    vNoGallery.setVisibility(View.VISIBLE);
                }

                sortGaleryByDate(vGaleriList);
                updateUI(vGaleriList, placeId);
                vProgressView.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.e(Constants.TAG_PARENT, TAG + "loadGallery:firebaseStatus: " + firebaseError.getMessage());
                vProgressView.setVisibility(View.GONE);
            }
        });
    }
}
