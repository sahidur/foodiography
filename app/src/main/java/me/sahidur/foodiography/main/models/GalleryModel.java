package me.sahidur.foodiography.main.models;


public class GalleryModel {

    String imageId;
    String uid;
    String placeId;
    String fileName;
    String timestamp;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPlaceId() {
        return placeId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getImageId() {
        return imageId;
    }

    public String getUid() {
        return uid;
    }


    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }


}
