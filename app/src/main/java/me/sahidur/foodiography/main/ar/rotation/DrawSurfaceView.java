package me.sahidur.foodiography.main.ar.rotation;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.ar.model.PlacesModel;

import java.util.List;

/*
 * Portions (c) 2009 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Coby Plain coby.plain@gmail.com, Ali Muzaffar ali@muzaffar.me
 */

public class DrawSurfaceView extends View {

	Paint mPaint = new Paint();
	private double OFFSET = 0d;
	private double screenWidth, screenHeight = 0d;
	private Bitmap mRadar;
	Context mContext;
	List<PlacesModel> mPlaceModelList;
	private Location currentLocation;
	int showDist;
	boolean useOrientation;
	LinearLayout insertPoint;
	private Bitmap[] mSpots;
	public DrawSurfaceView(Context c, Paint paint) {
		super(c);
	}

	public DrawSurfaceView(Context context, AttributeSet set) {
		super(context, set);
		this.mContext = context;
		mPaint.setColor(Color.GREEN);
		mPaint.setTextSize(50);
		mPaint.setAntiAlias(true);
        mPaint.setAlpha(100);

		mRadar = BitmapFactory.decodeResource(context.getResources(), R.drawable.radar);

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		screenWidth = (double) w;
		screenHeight = (double) h;
	}

	@Override
	protected void onDraw(Canvas canvas) {

		if (currentLocation == null) {
			return;
		}

		canvas.drawBitmap(mRadar, 50, 130, mPaint);

		Paint myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		myPaint.setColor(Color.WHITE);
		myPaint.setStrokeWidth(5);
		myPaint.setStyle(Paint.Style.FILL);

		Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setColor(Color.GREEN);
		mPaint.setStrokeWidth(5);
		mPaint.setStyle(Paint.Style.FILL);

        Paint angleText = new Paint(Paint.ANTI_ALIAS_FLAG);
		angleText.setColor(Color.GREEN);
		angleText.setTextSize(50);
		angleText.setAntiAlias(true);

		mSpots = new Bitmap[mPlaceModelList.size()];
		for (int i = 0; i < mSpots.length; i++)
			mSpots[i] = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.dot);

		int radarCentreX = (mRadar.getWidth() / 2) + 50;
		int radarCentreY = (mRadar.getHeight() / 2) + 130;
		canvas.drawCircle(radarCentreX, radarCentreY, 5, mPaint);
		int b = 0;
		for (int i = 0; i < mPlaceModelList.size(); i++) {

			String loc[] = mPlaceModelList.get(i).getLatlong().split(",");
			double lat = Double.parseDouble(loc[0].trim());
			double lng = Double.parseDouble(loc[1].trim());
            double dist = distInMetres(currentLocation.getLatitude(), currentLocation.getLongitude(), lat, lng) / 50;

			double angle = bearing(currentLocation.getLatitude(), currentLocation.getLongitude(), lat, lng) - OFFSET;
			double xPos, yPos;

			if(dist > 100) {
				dist = 100;
			}

			if(angle < 0)
				angle = (angle+360)%360;

			xPos = Math.sin(Math.toRadians(angle)) * dist;
			yPos = Math.sqrt(Math.pow(dist, 2) - Math.pow(xPos, 2));

			if (angle > 90 && angle < 270)
				yPos *= -1;

			double posInPx = angle * (screenWidth / 90d);
			Bitmap spot = mSpots[i];
			if((mPlaceModelList.get(i).getDistance() / 1000f) < showDist) {
				canvas.drawCircle((radarCentreX + (int) xPos), (radarCentreY - (int) yPos), 4, myPaint);

				if(useOrientation){
					int spotCentreX = spot.getWidth();
					int spotCentreY = spot.getHeight();
					xPos = posInPx - spotCentreX;
					float x, y;
					if (angle <= 45)
						x = (float) ((screenWidth / 2) + xPos);

					else if (angle >= 315)
						x = (float) ((screenWidth / 2) - ((screenWidth*4) - xPos));

					else
						x = (float) (float)(screenWidth*9); //somewhere off the screen

					y = ((float)screenHeight - 500) + b;
					insertPoint.getChildAt(i).setX(x);
					insertPoint.getChildAt(i).setY(y);
					insertPoint.getChildAt(i).setVisibility(VISIBLE);

					if(x > 0 && x < screenWidth){
						b = b - 200;
					}
				}

			}else{
				insertPoint.getChildAt(i).setVisibility(GONE);
			}

		}
	}

	public void setOffset(float offset) {
		this.OFFSET = offset;
	}

	public void updateCurrentLocation(Location currentLocation) {
		this.currentLocation = currentLocation;
		this.invalidate();
	}

	public void updateFilter(LinearLayout insertPoint, List<PlacesModel> mPlacesModelList, int showDist, boolean useOrientation){
		this.mPlaceModelList = mPlacesModelList;
		this.insertPoint = insertPoint;
		this.showDist = showDist;
		this.useOrientation = useOrientation;
		this.invalidate();
	}

    protected double distInMetres(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = earthRadius * c;

        return dist * 1000;
    }

	protected static double bearing(double lat1, double lon1, double lat2, double lon2) {
		double longDiff = Math.toRadians(lon2 - lon1);
		double la1 = Math.toRadians(lat1);
		double la2 = Math.toRadians(lat2);
		double y = Math.sin(longDiff) * Math.cos(la2);
		double x = Math.cos(la1) * Math.sin(la2) - Math.sin(la1) * Math.cos(la2) * Math.cos(longDiff);

		double result = Math.toDegrees(Math.atan2(y, x));
		return (result+360.0d)%360.0d;
	}
}
