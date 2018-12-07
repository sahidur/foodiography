package me.sahidur.foodiography.main.maps.online;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import me.sahidur.foodiography.R;
import me.sahidur.foodiography.main.description.DescriptionActivity;
import me.sahidur.foodiography.main.maps.shared.ClusterMarkerLocation;
import me.sahidur.foodiography.main.maps.shared.DataSearchAdapter;
import me.sahidur.foodiography.main.places.PlacesModel;
import me.sahidur.foodiography.main.utils.Constants;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MapsOnlineFragment extends Fragment {

    ProgressBar mProgressBar;
    EditText vEtSearch;
    GoogleMap mMap;
    FloatingActionButton vFabDir;
    LinearLayout vCoordinatorLayout;
    ListView vListView;

    double mCurrentLng, mCurrentLat;

    LatLng mCurrentLocation;

    FirebaseDatabase mFDatabase;

    List<PlacesModel> mPlaceModelList;

    ClusterManager<ClusterMarkerLocation> mClusterManager;

    ClusterMarkerLocation mClickedClusterItem;

    DataSearchAdapter mDataSearchAdapter;

    Polyline mPolyline = null;

    Dialog dDialogSearch;

    CustomRenderer mCustomRenderer;

    GoogleMap.InfoWindowAdapter mGlideInfoWindow;

    int type = 0;
    MapView mMapView;
    private Bundle mBundle;

    public MapsOnlineFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFDatabase = FirebaseDatabase.getInstance();
        mBundle = savedInstanceState;
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.maps_online_fragment_maps, container, false);

        //ButterKnife.bind(this, rootView);

        vCoordinatorLayout = (LinearLayout) rootView.findViewById(R.id.coordinatorLayout);

        vFabDir = (FloatingActionButton) rootView.findViewById(R.id.fabDirection);
        vFabDir.hide();

        dDialogSearch = new Dialog(getActivity(), R.style.CustomDialog);
        dDialogSearch.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dDialogSearch.setContentView(R.layout.shared_search_dialog);
        dDialogSearch.setCancelable(true);

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress_view);
        mProgressBar.setVisibility(View.GONE);

        String[] cuText = getArguments().getString("mCurrentLocation").split(",");
        mCurrentLat = Double.parseDouble(cuText[0]);
        mCurrentLng = Double.parseDouble(cuText[1]);
        mCurrentLocation = new LatLng(mCurrentLat, mCurrentLng);

        vListView = (ListView) dDialogSearch.findViewById(R.id.recycler_view);

        mPlaceModelList = new ArrayList<>();

        mMapView = (MapView) rootView.findViewById(R.id.map);
        mMapView.onCreate(mBundle);

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                CameraPosition position;
                mClusterManager = new ClusterManager<>(getActivity(), mMap);

                position = CameraPosition.builder()
                        .target(mCurrentLocation)
                        .zoom(16)
                        .build();

                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                if (ActivityCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                                PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    return;
                }
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(false);
                mMap.getUiSettings().setCompassEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                mMap.getUiSettings().setZoomGesturesEnabled(true);
                mMap.getUiSettings().setRotateGesturesEnabled(true);
                mMap.getUiSettings().isCompassEnabled();
                mMap.getUiSettings().isMapToolbarEnabled();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), null);

                mMap.setOnCameraChangeListener(mClusterManager);
                mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
                mMap.setOnInfoWindowClickListener(mClusterManager);

                mMap.setOnMarkerClickListener(mClusterManager);

                mClusterManager
                        .setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<ClusterMarkerLocation>() {
                            @Override
                            public boolean onClusterItemClick(ClusterMarkerLocation item) {
                                mClickedClusterItem = item;

                                for (Marker marker : mClusterManager.getMarkerCollection().getMarkers()) {
                                    if (marker.getPosition().latitude == item.getPosition().latitude &&
                                            marker.getPosition().longitude == item.getPosition().longitude) {
                                        marker.showInfoWindow();
                                        vFabDir.show();

                                        int zoom = (int) mMap.getCameraPosition().zoom;
                                        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(
                                                new LatLng(
                                                        item.getPosition().latitude + (double) 200 / Math.pow(2, zoom),
                                                        item.getPosition().longitude),
                                                zoom);
                                        mMap.animateCamera(cu);

                                    }
                                }
                                return true;
                            }
                        });

                mGlideInfoWindow = new GlideInfoWindowAdapter(getActivity());

                mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(mGlideInfoWindow);

                mClusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<ClusterMarkerLocation>() {
                    @Override
                    public void onClusterItemInfoWindowClick(ClusterMarkerLocation myItem) {
                        String sCurrentLoc = mCurrentLocation.latitude + "," + mCurrentLocation.longitude;
                        Intent go = new Intent(getActivity(), DescriptionActivity.class);
                        go.putExtra("placeId", myItem.getPlaceId());
                        go.putExtra("placeName", myItem.getPlaceName());
                        go.putExtra("address", myItem.getAddress());
                        go.putExtra("category", myItem.getCategory());
                        go.putExtra("description", myItem.getDescription());
                        go.putExtra("facilities", myItem.getFacilities());
                        go.putExtra("imageThumbnail", myItem.getImageThumbnail());
                        go.putExtra("info", myItem.getInfo());
                        go.putExtra("latlong", myItem.getLatlong());
                        go.putExtra("distance", myItem.getDistance());
                        go.putExtra("mCurrentLocation", sCurrentLoc);
                        startActivity(go);
                    }
                });

                mCustomRenderer = new CustomRenderer(getActivity(), mMap, mClusterManager);
                mClusterManager.setRenderer(mCustomRenderer);
                mCustomRenderer.setMarkersToCluster(false);

                getDataParent();

                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        vFabDir.hide();
                        mCustomRenderer.setMarkersToCluster(false);
                    }
                });

                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        if (type == 0) {
                            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                            type = 1;
                        } else {
                            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                            type = 0;
                        }
                    }
                });

                MapStyleOptions styleNight = MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.map_default);
                mMap.setMapStyle(styleNight);
            }

        });

        vEtSearch = (EditText) dDialogSearch.findViewById(R.id.etCari);
        vEtSearch.clearFocus();

        vEtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mDataSearchAdapter.getFilter().filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        vEtSearch.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Toast.makeText(getActivity(), v.getText() + "",
                            Toast.LENGTH_LONG).show();
                    handled = true;
                }
                return handled;
            }
        });

        vListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dDialogSearch.dismiss();

                getActivity().setTitle(mDataSearchAdapter.getItem(position).getPlaceName());

                String[] sp = mDataSearchAdapter.getItem(position).getLatlong().split(",");
                double a = Double.parseDouble(sp[0].trim());
                double b = Double.parseDouble(sp[1].trim());

                for (Marker marker : mClusterManager.getMarkerCollection().getMarkers()) {
                    mClickedClusterItem = new ClusterMarkerLocation(new LatLng(a, b),
                            mDataSearchAdapter.getItem(position).getPlaceId(),
                            mDataSearchAdapter.getItem(position).getPlaceName(),
                            String.valueOf(String.format("%.2f",
                                    mDataSearchAdapter.getItem(position).getDistance() / 1000f) + " Km"),
                            mDataSearchAdapter.getItem(position).getImageThumbnail(),
                            mDataSearchAdapter.getItem(position).getPlaceName(),
                            mDataSearchAdapter.getItem(position).getLatlong(),
                            mDataSearchAdapter.getItem(position).getDescription(),
                            mDataSearchAdapter.getItem(position).getCategory(),
                            mDataSearchAdapter.getItem(position).getAddress(),
                            mDataSearchAdapter.getItem(position).getInfo(),
                            mDataSearchAdapter.getItem(position).getFacilities(),
                            mDataSearchAdapter.getItem(position).getDistance());

                    if (marker.getPosition().latitude == a &&
                            marker.getPosition().longitude == b) {
                        marker.showInfoWindow();

                        int zoom = (int) mMap.getCameraPosition().zoom;
                        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(new LatLng(
                                a + (double) 200 / Math.pow(2, zoom),
                                b), zoom);
                        mMap.moveCamera(cu);
                    }
                }
            }
        });
        vListView.setTextFilterEnabled(true);

        vFabDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent go;
                if (cek_status(getActivity())) {
                    go = new Intent(getActivity(), DirectionActivity.class);
                    go.putExtra("destLat", mClickedClusterItem.getPosition().latitude);
                    go.putExtra("destLng", mClickedClusterItem.getPosition().longitude);
                    go.putExtra("placeName", mClickedClusterItem.getmTitle());
                    go.putExtra("category", mClickedClusterItem.getmSnippet());
                    startActivity(go);
                } else {
                    Toast.makeText(getActivity(), "Your internet is not connected!", Toast.LENGTH_LONG).show();
                }
            }
        });
        return rootView;
    }


    public boolean cek_status(Context cek) {
        ConnectivityManager cm = (ConnectivityManager) cek.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();

        return info != null && info.isConnected();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.maps_activity_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_list_search:
                dDialogSearch.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initMarkers(double mLatitude,
                             double mLongitude,
                             String placeId,
                             String mTitle,
                             String mSnippet,
                             String imageThumbnail,
                             String placeName,
                             String latlong,
                             String description,
                             String category,
                             String address,
                             String info,
                             String facilities,
                             float distance) {
        mClusterManager.addItem(new
                ClusterMarkerLocation(new LatLng(mLatitude, mLongitude),
                placeId,
                mTitle,
                mSnippet,
                imageThumbnail,
                placeName,
                latlong,
                description,
                category,
                address,
                info,
                facilities,
                distance));
    }

    public void getDataParent() {
        mProgressBar.setVisibility(View.VISIBLE);
        mPlaceModelList.clear();
        DatabaseReference myRefParent = mFDatabase.getReference("places");
        myRefParent.keepSynced(true);
        myRefParent.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                mProgressBar.setVisibility(View.VISIBLE);
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {

                    PlacesModel person = postSnapshot.getValue(PlacesModel.class);
                    float jarak;
                    double mLatitude = 0;
                    double mLongitude = 0;
                    if (!person.getLatlong().equals("0")) {
                        String[] newLoc = person.getLatlong().split(",");
                        mLatitude = Double.parseDouble(newLoc[0].trim());
                        mLongitude = Double.parseDouble(newLoc[1].trim());
                        jarak = distFrom(mCurrentLat, mCurrentLng, mLatitude, mLongitude, person.getPlaceName());
                    } else {
                        jarak = 0;
                    }

                    person.setPlaceId(postSnapshot.getKey());
                    person.setDistance(jarak);
                    initMarkers(mLatitude, mLongitude,
                            person.getPlaceId(),
                            person.getPlaceName(),
                            String.valueOf(String.format("%.2f", person.getDistance() / 1000f) + " Km"),
                            person.getImageThumbnail(),
                            person.getPlaceName(),
                            person.getLatlong(),
                            person.getDescription(),
                            person.getCategory(),
                            person.getAddress(),
                            person.getInfo(),
                            person.getFacilities(),
                            person.getDistance()
                    );

                    mPlaceModelList.add(person);

                }
                sortListByName();
                mDataSearchAdapter = new DataSearchAdapter(getActivity(), mPlaceModelList);
                vListView.setAdapter(mDataSearchAdapter);
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void sortListByName() {
        Collections.sort(mPlaceModelList, new Comparator<PlacesModel>() {
            public int compare(PlacesModel o1, PlacesModel o2) {
                return o1.getPlaceName().compareTo(o2.getPlaceName());
            }
        });
    }

    public float distFrom(double lat1, double lng1, double lat2, double lng2, String t) {
        Location markerLoc = new Location(t);
        markerLoc.setLatitude(lat2);
        markerLoc.setLongitude(lng2);
        Location currentLoc = new Location("My Location");
        currentLoc.setLatitude(lat1);
        currentLoc.setLongitude(lng1);
        return markerLoc.distanceTo(currentLoc);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    class GlideInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private final Map<Marker, Bitmap> images = new HashMap<>();
        private final Map<Marker, Target<Bitmap>> targets = new HashMap<>();
        private final View myContentsView;
        Context mContext;

        @SuppressLint("InflateParams")
        GlideInfoWindowAdapter(Context mContext) {
            myContentsView = getActivity().getLayoutInflater().inflate(
                    R.layout.maps_online_custom_info_window, null);
            this.mContext = mContext;
        }

        public View getInfoContents(Marker marker) {
            ImageView thumbnail = (ImageView) myContentsView.findViewById(R.id.gambar);

            TextView tvTitle = ((TextView) myContentsView
                    .findViewById(R.id.txtTitle));
            TextView tvSnippet = ((TextView) myContentsView
                    .findViewById(R.id.txtSnippet));

            tvTitle.setText(mClickedClusterItem.getmTitle());
            tvSnippet.setText(mClickedClusterItem.getmSnippet());

            String pathUrl = "gallery/" + mClickedClusterItem.getPlaceId() + "/" + mClickedClusterItem.getImageThumbnail() + "?alt=media";
            String urlImg;

            if (mClickedClusterItem.getImageThumbnail().equalsIgnoreCase("no_image.pngge.png")) {
                urlImg = "https://firebasestorage.googleapis.com/v0/b/"+Constants.FIREBASE_PROJECT_ID+".appspot.com/o/no_image.pngge.png?alt=media";
            } else {
                urlImg = "https://firebasestorage.googleapis.com/v0/b/"+Constants.FIREBASE_PROJECT_ID+".appspot.com/o/" + pathUrl.replace("/", "%2F");
            }

            Bitmap image = images.get(marker);
            if (image == null) {
                Glide.with(mContext)
                        .load(urlImg)
                        .asBitmap()
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(getTarget(marker));
                return null;
            } else {
                thumbnail.setImageBitmap(image);
            }
            return myContentsView;
        }

        public View getInfoWindow(Marker marker) {
            return null;
        }

        private Target<Bitmap> getTarget(Marker marker) {
            Target<Bitmap> target = targets.get(marker);
            if (target == null) {
                target = new InfoTarget(marker);
            }
            return target;
        }

        private class InfoTarget extends SimpleTarget<Bitmap> {
            Marker marker;

            InfoTarget(Marker marker) {
                super(100, 100); // otherwise Glide will load original sized bitmap which is huge
                this.marker = marker;
            }

            @Override
            public void onLoadStarted(Drawable placeholder) {
                mProgressBar.setVisibility(View.VISIBLE);
                super.onLoadStarted(placeholder);
            }

            @Override
            public void onLoadCleared(Drawable placeholder) {
                images.remove(marker);
            }

            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                mProgressBar.setVisibility(View.GONE);

                images.put(marker, resource);
                vFabDir.show();
                marker.showInfoWindow();
                if (mPolyline != null) {
                    mPolyline.remove();
                }
            }
        }
    }

    public class CustomRenderer extends DefaultClusterRenderer<ClusterMarkerLocation> {
        private static final int MIN_CLUSTER_SIZE = 1;
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getActivity().getApplicationContext());
        private boolean shouldCluster = true;

        public CustomRenderer(Context context, GoogleMap map,
                              ClusterManager<ClusterMarkerLocation> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(ClusterMarkerLocation item,
                                                   MarkerOptions markerOptions) {
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(setMarker(R.drawable.ic_marker)));

        }

        public Bitmap setMarker(int drawable) {
            int height = 100;
            int width = 100;
            BitmapDrawable bitmapdraw = (BitmapDrawable) ContextCompat.getDrawable(getActivity(), drawable);
            Bitmap b = bitmapdraw.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
            return smallMarker;
        }

        @Override
        protected void onClusterItemRendered(ClusterMarkerLocation clusterItem, Marker marker) {
            super.onClusterItemRendered(clusterItem, marker);
        }


        public void setMarkersToCluster(boolean toCluster) {
            this.shouldCluster = toCluster;
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster<ClusterMarkerLocation> cluster) {
            if (shouldCluster) {
                return cluster.getSize() > MIN_CLUSTER_SIZE;
            } else {
                return shouldCluster;
            }
        }
    }
}
