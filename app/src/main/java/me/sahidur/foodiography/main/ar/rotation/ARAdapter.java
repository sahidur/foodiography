package me.sahidur.foodiography.main.ar.rotation;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.description.DescriptionActivity;
import me.sahidur.foodiography.main.places.PlacesModel;
import me.sahidur.foodiography.main.utils.Config;

import java.util.ArrayList;
import java.util.List;


public class ARAdapter extends RecyclerView.Adapter<ARAdapter.MyViewHolder> implements Filterable {


    List<PlacesModel> mPlacesModelList;
    Context mContext;
    Location mCurrentLocation;
    String categoryId;
    String categoryName;
    List<PlacesModel> mStringFilterList;
    ValueFilter valueFilter;

    int showDist;

    public ARAdapter(Context mContext, List<PlacesModel> mPlacesModelList, String categoryID, String categoryName, Location mCurrentLocation, int showDist) {
        this.mContext = mContext;
        this.mPlacesModelList = mPlacesModelList;
        this.mCurrentLocation = mCurrentLocation;
        this.mStringFilterList = mPlacesModelList;
        this.showDist = showDist;

        this.categoryId = categoryID;
        this.categoryName = categoryName;
    }

    @Override
    public Filter getFilter() {
        if (valueFilter == null) {
            valueFilter = new ValueFilter();
        }
        return valueFilter;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView t;
        public TextView d;
        public View v;
        public MyViewHolder(View view) {
            super(view);
            t = (TextView) view.findViewById(R.id.t);
            d = (TextView) view.findViewById(R.id.d);
            v = view;
        }
    }


    @Override
    public ARAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ar_view, parent, false);
        return new MyViewHolder(itemView);
    }

    int setSize(float dist) {
        int size;
        if (dist > 50000) {
            size = 12;
        } else if (dist <= 50000 && dist > 10000) {
            size = 14;
        } else if (dist <= 10000 && dist > 5000) {
            size = 16;
        } else if (dist <= 5000 && dist > 1000) {
            size = 18;
        } else if (dist <= 1000 && dist > 500) {
            size = 20;
        } else if (dist <= 500) {
            size = 22;
        } else if (dist <= 100) {
            size = 60;
        } else{
            size = 5;
        }
        return size;
    }

    public PlacesModel getPlace(int pos){
        return mPlacesModelList.get(pos);
    }

    @Override
    public void onBindViewHolder(final ARAdapter.MyViewHolder holder, int position) {
        final PlacesModel placesModel = mPlacesModelList.get(position);

        String pName[] = placesModel.getPlaceName().split(" ");
        String pNewName;
        if (pName.length == 4) {
            pNewName = pName[0] + " " + pName[1] + "\n" + pName[2] + " " + pName[3];
        } else if (pName.length == 3) {
            pNewName = pName[0] + " " + pName[1] + "\n" + pName[2];
        } else {
            pNewName = placesModel.getPlaceName();
        }

        holder.t.setText(pNewName);
        holder.d.setText(Config.cDist(placesModel.getDistance()));
        holder.v.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        holder.t.setTextSize(setSize(placesModel.getDistance()));
        holder.d.setTextSize(setSize(placesModel.getDistance()) - 2);

        holder.v.setVisibility(View.GONE);
        holder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent go = new Intent(mContext, DescriptionActivity.class);
                go.putExtra("address", placesModel.getAddress());
                go.putExtra("category", placesModel.getCategory());
                go.putExtra("description", placesModel.getDescription());
                go.putExtra("distance", placesModel.getDistance());
                go.putExtra("facilities", placesModel.getFacilities());
                go.putExtra("imageThumbnail", placesModel.getImageThumbnail());
                go.putExtra("info", placesModel.getInfo());
                go.putExtra("latlong", placesModel.getLatlong());
                go.putExtra("placeId", placesModel.getPlaceId());
                go.putExtra("placeName", placesModel.getPlaceName());
                go.putExtra("mCurrentLocation", mCurrentLocation.getLatitude() +","+mCurrentLocation.getLongitude());
                mContext.startActivity(go);
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
