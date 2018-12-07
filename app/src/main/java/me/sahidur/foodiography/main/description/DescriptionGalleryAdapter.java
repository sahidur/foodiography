package me.sahidur.foodiography.main.description;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.models.GalleryModel;
import me.sahidur.foodiography.main.utils.Constants;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;



public class DescriptionGalleryAdapter extends RecyclerView.Adapter<DescriptionGalleryAdapter.MyViewHolder> {

    private Context mContext;
    private List<GalleryModel> mGalleryModel;
    FirebaseDatabase mFDatabase;
    FirebaseAuth mFAuth;

    public DescriptionGalleryAdapter(Context mContext, List<GalleryModel> mGalleryModel) {
        this.mContext = mContext;
        this.mGalleryModel = mGalleryModel;
        mFDatabase = FirebaseDatabase.getInstance();
        mFAuth = FirebaseAuth.getInstance();
    }

    @Override
    public DescriptionGalleryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.description_adapter_gallery_item, parent, false);

        return new DescriptionGalleryAdapter.MyViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(final DescriptionGalleryAdapter.MyViewHolder holder, int position) {

        final GalleryModel album = mGalleryModel.get(position);

        String pathUrl = "gallery/" + album.getPlaceId() + "/" + album.getFileName() + "?alt=media";
        final String urlImg = "https://firebasestorage.googleapis.com/v0/b/" + Constants.FIREBASE_PROJECT_ID + ".appspot.com/o/" + pathUrl.replace("/", "%2F");

        Glide.with(mContext.getApplicationContext())
                .load(urlImg)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .error(R.drawable.no_image)
                .into(holder.vImg);
        final String[] uName = new String[1];
        DatabaseReference myURef = mFDatabase.getReference("users").child(mFAuth.getCurrentUser().getUid()).child("displayName");
        myURef.keepSynced(true);
        myURef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String displayName = snapshot.getValue(String.class);
                uName[0] = displayName;
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.e(Constants.TAG_PARENT, "GalleryListAdapter:" + firebaseError.getMessage());
            }
        });

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent go = new Intent(mContext, GallerySingleActivity.class);
                go.putExtra("urlImg", urlImg);
                go.putExtra("fileName", album.getFileName());
                go.putExtra("userId", album.getUid());
                go.putExtra("placeId", album.getPlaceId());
                go.putExtra("imageId", album.getImageId());
                mContext.startActivity(go);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mGalleryModel.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView vImg;
        CardView cardView;

        public MyViewHolder(View view) {
            super(view);
            vImg = (ImageView) view.findViewById(R.id.imageView);
            cardView = (CardView) view.findViewById(R.id.cardView);

        }
    }
}