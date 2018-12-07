package me.sahidur.foodiography.main.feeds;

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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.description.DescriptionActivity;
import me.sahidur.foodiography.main.places.PlacesModel;
import me.sahidur.foodiography.main.utils.Constants;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;


public class FeedsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    List<PlacesModel> mStringFilterList;
    ValueFilter valueFilter;
    private Context mContext;
    private List<PlacesModel> mFirebaseDataModels;
    private LatLng mCurrentLocation;

    boolean enable_ads_native;

    public FeedsAdapter(Context mContext, List<PlacesModel> mFirebaseDataModels, LatLng mCurrentLocation, boolean enable_ads_native) {
        this.mContext = mContext;
        this.mFirebaseDataModels = mFirebaseDataModels;
        this.mCurrentLocation = mCurrentLocation;
        this.mStringFilterList = mFirebaseDataModels;
        this.enable_ads_native = enable_ads_native;
    }

    @Override
    public Filter getFilter() {
        if (valueFilter == null) {
            valueFilter = new ValueFilter();
        }
        return valueFilter;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case 1: {
                View v = inflater.inflate(R.layout.feeds_adapter_feeds_item, parent, false);
                viewHolder = new ItemHolder(v);
                break;
            }
            case 2: {
                View v = inflater.inflate(R.layout.list_admob, parent, false);
                viewHolder = new AdmobHolder(v);
                break;
            }
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        switch (getItem(position).getViewType()) {
            case 1: {
                final ItemHolder holder = (ItemHolder) viewHolder;
                final PlacesModel album = getItem(position);
                holder.title.setText(album.getPlaceName().replace("_", " "));
                holder.count.setText(album.getAddress());
                if (album.getDistance() == 0) {
                    holder.dist.setVisibility(View.INVISIBLE);
                } else {
                    holder.dist.setText(String.valueOf(String.format("%.2f", album.getDistance() / 1000f)) + " Km");
                }

                final String mCurLoc = mCurrentLocation.latitude + "," + mCurrentLocation.longitude;

                String pathUrl = "gallery/" + album.getPlaceId() + "/" + album.getImageThumbnail() + "?alt=media";
                String urlImg;

                if (album.getImageThumbnail().equalsIgnoreCase("no_image_image.png")) {
                    urlImg = "https://firebasestorage.googleapis.com/v0/b/"+Constants.FIREBASE_PROJECT_ID+".appspot.com/o/no_image.png?alt=media";
                } else {
                    urlImg = "https://firebasestorage.googleapis.com/v0/b/"+Constants.FIREBASE_PROJECT_ID+".appspot.com/o/" + pathUrl.replace("/", "%2F");
                }

                Glide.with(mContext.getApplicationContext()).load(urlImg)
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
                        .into(holder.thumbnail);

                holder.card_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent go = new Intent(mContext, DescriptionActivity.class);
                        go.putExtra("address", album.getAddress());
                        go.putExtra("category", album.getCategory());
                        go.putExtra("description", album.getDescription());
                        go.putExtra("distance", album.getDistance());
                        go.putExtra("facilities", album.getFacilities());
                        go.putExtra("imageThumbnail", album.getImageThumbnail());
                        go.putExtra("info", album.getInfo());
                        go.putExtra("latlong", album.getLatlong());
                        go.putExtra("placeId", album.getPlaceId());
                        go.putExtra("placeName", album.getPlaceName());
                        go.putExtra("mCurrentLocation", mCurLoc);

                        Pair<View, String> p1 = Pair.create((View) holder.thumbnail, "imageThumbnail");
                        Pair<View, String> p2 = Pair.create((View) holder.card_view, "backgroundCategories");

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, p1, p2);
                            mContext.startActivity(go, options.toBundle());
                        } else {
                            mContext.startActivity(go);
                        }
                    }
                });

                break;
            }
            case 2: {
                final AdmobHolder holder = (AdmobHolder) viewHolder;
                if(enable_ads_native){


                    holder.mAdView.setVisibility(View.GONE);

                    holder.mAdView.setAdListener(new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            holder.mAdView.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAdClosed() {
                            holder.mAdView.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAdFailedToLoad(int i) {
                            holder.mAdView.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAdOpened() {
                            holder.mAdView.setVisibility(View.VISIBLE);
                        }

                    });
                    AdRequest adRequest = new AdRequest.Builder()
                            // for testing add this .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                            .build();
                    holder.mAdView.loadAd(adRequest);
                }else{
                    holder.mAdView.setVisibility(View.GONE);
                }

                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mFirebaseDataModels.get(position).getViewType();
    }

    public PlacesModel getItem(int position) {
        return mFirebaseDataModels.get(position);
    }

    @Override
    public int getItemCount() {
        return mFirebaseDataModels.size();
    }

    public static class ItemHolder extends RecyclerView.ViewHolder {
        public TextView title, count, dist;
        public ImageView thumbnail;
        public CardView card_view;
        public ProgressBar progressView;

        public ItemHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            count = (TextView) view.findViewById(R.id.count);
            dist = (TextView) view.findViewById(R.id.dist);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            card_view = (CardView) view.findViewById(R.id.card_view);
            progressView = (ProgressBar) view.findViewById(R.id.progress_view);
        }
    }

    public static class AdmobHolder extends RecyclerView.ViewHolder {
        public NativeExpressAdView mAdView;

        public AdmobHolder(View view) {
            super(view);
            mAdView = (NativeExpressAdView) view.findViewById(R.id.adViewList);
        }
    }

    private class ValueFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                List<PlacesModel> filterList = new ArrayList<PlacesModel>();
                for (int i = 0; i < mStringFilterList.size(); i++) {
                    if (mStringFilterList.get(i).getViewType() == 2) {
                        mStringFilterList.remove(i);
                    }

                    if ((mStringFilterList.get(i).getPlaceName().toLowerCase())
                            .contains(constraint.toString().toLowerCase())) {

                        PlacesModel newFilter = new PlacesModel();
                        newFilter.setDistance(mStringFilterList.get(i).getDistance());
                        newFilter.setViewType(mStringFilterList.get(i).getViewType());
                        newFilter.setPlaceId(mStringFilterList.get(i).getPlaceId());
                        newFilter.setAddress(mStringFilterList.get(i).getAddress());
                        newFilter.setCategory(mStringFilterList.get(i).getCategory());
                        newFilter.setDescription(mStringFilterList.get(i).getDescription());
                        newFilter.setFacilities(mStringFilterList.get(i).getFacilities());
                        newFilter.setImageThumbnail(mStringFilterList.get(i).getImageThumbnail());
                        newFilter.setLatlong(mStringFilterList.get(i).getLatlong());
                        newFilter.setInfo(mStringFilterList.get(i).getInfo());
                        newFilter.setPlaceName(mStringFilterList.get(i).getPlaceName());
                        filterList.add(newFilter);
                    }
                }

                if(enable_ads_native){
                    PlacesModel nativeModel = new PlacesModel();
                    nativeModel.setViewType(2);
                    if (filterList.size() > 2) {
                        int interval = 3;
                        for (int i = 0; i < (filterList.size() / interval); i++) {
                            int posisi = ((i + 1) * (interval));
                            if (posisi < filterList.size()) {
                                filterList.add(posisi, nativeModel);
                            }
                        }
                    }
                }

                results.count = filterList.size();
                results.values = filterList;
            } else {
                results.count = mStringFilterList.size();
                results.values = mStringFilterList;
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            mFirebaseDataModels = (List<PlacesModel>) results.values;
            notifyDataSetChanged();
        }
    }
}