package me.sahidur.foodiography.main.maps.shared;

import com.google.android.gms.maps.model.LatLng;


public class DirectionMapModel {

    private String distance;
    private String duration;
    private LatLng end_location;
    private String html_instructions;
    private String maneuver;
    private LatLng start_locaition;

    public DirectionMapModel() {
    }

    public String getDistance() {
        return distance;
    }

    public LatLng getEnd_location() {
        return end_location;
    }

    public LatLng getStart_locaition() {
        return start_locaition;
    }

    public String getDuration() {
        return duration;
    }

    public String getHtml_instructions() {
        return html_instructions;
    }

    public String getManeuver() {
        return maneuver;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setEnd_location(LatLng end_location) {
        this.end_location = end_location;
    }

    public void setHtml_instructions(String html_instructions) {
        this.html_instructions = html_instructions;
    }

    public void setManeuver(String maneuver) {
        this.maneuver = maneuver;
    }

    public void setStart_locaition(LatLng start_locaition) {
        this.start_locaition = start_locaition;
    }
}
