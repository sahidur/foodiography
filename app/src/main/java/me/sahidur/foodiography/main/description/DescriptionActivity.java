package me.sahidur.foodiography.main.description;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.cropper.CropImage;
import me.sahidur.foodiography.main.cropper.CropImageView;
import me.sahidur.foodiography.main.gallery.GalleryListActivity;
import me.sahidur.foodiography.main.maps.online.MapsActivity;
import me.sahidur.foodiography.main.models.GalleryModel;
import me.sahidur.foodiography.main.models.UserRateModel;
import me.sahidur.foodiography.main.reviews.ReviewActivity;
import me.sahidur.foodiography.main.utils.AppController;
import me.sahidur.foodiography.main.utils.Config;
import me.sahidur.foodiography.main.utils.Constants;
import me.sahidur.foodiography.main.utils.MapDirectionParser;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class DescriptionActivity extends AppCompatActivity implements
        OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    @Bind(R.id.noreview)
    TextView noreview;
    @Bind(R.id.nogallery)
    TextView nogallery;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.image)
    ImageView backdrop;
    @Bind(R.id.tvAddress)
    TextView tvAddress;
    @Bind(R.id.tvCategory)
    TextView tvCategory;
    @Bind(R.id.tvInfor)
    TextView tvInfor;
    @Bind(R.id.tvDescription)
    TextView tvDescription;
    @Bind(R.id.tvFasilities)
    TextView tvFacilities;
    @Bind(R.id.rateNum)
    TextView ratNum;
    @Bind(R.id.totalRate)
    TextView totalRate;
    @Bind(R.id.buttonGet)
    Button btnGetDir;
    @Bind(R.id.buttonShow)
    Button btnShowMap;
    //    @Bind(R.id.viewPager) ViewPager viewPagerImage;
    @Bind(R.id.ratingBar)
    RatingBar rb;
    @Bind(R.id.addStarBtn)
    FloatingActionButton addStarBtn;
    @Bind(R.id.coordinatorLayout)
    RelativeLayout coordinatorLayout;
    @Bind(R.id.progress_view)
    ProgressBar progressView;
    @Bind(R.id.recycleComments)
    RecyclerView recyclerViewComments;
    @Bind(R.id.collapsing_tool)
    CollapsingToolbarLayout collapsingToolbar;
    @Bind(R.id.distance)
    TextView tvDistance;
    @Bind(R.id.adViewList)
    NativeExpressAdView mAdView;
    @Bind(R.id.txtPlaceName)
    TextView tvPlaceName;

    FirebaseAuth mFAuth = FirebaseAuth.getInstance();
    FirebaseAuth.AuthStateListener mAuthStateListener;
    FirebaseUser mFUser;

    List<GalleryModel> mGalleryList;
    List<UserRateModel> mUserRateModel;
    List<UserRateModel> mUserRateModelLimit;

    DescriptionReviewAdapter mDescriptionReviewAdapter;
    String sCurrentUid, sDisplayName;
    Dialog dDialogChoose;
    RelativeLayout dDialogCameraBtn;
    RelativeLayout dDialogGalleryBtn;
    Button dDialogCancel;

    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    SimpleDateFormat formatter2 = new SimpleDateFormat("dd-MM-yyyy");

    List<CardView> mViews;
    boolean enable_ads_native = true;

    Uri mImagePickUri;
    DescriptionGalleryAdapter mGalleryAdapter;
    //    private ShadowTransformer mCardShadowTransformer;
    @Bind(R.id.recycleGallery)
    RecyclerView recyclerViewGallery;
    private SupportMapFragment mMapFragment;
    private GoogleMap mMap;
    private ArrayList<LatLng> traceOfMe = null;
    private Polyline mPolyline = null;
    private LatLng mCurrentLocation;
    private LatLng mLocation;
    private double mCurrentLng;
    private double mCurrentLat;
    private FirebaseDatabase mFDatabase = FirebaseDatabase.getInstance();
    private FirebaseStorage mFStorage = FirebaseStorage.getInstance();
    //    private DescriptionGalleryPagerAdapter mPagerAdapter;
    private Dialog dRateDialog;
    private String sPlaceID;
    private String TAG = "DescriptionActivity:";
    private ProgressDialog mProgressDialog;

    void loadConfig() {
        DatabaseReference configRef = mFDatabase.getReference("config").child("ads_config").child(Constants.ENABLE_ADS_NATIVE);
        configRef.keepSynced(true);
        configRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    enable_ads_native = dataSnapshot.getValue(Boolean.class);
                }else{
                    enable_ads_native = true;
                }

                loadAds(enable_ads_native);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(Constants.TAG_PARENT, "-" + databaseError.getMessage());
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.description_activity_description);

        ButterKnife.bind(this);

        loadConfig();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        sPlaceID = getIntent().getStringExtra("placeId");

        progressView.setVisibility(View.INVISIBLE);

        final List<String> category = new ArrayList<>();

        String[] catName = getIntent().getStringExtra("category").split(",");

        for (String cat : catName) {
            DatabaseReference catRef = mFDatabase.getReference("categories").child(cat.trim()).child("categoryName");
            catRef.keepSynced(true);
            catRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    category.add(dataSnapshot.getValue(String.class));

                    StringBuilder categoryName = new StringBuilder();

                    for (int i = 0; i < category.size(); i++) {
                        String cate = category.get(i).replace("null", " ");
                        if (i == category.size() - 1) {
                            categoryName.append(cate);
                        } else {
                            categoryName.append(cate).append(", ");
                        }
                    }

                    tvCategory.setText(categoryName.toString().replace(" ,", ""));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "firebaseCancelled:" + databaseError.getMessage());
                    Config.showToast(coordinatorLayout, getString(R.string.firebase_error));
                }
            });
        }

        if (getIntent().getFloatExtra("distance", 0) < 1) {
            tvDistance.setVisibility(View.INVISIBLE);
        } else {
            tvDistance.setText(Config.cDist(getIntent().getFloatExtra("distance", 0)));
        }

        getSupportActionBar().setTitle("");

        tvPlaceName.setText(getIntent().getStringExtra("placeName"));

        tvAddress.setText(getIntent().getStringExtra("address"));
        tvInfor.setText(getIntent().getStringExtra("info"));

        Config.setTextViewHTML(this, tvDescription, getIntent().getStringExtra("description"));

        tvFacilities.setText(getIntent().getStringExtra("facilities"));

        String[] newLoc = getIntent().getStringExtra("latlong").split(",");

        double mLatitude = Double.parseDouble(newLoc[0].trim());
        double mLongitude = Double.parseDouble(newLoc[1].trim());
        String[] cur = getIntent().getStringExtra("mCurrentLocation").split(",");
        mCurrentLat = Double.parseDouble(cur[0]);
        mCurrentLng = Double.parseDouble(cur[1]);

        mCurrentLocation = new LatLng(mCurrentLat, mCurrentLng);
        mLocation = new LatLng(mLatitude, mLongitude);

        mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mMapFragment.getMapAsync(this);

        String pathUrl = "gallery/" + sPlaceID + "/" + getIntent().getStringExtra("imageThumbnail") + "?alt=media";
        String urlImg;

        if (getIntent().getStringExtra("imageThumbnail").equalsIgnoreCase("no_image.png")) {
            urlImg = "https://firebasestorage.googleapis.com/v0/b/" + Constants.FIREBASE_PROJECT_ID + ".appspot.com/o/no_image.png?alt=media";
        } else {
            urlImg = "https://firebasestorage.googleapis.com/v0/b/" + Constants.FIREBASE_PROJECT_ID + ".appspot.com/o/" + pathUrl.replace("/", "%2F");
        }

        Glide.with(getApplicationContext()).load(urlImg)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        progressView.setVisibility(View.INVISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressView.setVisibility(View.INVISIBLE);
                        return false;
                    }
                })
                .error(R.drawable.no_image)
                .into(backdrop);


        mUserRateModel = new ArrayList<>();

        getUserRate();
        getUserRateCommentsUid();

        RateDialogVoid();

        dDialogChoose = new Dialog(this, R.style.CustomDialog);
        dDialogChoose.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dDialogChoose.setContentView(R.layout.description_activity_dialog_upload_chooser);
        dDialogChoose.setCancelable(true);
        dDialogChoose.setTitle(R.string.txt_choose);

        dDialogCameraBtn = (RelativeLayout) dDialogChoose.findViewById(R.id.dialogCameraBtn);
        dDialogGalleryBtn = (RelativeLayout) dDialogChoose.findViewById(R.id.dialogGaleriBtn);
        dDialogCancel = (Button) dDialogChoose.findViewById(R.id.btnCancel);

    }



    @OnClick(R.id.buttonShow)
    void showMap() {
        Intent goTo;
        if (cekKoneksi(this)) {
            goTo = new Intent(this, MapsActivity.class);
            goTo.putExtra("placeName", getIntent().getStringExtra("placeName"));
            goTo.putExtra("latlong", getIntent().getStringExtra("latlong"));
            goTo.putExtra("distance", getIntent().getFloatExtra("distance", 0));
            goTo.putExtra("imageThumbnail", getIntent().getStringExtra("imageThumbnail"));
            goTo.putExtra("category", getIntent().getStringExtra("category"));
            goTo.putExtra("mCurrentLocation", getIntent().getStringExtra("mCurrentLocation"));
            goTo.putExtra("description", getIntent().getStringExtra("description"));
            goTo.putExtra("address", getIntent().getStringExtra("address"));
           // goTo.putExtra("info", getIntent().getStringExtra("info"));
            goTo.putExtra("facilities", getIntent().getStringExtra("facilities"));
            goTo.putExtra("placeId", getIntent().getStringExtra("placeId"));
            startActivity(goTo);
        } else {
            Snackbar snackbar = Snackbar
                    .make(coordinatorLayout, R.string.internet_not_connect, Snackbar.LENGTH_INDEFINITE);

            snackbar.setAction(R.string.snackbar_force, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //btnUpload.callOnClick();
                }
            });
            snackbar.show();
        }
    }

    @OnClick(R.id.addStarBtn)
    void addStarClick() {
        if (mFUser.isAnonymous()) {
            Config.alertLoginAnon(DescriptionActivity.this, getString(R.string.alert_anon_temporary));
        } else {
            dRateDialog.show();
        }
    }

    void RateDialogVoid() {

        dRateDialog = new Dialog(this, R.style.CustomDialog);
        dRateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dRateDialog.setContentView(R.layout.description_rating_dialog);
        dRateDialog.setCancelable(true);

        final RatingBar ratingBarDialog = (RatingBar) dRateDialog.findViewById(R.id.ratingBarDialog);
        ImageView dialogBack = (ImageView) dRateDialog.findViewById(R.id.dialogBack);
        TextView dialogDesk = (TextView) dRateDialog.findViewById(R.id.dialogDesk);
        TextView dialogJudul = (TextView) dRateDialog.findViewById(R.id.dialogJudul);
        dialogJudul.setText(getIntent().getStringExtra("placeName"));
        //final EditText dialogEditTextUlasan = (EditText) dRateDialog.findViewById(R.id.editTextUlasan);

        Button dialogBtnSaveRate = (Button) dRateDialog.findViewById(R.id.btnSimpanRate);
        Button dialogBtnBatalRate = (Button) dRateDialog.findViewById(R.id.btnBatalRate);
        Button dialogHapusRate = (Button) dRateDialog.findViewById(R.id.btnHapusRate);

        if (getIntent().getFloatExtra("distance", 0) < 1) {
            dialogDesk.setText("");
        } else {
            dialogDesk.setText(Config.cDist(getIntent().getFloatExtra("distance", 0)));
        }

        String pathUrl = "gallery/" + sPlaceID + "/" + getIntent().getStringExtra("imageThumbnail") + "?alt=media";
        String urlImg;

        if (getIntent().getStringExtra("imageThumbnail").equalsIgnoreCase("no_image.png")) {
            urlImg = "https://firebasestorage.googleapis.com/v0/b/" + Constants.FIREBASE_PROJECT_ID + ".appspot.com/o/no_image.png?alt=media";
        } else {
            urlImg = "https://firebasestorage.googleapis.com/v0/b/" + Constants.FIREBASE_PROJECT_ID + ".appspot.com/o/" + pathUrl.replace("/", "%2F");
        }

        Glide.with(getApplicationContext()).load(urlImg)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        progressView.setVisibility(View.INVISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressView.setVisibility(View.INVISIBLE);
                        return false;
                    }
                })
                .error(R.drawable.no_image)
                .into(dialogBack);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mFUser = firebaseAuth.getCurrentUser();

                if (mFUser != null) {

                    sCurrentUid = mFUser.getUid();
                    sDisplayName = mFUser.getDisplayName();

                    DatabaseReference getUidRate = mFDatabase.getReference("rate").child(sCurrentUid);
                    getUidRate.keepSynced(true);
                    getUidRate.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                UserRateModel userRateModel = postSnapshot.getValue(UserRateModel.class);
                                if (postSnapshot.getKey().equals(sPlaceID)) {
                                    ratingBarDialog.setRating(userRateModel.getRateNum());
                                    //dialogEditTextUlasan.setText(userRateModel.getReview());
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(Constants.TAG_PARENT, TAG + "RateDialogVoid:getUIDRate:" + databaseError.getMessage());
                            Config.showToast(coordinatorLayout, getString(R.string.firebase_error));
                        }
                    });
                } else {
                    Log.e(Constants.TAG_PARENT, TAG + "RateDialogVoid:CurrentUser:User Logout");
                }
            }
        };

        dialogBtnSaveRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float rating = ratingBarDialog.getRating();
               // String ulasan = dialogEditTextUlasan.getText().toString();
                if (rating > 0) {
                   // addStarComment(sCurrentUid, rating, ulasan);
                }
                dRateDialog.dismiss();
            }
        });

        dialogBtnBatalRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dRateDialog.dismiss();
            }
        });

        dialogHapusRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!sPlaceID.equals("")) {
                    DatabaseReference ref = mFDatabase.getReference("rate").child(sCurrentUid).child(sPlaceID);
                    ref.removeValue();
                    ratingBarDialog.setRating(0);
                  //  dialogEditTextUlasan.setText("");
                }
                dRateDialog.dismiss();
            }
        });
    }

    void loadAds(boolean isEnable) {
        if (isEnable) {

            mAdView.setVisibility(View.GONE);

            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    mAdView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdClosed() {
                    mAdView.setVisibility(View.GONE);
                }

                @Override
                public void onAdFailedToLoad(int i) {
                    mAdView.setVisibility(View.GONE);
                }

                @Override
                public void onAdOpened() {
                    mAdView.setVisibility(View.VISIBLE);
                }

            });
            AdRequest adRequest = new AdRequest.Builder()
                    //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .build();
            mAdView.loadAd(adRequest);
        } else {
            mAdView.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mFAuth.addAuthStateListener(mAuthStateListener);
    }

    private void addStarComment(String Uid, final float rate, final String comment) {
        DatabaseReference mDatabase = mFDatabase.getReference("rate");

        SimpleDateFormat formatD = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        formatD.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timeStamp = formatD.format(new Date());

        PostRating post = new PostRating(rate, comment, timeStamp);
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put(Uid + "/" + sPlaceID, postValues);

        mDatabase.updateChildren(childUpdates);

        dRateDialog.dismiss();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Uri resultUri = result.getUri();
            String out = getFileName(resultUri);
            InputStream image_stream = null;
            try {
                image_stream = getContentResolver().openInputStream(resultUri);
                Bitmap bitmap = BitmapFactory.decodeStream(image_stream);

                doUploadGallery(bitmap, out, sCurrentUid);

            } catch (FileNotFoundException e) {
                Log.e(Constants.TAG_PARENT, TAG + "bitmap0:Error:" + e.getMessage());
            }
        } else {
            Log.e(Constants.TAG_PARENT, TAG + "requestCode:" + requestCode + ":resultCode:" + resultCode);
        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public void doUploadGallery(Bitmap bitmap, final String filename, final String upload_by) {
        mFStorage.setMaxUploadRetryTimeMillis(20000); // 20 second
        StorageReference storageRef = mFStorage.getReferenceFromUrl("gs://" + Constants.FIREBASE_PROJECT_ID + ".appspot.com/gallery/" + sPlaceID);
        final String newName = "IMG_" + filename.substring(7);
        final StorageReference galeriRef = storageRef.child(newName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        final byte[] data = baos.toByteArray();

        mProgressDialog = new ProgressDialog(DescriptionActivity.this);
        mProgressDialog.setMessage("Uploading image...");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        UploadTask uploadTask = galeriRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {

                mProgressDialog.hide();

                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, R.string.upload_error, Snackbar.LENGTH_INDEFINITE);

                snackbar.setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //btnUpload.callOnClick();
                    }
                });

                snackbar.show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                DatabaseReference mDatabase = mFDatabase.getReference("gallery");

                String key = mDatabase.child(upload_by).push().getKey();

                SimpleDateFormat formatD = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                formatD.setTimeZone(TimeZone.getTimeZone("UTC"));
                String timeStamp = formatD.format(new Date());

                PostImage post = new PostImage(newName, timeStamp);
                Map<String, Object> postValues = post.toMap();

                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put(upload_by + "/" + sPlaceID + "/" + key, postValues);
                mDatabase.updateChildren(childUpdates);
                mProgressDialog.dismiss();
                Config.showToast(coordinatorLayout, getString(R.string.upload_success));
            }
        });
    }

    public boolean cekKoneksi(Context cek) {
        ConnectivityManager cm = (ConnectivityManager) cek.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    void getUserRate() {
        DatabaseReference rateRef = mFDatabase.getReference("rate");
        rateRef.keepSynced(true);
        rateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUserRateModel.clear();
                if (dataSnapshot.getValue() == null) {
                    rb.setRating(0);
                    ratNum.setText("0");
                    totalRate.setText("(0)");
                }

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    getUserRateUid(postSnapshot.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Config.showToast(coordinatorLayout, getString(R.string.firebase_error));
            }
        });

    }

    void getUserRateUid(final String userid) {

        DatabaseReference rateRef = mFDatabase.getReference("rate").child(userid);
        rateRef.keepSynced(true);

        rateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    UserRateModel person = postSnapshot.getValue(UserRateModel.class);
                    if (postSnapshot.getKey().equals(sPlaceID)) {
                        person.setPlaceId(postSnapshot.getKey());
                        person.setUid(userid);
                        mUserRateModel.add(person);
                    }
                }
                updateUIRating(mUserRateModel);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Config.showToast(coordinatorLayout, getString(R.string.firebase_error));
            }
        });
    }

    void getUserRateCommentsUid() {

        DatabaseReference rateRef = mFDatabase.getReference("rate");
        rateRef.keepSynced(true);
        rateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUserRateModelLimit = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    getUserRateComments(postSnapshot.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(Constants.TAG_PARENT, TAG + "getUserRateComments:" + mUserRateModel.toString());
                Config.showToast(coordinatorLayout, getString(R.string.firebase_error));
            }
        });
    }

    void getUserRateComments(final String userid) {

        DatabaseReference rateRef = mFDatabase.getReference("rate").child(userid);

        rateRef.keepSynced(true);

        rateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    UserRateModel person = postSnapshot.getValue(UserRateModel.class);
                    if (postSnapshot.getKey().equals(sPlaceID)) {
                        person.setPlaceId(postSnapshot.getKey());
                        person.setUid(userid);
                        mUserRateModelLimit.add(person);
                    }
                }

                sortListByDate();
                List<UserRateModel> mLimit = new ArrayList<UserRateModel>();
                if (mUserRateModelLimit.size() >= 5) {
                    for (int i = 0; i < 5; i++) {
                        mLimit.add(mUserRateModelLimit.get(i));
                    }
                    mDescriptionReviewAdapter = new DescriptionReviewAdapter(DescriptionActivity.this, mLimit);
                } else {
                    mDescriptionReviewAdapter = new DescriptionReviewAdapter(DescriptionActivity.this, mUserRateModelLimit);
                }

                RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(DescriptionActivity.this, 1);
                recyclerViewComments.setLayoutManager(mLayoutManager);
                recyclerViewComments.setItemAnimator(new DefaultItemAnimator());
                recyclerViewComments.setAdapter(mDescriptionReviewAdapter);
                mDescriptionReviewAdapter.notifyDataSetChanged();

                if (mUserRateModelLimit.size() > 0) {
                    noreview.setVisibility(View.GONE);
                    //showAllComments.setEnabled(true);
                } else {
                    noreview.setVisibility(View.VISIBLE);
                    //showAllComments.setEnabled(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(Constants.TAG_PARENT, TAG + "getUserRateComments:" + mUserRateModel.toString());
                Config.showToast(coordinatorLayout, getString(R.string.firebase_error));
            }
        });
    }

    public void sortListByDate() {

        Collections.sort(mUserRateModelLimit, new Comparator<UserRateModel>() {
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

    public void updateUIRating(List<UserRateModel> ulist) {
        float total_r;
        float r1 = 0;
        float r2 = 0;
        float r3 = 0;
        float r4 = 0;
        float r5 = 0;
        for (int i = 0; i < ulist.size(); i++) {
            float r = ulist.get(i).getRateNum();
            if (r >= 0 && r <= 1) {
                r1 += 1;
            } else if (r > 1 && r <= 2) {
                r2 += 1;
            } else if (r > 2 && r <= 3) {
                r3 += 1;
            } else if (r > 3 && r <= 4) {
                r4 += 1;
            } else if (r > 4 && r <= 5) {
                r5 += 1;
            }
        }

        total_r = (r5 + r4 + r3 + r2 + r1);
        float rating = (5 * r5 + 4 * r4 + 3 * r3 + 2 * r2 + 1 * r1) / total_r;

        rb.setRating(rating);

        ratNum.setText(String.valueOf(String.format("%.1f", rb.getRating())));
        totalRate.setText("(" + String.valueOf(String.format("%.0f", total_r)) + ")");
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.description_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public Uri getLocalBitmapUri(Bitmap bmp, String filename) {
        Uri bmpUri = null;
        try {
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename);
            if (!file.exists()) {
                FileOutputStream out = new FileOutputStream(file);
                bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
                out.close();
            }
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_share:

                progressView.setVisibility(View.VISIBLE);

                final String fileName = getIntent().getStringExtra("imageThumbnail");

                String pathUrl = "gallery/" + sPlaceID + "/" + fileName + "?alt=media";

                String urlImg = "https://firebasestorage.googleapis.com/v0/b/" + Constants.FIREBASE_PROJECT_ID + ".appspot.com/o/" + pathUrl.replace("/", "%2F");

                String sAux = getIntent().getStringExtra("placeName") + "\n" + tvAddress.getText().toString() + "\n\n";
                sAux = sAux + "Download " + getString(R.string.app_name) + " on \nhttps://play.google.com/store/apps/details?id=" + getPackageName();
                final String sharT = sAux;

                Glide.with(getApplicationContext())
                        .load(urlImg)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                progressView.setVisibility(View.INVISIBLE);
                                Intent shareIntent = new Intent();
                                shareIntent.setAction(Intent.ACTION_SEND);
                                shareIntent.putExtra(Intent.EXTRA_TEXT, sharT);
                                shareIntent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(resource, fileName));
                                shareIntent.setType("image/jpeg");
                                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_place)));
                            }
                        });
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        supportFinishAfterTransition();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().isCompassEnabled();
        mMap.getUiSettings().isMapToolbarEnabled();

        createMarker();

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocation, 12));

        mMap.setOnMarkerClickListener(this);
    }

    public void createMarker() {
        mMap.addMarker(new MarkerOptions()
                .position(mLocation)
                .icon(BitmapDescriptorFactory.fromBitmap(setMarker(R.drawable.ic_marker))));
    }

    public Bitmap setMarker(int drawable) {
        int height = 100;
        int width = 100;
        BitmapDrawable bitmapdraw = (BitmapDrawable) ContextCompat.getDrawable(DescriptionActivity.this, drawable);
        Bitmap b = bitmapdraw.getBitmap();
        return Bitmap.createScaledBitmap(b, width, height, false);
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        String units = "units=metric";
        String mode = "mode=walking";
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + units + "&" + mode;
        String output = "json";
        return String.format("https://maps.googleapis.com/maps/api/directions/%s?%s", output, parameters);
    }

    private void traceMe(final LatLng srcLatLng, final LatLng destLatLng) {
        String url = getDirectionsUrl(srcLatLng, destLatLng);
        progressView.setVisibility(View.VISIBLE);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        MapDirectionParser parser = new MapDirectionParser();
                        List<List<HashMap<String, String>>> routes = parser.parse(response);
                        ArrayList<LatLng> points = null;

                        for (int i = 0; i < routes.size(); i++) {
                            points = new ArrayList<>();
                            List<HashMap<String, String>> path = routes.get(i);
                            for (int j = 0; j < path.size(); j++) {
                                HashMap<String, String> point = path.get(j);
                                double lat = Double.parseDouble(point.get("lat"));
                                double lng = Double.parseDouble(point.get("lng"));
                                LatLng position = new LatLng(lat, lng);
                                points.add(position);
                            }
                        }

                        drawPoints(points);
                        LatLngBounds.Builder mBoundsBuilder = new LatLngBounds.Builder();
                        mBoundsBuilder.include(srcLatLng);
                        mBoundsBuilder.include(destLatLng);
                        LatLngBounds mBounds = mBoundsBuilder.build();
                        int padding = 30;
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(mBounds, padding);

                        mMap.animateCamera(cu);
                        progressView.setVisibility(View.INVISIBLE);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressView.setVisibility(View.INVISIBLE);
                    }
                });

        AppController.getInstance().addToReqQueue(jsonObjectRequest);
    }

    private void drawPoints(ArrayList<LatLng> points) {
        if (points == null) {
            return;
        }
        traceOfMe = points;
        PolylineOptions polylineOpt = new PolylineOptions();
        for (LatLng latlng : traceOfMe) {
            polylineOpt.add(latlng);
        }
        polylineOpt.color(ContextCompat.getColor(this, R.color.colorAccent));
        if (mPolyline != null) {
            mPolyline.remove();
            mPolyline = null;
        }
        if (mMap != null) {
            mPolyline = mMap.addPolyline(polylineOpt);
        }
        if (mPolyline != null)
            mPolyline.setWidth(6);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return true;
    }

    @IgnoreExtraProperties
    private class PostRating {
        float rateNum;
        String review;
        String timestamp;

        PostRating() {
            // Default constructor required for calls to DataSnapshot.getValue(Post.class)
        }

        PostRating(float rateNum, String review, String timestamp) {
            this.rateNum = rateNum;
            this.review = review;
            this.timestamp = timestamp;
        }

        @Exclude
        Map<String, Object> toMap() {
            HashMap<String, Object> result = new HashMap<>();
            result.put("rateNum", rateNum);
            result.put("review", review);
            result.put("timestamp", timestamp);
            return result;
        }
    }

    @IgnoreExtraProperties
    public class PostImage {

        public String fileName;
        public String timestamp;

        public PostImage() {
            // Default constructor required for calls to DataSnapshot.getValue(Post.class)
        }

        public PostImage(String fileName, String timestamp) {
            this.fileName = fileName;
            this.timestamp = timestamp;
        }

        @Exclude
        public Map<String, Object> toMap() {
            HashMap<String, Object> result = new HashMap<>();
            result.put("fileName", fileName);
            result.put("timestamp", timestamp);
            return result;
        }
    }

}

