package me.sahidur.foodiography.main.users;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.models.UserRateModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserRateFragment extends Fragment {

    FirebaseDatabase mFDatabase;
    ProgressBar vProgress;
    RecyclerView vRecycleView;
    private List<UserRateModel> mUserRateModelList;
    private UserRateAdapter mUserRateAdapter;
    String userID;
    LinearLayout vNoReview;
    LatLng mCurrentLocation;
    TextView vTextRev;
    ImageView noImage;
    double mCurrentLat, mCurrentLng;

    public UserRateFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.users_fragment_user, container, false);
        setHasOptionsMenu(true);
        userID = getArguments().getString("userID");

        String[] cuText = getArguments().getString("mCurrentLocation").split(",");
        mCurrentLat = Double.parseDouble(cuText[0]);
        mCurrentLng = Double.parseDouble(cuText[1]);

        mCurrentLocation = new LatLng(mCurrentLat, mCurrentLng);

        mUserRateModelList = new ArrayList<>();

        vProgress = (ProgressBar) v.findViewById(R.id.progressBar);
        vNoReview = (LinearLayout) v.findViewById(R.id.noreview);
        vTextRev = (TextView) v.findViewById(R.id.txRev);
        noImage = (ImageView) v.findViewById(R.id.noImg);

        noImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_no_review));
        vTextRev.setText(R.string.no_reviews);

        vProgress.setVisibility(View.VISIBLE);

        vRecycleView = (RecyclerView) v.findViewById(R.id.recycler_view);

        mUserRateAdapter = new UserRateAdapter(getActivity(), mUserRateModelList, mCurrentLocation);

        getData(userID);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        //RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 1);
        vRecycleView.setLayoutManager(layoutManager);
        vRecycleView.setItemAnimator(new DefaultItemAnimator());
        vRecycleView.setAdapter(mUserRateAdapter);

        return v;
    }

    public void getData(final String uid) {
        vProgress.setVisibility(View.VISIBLE);

        DatabaseReference myRef = mFDatabase.getReference("rate").child(uid);
        myRef.keepSynced(true);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                mUserRateModelList.clear();

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {

                    UserRateModel userRateModel = postSnapshot.getValue(UserRateModel.class);
                    userRateModel.setUid(uid);
                    userRateModel.setPlaceId(postSnapshot.getKey());
                    mUserRateModelList.add(userRateModel);
                }

                if(mUserRateModelList.size() > 0){
                    vNoReview.setVisibility(View.GONE);
                }else{
                    vNoReview.setVisibility(View.VISIBLE);
                }

                mUserRateAdapter.notifyDataSetChanged();

                vProgress.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                vProgress.setVisibility(View.INVISIBLE);
            }
        });
    }
}
