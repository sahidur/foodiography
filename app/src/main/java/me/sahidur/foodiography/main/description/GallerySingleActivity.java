package me.sahidur.foodiography.main.description;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;

import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.utils.ScaleImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import butterknife.Bind;
import butterknife.ButterKnife;


public class GallerySingleActivity extends AppCompatActivity {
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.image)
    ScaleImageView vScaleImage;

    @Bind(R.id.coordinator)
    RelativeLayout vCoordinatorLayout;

    @Bind(R.id.buttonDelete)
    FloatingActionButton vBtnDelete;

    private final String TAG = "ImageShowActivity:";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            Transition transition;
            transition = TransitionInflater.from(this).inflateTransition(R.transition.slide_from_bottom);
            getWindow().setEnterTransition(transition);

            getWindow().setExitTransition(transition);
        }
        setContentView(R.layout.shared_activity_image_show);

        ButterKnife.bind(this);

        vBtnDelete.setVisibility(View.GONE);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getIntent().getStringExtra("fileName"));

        String urlImg = getIntent().getStringExtra("urlImg");

        Glide.with(getApplicationContext())
                .load(urlImg)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new BitmapImageViewTarget(vScaleImage) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        vScaleImage.setImageBitmap(resource);
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        supportFinishAfterTransition();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}