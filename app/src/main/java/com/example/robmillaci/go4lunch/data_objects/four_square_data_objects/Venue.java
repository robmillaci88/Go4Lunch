package com.example.robmillaci.go4lunch.data_objects.four_square_data_objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
/**
 * Used in parsing {@link com.example.robmillaci.go4lunch.web_service.FourSquareAPI#getPlaceType(String, String)}
 */
public class Venue {
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("categories")
    @Expose
    private List<Category> categories = null;

    public String getName() {
        return name;
    }
    public List<Category> getCategories() {
        return categories;
    }


}
