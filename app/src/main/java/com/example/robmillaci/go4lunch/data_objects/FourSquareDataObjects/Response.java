package com.example.robmillaci.go4lunch.data_objects.FourSquareDataObjects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
/**
 * Used in parsing {@link com.example.robmillaci.go4lunch.web_service.FourSquareAPI#getPlaceType(String, String)}
 */
public class Response {
    @SerializedName("venues")
    @Expose
    private List<Venue> venues = null;

    public List<Venue> getVenues() {
        return venues;
    }

}
