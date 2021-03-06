package com.example.robmillaci.go4lunch.web_service;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Creates and returns an instance of Retrofit<br>
 * The URL is hardcoded https://maps.googleapis.com/ as well as the API key
 */
public class ServiceGenerator {
    private static Retrofit retrofit;
    private static Retrofit retrofit4Square;

    private static final String BASE_URL = "https://maps.googleapis.com/";
    private static final String BASE_4SQR_URL = "https://api.foursquare.com/v2/";
    private static final String GOOGLE_API_KEY = "AIzaSyAUCK3HeoKDGeqfL9IcqdRQLe9an9Cly5Y";

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static String getGoogleAPIKey() {
        return GOOGLE_API_KEY;
    }


    public static Retrofit getFourSquareRetrofitInstance(){
        if (retrofit4Square == null){
            retrofit4Square =new Retrofit.Builder()
                    .baseUrl(BASE_4SQR_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit4Square;
        }
    }



