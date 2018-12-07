package me.sahidur.foodiography.main.utils;

import me.sahidur.foodiography.main.models.address.AddressLoc;
import me.sahidur.foodiography.main.models.elevation.Elevation;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;



public interface RestInterface {

    String sensor = "false";
    String URL = "http://maps.googleapis.com/maps/api/geocode";

    String ELEV = "http://maps.googleapis.com/maps/api/elevation";

    @GET("/json")
    void getAddress(@Query("address") String key,
                    @Query("sensor") String part,
                    Callback<AddressLoc> cb);

    @GET("/json")
    void getElevation(@Query("locations") String location,
                      @Query("sensor") String sensor,
                      Callback<Elevation> el);
}
