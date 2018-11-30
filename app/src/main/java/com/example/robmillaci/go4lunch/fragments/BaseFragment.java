package com.example.robmillaci.go4lunch.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.example.robmillaci.go4lunch.adapters.AddedUsersAdapter;
import com.example.robmillaci.go4lunch.adapters.UsersListAdapter;
import com.example.robmillaci.go4lunch.data_objects.Users;
import com.example.robmillaci.go4lunch.firebase.FirebaseHelper;
import com.example.robmillaci.go4lunch.utils.NetworkInfoChecker;

import java.util.ArrayList;

/**
 * The base Fragment of all fragments implementing FirebaseHelper.firebaseDataCallback
 */
public abstract class BaseFragment extends Fragment implements FirebaseHelper.firebaseDataCallback {
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
        if (!NetworkInfoChecker.isNetworkAvailable(getActivity())) {
            getActivity().finish();
            getActivity().recreate();
        }
    }
}
