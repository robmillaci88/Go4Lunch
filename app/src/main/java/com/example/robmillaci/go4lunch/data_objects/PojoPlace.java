package com.example.robmillaci.go4lunch.data_objects;

import com.example.robmillaci.go4lunch.data_objects.four_square_data_objects.Category;
import com.example.robmillaci.go4lunch.web_service.FourSquareAPI;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.List;

/**
 * This class is used to convert {@link com.google.android.gms.location.places.Place} into plain old java object Places
 * This allows us to close the {@link com.google.android.gms.location.places.PlaceBufferResponse} to prevent memory leaks
 * It also allows us to make the minimum amount of requests for Google places
 */
public class PojoPlace implements Serializable, FourSquareAPI.FourSquareCallback {
    public static final String PLACE_SERIALIZABLE_KEY = "PojoPlace"; //the key for storing and retrieving PojoPlace objects
    public static final String PLACE_ID_KEY = "placeID"; //the key for storing the place ID
    public static final String PLACE_NAME_KEY = "placeName"; //the key for storing the place name
    private static final String NO_VALUE_FOUND = "Not available";

    private final String name;
    private final String address;
    private final String website;
    private final String phoneNumber;
    private final float rating;
    private final String id;
    private final double latitude;
    private final double longitude;
    private String placeType;

    public PojoPlace(Place googlePlace, PlaceBufferResponse placeBufferResponse) {
        this.name = googlePlace.getName() == null ? NO_VALUE_FOUND : googlePlace.getName().toString();
        this.address = googlePlace.getAddress() == null ? NO_VALUE_FOUND : googlePlace.getAddress().toString();
        this.website = googlePlace.getWebsiteUri() == null ? NO_VALUE_FOUND : googlePlace.getWebsiteUri().toString();
        this.phoneNumber = googlePlace.getPhoneNumber() == null ? NO_VALUE_FOUND : googlePlace.getPhoneNumber().toString();

        this.rating = googlePlace.getRating();
        this.id = googlePlace.getId();
        this.latitude = googlePlace.getLatLng().latitude;
        this.longitude = googlePlace.getLatLng().longitude;

        getAdditionalData();

        if (placeBufferResponse != null && !placeBufferResponse.isClosed()) {
            placeBufferResponse.close();
        }
    }

    private void getAdditionalData() {
        String latlng = String.valueOf(this.latitude) +
                "," +
                String.valueOf(this.longitude);
        new FourSquareAPI(this).getPlaceType(latlng, this.name.toLowerCase());
    }


    public String getPlaceType() {
        return placeType == null ? "" : placeType;
    }

    public String getId() {
        return id;
    }

    public LatLng getLocation() {
        return new LatLng(latitude, longitude);
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getWebsite() {
        return website;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public float getRating() {
        return rating;
    }

    @Override
    public void gotCategories(List<Category> categories) {
        for (Category s : categories) {
            this.placeType = s.getName();
        }
    }

}
