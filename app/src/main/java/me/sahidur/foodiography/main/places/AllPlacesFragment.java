package me.sahidur.foodiography.main.places;



import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.ar.rotation.ARActivity;
import me.sahidur.foodiography.main.contextmenu.ContextMenuDialogFragment;
import me.sahidur.foodiography.main.contextmenu.MenuObject;
import me.sahidur.foodiography.main.contextmenu.MenuParams;
import me.sahidur.foodiography.main.contextmenu.interfaces.OnMenuItemClickListener;
import me.sahidur.foodiography.main.utils.Config;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


public class AllPlacesFragment extends Fragment implements OnMenuItemClickListener {

    @Bind(R.id.recycleView)
    RecyclerView recyclerView;

    FirebaseDatabase mFDatabase;
    List<PlacesModel> mPlacesModelList;
    AllPlacesAdapter mAllPlacesAdapter;
    StaggeredGridLayoutManager mGaggeredGridLayoutManager;
    FragmentManager mFragmentManager;
    ContextMenuDialogFragment mMenuDialogFragment;

    public AllPlacesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFDatabase = FirebaseDatabase.getInstance();
        mFragmentManager = getChildFragmentManager();
        setHasOptionsMenu(true);
        initMenuFragment();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.all_place_fragment_menu, menu);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        View v = searchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
        v.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.transparent_color));
        searchView.setQueryHint(getString(R.string.hint_search));
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconifiedByDefault(true);

        SearchView.OnQueryTextListener textChangeListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String query) {
                query = query.toLowerCase();
                callSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                query = query.toLowerCase();
                callSearch(query);
                searchView.clearFocus();
                return true;
            }

            public void callSearch(String query) {
                mAllPlacesAdapter.getFilter().filter(query);
            }
        };
        searchView.setOnQueryTextListener(textChangeListener);
    }

    @Override
    public void onMenuItemClick(View clickedView, int position) {
        if (position == 1) {
            sortListByName();
            mAllPlacesAdapter.notifyDataSetChanged();
        } else if (position == 2) {
            sortListByDistance();
            mAllPlacesAdapter.notifyDataSetChanged();
        } else if (position == 3) {
            Intent ar = new Intent(getActivity(), ARActivity.class);
            ar.putExtra("categoryId", "all_place");
            ar.putExtra("categoryName", "AR Location");
            startActivity(ar);
        }
    }

    public void sortListByDistance() {
        Collections.sort(mPlacesModelList, new Comparator<PlacesModel>() {
            @Override
            public int compare(PlacesModel one, PlacesModel another) {
                int returnVal = 0;

                if (one.getDistance() < another.getDistance()) {
                    returnVal = -1;
                } else if (one.getDistance() > another.getDistance()) {
                    returnVal = 1;
                } else if (one.getDistance() == another.getDistance()) {
                    returnVal = 0;
                }
                return returnVal;
            }
        });
    }

    public void sortListByName() {

        Collections.sort(mPlacesModelList, new Comparator<PlacesModel>() {
            public int compare(PlacesModel o1, PlacesModel o2) {
                return o1.getPlaceName().compareTo(o2.getPlaceName());
            }
        });
    }

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.context_menu:
                if (mFragmentManager.findFragmentByTag(ContextMenuDialogFragment.TAG) == null) {
                    mMenuDialogFragment.show(mFragmentManager, ContextMenuDialogFragment.TAG);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.shared_fragment, container, false);

        ButterKnife.bind(this, rootView);

        mPlacesModelList = new ArrayList<>();
        mAllPlacesAdapter = new AllPlacesAdapter(getActivity(), mPlacesModelList, getArguments().getString("mCurrentLocation"));

        loadAllPlaces();

        if (Config.getDensityDpi(getActivity()) > 7) {
            mGaggeredGridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        } else {
            mGaggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        }

        recyclerView.setLayoutManager(mGaggeredGridLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(2), true));
        recyclerView.setAdapter(mAllPlacesAdapter);

        // Inflate the layout for this fragment
        return rootView;
    }

    void loadAllPlaces() {
        DatabaseReference dRef = mFDatabase.getReference("places");
        dRef.keepSynced(true);
        dRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mPlacesModelList.clear();
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    PlacesModel model = snap.getValue(PlacesModel.class);
                    model.setPlaceId(snap.getKey());
                    float dist;
                    if (!model.getLatlong().equals("0")) {
                        String[] newLoc = model.getLatlong().split(",");
                        double mLatitude = Double.parseDouble(newLoc[0].trim());
                        double mLongitude = Double.parseDouble(newLoc[1].trim());

                        String[] loc = getArguments().getString("mCurrentLocation").split(",");
                        double curLat = Double.parseDouble(loc[0].trim());
                        double curLong = Double.parseDouble(loc[1].trim());

                        if (curLat == 0 || curLong == 0) {
                            dist = 0;
                        } else {
                            dist = Config.distanceFrom(curLat, curLong, mLatitude, mLongitude);
                        }
                    } else {
                        dist = 0;
                    }

                    model.setDistance(dist);
                    mPlacesModelList.add(model);
                }
                mAllPlacesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
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
