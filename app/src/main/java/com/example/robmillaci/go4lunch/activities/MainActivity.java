package com.example.robmillaci.go4lunch.activities;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.robmillaci.go4lunch.R;
import com.example.robmillaci.go4lunch.alarms_and_receivers.NetworkStateReceiver;
import com.example.robmillaci.go4lunch.data_objects.PojoPlace;
import com.example.robmillaci.go4lunch.fragments.BaseFragment;
import com.example.robmillaci.go4lunch.fragments.GoogleMapsFragment;
import com.example.robmillaci.go4lunch.fragments.LikedRestaurantsFragment;
import com.example.robmillaci.go4lunch.fragments.RestaurantListFragment;
import com.example.robmillaci.go4lunch.fragments.UserListFragment;
import com.example.robmillaci.go4lunch.utils.NetworkInfoChecker;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import static android.support.design.widget.TabLayout.OnTabSelectedListener;
import static android.support.design.widget.TabLayout.Tab;

/**
 * The mainActivity of this applications responsible for creation of<br>
 * {@link UserListFragment}<br>
 * {@link GoogleMapsFragment}<br>
 * {@link LikedRestaurantsFragment}<br>
 * {@link RestaurantListFragment}<br>
 */

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static NetworkStateReceiver mNetworkStateReceiver;
    private Tab mMapTab; //Map tap item that displays the GoogleMapsFragment
    private Tab mListTab; //Map tap item that displays the GoogleMapsFragment
    private Tab mFriendsTab; //Map tap item that displays the GoogleMapsFragment
    private Tab mLikedTab; //Map tap item that displays the GoogleMapsFragment

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.main_title)); //Sets the title of this activity

        ImageView noInternetImage = findViewById(R.id.no_internet_image);
        Button tryAgainButton = findViewById(R.id.try_again_button);


        if (!NetworkInfoChecker.isNetworkAvailable(this)) {
            noInternetImage.setVisibility(View.VISIBLE);
            tryAgainButton.setVisibility(View.VISIBLE);
            tryAgainButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recreate();
                }
            });
        } else {
            noInternetImage.setVisibility(View.GONE);
            tryAgainButton.setVisibility(View.GONE);


            mNetworkStateReceiver = new NetworkStateReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            registerReceiver(mNetworkStateReceiver, filter);

       /*
       create the nav drawer and set the name, email and user pic to be displayed
        */
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
            navigationView.setItemIconTintList(null);

            EditText navName = navigationView.getHeaderView(0).findViewById(R.id.navbarName);
            TextView navEmail = navigationView.getHeaderView(0).findViewById(R.id.navbarEmail);
            ImageView navPic = navigationView.getHeaderView(0).findViewById(R.id.profPic);

            navName.setText(StartActivity.loggedInUser);
            navEmail.setText(StartActivity.loggedInEmail);
            Picasso.get().load(StartActivity.loggedInPic).into(navPic);


            //create tab listeners which will perform UI changes to the tab icons and create the specific fragments
            TabLayout mTabLayout = findViewById(R.id.tabLayout);
            mMapTab = mTabLayout.getTabAt(0);
            mListTab = mTabLayout.getTabAt(1);
            mFriendsTab = mTabLayout.getTabAt(2);
            mLikedTab = mTabLayout.getTabAt(3);

            mTabLayout.addOnTabSelectedListener(new OnTabSelectedListener() {
                @Override
                public void onTabSelected(Tab tab) {
                    switch (tab.getPosition()) {
                        case 0:
                            mListTab.setIcon(R.drawable.list);
                            mFriendsTab.setIcon(R.drawable.work_mates);
                            mMapTab.setIcon(R.drawable.map_icon_select);
                            mLikedTab.setIcon(R.drawable.like);
                            createGoogleMapsFragment();
                            break;

                        case 1:
                            mListTab.setIcon(R.drawable.list_selected);
                            mFriendsTab.setIcon(R.drawable.work_mates);
                            mMapTab.setIcon(R.drawable.map_icon);
                            mLikedTab.setIcon(R.drawable.like);
                            createRestaurantListFragment();
                            break;

                        case 2:
                            mListTab.setIcon(R.drawable.list);
                            mFriendsTab.setIcon(R.drawable.works_mates_selected);
                            mMapTab.setIcon(R.drawable.map_icon);
                            mLikedTab.setIcon(R.drawable.like);
                            createUserListFragment();
                            break;

                        case 3:
                            mLikedTab.setIcon(R.drawable.like_selected);
                            mListTab.setIcon(R.drawable.list);
                            mFriendsTab.setIcon(R.drawable.work_mates);
                            mMapTab.setIcon(R.drawable.map_icon);
                            createLikedRestaurantFragment();
                    }
                }

                @Override
                public void onTabUnselected(Tab tab) {
                }

                @Override
                public void onTabReselected(Tab tab) {
                    onTabSelected(tab);
                }
            });


            createGoogleMapsFragment();

        }
    }

    /**
     * Creates the LikedRestaurantFragment
     * see {@link LikedRestaurantsFragment}
     */
    private void createLikedRestaurantFragment() {
        final LikedRestaurantsFragment likedRestaurantsFragment = new LikedRestaurantsFragment();
        FragmentManager manager = this.getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container, likedRestaurantsFragment, String.valueOf(BaseFragment.LIKED_RESTAURANT_FRAGMENT));
        transaction.addToBackStack(null);
        transaction.commit();
    }


    /**
     * Creates the UserListFragment
     * see {@link UserListFragment}
     */
    private void createUserListFragment() {
        final UserListFragment friendsFragment = new UserListFragment();
        FragmentManager manager = this.getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container, friendsFragment, String.valueOf(BaseFragment.USER_LIST_FRAGMENT));
        transaction.addToBackStack(null);
        transaction.commit();
    }


    /**
     * Creates the GoogleMapsFragment<br>
     * First this method will check for Location permissions
     * see {@link GoogleMapsFragment}
     */
    private void createGoogleMapsFragment() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            final GoogleMapsFragment mapsFragment = new GoogleMapsFragment();
            FragmentManager manager = this.getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.fragment_container, mapsFragment, String.valueOf(BaseFragment.GOOGLE_MAPS_FRAGMENT));
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }


    /**
     * Creates the RestaurantListFragment
     * see {@link RestaurantListFragment}
     */
    private void createRestaurantListFragment() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            final RestaurantListFragment listFragment = new RestaurantListFragment();
            FragmentManager manager = this.getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.fragment_container, listFragment, String.valueOf(BaseFragment.RESTAURANT_LIST_FRAGMENT));
            transaction.commit();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    //Defines the behaviour of the nav drawer menu items
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.menu_lunch: //Get the current selected place to eat u
                item.setChecked(false);
                PojoPlace myPlace = GoogleMapsFragment.getEatingAtPlace(getApplicationContext()); //retrieve the users selected place to eat
                if (myPlace == null) {
                    Toast.makeText(this, R.string.no_place_selected_yet, Toast.LENGTH_LONG).show(); //if not place is selected show a toast
                } else {
                    Intent restaurantDetailPage = new Intent(getApplicationContext(), RestaurantActivity.class);
                    restaurantDetailPage.putExtra(PojoPlace.PLACE_SERIALIZABLE_KEY, myPlace);
                    getApplicationContext().startActivity(restaurantDetailPage);
                }
                break;

            case R.id.menu_settings:
                item.setChecked(false);
                startActivity(new Intent(this,SettingsActivity.class));
                break;

            case R.id.menu_logout: //Logs the current user out of the application, returning them to the StartActivity
                item.setChecked(false);
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();

                Toast.makeText(this, R.string.logout_text, Toast.LENGTH_LONG).show();

                Intent i = new Intent(this, StartActivity.class);
                startActivity(i);
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    //Handle the location permission request. If permission is granted, create GoogleMapsFragment
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // permission was granted, yay!
            createGoogleMapsFragment();

        } else {
            // permission denied
            //noinspection ConstantConditions
            Snackbar.make(getCurrentFocus(), getString(com.example.robmillaci.go4lunch.R.string.permission_needed), Snackbar.LENGTH_LONG).show();
        }

    }


    //save the users currently selected tab so we can restore this value when returning to MainActivity
    @Override
    protected void onPause() {
        super.onPause();
        try {
            this.unregisterReceiver(mNetworkStateReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
