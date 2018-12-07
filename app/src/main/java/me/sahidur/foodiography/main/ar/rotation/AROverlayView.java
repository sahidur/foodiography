package me.sahidur.foodiography.main.ar.rotation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.opengl.Matrix;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.LinearLayout;

import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.ar.model.LocationHelper;
import me.sahidur.foodiography.main.ar.model.PlacesModel;

import java.util.List;

public class AROverlayView extends View {

    Context mContext;
    int showDist;
    private float[] rotatedProjectionMatrix = new float[16];
    private Location currentLocation;
    private List<PlacesModel> mPlacesModelList;

    LinearLayout insertPoint;
    boolean useOrientation;

    public AROverlayView(Context mContext, LinearLayout insertPoint, int showDist) {
        super(mContext);
        this.insertPoint = insertPoint;
        this.mContext = mContext;
        this.showDist = showDist;
    }

    public void updateRotatedProjectionMatrix(float[] rotatedProjectionMatrix) {
        this.rotatedProjectionMatrix = rotatedProjectionMatrix;
        this.invalidate();
    }

    public void updateCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
        //loadAllPlaces(categoryId);
        this.invalidate();
    }

    public void updateFilter(LinearLayout insertPoint, List<PlacesModel> mPlacesModelList, int showDist, boolean useOrientation){
        this.insertPoint = insertPoint;
        this.mPlacesModelList = mPlacesModelList;
        this.showDist = showDist;
        this.useOrientation = useOrientation;
        this.invalidate();
    }

    @Override
    @SuppressLint("DrawAllocation")
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (currentLocation == null) {
            return;
        }

        Paint pTitle = new Paint(Paint.ANTI_ALIAS_FLAG);
        pTitle.setStyle(Paint.Style.FILL);
        pTitle.setColor(Color.WHITE);
        pTitle.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        pTitle.setFakeBoldText(true);
        pTitle.setAntiAlias(true);

        Paint pDist = new Paint(Paint.ANTI_ALIAS_FLAG);
        pDist.setStyle(Paint.Style.FILL);
        pDist.setColor(Color.WHITE);
        pDist.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        pDist.setTextSize(40);
        pDist.setAntiAlias(true);

        Paint myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        myPaint.setColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        myPaint.setStrokeWidth(5);
        myPaint.setStyle(Paint.Style.FILL);
        myPaint.setAntiAlias(true);

        for (int i = 0; i < mPlacesModelList.size(); i++) {

            int radius = setRadius(mPlacesModelList.get(i).getDistance());
            pTitle.setTextSize(radius);
            float[] currentLocationInECEF = LocationHelper.WSG84toECEF(currentLocation);

            String loc[] = mPlacesModelList.get(i).getLatlong().split(",");
            double lat = Double.parseDouble(loc[0].trim());
            double lng = Double.parseDouble(loc[1].trim());
            Location ARloc = new Location(mPlacesModelList.get(i).getPlaceName());
            ARloc.setAltitude(mPlacesModelList.get(i).getElevation());
            ARloc.setLatitude(lat);
            ARloc.setLongitude(lng);

            float[] pointInECEF = LocationHelper.WSG84toECEF(ARloc);
            float[] pointInENU = LocationHelper.ECEFtoENU(currentLocation, currentLocationInECEF, pointInECEF);

            float[] cameraCoordinateVector = new float[4];
            Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);
            if (cameraCoordinateVector[2] < 0) {
                float x = (0.5f + cameraCoordinateVector[0] / cameraCoordinateVector[3]) * canvas.getWidth();
                float y = (0.5f - cameraCoordinateVector[1] / cameraCoordinateVector[3]) * canvas.getHeight();

                insertPoint.getChildAt(i).setX(x - (insertPoint.getChildAt(i).getWidth()/2));
                insertPoint.getChildAt(i).setY(y);
                if((mPlacesModelList.get(i).getDistance() / 1000f) < showDist) {
                    if(!useOrientation){
                        insertPoint.getChildAt(i).setVisibility(VISIBLE);
                        //canvas.drawCircle(x, y, radius, myPaint);
                    }
                }else{
                    insertPoint.getChildAt(i).setVisibility(GONE);
                }


            }
        }
    }

    int setRadius(float dist) {
        int rad;
        if (dist <= 50000 && dist > 10000) {
            rad = 10;
        } else if (dist <= 10000 && dist > 5000) {
            rad = 20;
        } else if (dist <= 5000 && dist > 1000) {
            rad = 30;
        } else if (dist <= 1000 && dist > 500) {
            rad = 40;
        } else if (dist <= 500) {
            rad = 50;
        } else if (dist <= 100) {
            rad = 60;
        } else {
            rad = 5;
        }
        return rad;
    }
}
