package me.sahidur.foodiography.main.description;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.models.GalleryModel;
import me.sahidur.foodiography.main.utils.CardAdapter;
import me.sahidur.foodiography.main.utils.Constants;
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



public class DescriptionGalleryPagerAdapter extends PagerAdapter implements CardAdapter {

    ImageView vImageView, vImgUser;
    TextView vUploadBy;
    ProgressBar vProgressView;
    FirebaseDatabase mFDatabase;

    String sDisplayName = "";

    private List<CardView> mViews;
    private List<GalleryModel> mData;
    private float mBaseElevation;
    private Context mContext;

    public DescriptionGalleryPagerAdapter(Context mContext, List<GalleryModel> mData, List<CardView> mViews) {
        this.mContext = mContext;
        this.mData = mData;
        this.mViews = mViews;
        mFDatabase = FirebaseDatabase.getInstance();
    }

    public float getBaseElevation() {
        return mBaseElevation;
    }

    @Override
    public CardView getCardViewAt(int position) {
        return mViews.get(position);
    }

    @Override
    public int getCount() {
        if (!mData.isEmpty()) {
            return mData.size();
        } else {
            return 0;
        }

    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // TODO Auto-generated method stub
        View view = LayoutInflater.from(container.getContext())
                .inflate(R.layout.description_adapter_gallery_pager_item, container, false);
        container.addView(view);
        vImageView = (ImageView) view.findViewById(R.id.imageView);
        vImgUser = (ImageView) view.findViewById(R.id.img_user);
        vUploadBy = (TextView) view.findViewById(R.id.upload_at);
        vProgressView = (ProgressBar) view.findViewById(R.id.progress_view);

        bind(mData.get(position), vUploadBy, vImageView, vProgressView, vImgUser);

        CardView cardView = (CardView) view.findViewById(R.id.cardView);

        if (mBaseElevation == 0) {
            mBaseElevation = cardView.getCardElevation();
        }

        cardView.setMaxCardElevation(mBaseElevation * MAX_ELEVATION_FACTOR);
        mViews.set(position, cardView);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        mViews.set(position, null);
    }

    private void bind(final GalleryModel item, final TextView uploadBy,
                      ImageView imageView, final ProgressBar progressView, final ImageView imageView2) {
        progressView.setVisibility(View.VISIBLE);

        DatabaseReference myRef = mFDatabase.getReference("users").child(item.getUid()).child("displayName");
        myRef.keepSynced(true);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                sDisplayName = snapshot.getValue(String.class);
                uploadBy.setText(sDisplayName);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.e(Constants.TAG_PARENT, "DescriptionGalleryPagerAdapter:sDisplayName:" + firebaseError.getMessage());
            }
        });

        DatabaseReference myRefFoto = mFDatabase.getReference("users").child(item.getUid()).child("photoUrl");
        myRefFoto.keepSynced(true);
        myRefFoto.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Glide.with(mContext.getApplicationContext())
                        .load(snapshot.getValue(String.class))
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .error(R.drawable.no_image)
                        .into(new BitmapImageViewTarget(imageView2) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                RoundedBitmapDrawable rounded =
                                        RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
                                rounded.setCircular(true);
                                imageView2.setImageDrawable(rounded);
                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                Log.e(Constants.TAG_PARENT, "DescriptionGalleryPagerAdapter:PhotoUrl:" + firebaseError.getMessage());
            }
        });

        String pathUrl = "gallery/" + item.getPlaceId() + "/" + item.getFileName() + "?alt=media";
        String urlImg = "https://firebasestorage.googleapis.com/v0/b/"+Constants.FIREBASE_PROJECT_ID+".appspot.com/o/" + pathUrl.replace("/", "%2F");

        Glide.with(mContext.getApplicationContext()).load(urlImg)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        progressView.setVisibility(View.INVISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressView.setVisibility(View.INVISIBLE);
                        return false;
                    }
                })
                .error(R.drawable.no_image)
                .into(imageView);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

}