package com.example.robmillaci.go4lunch.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.robmillaci.go4lunch.R;
import com.example.robmillaci.go4lunch.adapters.RestaurantListAdapter;
import com.example.robmillaci.go4lunch.data_objects.PojoPlace;
import com.example.robmillaci.go4lunch.firebase.FirebaseHelper;
import com.example.robmillaci.go4lunch.utils.RecyclerViewMods;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;

import java.util.ArrayList;


/**
 * This fragment is responsible for creating and displaying the users liked restaurants<br>
 * Builds the likedRestaurants using {@link #buildLikedRestaurants()} which calls {@link FirebaseHelper#getLikedPlaces()}
 */
public class LikedRestaurantsFragment extends BaseFragment implements IgooglePlacescallback {
    private RecyclerView likedRecyclerView;
    private RestaurantListAdapter mAdaptor;
    private ImageView noLikedRestaurants;
    private ArrayList<PojoPlace> pojoPlaces;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //noinspection ConstantConditions
        getActivity().setTitle(getString(R.string.liked_restaurant_title));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.liked_restaurant_fragment, container, false);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        pojoPlaces = new ArrayList<>();
        likedRecyclerView = view.findViewById(R.id.likedRecyclerView);
        noLikedRestaurants = view.findViewById(R.id.no_liked_places_found);
        noLikedRestaurants.setVisibility(View.GONE);
        setHasOptionsMenu(true);
        buildLikedRestaurants(); //see class docs
    }


    /**
     * Calls {@link FirebaseHelper#getLikedPlaces()} which calls back to {@link #finishedGettingLikedRestaurants(ArrayList)}
     */
    private void buildLikedRestaurants() {
        FirebaseHelper firebaseHelper = new FirebaseHelper(this);
        firebaseHelper.getLikedPlaces(); //see class docs
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdaptor.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (mAdaptor != null) {
                    mAdaptor.getFilter().filter(newText);
                }
                return false;
            }
        });


    }


    /**
     * Callback from {@link FirebaseHelper#getLikedPlaces()}
     *
     * @param places returned liked place ID's
     */
    @Override
    public void finishedGettingLikedRestaurants(ArrayList<String> places) {
        //now get the places by their ID returned
        new GoogleMapsFragment().getPlaceById(places, LikedRestaurantsFragment.this);
    }


    /**
     * Callback from {@link GoogleMapsFragment#getPlaceById(ArrayList, IgooglePlacescallback)}
     * Converts the googlePlaces into PojoPlaces and creates and sets the recyclerview adapter to display liked places
     * * @param places the list of the returned places
     *
     * @param placesBuffer the placeBufferResponse so we can close it once we are finished
     */
    @SuppressWarnings("ConstantConditions")
    @Override
    public void gotplaces(ArrayList<Place> places, PlaceBufferResponse placesBuffer) {
        for (Place p : places) {
            pojoPlaces.add(new PojoPlace(p, null));
        }

        if (pojoPlaces.size() == 0) {
            noLikedRestaurants.setVisibility(View.VISIBLE);
            setRecyclerView(pojoPlaces, placesBuffer);
        } else {
            noLikedRestaurants.setVisibility(View.GONE);
            setRecyclerView(pojoPlaces, placesBuffer);
        }

        if(placesBuffer != null && !placesBuffer.isClosed()) {
            placesBuffer.close();
        }
    }

    private void setRecyclerView(ArrayList<PojoPlace> places, PlaceBufferResponse placeBufferResponse) {
        mAdaptor = new RestaurantListAdapter(places, GoogleMapsFragment.getCurrentlocation(), placeBufferResponse, getContext());
        likedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerViewMods.setAnimation(likedRecyclerView);
        likedRecyclerView.setAdapter(mAdaptor);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mAdaptor.notifyDataSetChanged();
            }
        }, 1000);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mAdaptor != null) {
            buildLikedRestaurants();
        }
    }

    public ArrayList<PojoPlace> getPojoPlaces() {
        return pojoPlaces;
    }
}
