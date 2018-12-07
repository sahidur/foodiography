package me.sahidur.foodiography.main.users;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.models.GalleryModel;
import me.sahidur.foodiography.main.places.PlacesModel;
import me.sahidur.foodiography.main.utils.Config;
import me.sahidur.foodiography.main.utils.Constants;
import me.sahidur.foodiography.main.utils.TimeDifference;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;



public class UserGalleryAdapter extends RecyclerView.Adapter<UserGalleryAdapter.MyViewHolder> {

    private Context mContext;
    private List<GalleryModel> mGalleriModelList;

    FirebaseDatabase mFDatabase;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, titleNum;
        public ImageView thumbnail;
        public CardView card_view;
        ProgressBar progressView;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            titleNum = (TextView) view.findViewById(R.id.titleNum);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            card_view = (CardView) view.findViewById(R.id.card_view);
            progressView = (ProgressBar) view.findViewById(R.id.progress_view);
        }
    }


    public UserGalleryAdapter(Context mContext, List<GalleryModel> mGalleriModelList) {
        this.mContext = mContext;
        this.mGalleriModelList = mGalleriModelList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.users_adapter_gallery_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        mFDatabase = FirebaseDatabase.getInstance();

        final GalleryModel album = mGalleriModelList.get(position);
        String pathUrl = "gallery/" + album.getPlaceId() + "/" + album.getFileName() + "?alt=media";
        final String urlImg = "https://firebasestorage.googleapis.com/v0/b/"+ Constants.FIREBASE_PROJECT_ID+".appspot.com/o/" + pathUrl.replace("/", "%2F");
        Glide.with(mContext.getApplicationContext()).load(urlImg)
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
                .into(holder.thumbnail);

        TimeDifference now = new TimeDifference(mContext, Config.getDefault(), Config.serverDate(album.getTimestamp()));
        holder.title.setText(now.getDifferenceString());

        holder.card_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent go = new Intent(mContext, ImageShowActivity.class);
                go.putExtra("urlImg", urlImg);
                go.putExtra("fileName", album.getFileName());
                go.putExtra("userId", album.getUid());
                go.putExtra("placeId", album.getPlaceId());
                go.putExtra("imageId", album.getImageId());

                Pair<View, String> p4 = Pair.create((View) holder.thumbnail, "imageThumbnail");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, p4);
                    mContext.startActivity(go, options.toBundle());
                } else {
                    mContext.startActivity(go);
                }
            }
        });

        getSingleData(holder, album.getPlaceId());
    }

    void getSingleData(final MyViewHolder holder, final String placeId) {
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
                        fb.setDistance(0);
                        fb.setPlaceId(postSnapshot.getKey());

                        holder.titleNum.setText(fb.getPlaceName());
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
        return mGalleriModelList.size();
    }
}
