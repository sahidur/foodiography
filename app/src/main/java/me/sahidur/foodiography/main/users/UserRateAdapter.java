package me.sahidur.foodiography.main.users;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.description.DescriptionActivity;
import me.sahidur.foodiography.main.models.UserRateModel;
import me.sahidur.foodiography.main.places.PlacesModel;
import me.sahidur.foodiography.main.utils.Config;
import me.sahidur.foodiography.main.utils.Constants;
import me.sahidur.foodiography.main.utils.TimeDifference;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;


public class UserRateAdapter extends RecyclerView.Adapter<UserRateAdapter.MyViewHolder> {

    private Context mContext;
    private List<UserRateModel> mUserRateModelList;
    private LatLng mCurrentLocation;

    FirebaseDatabase mFDatabase;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, comment, date, rate;
        public ImageView thumbnail;
        public RelativeLayout card_view;
        public ProgressBar progressView;
        public RatingBar ratingBar;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            comment = (TextView) view.findViewById(R.id.review);
            date = (TextView) view.findViewById(R.id.timestamp);
            rate = (TextView) view.findViewById(R.id.rateNum);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            card_view = (RelativeLayout) view.findViewById(R.id.card_view);
            progressView = (ProgressBar) view.findViewById(R.id.progress_view);
            ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);
        }
    }

    public UserRateAdapter(Context mContext, List<UserRateModel> firebaseDBs, LatLng mCurrentLocation) {
        this.mContext = mContext;
        this.mUserRateModelList = firebaseDBs;
        this.mCurrentLocation = mCurrentLocation;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.users_adapter_user_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final UserRateModel album = mUserRateModelList.get(position);

        mFDatabase = FirebaseDatabase.getInstance();

        holder.comment.setText(album.getReview());
        TimeDifference now = new TimeDifference(mContext, Config.getDefault(), Config.serverDate(album.getTimestamp()));
        holder.date.setText(now.getDifferenceString());
        holder.rate.setText(String.valueOf(album.getRateNum()));
        holder.ratingBar.setRating(album.getRateNum());

        getData(holder, album.getPlaceId());

    }

    public void getData(final MyViewHolder holder, final String placeId) {
        holder.progressView.setVisibility(View.VISIBLE);
        final Animation animAlpha = AnimationUtils.loadAnimation(mContext,
                R.anim.alpha_button);
        DatabaseReference myRef = mFDatabase.getReference("places");
        myRef.keepSynced(true);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                holder.progressView.setVisibility(View.VISIBLE);
                //firebaseDBList.clear();

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {

                    if (postSnapshot.getKey().equals(placeId)) {
                        final PlacesModel fb = postSnapshot.getValue(PlacesModel.class);
                        float dist;
                        if (!fb.getLatlong().equals("0")) {
                            String[] newLoc = fb.getLatlong().split(",");
                            double mLatitude = Double.parseDouble(newLoc[0].trim());
                            double mLongitude = Double.parseDouble(newLoc[1].trim());

                            if (mCurrentLocation.latitude == 0 || mCurrentLocation.longitude == 0) {
                                dist = 0;
                            } else {
                                dist = Config.distanceFrom(mCurrentLocation.latitude, mCurrentLocation.longitude, mLatitude, mLongitude);
                            }
                        } else {
                            dist = 0;
                        }

                        fb.setDistance(dist);
                        fb.setPlaceId(postSnapshot.getKey());

                        String pathUrl = "gallery/" + placeId + "/" + fb.getImageThumbnail() + "?alt=media";
                        String urlImg;

                        if (fb.getImageThumbnail().equalsIgnoreCase("no_image.png")) {
                            urlImg = "https://firebasestorage.googleapis.com/v0/b/"+Constants.FIREBASE_PROJECT_ID+".appspot.com/o/no_image.png?alt=media";
                        } else {
                            urlImg = "https://firebasestorage.googleapis.com/v0/b/"+Constants.FIREBASE_PROJECT_ID+".appspot.com/o/" + pathUrl.replace("/", "%2F");
                        }

                        Glide.with(mContext).load(urlImg)
                                .crossFade()
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .listener(new RequestListener<String, GlideDrawable>() {
                                    @Override
                                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                        holder.progressView.setVisibility(View.INVISIBLE);
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                        holder.progressView.setVisibility(View.INVISIBLE);
                                        return false;
                                    }
                                })
                                .error(R.drawable.no_image)
                                .override(500, 250)
                                .into(holder.thumbnail);

                        holder.title.setText(fb.getPlaceName());
                        final String cur = mCurrentLocation.latitude + "," + mCurrentLocation.longitude;
                        holder.card_view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                view.startAnimation(animAlpha);
                                Intent go = new Intent(mContext, DescriptionActivity.class);
                                go.putExtra("address", fb.getAddress());
                                go.putExtra("category", fb.getCategory());
                                go.putExtra("description", fb.getDescription());
                                go.putExtra("distance", fb.getDistance());
                                go.putExtra("facilities", fb.getFacilities());
                                go.putExtra("imageThumbnail", fb.getImageThumbnail());
                                go.putExtra("info", fb.getInfo());
                                go.putExtra("latlong", fb.getLatlong());
                                go.putExtra("placeId", fb.getPlaceId());
                                go.putExtra("placeName", fb.getPlaceName());
                                go.putExtra("mCurrentLocation", cur);

                                Pair<View, String> p1 = Pair.create((View) holder.title, "judul");
                                //Pair<View, String> p2 = Pair.create((View) holder.count, "alamat");
                                //Pair<View, String> p3 = Pair.create((View) holder.dist, "jarak");
                                Pair<View, String> p4 = Pair.create((View) holder.thumbnail, "imageThumbnail");

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, p1, p4);
                                    mContext.startActivity(go, options.toBundle());
                                } else {
                                    mContext.startActivity(go);
                                }
                            }
                        });
                    }

                }

                holder.progressView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                holder.progressView.setVisibility(View.INVISIBLE);
            }
        });
    }
    @Override
    public int getItemCount() {
        return mUserRateModelList.size();
    }
}