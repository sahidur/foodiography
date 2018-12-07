package me.sahidur.foodiography.main.feeds;



import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.ar.rotation.ARActivity;
import me.sahidur.foodiography.main.contextmenu.ContextMenuDialogFragment;
import me.sahidur.foodiography.main.contextmenu.MenuObject;
import me.sahidur.foodiography.main.contextmenu.MenuParams;
import me.sahidur.foodiography.main.contextmenu.interfaces.OnMenuItemClickListener;
import me.sahidur.foodiography.main.places.PlacesModel;
import me.sahidur.foodiography.main.utils.Config;
import me.sahidur.foodiography.main.utils.Constants;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FeedsActivity extends AppCompatActivity implements OnMenuItemClickListener {

    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @Bind(R.id.imageCategory)
    ImageView mImageCategory;
    @Bind(R.id.collapsing_tool)
    CollapsingToolbarLayout vCollapsingToolbarLayout;
    @Bind(R.id.progressBar)
    ProgressBar progressView;
    @Bind(R.id.searchtoolbar)
    Toolbar searchtollbar;

    FirebaseDatabase mFDatabase = FirebaseDatabase.getInstance();

    FeedsAdapter mFeedsAdapter;
    List<PlacesModel> mPlacesModelList;
    LatLng mCurrentLocation;
    StaggeredGridLayoutManager mGaggeredGridLayoutManager;
    FragmentManager mFragmentManager;

    Menu search_menu;
    MenuItem item_search;
    boolean enable_ads_native = true;
    private double mCurrentLng, mCurrentLat;
    private ContextMenuDialogFragment mMenuDialogFragment;
    private String TAG = "FeedsActivity:";

    private void initMenuFragment() {
        MenuParams menuParams = new MenuParams();
        menuParams.setActionBarSize((int) getResources().getDimension(R.dimen.tool_bar_height));
        menuParams.setMenuObjects(getMenuObjects());
        menuParams.setClosableOutside(false);
        mMenuDialogFragment = ContextMenuDialogFragment.newInstance(menuParams);
        mMenuDialogFragment.setItemClickListener(this);
    }

    private List<MenuObject> getMenuObjects() {
        List<MenuObject> menuObjects = new ArrayList<>();

        MenuObject close = new MenuObject();
        close.setResource(R.drawable.ic_clear);

        MenuObject itemA = new MenuObject(getString(R.string.menu_orders_abjad));
        itemA.setResource(R.drawable.ic_sort_by_alpha);
        itemA.setMenuTextAppearanceStyle(R.style.TextAppearance_FontPath_Menu);

        MenuObject itemB = new MenuObject(getString(R.string.menu_orders_distance));
        itemB.setResource(R.drawable.ic_distance);
        itemB.setMenuTextAppearanceStyle(R.style.TextAppearance_FontPath_Menu);

        MenuObject itemC = new MenuObject(getString(R.string.nav_ar_map));
        itemC.setResource(R.drawable.ic_ar);
        itemC.setMenuTextAppearanceStyle(R.style.TextAppearance_FontPath_Menu);

        menuObjects.add(close);
        menuObjects.add(itemA);
        menuObjects.add(itemB);
        menuObjects.add(itemC);

        return menuObjects;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    void loadConfig() {
        DatabaseReference configRef = mFDatabase.getReference("config").child("ads_config").child(Constants.ENABLE_ADS_NATIVE);
        configRef.keepSynced(true);
        configRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                enable_ads_native = dataSnapshot.getValue(Boolean.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Config.showToast(vCollapsingToolbarLayout, getString(R.string.firebase_error));
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            Transition transition;
            transition = TransitionInflater.from(this).inflateTransition(R.transition.slide_from_bottom);
            getWindow().setEnterTransition(transition);

            getWindow().setExitTransition(transition);
        }

        setContentView(R.layout.feeds_activity_feeds);

        ButterKnife.bind(this);

        loadConfig();

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setSearchtollbar();

        mFragmentManager = getSupportFragmentManager();
        initMenuFragment();

        Glide.with(getApplicationContext()).load(getIntent().getStringExtra("iconFileName"))
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.mipmap.ic_launcher)
                .into(mImageCategory);

        String[] cuText = getIntent().getStringExtra("mCurrentLocation").split(",");
        mCurrentLat = Double.parseDouble(cuText[0]);
        mCurrentLng = Double.parseDouble(cuText[1]);
        mCurrentLocation = new LatLng(mCurrentLat, mCurrentLng);

        vCollapsingToolbarLayout.setTitle(getIntent().getStringExtra("categoryName"));

        mPlacesModelList = new ArrayList<>();
        mFeedsAdapter = new FeedsAdapter(this, mPlacesModelList, mCurrentLocation, enable_ads_native);

        loadAllPlaces(getIntent().getStringExtra("category"));

        if (Config.getDensityDpi(this) > 7) {
            mGaggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        } else {
            mGaggeredGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        }

        mRecyclerView.setLayoutManager(mGaggeredGridLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(1, dpToPx(5), true));
        mRecyclerView.setAdapter(mFeedsAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.feeds_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void initSearchView() {
        final SearchView searchView =
                (SearchView) search_menu.findItem(R.id.action_filter_search).getActionView();
        searchView.setSubmitButtonEnabled(false);

        ImageView closeButton = (ImageView) searchView.findViewById(R.id.search_close_btn);
        closeButton.setImageResource(R.drawable.ic_close);

        EditText txtSearch = ((EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text));
        txtSearch.setHint(R.string.search_hint);
        txtSearch.setHintTextColor(Color.DKGRAY);
        txtSearch.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        txtSearch.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent_color));

        // set the cursor

        AutoCompleteTextView searchTextView = (AutoCompleteTextView) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        try {
            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(searchTextView, R.drawable.search_cursor); //This sets the cursor resource ID to 0 or @null which will make it visible on white background
        } catch (Exception e) {
            e.printStackTrace();
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                callSearch(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                callSearch(newText);
                return true;
            }

            public void callSearch(String query) {
                mFeedsAdapter.getFilter().filter(query);
            }
        });
    }

    public void setSearchtollbar() {
        if (searchtollbar != null) {
            searchtollbar.inflateMenu(R.menu.menu_search);
            search_menu = searchtollbar.getMenu();

            searchtollbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        circleReveal(R.id.searchtoolbar, 1, true, false);
                    else
                        searchtollbar.setVisibility(View.GONE);
                }
            });

            item_search = search_menu.findItem(R.id.action_filter_search);

            MenuItemCompat.setOnActionExpandListener(item_search, new MenuItemCompat.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionCollapse(MenuItem item) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        circleReveal(R.id.searchtoolbar, 1, true, false);
                    } else
                        searchtollbar.setVisibility(View.GONE);

                    mToolbar.setBackgroundColor(ContextCompat.getColor(FeedsActivity.this, R.color.transparent_color));
                    vCollapsingToolbarLayout.setCollapsedTitleTextColor(ContextCompat.getColor(FeedsActivity.this, android.R.color.black));
                    return true;
                }

                @Override
                public boolean onMenuItemActionExpand(MenuItem item) {

                    return true;
                }
            });

            initSearchView();
        } else
            Log.w("mToolbar", "setSearchtollbar: NULL");
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void circleReveal(int viewID, int posFromRight, boolean containsOverflow, final boolean isShow) {
        final View myView = findViewById(viewID);

        int width = myView.getWidth();

        if (posFromRight > 0)
            width -= (posFromRight * getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material)) - (getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_material) / 2);
        if (containsOverflow)
            width -= getResources().getDimensionPixelSize(R.dimen.abc_action_button_min_width_overflow_material);

        int cx = width;
        int cy = myView.getHeight() / 2;

        Animator anim;
        if (isShow)
            anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, (float) width);
        else
            anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, (float) width, 0);

        anim.setDuration((long) 300);

        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isShow) {
                    super.onAnimationEnd(animation);
                    myView.setVisibility(View.INVISIBLE);
                }
            }
        });

        if (isShow)
            myView.setVisibility(View.VISIBLE);

        anim.start();
    }


    @Override
    public void onMenuItemClick(View clickedView, int position) {
        if (position == 1) {
            sortAllPlacesListByName();
            mFeedsAdapter.notifyDataSetChanged();
            addAdsToList();
        } else if (position == 2) {
            sortAllPlacesListByDistance();
            mFeedsAdapter.notifyDataSetChanged();
            addAdsToList();
        } else if (position == 3) {
            Intent ar = new Intent(FeedsActivity.this, ARActivity.class);
            ar.putExtra("categoryId", getIntent().getStringExtra("category"));
            ar.putExtra("categoryName", getIntent().getStringExtra("categoryName"));
            startActivity(ar);
        }
    }

    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    void addAdsToList() {
        PlacesModel nativeModel = new PlacesModel();
        nativeModel.setViewType(2);
        if (mPlacesModelList.size() > 2) {
            int interval = 3;
            for (int i = 0; i < (mPlacesModelList.size() / interval); i++) {
                int posisi = ((i + 1) * (interval));
                if (posisi < mPlacesModelList.size()) {
                    mPlacesModelList.add(posisi, nativeModel);
                }
            }
        }

        mFeedsAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    public void loadAllPlaces(final String from) {

        progressView.setVisibility(View.VISIBLE);
        DatabaseReference myRefParent = mFDatabase.getReference("places");
        myRefParent.keepSynced(true);
        myRefParent.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                mPlacesModelList.clear();
                progressView.setVisibility(View.VISIBLE);
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    String parent = postSnapshot.getKey();
                    PlacesModel placesModel = postSnapshot.getValue(PlacesModel.class);
                    if (placesModel.getCategory().contains(from)) {
                        placesModel.setPlaceId(parent);
                        placesModel.setViewType(1);
                        float dist;
                        if (!placesModel.getLatlong().equals("0")) {
                            String[] newLoc = placesModel.getLatlong().split(",");
                            double mLatitude = Double.parseDouble(newLoc[0].trim());
                            double mLongitude = Double.parseDouble(newLoc[1].trim());

                            if (mCurrentLat == 0 || mCurrentLng == 0) {
                                dist = 0;
                            } else {
                                dist = Config.distanceFrom(mCurrentLat, mCurrentLng, mLatitude, mLongitude);
                            }
                        } else {
                            dist = 0;
                        }

                        placesModel.setDistance(dist);
                        mPlacesModelList.add(placesModel);
                    }

                }

                addAdsToList();

                mFeedsAdapter.notifyDataSetChanged();
                progressView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                progressView.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        supportFinishAfterTransition();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_search:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    circleReveal(R.id.searchtoolbar, 1, true, true);
                else
                    searchtollbar.setVisibility(View.VISIBLE);

                mToolbar.setBackgroundColor(ContextCompat.getColor(FeedsActivity.this, R.color.material_gery));
                vCollapsingToolbarLayout.setCollapsedTitleTextColor(ContextCompat.getColor(this, R.color.transparent_color));
                item_search.expandActionView();
                return true;
            case R.id.context_menu:
                if (mFragmentManager.findFragmentByTag(ContextMenuDialogFragment.TAG) == null) {
                    mMenuDialogFragment.show(mFragmentManager, ContextMenuDialogFragment.TAG);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void sortAllPlacesListByDistance() {
        for (int i = 0; i < mPlacesModelList.size(); i++) {
            if (mPlacesModelList.get(i).getViewType() == 2) {
                mPlacesModelList.remove(i);
            }
        }
        Collections.sort(mPlacesModelList, new Comparator<PlacesModel>() {
            @Override
            public int compare(PlacesModel one, PlacesModel another) {
                int returnVal = 0;
                if (one.getViewType() != 2) {
                    if (one.getDistance() < another.getDistance()) {
                        returnVal = -1;
                    } else if (one.getDistance() > another.getDistance()) {
                        returnVal = 1;
                    } else if (one.getDistance() == another.getDistance()) {
                        returnVal = 0;
                    }
                    return returnVal;
                } else {
                    return returnVal;
                }
            }
        });
    }

    public void sortAllPlacesListByName() {
        for (int i = 0; i < mPlacesModelList.size(); i++) {
            if (mPlacesModelList.get(i).getViewType() == 2) {
                mPlacesModelList.remove(i);
            }
        }
        Collections.sort(mPlacesModelList, new Comparator<PlacesModel>() {
            public int compare(PlacesModel o1, PlacesModel o2) {
                if (o2.getViewType() != 2 && o1.getViewType() != 2) {
                    return o1.getPlaceName().compareTo(o2.getPlaceName());
                } else {
                    return 0;
                }
            }
        });
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

}
