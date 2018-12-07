package me.sahidur.foodiography.main.description;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
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
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;



public class DescriptionReviewAdapter extends RecyclerView.Adapter<DescriptionReviewAdapter.MyViewHolder> {

    private Context mContext;
    private List<UserRateModel> mUserRateModels;
    private FirebaseDatabase mFDatabase;

    public DescriptionReviewAdapter(Context mContext, List<UserRateModel> mUserRateModels) {
        this.mContext = mContext;
        this.mUserRateModels = mUserRateModels;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reviews_review_adapter_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        final UserRateModel album = mUserRateModels.get(position);
        mFDatabase = FirebaseDatabase.getInstance();
        TimeDifference now = new TimeDifference(mContext, Config.getDefault(), Config.serverDate(album.getTimestamp()));
        holder.vDateText.setText(now.getDifferenceString());
        holder.vCommentText.setText(album.getReview());
        holder.vRatingBar.setRating(album.getRateNum());

        DatabaseReference userNameRef = mFDatabase.getReference("users").child(album.getUid()).child("displayName");
        userNameRef.keepSynced(true);
        userNameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                holder.vUserText.setText(dataSnapshot.getValue(String.class));
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
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .error(R.drawable.no_image)
                .into(new BitmapImageViewTarget(v) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable rounded =
                                RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
                        rounded.setCircular(true);
                        v.setImageDrawable(rounded);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return mUserRateModels.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView vCommentText, vDateText, vUserText;
        public ImageView vUserImg;
        RelativeLayout vRelativeLayout;
        RatingBar vRatingBar;

        public MyViewHolder(View view) {
            super(view);
            vCommentText = (TextView) view.findViewById(R.id.textComments);
            vUserText = (TextView) view.findViewById(R.id.textUser);
            vDateText = (TextView) view.findViewById(R.id.textTanggal);
            vUserImg = (ImageView) view.findViewById(R.id.img_user);
            vRelativeLayout = (RelativeLayout) view.findViewById(R.id.relFirst);
            vRatingBar = (RatingBar) view.findViewById(R.id.ratingBar);
        }
    }
}