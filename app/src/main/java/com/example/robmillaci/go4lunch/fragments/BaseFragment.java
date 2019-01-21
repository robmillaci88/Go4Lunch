package com.example.robmillaci.go4lunch.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;

import com.example.robmillaci.go4lunch.adapters.AddedUsersAdapter;
import com.example.robmillaci.go4lunch.data_objects.Users;
import com.example.robmillaci.go4lunch.firebase.FirebaseHelper;
import com.example.robmillaci.go4lunch.utils.NetworkInfoChecker;

import java.util.ArrayList;

/**
 * The base Fragment of all fragments implementing FirebaseHelper.firebaseDataCallback
 */
public abstract class BaseFragment extends Fragment implements FirebaseHelper.firebaseDataCallback {

    public static final int GOOGLE_MAPS_FRAGMENT = 0;
    public static final int LIKED_RESTAURANT_FRAGMENT = 1;
    public static final int RESTAURANT_LIST_FRAGMENT = 2;
    public static final int USER_LIST_FRAGMENT = 3;


    @Override
    public void finishGettingUsersEatingHere(ArrayList<Users> users, RecyclerView.ViewHolder v) {
    }

    @Override
    public void datadownloadedcallback(ArrayList<Object> arrayList) {
    }

    @Override
    public void workUsersDataCallback(ArrayList<Users> arrayList, Object o) {
    }

    @Override
    public void finishedGettingPlace(AddedUsersAdapter.MyviewHolder myviewHolder, String s, String placeId) {
    }

    @Override
    public void isItLikedCallback(boolean response) {
    }

    @Override
    public void finishedGettingLikedRestaurants(ArrayList<String> places) {
    }

    @Override
    public void isPlaceSelected(boolean currentUserSelectedPlace, boolean otherUsersSelectedPlace) {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //noinspection ConstantConditions
        if (!NetworkInfoChecker.isNetworkAvailable(getActivity())) {
            getActivity().finish();
            getActivity().recreate();
        }
    }
}
