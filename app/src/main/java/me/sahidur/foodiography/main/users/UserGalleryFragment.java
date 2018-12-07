package me.sahidur.foodiography.main.users;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.models.GalleryModel;
import me.sahidur.foodiography.main.utils.Config;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class UserGalleryFragment extends Fragment {

    FirebaseDatabase mFDatabase;
    ProgressBar vProgress;
    RecyclerView vRecycleView;
    LinearLayout vNoGallery;
    TextView vTextRev;
    ImageView noImage;
    private List<GalleryModel> mGalleryModelList;
    private UserGalleryAdapter mUserGalleryAdapter;

    String userID;

    LatLng mCurrentLocation;
    double mCurrentLat, mCurrentLng;

    public UserGalleryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFDatabase = FirebaseDatabase.getInstance();
    }

    double densityDpi;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.users_fragment_user, container, false);
        setHasOptionsMenu(true);
        densityDpi = Config.getDensityDpi(getActivity());

        userID = getArguments().getString("userID");

        String[] cuText = getArguments().getString("mCurrentLocation").split(",");
        mCurrentLat = Double.parseDouble(cuText[0]);
        mCurrentLng = Double.parseDouble(cuText[1]);

        mCurrentLocation = new LatLng(mCurrentLat, mCurrentLng);

        mGalleryModelList = new ArrayList<>();

        vProgress = (ProgressBar) v.findViewById(R.id.progressBar);
        vNoGallery = (LinearLayout) v.findViewById(R.id.noreview);

        vTextRev = (TextView) v.findViewById(R.id.txRev);
        noImage = (ImageView) v.findViewById(R.id.noImg);
        noImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_images_no));
        vTextRev.setText(R.string.no_image_found);

        vProgress.setVisibility(View.VISIBLE);

        vRecycleView = (RecyclerView) v.findViewById(R.id.recycler_view);

        getPlaceId(userID);

        return v;
    }

    void getPlaceId(final String uid){
        DatabaseReference myRef = mFDatabase.getReference("gallery").child(uid);
        myRef.keepSynced(true);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                mGalleryModelList.clear();

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    getData(uid, postSnapshot.getKey());
                }

                vProgress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                vProgress.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void getData(final String uid, final String placeId) {
        vProgress.setVisibility(View.VISIBLE);

        DatabaseReference myRef = mFDatabase.getReference("gallery").child(uid).child(placeId);
        myRef.keepSynced(true);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                mGalleryModelList.clear();

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {

                    GalleryModel userRateModel = postSnapshot.getValue(GalleryModel.class);
                    userRateModel.setUid(uid);
                    userRateModel.setImageId(postSnapshot.getKey());
                    userRateModel.setPlaceId(placeId);
                    mGalleryModelList.add(userRateModel);

                }

                if(mGalleryModelList.size() > 0){
                    vNoGallery.setVisibility(View.GONE);
                }else{
                    vNoGallery.setVisibility(View.VISIBLE);
                }

                mUserGalleryAdapter = new UserGalleryAdapter(getActivity(), mGalleryModelList);

                RecyclerView.LayoutManager mLayoutManager;
                if (densityDpi > 7) {
                    mLayoutManager = new GridLayoutManager(getActivity(), 3);
                } else {
                    mLayoutManager = new GridLayoutManager(getActivity(), 2);
                }

                vRecycleView.setLayoutManager(mLayoutManager);
                vRecycleView.setItemAnimator(new DefaultItemAnimator());
                vRecycleView.setAdapter(mUserGalleryAdapter);
                vProgress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                vProgress.setVisibility(View.INVISIBLE);
            }
        });
    }
}
