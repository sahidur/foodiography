package me.sahidur.foodiography.main.models.elevation;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;



public class Result {

    @SerializedName("elevation")
    @Expose
    private Double elevation;
    @SerializedName("location")
    @Expose
    private Location location;
    @SerializedName("resolution")
    @Expose
    private Double resolution;

    public Double getElevation() {
        return elevation;
    }

    public void setElevation(Double elevation) {
        this.elevation = elevation;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Double getResolution() {
        return resolution;
    }

    public void setResolution(Double resolution) {
        this.resolution = resolution;
    }

}