package me.sahidur.foodiography.main.maps.online;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.maps.shared.DirectionMapModel;

import java.util.List;


public class StepAdapter extends RecyclerView.Adapter<StepAdapter.ViewHolder> {

    private List<DirectionMapModel> mItems;
    private ItemListener mListener;
    Activity mContext;

    public StepAdapter(Activity context, List<DirectionMapModel> items, ItemListener listener) {
        mItems = items;
        mListener = listener;
        mContext = context;
    }

    public void setListener(ItemListener listener) {
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.maps_online_adapter_step_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setData(mItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView imageView;
        public TextView arah, keterangan;
        public DirectionMapModel item;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            imageView = (ImageView) itemView.findViewById(R.id.man_icon);
            arah = (TextView) itemView.findViewById(R.id.arah);
            keterangan = (TextView) itemView.findViewById(R.id.keterangan);

        }

        public void setData(DirectionMapModel item) {
            this.item = item;
//            imageView.setImageResource(item.getManeuver());
            String name = item.getManeuver().replace("-", "_");

            int resId = mContext.getResources().getIdentifier("ic_" + name, "drawable", mContext.getPackageName());
            if (resId != 0) {  // the resouce exists...
                Drawable d = ContextCompat.getDrawable(mContext, resId);
                imageView.setImageDrawable(d);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                arah.setText(Html.fromHtml(item.getHtml_instructions().trim(), Html.FROM_HTML_MODE_COMPACT));
            } else {
                arah.setText(Html.fromHtml(item.getHtml_instructions().trim()));
            }
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(item);
            }
        }
    }

    public interface ItemListener {
        void onItemClick(DirectionMapModel item);
    }
}