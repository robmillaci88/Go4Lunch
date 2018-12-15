package com.example.robmillaci.go4lunch.web_service;

import android.support.annotation.NonNull;
import android.util.Log;

import com.example.robmillaci.go4lunch.data_objects.four_square_data_objects.Category;
import com.example.robmillaci.go4lunch.data_objects.four_square_data_objects.RawData;
import com.example.robmillaci.go4lunch.data_objects.four_square_data_objects.Venue;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Responsible for retrieving additional place data not currently available on Google places API
 * {@link #getPlaceType(String, String)} takes a places lat and long to retrieve a details place type
 * Returns a list of place types
 */
public class FourSquareAPI {
    private static final String CLIENTID = "RBY20QAEPFOLYHDPYTZZCMEGFSJTF2IFOT1ZVE1ZX1FEWBOJ";
    private static final String CLIENTSECRET = "B2ETQHPZO0LQ514XHWOKK40HH5MRFP4FG35PMQUVQKXRYNFZ";
    private static final String VERSION_DATE = "20181201";

    private final FourSquareCallback mFourSquareCallback;

    public FourSquareAPI(FourSquareCallback callback) {
        this.mFourSquareCallback = callback;
    }

    public void getPlaceType(String placeLatLng, final String placeNameLowerCase) {

        GetDataService service = ServiceGenerator.getFourSquareRetrofitInstance().create(GetDataService.class);
        retrofit2.Call<RawData> call = service.get4squareDetail(placeLatLng, CLIENTID, CLIENTSECRET, VERSION_DATE);

        call.enqueue(new Callback<com.example.robmillaci.go4lunch.data_objects.four_square_data_objects.RawData>() {
            @Override
            public void onResponse(@NonNull Call<RawData> call, @NonNull retrofit2.Response<RawData> response) {
                final RawData responseData = response.body();
                if (responseData != null) {
                    for (Venue v : responseData.getResponse().getVenues()) {
                        if (v.getName().toLowerCase().contains(placeNameLowerCase)
                                || placeNameLowerCase.contains(v.getName().toLowerCase())
                                || v.getName().toLowerCase().replaceAll(" ", "").contains(placeNameLowerCase.toLowerCase().replaceAll(" ", ""))
                                || placeNameLowerCase.replaceAll(" ", "").contains(v.getName().toLowerCase().replaceAll(" ", ""))) {

                            mFourSquareCallback.gotCategories(v.getCategories());
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<RawData> call, @NonNull Throwable t) {
                Log.d("getPlaceType", "onFailure: " + t.getMessage());
            }
        });
    }

    public interface FourSquareCallback {
        void gotCategories(List<Category> categories);
    }

}
