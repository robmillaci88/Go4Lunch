package com.example.robmillaci.go4lunch.adapters;

import android.support.v7.widget.RecyclerView;

import com.example.robmillaci.go4lunch.data_objects.Users;
import com.example.robmillaci.go4lunch.firebase.FirebaseHelper;

import java.util.ArrayList;
/**
 * The base adapter class of all adapters implementing FirebaseHelper.firebaseDataCallback
 */
public abstract class BaseAdapterClass extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements FirebaseHelper.firebaseDataCallback{
    @Override
    public void finishedGettingEaters(ArrayList<Users> users, RecyclerView.ViewHolder v) {
    }

    @Override
    public void datadownloadedcallback(ArrayList<Object> arrayList) {
    }

    @Override
    public void workUsersDataCallback(ArrayList<Users> arrayList) {
    }

    @Override
    public void finishedGettingUsers(String[] users, UsersListAdapter.MyviewHolder viewHolder) {
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
}
