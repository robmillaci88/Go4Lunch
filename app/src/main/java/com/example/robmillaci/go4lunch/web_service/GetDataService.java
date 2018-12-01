package com.example.robmillaci.go4lunch.web_service;

import com.example.robmillaci.go4lunch.data_objects.FourSquareDataObjects.Response;
import com.example.robmillaci.go4lunch.data_objects.places_details.RawData;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GetDataService {
    @GET("maps/api/place/details/json?")
    Call<RawData> getDetails(
            @Query("placeid") String placeId,
            @Query("fields") String fields,
            @Query("key") String apiKey);

    @GET("maps/api/place/details/json?")
    Call<com.example.robmillaci.go4lunch.data_objects.reviews_objects.Reviews> getReviews(
            @Query("placeid") String placeId,
            @Query("fields") String fields,
            @Query("key") String apiKey);

    @GET("venues/search?")
    Call<com.example.robmillaci.go4lunch.data_objects.FourSquareDataObjects.RawData> get4squareDetail(
            @Query("ll") String latLong,
            @Query("client_id") String clientId,
            @Query("client_secret") String clientSecret,
            @Query("v") String versionDate);
}