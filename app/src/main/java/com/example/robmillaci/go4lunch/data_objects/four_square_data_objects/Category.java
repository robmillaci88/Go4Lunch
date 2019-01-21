package com.example.robmillaci.go4lunch.data_objects.four_square_data_objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Used in parsing {@link com.example.robmillaci.go4lunch.web_service.FourSquareAPI#getPlaceType(String, String)}
 */
public class Category {
     @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("pluralName")

    public String getName() {
        return name;
    }

}

