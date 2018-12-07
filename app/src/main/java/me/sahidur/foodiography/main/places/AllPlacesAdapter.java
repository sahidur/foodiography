package me.sahidur.foodiography.main.places;

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
import android.widget.TextView;

import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.description.DescriptionActivity;
import me.sahidur.foodiography.main.utils.Constants;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;



public class AllPlacesAdapter extends RecyclerView.Adapter<AllPlacesAdapter.MyViewHolder> implements Filterable {


    List<PlacesModel> mPlacesModelList;
    Context mContext;
    String mCurrentLocation;

    List<PlacesModel> mStringFilterList;
    ValueFilter valueFilter;

    public AllPlacesAdapter(Context mContext, List<PlacesModel> mPlacesModelList, String mCurrentLocation) {
        this.mContext = mContext;
        this.mPlacesModelList = mPlacesModelList;
        this.mCurrentLocation = mCurrentLocation;
        this.mStringFilterList = mPlacesModelList;
    }

    @Override
    public Filter getFilter() {
        if (valueFilter == null) {
            valueFilter = new ValueFilter();
        }
        return valueFilter;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView txtPlaceName;
        public TextView txtDistance;
        public ImageView imgThumbnail;
        public CardView card_view;

        public MyViewHolder(View view) {
            super(view);
            txtPlaceName = (TextView) view.findViewById(R.id.txtPlaceName);
            txtDistance = (TextView) view.findViewById(R.id.txtDistance);
            imgThumbnail = (ImageView) view.findViewById(R.id.imgThumbnail);
            card_view = (CardView) view.findViewById(R.id.cardView);
        }
    }


    @Override
    public AllPlacesAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.places_allplaces_adapter_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final AllPlacesAdapter.MyViewHolder holder, int position) {
        final PlacesModel places = mPlacesModelList.get(position);
        holder.txtPlaceName.setText(places.getPlaceName());
        holder.txtDistance.setText(String.valueOf(String.format("%.2f", places.getDistance() / 1000f)) + " Km");

        String imgUrl = "https://firebasestorage.googleapis.com/v0/b/"+ Constants.FIREBASE_PROJECT_ID+".appspot.com/o/gallery%2F" + places.getPlaceId() + "%2F" + places.getImageThumbnail() + "?alt=media";

        Glide.with(mContext.getApplicationContext()).load(imgUrl)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.no_image)
                .into(holder.imgThumbnail);

        holder.card_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent go = new Intent(mContext, DescriptionActivity.class);
                go.putExtra("address", places.getAddress());
                go.putExtra("category", places.getCategory());
                go.putExtra("description", places.getDescription());
                go.putExtra("distance", places.getDistance());
                go.putExtra("facilities", places.getFacilities());
                go.putExtra("imageThumbnail", places.getImageThumbnail());
                go.putExtra("info", places.getInfo());
                go.putExtra("latlong", places.getLatlong());
                go.putExtra("placeId", places.getPlaceId());
                go.putExtra("placeName", places.getPlaceName());
                go.putExtra("mCurrentLocation", mCurrentLocation);

                Pair<View, String> p1 = Pair.create((View) holder.imgThumbnail, "imageThumbnail");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, p1);
                    mContext.startActivity(go, options.toBundle());
                } else {
                    mContext.startActivity(go);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPlacesModelList.size();
    }

    private class ValueFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                List<PlacesModel> filterList = new ArrayList<PlacesModel>();
                for (int i = 0; i < mStringFilterList.size(); i++) {

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
            mPlacesModelList = (List<PlacesModel>) results.values;
            notifyDataSetChanged();
        }
    }
}
