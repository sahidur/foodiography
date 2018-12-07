package me.sahidur.foodiography.main.maps.shared;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.places.PlacesModel;

import java.util.ArrayList;
import java.util.List;

public class DataSearchAdapter extends BaseAdapter implements Filterable {

    ValueFilter valueFilter;
    List<PlacesModel> mStringFilterList;
    List<PlacesModel> wordList;
    private LayoutInflater mLayoutInflater;
    Context context;
    MyViewHolder mViewHolder;

    public DataSearchAdapter(Context context, List<PlacesModel> wordList) {
        this.wordList = wordList;
        mStringFilterList = wordList;
        this.context = context;
        mLayoutInflater = LayoutInflater.from(this.context);
    }

    public int getCount() {
        return wordList.size();
    }

    public PlacesModel getItem(int position) {
        return wordList.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.shared_adapter_search_item, parent, false);
            mViewHolder = new MyViewHolder(convertView);
            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (MyViewHolder) convertView.getTag();
        }
        final PlacesModel currentListData = getItem(position);
        mViewHolder.tc.setText(currentListData.getPlaceName());
        return convertView;
    }

    private class MyViewHolder {

        TextView tc;

        public MyViewHolder(View item) {
            tc = (TextView) item.findViewById(R.id.tc);
        }
    }

    @Override
    public Filter getFilter() {
        if (valueFilter == null) {
            valueFilter = new ValueFilter();
        }
        return valueFilter;
    }

    private class ValueFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                List<PlacesModel> filterList = new ArrayList<PlacesModel>();
                for (int i = 0; i < mStringFilterList.size(); i++) {
                    if ((mStringFilterList.get(i).getPlaceName().toUpperCase())
                            .contains(constraint.toString().toUpperCase())) {

                        PlacesModel country = new PlacesModel();
                        country.setPlaceName(mStringFilterList.get(i).getPlaceName());
                        country.setLatlong(mStringFilterList.get(i).getLatlong());
                        country.setDistance(mStringFilterList.get(i).getDistance());
                        country.setPlaceId(mStringFilterList.get(i).getPlaceId());
                        country.setInfo(mStringFilterList.get(i).getInfo());
                        country.setAddress(mStringFilterList.get(i).getAddress());
                        country.setCategory(mStringFilterList.get(i).getCategory());
                        country.setDescription(mStringFilterList.get(i).getDescription());
                        country.setFacilities(mStringFilterList.get(i).getFacilities());
                        country.setImageThumbnail(mStringFilterList.get(i).getImageThumbnail());
                        filterList.add(country);
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
            wordList = (List<PlacesModel>) results.values;
            notifyDataSetChanged();
        }
    }


}
