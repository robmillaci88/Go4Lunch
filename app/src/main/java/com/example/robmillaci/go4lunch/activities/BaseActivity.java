package com.example.robmillaci.go4lunch.activities;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.example.robmillaci.go4lunch.adapters.AddedUsersAdapter;
import com.example.robmillaci.go4lunch.alarms_and_receivers.NetworkStateReceiver;
import com.example.robmillaci.go4lunch.data_objects.Users;
import com.example.robmillaci.go4lunch.firebase.FirebaseHelper;

import java.util.ArrayList;

/**
 * The base activity of all activities implementing FirebaseHelper.firebaseDataCallback
 * Also implements onResume to register a network state change listener and onPause to unregister the reciever
 */

public abstract class BaseActivity extends AppCompatActivity implements FirebaseHelper.firebaseDataCallback {

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        if (MainActivity.mNetworkStateReceiver != null) {
            registerReceiver(MainActivity.mNetworkStateReceiver, intentFilter);
        }else {
            registerReceiver(new NetworkStateReceiver(), intentFilter);
        }
    }

    //on Pause unregister the network state reciever
    @Override
    protected void onPause() {
        try {
            this.unregisterReceiver(MainActivity.mNetworkStateReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }

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
}
