package me.sahidur.foodiography.main.maps.shared;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;


public class ClusterMarkerLocation implements ClusterItem {

    private LatLng mPosition;
    private String placeId;
    private String mTitle;
    private String mSnippet;
    private String imageThumbnail;
    private String placeName;
    private String latlong;
    private String description;
    private String category;
    private String address;
    private String info;
    private String facilities;
    private float distance;

    public ClusterMarkerLocation(LatLng latLng,
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
        mPosition = latLng;
        this.mTitle = mTitle;
        this.mSnippet = mSnippet;
        this.imageThumbnail = imageThumbnail;
        this.placeName = placeName;
        this.placeId = placeId;
        this.latlong = latlong;
        this.description = description;
        this.category = category;
        this.address = address;
        this.info = info;
        this.facilities = facilities;
        this.distance = distance;

    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public void setLatlong(String latlong) {
        this.latlong = latlong;
    }

    public void setmPosition(LatLng mPosition) {
        this.mPosition = mPosition;
    }

    public LatLng getmPosition() {
        return mPosition;
    }

    public String getLatlong() {
        return latlong;
    }


    public void setPosition(LatLng mPosition) {
        this.mPosition = mPosition;
    }

    public String getmSnippet() {
        return mSnippet;
    }

    public void setmSnippet(String mSnippet) {
        this.mSnippet = mSnippet;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getPlaceId() {
        return placeId;
    }

    public float getDistance() {
        return distance;
    }

    public String getAddress() {
        return address;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getFacilities() {
        return facilities;
    }

    public String getImageThumbnail() {
        return imageThumbnail;
    }

    public String getInfo() {
        return info;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public void setFacilities(String facilities) {
        this.facilities = facilities;
    }

    public void setImageThumbnail(String imageThumbnail) {
        this.imageThumbnail = imageThumbnail;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }
}