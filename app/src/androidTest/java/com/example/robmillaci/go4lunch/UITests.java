package com.example.robmillaci.go4lunch;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.example.robmillaci.go4lunch.activities.CallersEnum;
import com.example.robmillaci.go4lunch.activities.MainActivity;
import com.example.robmillaci.go4lunch.activities.StartActivity;
import com.example.robmillaci.go4lunch.fragments.GoogleMapsFragment;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)

public class UITests {
    @Rule
    public ActivityTestRule<StartActivity> mStartActivityActivityTestRule =
            new ActivityTestRule<>(StartActivity.class);

    @Rule
    public ActivityTestRule<MainActivity> mMainActivityActivityTestRule =
            new ActivityTestRule<>(MainActivity.class);


    //Test opening the app finds the users location and googleMaps fragment is displayed
    @Test
    public void gotLocationAndMapVisibility() {
        try {
            Thread.sleep(5000); //wait 5 seconds to find location
            assertTrue(GoogleMapsFragment.getCurrentlocation() != null);

        } catch (InterruptedException e) {
            //get the devices location
            e.printStackTrace();
        }

        Fragment myFragment = mMainActivityActivityTestRule.getActivity().getSupportFragmentManager().findFragmentByTag(CallersEnum.GOOGLE_MAPS_FRAGMENT.name());
        assertTrue(myFragment != null && myFragment.isVisible());

    }


    @Test
    public void clickListTabOpensListView() {
        try {
        Thread.sleep(5000); //wait for found places to settle

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction tabLayout = onView(ViewMatchers.withId(R.id.tabLayout));
        tabLayout.check(matches(isEnabled()));

            int numberOfMarkers = GoogleMapsFragment.getAllMarkers().size(); //get the number of markers on the map
            onView(withText("List View")).perform(click()); //click on the list view tab

            //check that the Restaurant list fragment is visible
            Fragment myFragment = mMainActivityActivityTestRule.getActivity().getSupportFragmentManager().findFragmentByTag(CallersEnum.RESTAURANT_LIST_FRAGMENT.name());

            //now check the count of the recyclerview matches the number of markers on the map
            onView(withId(R.id.restaurant_list_recycler_view)).check(new RecyclerViewItemCountAssertion(numberOfMarkers));

    }


}

