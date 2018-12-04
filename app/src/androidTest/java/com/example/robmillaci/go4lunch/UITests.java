
package com.example.robmillaci.go4lunch;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.matcher.RootMatchers;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.robmillaci.go4lunch.activities.MainActivity;
import com.example.robmillaci.go4lunch.activities.RestaurantActivity;
import com.example.robmillaci.go4lunch.activities.ReviewsActivity;
import com.example.robmillaci.go4lunch.activities.StartActivity;
import com.example.robmillaci.go4lunch.adapters.RestaurantListAdapter;
import com.example.robmillaci.go4lunch.fragments.BaseFragment;
import com.example.robmillaci.go4lunch.fragments.GoogleMapsFragment;
import com.example.robmillaci.go4lunch.fragments.LikedRestaurantsFragment;
import com.example.robmillaci.go4lunch.fragments.UserListFragment;
import com.example.robmillaci.go4lunch.view_actions.MyViewActions;
import com.example.robmillaci.go4lunch.view_matchers.ChildPositionMatchers;
import com.google.android.gms.maps.model.Marker;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
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

        Fragment myFragment = mMainActivityActivityTestRule.getActivity().getSupportFragmentManager().findFragmentByTag(String.valueOf(BaseFragment.GOOGLE_MAPS_FRAGMENT));
        assertTrue(myFragment != null && myFragment.isVisible());

    }


    @Test
    public void clickListTabOpensListView() {
        try {
            Thread.sleep(3000); //wait for found places to settle

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction tabLayout = onView(ViewMatchers.withId(R.id.tabLayout));
        tabLayout.check(matches(isEnabled()));

        int numberOfMarkers = 0;
        if (GoogleMapsFragment.getAllMarkers() != null) {
            numberOfMarkers = GoogleMapsFragment.getAllMarkers().size(); //get the number of markers on the map
        }

        onView(withText("List View")).perform(click()); //click on the list view tab

        //check that the Restaurant list fragment is visible
        Fragment myFragment = mMainActivityActivityTestRule.getActivity().getSupportFragmentManager().findFragmentByTag(String.valueOf(BaseFragment.RESTAURANT_LIST_FRAGMENT));
        assertTrue(myFragment != null && myFragment.isVisible());

        //now check the count of the recyclerview matches the number of markers on the map
        onView(withId(R.id.restaurant_list_recycler_view)).check(new RecyclerViewItemCountAssertion(numberOfMarkers));

    }


    @Test
    public void clickLikedTabOpensLikedRestaurants() {
        try {
            Thread.sleep(3000); //wait for found places to settle

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction tabLayout = onView(ViewMatchers.withId(R.id.tabLayout));
        tabLayout.check(matches(isEnabled()));

        onView(withText("Liked")).perform(click()); //click on the liked  tab

        //check that the Restaurant list fragment is visible
        Fragment myFragment = mMainActivityActivityTestRule.getActivity().getSupportFragmentManager().findFragmentByTag(String.valueOf(BaseFragment.LIKED_RESTAURANT_FRAGMENT));
        assertTrue(myFragment != null && myFragment.isVisible());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //now check the count of the recyclerview matches the number of markers on the map
        final int count = ((LikedRestaurantsFragment) myFragment).getPojoPlaces().size();

        onView(withId(R.id.likedRecyclerView)).check(new RecyclerViewItemCountAssertion(count));

    }

    @Test
    public void clickAllUsersDisplaysAllUsers() {
        onView(withText("Mates")).perform(click()); //click on the liked  tab

        //check that the Restaurant list fragment is visible
        Fragment myFragment = mMainActivityActivityTestRule.getActivity().getSupportFragmentManager().findFragmentByTag(String.valueOf(BaseFragment.USER_LIST_FRAGMENT));
        assertTrue(myFragment != null && myFragment.isVisible());

        onView(withText("ALL USERS")).perform(click()); //click on all users tab

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //now check the count of the recyclerview matches the number of users
        final int count = ((UserListFragment) myFragment).getAllusers().size();

        onView(withId(R.id.usersListRecyclerView)).check(new RecyclerViewItemCountAssertion(count));
    }

    @Test
    public void clickFriendsUsersDisplaysFriendsUsers() {
        onView(withText("Mates")).perform(click()); //click on the liked  tab

        //check that the Restaurant list fragment is visible
        Fragment myFragment = mMainActivityActivityTestRule.getActivity().getSupportFragmentManager().findFragmentByTag(String.valueOf(BaseFragment.USER_LIST_FRAGMENT));
        assertTrue(myFragment != null && myFragment.isVisible());

        onView(withText("MY WORKMATES")).perform(click()); //click on all users tab

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //now check the count of the recyclerview matches the number of users
        final int count = ((UserListFragment) myFragment).getMyFriends().size();

        onView(withId(R.id.usersListRecyclerView)).check(new RecyclerViewItemCountAssertion(count));
    }


    @Test
    public void checkNavDrawerHasData() {
        onView(withId(R.id.drawer_layout)).perform(click()); //open the nav drawer

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final String userName = StartActivity.loggedInUser;
        final String email = StartActivity.loggedInEmail;

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //check username is correct
        onView(withId(R.id.navbarName)).check(new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                if (noViewFoundException != null) {
                    throw noViewFoundException;
                }
                assertTrue(((TextView) view).getText().equals(userName));
            }
        });

        //check user email is correct
        onView(withId(R.id.navbarEmail)).check(new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                if (noViewFoundException != null) {
                    throw noViewFoundException;
                }
                assertTrue(((TextView) view).getText().equals(email));
            }
        });
    }

    @Test
    public void checkNumberofUsersEatingHereMatchesRecyclerView() {
//wait for the places to settle
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int loopCount = GoogleMapsFragment.getAllMarkers().size();

        ViewInteraction tabLayout = onView(ViewMatchers.withId(R.id.tabLayout));
        tabLayout.check(matches(isEnabled()));


        onView(withText("List View")).perform(click()); //click on the list view tab

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < loopCount; i++) {
            final String[] numberDisplayed = new String[1];
            final int finalI = i;
            onView(withId(R.id.restaurant_list_recycler_view)).check(new ViewAssertion() {
                @Override
                public void check(View view, NoMatchingViewException noViewFoundException) {
                    RecyclerView.ViewHolder thisHolder = ((RecyclerView) view).findViewHolderForAdapterPosition(finalI);
                    numberDisplayed[0] = ((RestaurantListAdapter.MyviewHolder) thisHolder).getNumberOfEaters().getText().toString();

                }
            });

            onView(withId(R.id.restaurant_list_recycler_view))
                    .perform(RecyclerViewActions.<RestaurantListAdapter.MyviewHolder>actionOnItemAtPosition(finalI, click()));

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            onView(withId(R.id.peopleEatingRecyclerView)).check(new ViewAssertion() {
                @Override
                public void check(View view, NoMatchingViewException noViewFoundException) {
                    int recyclerViewCount = ((RecyclerView) view).getAdapter().getItemCount();

                    assertTrue(numberDisplayed[0].equals(String.valueOf(recyclerViewCount) + " friend(s)"));
                }
            });

            onView(withId(R.id.backbtn)).perform(click());

        }
    }


    @Test
    public void checkReviews() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction tabLayout = onView(ViewMatchers.withId(R.id.tabLayout));
        tabLayout.check(matches(isEnabled()));

        onView(withText("List View")).perform(click()); //click on the list view tab

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int count = GoogleMapsFragment.getAllMarkers().size();

        if (count > 0) {
            Intents.init();

            onView(withId(R.id.restaurant_list_recycler_view))
                    .perform(RecyclerViewActions.<RestaurantListAdapter.MyviewHolder>actionOnItemAtPosition(0, MyViewActions.clickChildViewWithId(R.id.reviews)));

            intended(hasComponent(ReviewsActivity.class.getName()));

            Intents.release();
        }

    }

    @Test
    public void checkOpeningTimesDialogOnListView() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction tabLayout = onView(ViewMatchers.withId(R.id.tabLayout));
        tabLayout.check(matches(isEnabled()));

        onView(withText("List View")).perform(click()); //click on the list view tab

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int count = GoogleMapsFragment.getAllMarkers().size();

        if (count > 0) {
            for (int i = 0; i < count; i++) {
                onView(withId(R.id.restaurant_list_recycler_view))
                        .perform(RecyclerViewActions.<RestaurantListAdapter.MyviewHolder>actionOnItemAtPosition(i, MyViewActions.clickChildViewWithId(R.id.openinghours)));

                onView(withId(R.id.mondayTextView)).inRoot(isDialog())
                        .check(matches(isDisplayed()));
                onView(withId(R.id.tuesdayTextView)).inRoot(isDialog())
                        .check(matches(isDisplayed()));
                onView(withId(R.id.wednesdayTextView)).inRoot(isDialog())
                        .check(matches(isDisplayed()));
                onView(withId(R.id.thursdayTextView)).inRoot(isDialog())
                        .check(matches(isDisplayed()));
                onView(withId(R.id.fridayTextView)).inRoot(isDialog())
                        .check(matches(isDisplayed()));
                onView(withId(R.id.saturdayTextView)).inRoot(isDialog())
                        .check(matches(isDisplayed()));
                onView(withId(R.id.sundayTextView)).inRoot(isDialog())
                        .check(matches(isDisplayed()));

                onView(withText("OK")).perform(click());
            }
        }
    }


    @Test
    public void checkOpeningTimesDialogOnLiked() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction tabLayout = onView(ViewMatchers.withId(R.id.tabLayout));
        tabLayout.check(matches(isEnabled()));

        onView(withText("Liked")).perform(click()); //click on the list view tab

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final int[] count = new int[1];
        onView(withId(R.id.likedRecyclerView)).check(new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                count[0] = ((RecyclerView) view).getAdapter().getItemCount();
            }
        });

        for (int i = 0; i < count[0]; i++) {
            onView(withId(R.id.likedRecyclerView))
                    .perform(RecyclerViewActions.<RestaurantListAdapter.MyviewHolder>actionOnItemAtPosition(0, MyViewActions.clickChildViewWithId(R.id.openinghours)));

            onView(withId(R.id.mondayTextView)).inRoot(isDialog())
                    .check(matches(isDisplayed()));
            onView(withId(R.id.tuesdayTextView)).inRoot(isDialog())
                    .check(matches(isDisplayed()));
            onView(withId(R.id.wednesdayTextView)).inRoot(isDialog())
                    .check(matches(isDisplayed()));
            onView(withId(R.id.thursdayTextView)).inRoot(isDialog())
                    .check(matches(isDisplayed()));
            onView(withId(R.id.fridayTextView)).inRoot(isDialog())
                    .check(matches(isDisplayed()));
            onView(withId(R.id.saturdayTextView)).inRoot(isDialog())
                    .check(matches(isDisplayed()));
            onView(withId(R.id.sundayTextView)).inRoot(isDialog())
                    .check(matches(isDisplayed()));

            onView(withText("OK")).perform(click());
        }
    }

    @Test
    public void clickMarkerOpens() {

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (String title : GoogleMapsFragment.getAllMarkers().keySet()) {
            final Marker m = GoogleMapsFragment.getSpecificMarker(title);
            Fragment myFragment = mMainActivityActivityTestRule.getActivity().getSupportFragmentManager().findFragmentByTag(String.valueOf(BaseFragment.GOOGLE_MAPS_FRAGMENT));
            //noinspection ConstantConditions
            myFragment.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new GoogleMapsFragment().onMarkerClick(m);
                }
            });

            Intents.init();


            intended(hasComponent(RestaurantActivity.class.getName()));

            onView(withText(title)).check(matches(isDisplayed()));

            onView(withId(R.id.backbtn)).perform(click());
            Intents.release();

        }
    }

    @Test
    public void markerFilterNoValues() {

        onView(withId(R.id.action_search)).perform(click());
        onView(withId(android.support.design.R.id.search_src_text)).perform(typeText("zxzcvxzvzcvzsvzsvzvxv")); //type a string that will display no markers

        assertTrue(GoogleMapsFragment.getmPlaces().size() == 0);

    }

    @Test
    public void markerFilterWithValues() {

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int markersOnMap = GoogleMapsFragment.getmPlaces().size();

        if (markersOnMap > 0) {
            //noinspection LoopStatementThatDoesntLoop
            for (String s : GoogleMapsFragment.getAllMarkers().keySet()) {
                String textToSearch = GoogleMapsFragment.getmPlaces().get(s).getName();

                onView(withId(R.id.action_search)).perform(click());
                onView(withId(android.support.design.R.id.search_src_text)).perform(typeText(textToSearch)); //type a string that will display 1 marker

                assertTrue(GoogleMapsFragment.getmPlaces().size() == 1);

                break;
            }
        }
    }


    @Test
    public void placesSearch(){
        String searchText = "2 Dunsley vale swindon";
        String matchingText = "2 Dunsley Vale";

        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.placesSearch), withContentDescription("Item"),
                        ChildPositionMatchers.childAtPosition(
                                ChildPositionMatchers.childAtPosition(
                                        withId(R.id.toolbar),
                                        2),
                                1),
                        isDisplayed()));
        actionMenuItemView.perform(click());

        UiDevice mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject searchView = mDevice.findObject(new UiSelector().text("Search"));
        try {
            searchView.setText(searchText);
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(2000);
            mDevice.findObject(new UiSelector().text(matchingText)).click();
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        HashMap<String,Marker> markers = GoogleMapsFragment.getAllMarkers();
        assertTrue(markers.containsKey(matchingText));
    }
}

