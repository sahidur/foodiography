package me.sahidur.foodiography.main.categories;

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
import me.sahidur.foodiography.main.feeds.FeedsActivity;
import me.sahidur.foodiography.main.utils.Constants;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;


public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.MyViewHolder> {

    List<CategoryModel> mCategoryModelList;
    Context mContext;
    String mCurrentLocation;
    public CategoryAdapter(Context mContext, List<CategoryModel> mCategoryModelList, String mCurrentLocation) {
        this.mContext = mContext;
        this.mCategoryModelList = mCategoryModelList;
        this.mCurrentLocation = mCurrentLocation;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtCategory;
        ImageView imgCategory;
        CardView card_view;
        ProgressBar progressBar;
        public MyViewHolder(View view) {
            super(view);
            txtCategory = (TextView) view.findViewById(R.id.txtCategory);
            imgCategory = (ImageView) view.findViewById(R.id.imgCategory);
            card_view = (CardView) view.findViewById(R.id.cardView);
            progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        }
    }


    @Override
    public CategoryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.categories_category_adapter_item, parent, false);


        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final CategoryAdapter.MyViewHolder holder, int position) {
        final CategoryModel category = mCategoryModelList.get(position);
        holder.txtCategory.setText(category.getCategoryName().toUpperCase());
        final String imgUrl = "https://firebasestorage.googleapis.com/v0/b/" + Constants.FIREBASE_PROJECT_ID + ".appspot.com/o/icon%2F" + category.getIconFileName() + "?alt=media";

        Glide.with(mContext.getApplicationContext()).load(imgUrl)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.mipmap.ic_launcher)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(holder.imgCategory);

        holder.card_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent go = new Intent(mContext, FeedsActivity.class);
                go.putExtra("category", category.getCategoryId());
                go.putExtra("iconFileName", imgUrl);
                go.putExtra("categoryName", category.getCategoryName());
                go.putExtra("mCurrentLocation", mCurrentLocation);

                Pair<View, String> p1 = Pair.create((View) holder.imgCategory, "imgCategory");

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
        return mCategoryModelList.size();
    }
}
