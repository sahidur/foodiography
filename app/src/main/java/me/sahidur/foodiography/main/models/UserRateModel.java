package me.sahidur.foodiography.main.models;


public class UserRateModel {

    String uid;
    String placeId;
    float rateNum;
    String review;
    String timestamp;

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getUid() {
        return uid;
    }

    public String getPlaceId() {
        return placeId;
    }

    public float getRateNum() {
        return rateNum;
    }

    public String getReview() {
        return review;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public void setRateNum(float rateNum) {
        this.rateNum = rateNum;
    }

    public void setReview(String review) {
        this.review = review;
    }
}
