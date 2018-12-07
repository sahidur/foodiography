package me.sahidur.foodiography.main.reviews;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.models.UserRateModel;
import me.sahidur.foodiography.main.utils.Config;
import me.sahidur.foodiography.main.utils.TimeDifference;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.MyViewHolder> {

    private Context mContext;
    private List<UserRateModel> mUserRateModelList;
    private FirebaseDatabase mFDatabase;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView vComments, vDate, vDisplayName;
        public ImageView vUserImg;
        RelativeLayout vRelativeLayout;
        RatingBar vRatingBar;

        public MyViewHolder(View view) {
            super(view);
            vComments = (TextView) view.findViewById(R.id.textComments);
            vDisplayName = (TextView) view.findViewById(R.id.textUser);
            vDate = (TextView) view.findViewById(R.id.textTanggal);
            vUserImg = (ImageView) view.findViewById(R.id.img_user);
            vRelativeLayout = (RelativeLayout) view.findViewById(R.id.relFirst);
            vRatingBar = (RatingBar) view.findViewById(R.id.ratingBar);
        }
    }

    public ReviewAdapter(Context mContext, List<UserRateModel> mUserRateModelList) {
        this.mContext = mContext;
        this.mUserRateModelList = mUserRateModelList;
    }

    @Override
    public ReviewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reviews_review_adapter_item, parent, false);

        return new ReviewAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ReviewAdapter.MyViewHolder holder, int position) {

        final UserRateModel album = mUserRateModelList.get(position);
        mFDatabase = FirebaseDatabase.getInstance();

        holder.vComments.setText(album.getReview());
        TimeDifference now = new TimeDifference(mContext, Config.getDefault(), Config.serverDate(album.getTimestamp()));
        holder.vDate.setText(now.getDifferenceString());
        holder.vRatingBar.setRating(album.getRateNum());

        DatabaseReference userNameRef = mFDatabase.getReference("users").child(album.getUid()).child("displayName");
        userNameRef.keepSynced(true);
        userNameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                holder.vDisplayName.setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "error:" + databaseError.getMessage());
            }
        });

        DatabaseReference fotoUsetRef = mFDatabase.getReference("users").child(album.getUid()).child("photoUrl");
        fotoUsetRef.keepSynced(true);
        fotoUsetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                updateUI(dataSnapshot.getValue(String.class), holder.vUserImg);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "error:" + databaseError.getMessage());
            }
        });

    }

    void updateUI(String img, final ImageView v) {

        Glide.with(mContext.getApplicationContext())
                .load(img)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.no_image)
                .into(v);

    }

    @Override
    public int getItemCount() {
        return mUserRateModelList.size();
    }
}