package me.sahidur.foodiography.main.gallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.models.GalleryModel;
import me.sahidur.foodiography.main.utils.Config;
import me.sahidur.foodiography.main.utils.Constants;
import me.sahidur.foodiography.main.utils.TimeDifference;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;


public class GalleryListAdapter extends RecyclerView.Adapter<GalleryListAdapter.MyViewHolder> {

    private Context mContext;
    private List<GalleryModel> mGalleyModelList;
    private FirebaseDatabase mFDatabase;
    String sPlaceId;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView vTitle, vTitleName;
        public ImageView vThumbnail, vImgUser;
        public CardView vCardView;
        ProgressBar vProgressBar;

        public MyViewHolder(View view) {
            super(view);
            vTitle = (TextView) view.findViewById(R.id.title);
            vTitleName = (TextView) view.findViewById(R.id.timestamp);
            vThumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            vImgUser = (ImageView) view.findViewById(R.id.imgUser);
            vCardView = (CardView) view.findViewById(R.id.card_view);
            vProgressBar = (ProgressBar) view.findViewById(R.id.progress_view);
        }
    }


    public GalleryListAdapter(Context mContext, List<GalleryModel> mGalleyModelList, String sPlaceId) {
        this.mContext = mContext;
        this.mGalleyModelList = mGalleyModelList;
        this.sPlaceId = sPlaceId;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gallery_list_adapter_items, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        mFDatabase = FirebaseDatabase.getInstance();

        GalleryModel album = mGalleyModelList.get(position);
        TimeDifference now = new TimeDifference(mContext, Config.getDefault(), Config.serverDate(album.getTimestamp()));
        holder.vTitleName.setText(now.getDifferenceString());

        String pathUrl = "gallery/" + sPlaceId + "/" + album.getFileName() + "?alt=media";
        String urlImg = "https://firebasestorage.googleapis.com/v0/b/"+Constants.FIREBASE_PROJECT_ID+".appspot.com/o/" + pathUrl.replace("/", "%2F");

        Glide.with(mContext.getApplicationContext())
                .load(urlImg)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        holder.vProgressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        holder.vProgressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .error(R.drawable.no_image)
                .into(holder.vThumbnail);

        DatabaseReference myRef = mFDatabase.getReference("users").child( album.getUid()).child("displayName");
        myRef.keepSynced(true);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String displayName = snapshot.getValue(String.class);
                holder.vTitle.setText(displayName);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.e(Constants.TAG_PARENT, "GalleryListAdapter:" + firebaseError.getMessage());
            }
        });

        DatabaseReference myRefFoto = mFDatabase.getReference("users").child( album.getUid()).child("photoUrl");
        myRefFoto.keepSynced(true);
        myRefFoto.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Glide.with(mContext.getApplicationContext())
                        .load(snapshot.getValue(String.class))
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .error(R.drawable.ic_app)
                        .into(new BitmapImageViewTarget(holder.vImgUser) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                RoundedBitmapDrawable rounded =
                                        RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
                                rounded.setCircular(true);
                                holder.vImgUser.setImageDrawable(rounded);
                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.e(Constants.TAG_PARENT, "GalleryListAdapter:PhotoUrl:" + firebaseError.getMessage());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mGalleyModelList.size();
    }
}
