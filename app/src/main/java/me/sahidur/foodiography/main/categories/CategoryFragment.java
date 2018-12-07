package me.sahidur.foodiography.main.categories;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.utils.Config;
import me.sahidur.foodiography.main.utils.Constants;
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


public class CategoryFragment extends Fragment {

    @Bind(R.id.recycleView)
    RecyclerView recyclerView;

    FirebaseDatabase mFData;
    List<CategoryModel> mCategoryModelList;
    CategoryAdapter mCategoryAdapter;
    StaggeredGridLayoutManager mGaggeredGridLayoutManager;


    public CategoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFData = FirebaseDatabase.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.shared_fragment, container, false);

        ButterKnife.bind(this, rootView);

        mCategoryModelList = new ArrayList<>();
        mCategoryAdapter = new CategoryAdapter(getActivity(), mCategoryModelList, getArguments().getString("mCurrentLocation"));

        loadCategoryList();

        if(Config.getDensityDpi(getActivity()) > 7) {
            mGaggeredGridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        }else{
            mGaggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        }

        recyclerView.setLayoutManager(mGaggeredGridLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, Config.dpToPx(getActivity(),10), true));
        recyclerView.setAdapter(mCategoryAdapter);

        return rootView;
    }

    void loadCategoryList(){
        DatabaseReference dRef = mFData.getReference("categories");
        dRef.keepSynced(true);
        dRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mCategoryModelList.clear();
                for(DataSnapshot snap : dataSnapshot.getChildren()){
                    CategoryModel model = snap.getValue(CategoryModel.class);
                    model.setCategoryId(snap.getKey());
                    mCategoryModelList.add(model);
                }
                sortCategoryListByName();
                mCategoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(Constants.TAG_PARENT, databaseError.getMessage());
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

    public void sortCategoryListByName() {

        Collections.sort(mCategoryModelList, new Comparator<CategoryModel>() {
            public int compare(CategoryModel o1, CategoryModel o2) {
                return o1.getCategoryName().compareTo(o2.getCategoryName());
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
