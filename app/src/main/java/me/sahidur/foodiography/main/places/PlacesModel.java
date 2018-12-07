package me.sahidur.foodiography.main.places;



public class PlacesModel {
    String placeId;
    String address;
    String category;
    String description;
    String imageThumbnail;
    String latlong;
    String placeName;
    String facilities;
    String info;

    Integer viewType;

    float distance;
    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public Integer getViewType() {
        return viewType;
    }

    public void setViewType(Integer viewType) {
        this.viewType = viewType;
    }


    public String getFacilities() {
        return facilities;
    }

    public void setFacilities(String facilities) {
        this.facilities = facilities;
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

    public void setImageThumbnail(String imageThumbnail) {
        this.imageThumbnail = imageThumbnail;
    }

    public void setLatlong(String latlong) {
        this.latlong = latlong;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getPlaceId() {
        return placeId;
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

    public String getImageThumbnail() {
        return imageThumbnail;
    }

    public String getLatlong() {
        return latlong;
    }

    public String getPlaceName() {
        return placeName;
    }

    public String getInfo() {
        return info;
    }
}
