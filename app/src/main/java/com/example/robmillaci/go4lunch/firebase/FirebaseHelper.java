package com.example.robmillaci.go4lunch.firebase;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import com.example.robmillaci.go4lunch.R;
import com.example.robmillaci.go4lunch.adapters.AddedUsersAdapter;
import com.example.robmillaci.go4lunch.data_objects.Users;
import com.example.robmillaci.go4lunch.data_objects.chat_objects.ChatObject;
import com.example.robmillaci.go4lunch.fragments.GoogleMapsFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.example.robmillaci.go4lunch.activities.RestaurantActivity.MARKER_UNSELECTED;

public class FirebaseHelper {
    private static final String mCurrentUserId; //the current users ID
    private static final String mCurrentUserPicUrl; //the current users picture URL
    private static final DocumentReference userDoc; //the users document reference
    private static final String EXCLUDED_USER;

    private int count; //the count of the added friends

    //Database field values

    private static final String DATABASE_CHAT_COLLECTION = "chatdata";
    private static final String DATABASE_CHAT_NOTIFICATIONS = "chatNotifications";
    private static final String DATABASE_LIKED_RESTAURANTS_FIELD = "likedPlaces";
    private static final String NEW_MESSAGE_FIELD = "New message";

    public static final String DATABASE_TOKEN_PATH = "token";

    public static final String DATABASE_COLLECTION_PATH = "users";
    public static final String DATABASE_NAME_FIELD = "username";
    public static final String DATABASE_EMAIL_FIELD = "userEmail";
    public static final String DATABASE_PICTURE_FIELD = "picture";
    public static final String DATABASE_UNIQUE_ID_FIELD = "uniqueID";
    public static final String DATABASE_ADDED_USERS_FIELD = "addedUsers";
    public static final String DATABASE_SELECTED_RESTAURANT_FIELD = "selectedRestaurant";
    public static final String DATABASE_SELECTED_RESTAURANT_ID_FIELD = "selectedPlaceID";


    private firebaseDataCallback mFirebaseDataCallback;
    private chatData mChatDataCallback;

    private ArrayList<Users> usersObjects = new ArrayList<>();


    /**
     * 2 constructors for this class one for each different callback
     *
     * @param mCallback the callback for this instance of the class
     */

    public FirebaseHelper(firebaseDataCallback mCallback) {
        mFirebaseDataCallback = mCallback;
    }

    public FirebaseHelper(chatData mCallback) {
        mChatDataCallback = mCallback;
    }


    static {
        FirebaseAuth auth = FirebaseAuth.getInstance();   //get the Firebase Auth instance
        FirebaseUser currentUser = auth.getCurrentUser(); //get the current user
        assert currentUser != null;
        mCurrentUserId = currentUser.getUid(); //get the current users ID
        EXCLUDED_USER = mCurrentUserId;
        //noinspection ConstantConditions
        mCurrentUserPicUrl = currentUser.getPhotoUrl().toString(); //get the current users photo url
        userDoc = FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(FirebaseHelper.getmCurrentUserId()); //get the current users doc
    }


    private static Query getQueryDocSnapshot(String userId) {
        return FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).whereEqualTo(DATABASE_UNIQUE_ID_FIELD, userId);
    }

    public static Query getCurrentUserData() {
        return getQueryDocSnapshot(mCurrentUserId);
    }


    /**
     * Returns the current users friends from the database and then calls {@link #createUserObjects(ArrayList, Object)} to be used by the application
     */
    @SuppressWarnings("ConstantConditions")
    public void getMyWorkUsers(final Object returnObject) {
        FirebaseHelper.getCurrentUserData().get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                HashSet<String> hashSet = new HashSet<>(); //used to remove any duplication

                if (task.isSuccessful()) {
                    try {
                        QuerySnapshot taskResults = task.getResult();
                        List<DocumentSnapshot> documents = taskResults.getDocuments();
                        String[] addedUsers = documents.get(0).get(DATABASE_ADDED_USERS_FIELD).toString().split(","); //split the returned "added users" string
                        hashSet.addAll(Arrays.asList(addedUsers)); //ensure not duplicates
                        //add the results to an array list for return
                        ArrayList<String> addedUsersArray = new ArrayList<>(hashSet);

                        createUserObjects(addedUsersArray, returnObject); //create the users objects from the arraylist of added users

                    } catch (Exception e) {
                        e.printStackTrace();
                        createUserObjects(null, null);
                    }
                }

            }
        });
    }


    /**
     * From a string of user Id's this method will extract the relevant user details from Firebase and create {@link Users}
     *
     * @param addedUsersArray the arraylist of user IDs to lookup in the database
     * @param returnObject    the object that is required to return to the callback method
     */
    private void createUserObjects(ArrayList<String> addedUsersArray, final Object returnObject) {
        if (addedUsersArray == null) {
            mFirebaseDataCallback.workUsersDataCallback(null, null);
        } else {
            usersObjects = new ArrayList<>();
            count = addedUsersArray.size(); //count to determine when we have looped through each user. Minus 1 because we are not including the logged in user

            for (String s : addedUsersArray) { //for each user ID in the addedUsersArray, loop through and extract their relevant information to create Users objects
                count--;
                Query docRef = FirebaseHelper.getQueryDocSnapshot(s);
                docRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot taskResults = task.getResult();
                            List<DocumentSnapshot> documents = taskResults.getDocuments();
                            String userName;
                            String email;
                            String id;
                            String picture;
                            try {
                                DocumentSnapshot d = documents.get(0);
                                userName = (String) d.get(DATABASE_NAME_FIELD);
                                email = (String) d.get(DATABASE_EMAIL_FIELD);
                                id = (String) d.get(DATABASE_UNIQUE_ID_FIELD);
                                picture = (String) d.get(DATABASE_PICTURE_FIELD);

                                if (!userName.equals("") && !id.equals("")) {
                                    Users user = new Users(userName, id, email, picture);
                                    usersObjects.add(user);
                                }
                                if (count == 0) { //if the count is 0, we have looped through every user ID so we can now callback with the resulting user objects
                                    mFirebaseDataCallback.workUsersDataCallback(usersObjects, returnObject);
                                }
                            } catch (Exception e) {
                                if (count == 0) { //if the count is 0, we have looped through every user ID so we can now callback with the resulting user objects
                                    mFirebaseDataCallback.workUsersDataCallback(usersObjects, returnObject);
                                }
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        }
    }


    /**
     * Gets all the users held in the database and creates {@link #usersObjects}<br>
     */
    public void getAllUsers() {
        FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot taskResults = task.getResult();

                    if (taskResults != null) {
                        List<DocumentSnapshot> documents = taskResults.getDocuments(); //get all the document snapshots from the DATABASE_COLLECTION_PATH

                        ArrayList<Object> usersArrayList = new ArrayList<>(); //create an arraylist to hold the User objects

                        for (DocumentSnapshot d : documents) { //loop through each document and get the relevant data to create Users objects
                            String name = (String) d.get(DATABASE_NAME_FIELD);
                            String email = (String) d.get(DATABASE_EMAIL_FIELD);
                            String picture = (String) d.get(DATABASE_PICTURE_FIELD);
                            String uniqueID = (String) d.get(DATABASE_UNIQUE_ID_FIELD);

                            if (uniqueID != null && !uniqueID.equals(EXCLUDED_USER)) {
                                usersArrayList.add(new Users(name, uniqueID, email, picture));
                            }
                        }
                        mFirebaseDataCallback.datadownloadedcallback(usersArrayList); //callback with the results
                    }
                }
            }
        });
    }


    /**
     * Delete a specific field from the database
     *
     * @param fieldToDelete the specific field to be deleted
     */
    public static void deleteField(String fieldToDelete) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(fieldToDelete, FieldValue.delete());
        userDoc.update(updates);
    }

    /**
     * Update a specific field in the database
     *
     * @param field  the field to be updated
     * @param values the value to update
     */
    public static void updateField(String field, String values) {
        userDoc.update(field, values);
    }


    /**
     * Add the selected place to the database - firstly deleting the old selected place data and then adding the new information
     *
     * @param markerId   - the id of the marker related to this place so at the same time we can update the marker information
     * @param placeId    - the id of the place that has been selected
     * @param oldplaceId - the id of the old selected place that we need to remove
     */
    public void addSelectedPlace(String markerId, String placeId, String oldplaceId) {
        try {
            FirebaseHelper.deleteField(DATABASE_SELECTED_RESTAURANT_FIELD);
            FirebaseHelper.deleteField(DATABASE_SELECTED_RESTAURANT_ID_FIELD);
            Marker m = GoogleMapsFragment.getSpecificMarker(oldplaceId);
            if (m != null) {
                m.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_orange));
                m.setTag(MARKER_UNSELECTED);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put(DATABASE_SELECTED_RESTAURANT_FIELD, markerId);
        updates.put(DATABASE_SELECTED_RESTAURANT_ID_FIELD, placeId);
        userDoc.update(updates);
    }


    /**
     * Like a selected place - first we extract any other liked place id's, and then append the newly liked place ID to the string
     *
     * @param placeID the id of the place to 'like'
     */
    public static void likeRestaurant(final String placeID) {
        FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(getmCurrentUserId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                StringBuilder sb = new StringBuilder(); //String builder to create the new liked places CSV string

                if (task.isSuccessful()) {
                    DocumentSnapshot taskResults = task.getResult();
                    sb.append(placeID); //add the liked place to the start of the string builder object
                    try {
                        //noinspection ConstantConditions /
                        String likedPlaces = taskResults.get(DATABASE_LIKED_RESTAURANTS_FIELD).toString(); //get the previously liked places CSV string
                        String[] likedPlacesArray = likedPlaces.split(","); //split the CSV string into an Array

                        for (String s : likedPlacesArray) { //for each string in the string array, append the comma separation and then append the place ID
                            sb.append(",");
                            sb.append(s);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Map<String, Object> updates = new HashMap<>();
                    updates.put(DATABASE_LIKED_RESTAURANTS_FIELD, sb.toString());
                    userDoc.update(updates); //update the database with the new 'liked' values
                }
            }
        });
    }


    /**
     * Determines wether a specific placeId is selected to eat at.
     *
     * @param placeId the id of the place to check
     * @param users   the list of friends of the user, this is important because we don't want to display information about 'non friend' users
     */
    public void isPlaceSelected(final String placeId, final String[] users) {
        FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    try {
                        QuerySnapshot taskResults = task.getResult();
                        ArrayList<String> addedUsers = new ArrayList<>(Arrays.asList(users)); //create an arraylist holding the friends of the user
                        if (taskResults != null) {
                            ArrayList<String> usersSelectedThisPlace = new ArrayList<>(); //create an arraylist to hold all the users who have selected this place
                            List<DocumentSnapshot> documents = taskResults.getDocuments();

                            for (DocumentSnapshot d : documents) { //loop through each document returned
                                String selectedPlace = (String) d.get(DATABASE_SELECTED_RESTAURANT_ID_FIELD); //get the documents DATABASE_SELECTED_RESTAURANT_ID_FIELD value
                                String uniqueID = (String) d.get(DATABASE_UNIQUE_ID_FIELD); //get the documents DATABASE_UNIQUE_ID_FIELD (the id of the users)

                                //if the place ID we are checking against is present in the returned users 'selected place' & the returned user is a friend then
                                //add to the usersSelectedThisPlace array (we are only interested in those users who have selected this place AND are our friend
                                if (placeId.equals(selectedPlace) && addedUsers.contains(uniqueID)) {
                                    usersSelectedThisPlace.add(uniqueID);
                                }
                            }

                            if (usersSelectedThisPlace.size() > 0) {
                                //check wether the current user has selected this place and wether other users in the app have also selected this place
                                if (usersSelectedThisPlace.contains(mCurrentUserId) && usersSelectedThisPlace.size() > 1) {
                                    mFirebaseDataCallback.isPlaceSelected(true, true);
                                } else if (usersSelectedThisPlace.contains(mCurrentUserId) && usersSelectedThisPlace.size() == 1) {
                                    mFirebaseDataCallback.isPlaceSelected(true, false);
                                } else if (!usersSelectedThisPlace.contains(mCurrentUserId)) {
                                    mFirebaseDataCallback.isPlaceSelected(false, true);
                                } else {
                                    mFirebaseDataCallback.isPlaceSelected(false, false);
                                }
                            } else {
                                mFirebaseDataCallback.isPlaceSelected(false, false);
                            }

                        }
                    } catch (Exception e) {
                        mFirebaseDataCallback.isPlaceSelected(false, false);
                    }
                }
            }
        });
    }


    /**
     * Removes a liked restaurant from the users data in the database
     *
     * @param id the id of the place to remove
     */
    public static void removeLikedPlace(final String id) {
        FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(getmCurrentUserId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                StringBuilder sb = new StringBuilder(); //create a string builder to hold the new CSV string of liked places

                if (task.isSuccessful()) {
                    DocumentSnapshot taskResults = task.getResult();
                    try {
                        @SuppressWarnings("ConstantConditions") String likedPlaces = taskResults.get(DATABASE_LIKED_RESTAURANTS_FIELD).toString(); //get the CSV string of liked places for the user
                        String[] likedPlacesArray = likedPlaces.split(","); //split the CSV liked places into a string[]

                        int count = 0;
                        for (String s : likedPlacesArray) { //for each string in the liked places array
                            if (!s.equals(id)) {//if the liked place id doesn't = the id of the place to remove
                                if (count == 0) { //are we at the first position in the new CSV string ?
                                    sb.append(s); //yes - then append the value to the string builder
                                    count++; //and then increment the count
                                } else {
                                    sb.append(","); //we are not at the first position in the CSV string of liked places, therefore first append a comma separator
                                    sb.append(s); //now append the place ID to build the CSV string
                                }
                            }
                        }

                        if (sb.length() > 0) { //now update the database with the new CSV string of liked place id's
                            Map<String, Object> updates = new HashMap<>();
                            updates.put(DATABASE_LIKED_RESTAURANTS_FIELD, sb.toString());
                            userDoc.update(updates);
                        } else {
                            userDoc.update(DATABASE_LIKED_RESTAURANTS_FIELD, FieldValue.delete());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    /**
     * Checks wether a specific place ID is 'liked'
     *
     * @param id the Id of the place to check if its liked
     */
    public void isItLiked(final String id) {
        FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(getmCurrentUserId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot taskResults = task.getResult();
                    try {
                        @SuppressWarnings("ConstantConditions") String likedPlaces = taskResults.get(DATABASE_LIKED_RESTAURANTS_FIELD).toString(); //get the CSV string of the users 'liked places'
                        String[] likedPlacesArray = likedPlaces.split(","); //split the CSV string into a String [] so we can loop through each one
                        for (String s : likedPlacesArray) { //for each liked place id (s)
                            if (s.equals(id)) { //if the liked place id from the users document = the place id we are checking against
                                mFirebaseDataCallback.isItLikedCallback(true); //callback with true
                                return;
                            }
                        }

                    } catch (Exception e) {
                        mFirebaseDataCallback.isItLikedCallback(false);
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    /**
     * Gets all the users liked places
     */
    public void getLikedPlaces() {
        final ArrayList<String> places = new ArrayList<>();
        FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(mCurrentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot taskResults = task.getResult();
                    try {
                        //noinspection ConstantConditions
                        String likedPlaces = taskResults.get(DATABASE_LIKED_RESTAURANTS_FIELD).toString(); //get the liked places CSV string from the users document
                        String[] likedPlacesArray = likedPlaces.split(","); //split the CSV string into a String[]

                        places.addAll(Arrays.asList(likedPlacesArray)); //Add the data from the liked places string [] into an ArrayList

                        mFirebaseDataCallback.finishedGettingLikedRestaurants(places); //send the results back

                    } catch (Exception e) {
                        mFirebaseDataCallback.finishedGettingLikedRestaurants(places);
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    /**
     * Gets the passed users selected place
     *
     * @param userId the user id to search for
     * @param holder the viewholder object to be passed back in the callback
     */
    public void getSelectedPlace(String userId,
                                 final AddedUsersAdapter.MyviewHolder holder) {
        FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot taskResults = task.getResult();
                    try {
                        if (taskResults.get(DATABASE_SELECTED_RESTAURANT_FIELD) != null) {
                            String place = taskResults.get(DATABASE_SELECTED_RESTAURANT_FIELD).toString();
                            String placeId = taskResults.get(DATABASE_SELECTED_RESTAURANT_ID_FIELD).toString();
                            mFirebaseDataCallback.finishedGettingPlace(holder, place, placeId);
                        } else {
                            mFirebaseDataCallback.finishedGettingPlace(holder, "null", "");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        mFirebaseDataCallback.finishedGettingPlace(holder, "null", "");
                    }
                }
            }
        });
    }


    /**
     * First extract the users added friends, then loop through each friend and determine if they have selected specified place
     * Calls back to {@link firebaseDataCallback#finishGettingUsersEatingHere(ArrayList, RecyclerView.ViewHolder)}
     *
     * @param placeId the place we are checking for friends eating at
     * @param v       the viewholder to be updated after callback
     */
    @SuppressWarnings("ConstantConditions")
    public void getUsersEatingHere(final String placeId, final RecyclerView.ViewHolder v) {
        //first get the users added friends
        FirebaseHelper.getCurrentUserData().get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                final ArrayList<Users> friendsEatingHere = new ArrayList<>(); //To hold those users that are eating at this place
                final HashSet<String> usersHashSet = new HashSet<>(); //to 100% ensure we have no duplicates
                if (task.isSuccessful()) {
                    final QuerySnapshot taskResults = task.getResult();
                    List<DocumentSnapshot> documents = taskResults.getDocuments();
                    if (documents.get(0).get(DATABASE_ADDED_USERS_FIELD) != null) {
                        String[] addedUsers = documents.get(0).get(DATABASE_ADDED_USERS_FIELD).toString().split(","); //split the returned users CSV string into a String[]
                        //noinspection unchecked

                        usersHashSet.addAll(Arrays.asList(addedUsers)); //to 100% ensure no duplicates
                        final ArrayList<String> addedUsersArray = new ArrayList<>(usersHashSet); //create an arraylist of added user id's to loop through each one

                        final int[] count = new int[1];
                        count[0] = addedUsers.length; //enables counting down to 0 so we know when we have looped through each user and can callback

                        //Now we have the list of added friends, loop through each one and see if they have selected the specific place
                        if (addedUsers.length > 0) {
                            for (String s : addedUsersArray) {
                                FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH)
                                        .document(s)
                                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() { //get the friends document

                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        count[0]--;//reduce the count so we know when we have checked every user

                                        if (task.isSuccessful() && task != null) {

                                            DocumentSnapshot taskResults = task.getResult();
                                            try {
                                                if (taskResults != null) {

                                                    String selectedPlaceId =
                                                            taskResults.get(DATABASE_SELECTED_RESTAURANT_ID_FIELD).toString(); //extract the friends selected place id

                                                    if (selectedPlaceId.equals(placeId)) { //if the friends selected place is equal to the place we are checking against, add to the arraylist
                                                        String id = taskResults.get(DATABASE_SELECTED_RESTAURANT_ID_FIELD).toString(); //extract the friends ID
                                                        String name = taskResults.get(DATABASE_NAME_FIELD).toString(); //extract the friends name
                                                        String email = taskResults.get(DATABASE_EMAIL_FIELD).toString(); //extract the friends email
                                                        String picture = taskResults.get(DATABASE_PICTURE_FIELD).toString(); //extract the friends picture

                                                        friendsEatingHere.add(new Users(name, id, email, picture)); //add the user object to an arraylist for returning
                                                    }

                                                    if (count[0] == 0) { //if we have looped through every friend, callback the results
                                                        mFirebaseDataCallback.finishGettingUsersEatingHere(friendsEatingHere, v);
                                                    }
                                                }
                                            } catch (Exception e) {
                                                if (count[0] == 0) { //if we have looped through every friend, callback the results
                                                    mFirebaseDataCallback.finishGettingUsersEatingHere(friendsEatingHere, v);
                                                }
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                });
                            }
                        } else {
                            mFirebaseDataCallback.finishGettingUsersEatingHere(friendsEatingHere, v);
                        }
                    } else {
                        getUsersEatingHereError(friendsEatingHere, v);
                    }
                } else { //if the task isn't successful, callback with an Array of size 0
                    getUsersEatingHereError(friendsEatingHere, v);
                }
            }
        });

    }


    /**
     * called when an error is experience in {@link #getUsersEatingHere}
     * this ensures that the callback functionality is still run even if we experience and error getting the users
     */
    private void getUsersEatingHereError
    (ArrayList<Users> friendsEatingHere, RecyclerView.ViewHolder v) {
        mFirebaseDataCallback.finishGettingUsersEatingHere(friendsEatingHere, v);
    }


    /**
     * Adds users chat data to the database
     *
     * @param chatData   - Map holding the chat data
     * @param chattingTo - the user the receiving the chat data
     */
    public void addChatData(final Map<String, Object> chatData, final String chattingTo) {
        final DocumentReference dbRef = FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(mCurrentUserId);

        dbRef.collection(DATABASE_CHAT_COLLECTION)
                .document(chattingTo).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot taskResults = task.getResult();
                if (taskResults != null) {
                    Map<String, Object> userChatData = taskResults.getData();
                    if (userChatData == null) {
                        dbRef.collection(DATABASE_CHAT_COLLECTION).document(chattingTo).set(chatData);
                        mChatDataCallback.refreshData();
                    } else {
                        userChatData.putAll(chatData);
                        dbRef.collection(DATABASE_CHAT_COLLECTION).document(chattingTo).set(userChatData);
                        mChatDataCallback.refreshData();
                    }
                }
            }
        });
    }


    /**
     * Returns the chat data for the current user. Calls {@link #getChattingUserData}
     *
     * @param userId       the id of the user sending the messages
     * @param chattingToId the id of the user receiving the messages
     */
    public void getCurrentUserChatData(final String userId, final String chattingToId) {

        FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(userId).collection(DATABASE_CHAT_COLLECTION)
                .document(chattingToId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot taskResults = task.getResult();

                if (taskResults != null) {
                    Map<String, Object> userChatData = taskResults.getData(); //get the database chat data for the current user

                    ArrayList<ChatObject> chatObjects = new ArrayList<>();
                    if (userChatData != null) {
                        Object[] keySet = userChatData.keySet().toArray();

                        //create chat objects from the returned user chat data
                        for (int i = 0; i < userChatData.size(); i++) {
                            String key = (String) keySet[i];
                            String messageBody = (String) userChatData.get(key);
                            chatObjects.add(new ChatObject(key, messageBody, false));
                        }
                    }

                    //now do the same as above but for the user we are chatting to
                    getChattingUserData(chattingToId, userId, chatObjects);
                }
            }
        });
    }


    /**
     * Gets the chat data for the user who is being sent messages
     *
     * @param chattingToId the id of the user receiving the messages
     * @param userId       the current users id
     * @param userChatData the chat data returned from {@link #getCurrentUserChatData}
     */
    private void getChattingUserData(String chattingToId, String userId,
                                     final ArrayList<ChatObject> userChatData) {
        FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(chattingToId).collection(DATABASE_CHAT_COLLECTION)
                .document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot taskResults = task.getResult();

                //We get the chat data from the database and convert this into an arraylist of chat objects
                //This method will callback with an arraylist of chat data for both the current user and the user we are chatting to
                if (taskResults != null) {
                    Map<String, Object> chattingToUserData = taskResults.getData();
                    ArrayList<ChatObject> ChattingTochatObjects = new ArrayList<>();
                    if (chattingToUserData != null) {
                        Object[] keySet = chattingToUserData.keySet().toArray();

                        for (int i = 0; i < chattingToUserData.size(); i++) {
                            String key = (String) keySet[i];
                            String messageBody = (String) chattingToUserData.get(key);
                            ChattingTochatObjects.add(new ChatObject(key, messageBody, true));
                        }
                    }
                    mChatDataCallback.gotChatData(userChatData, ChattingTochatObjects);
                }
            }
        });
    }


    /**
     * Called when a new message is sent in the ChatActivity. This adds a field to the database that acts as a trigger to send a notification
     * A function is listening to changes on this field in the database, when the change happens a message is sent to the recipient users phone which triggers
     * a notification (see {@link MyFirebaseMessagingService}
     *
     * @param messageFromUserId the id of the user sending the message
     */
    public static void newMessage(final String messageFromUserId) {
        final DocumentReference dbRef = FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(mCurrentUserId);

        dbRef.collection(DATABASE_CHAT_NOTIFICATIONS)
                .document(messageFromUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot taskResults = task.getResult();
                    if (taskResults != null) {
                        Map<String, Object> notificationData = taskResults.getData();
                        if (notificationData == null) {
                            notificationData = new HashMap<>();
                            notificationData.put(NEW_MESSAGE_FIELD, 1);
                            dbRef.collection(DATABASE_CHAT_NOTIFICATIONS).document(messageFromUserId).set(notificationData);
                        } else {
                            notificationData.put(NEW_MESSAGE_FIELD, 1);
                            dbRef.collection(DATABASE_CHAT_NOTIFICATIONS).document(messageFromUserId).set(notificationData);
                        }
                    }
                }
            }
        });
    }


    /**
     * This is called from the user list recycler view when binding the viewholder, it is used to determine wether we should display a "new message" chat icon
     *
     * @param userID     the id of the user to check
     * @param viewHolder the viewholder associated with this user
     */
    public void checkNewNotifications(final String userID,
                                      final RecyclerView.ViewHolder viewHolder) {
        if (!userID.equals(mCurrentUserId)) {
            FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(mCurrentUserId).collection(DATABASE_CHAT_NOTIFICATIONS)
                    .document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().get(NEW_MESSAGE_FIELD) != null) {
                        //we have a new notification from this user
                        ArrayList<Object> response = new ArrayList<>();
                        response.add(true);
                        response.add(viewHolder);
                        mFirebaseDataCallback.datadownloadedcallback(response);

                    }
                }
            });
        }
    }


    /**
     * This is called when a user has opened the chat containing a 'new message', this removes the new message notification from the database
     *
     * @param chattingToUserId the id of the user we are chatting to
     */
    public static void removeChatNotification(String chattingToUserId) {
        FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(mCurrentUserId).collection(DATABASE_CHAT_NOTIFICATIONS)
                .document(chattingToUserId).delete();
    }


    /**
     * This is called when we add a friend. Updates the database 'added users' field
     *
     * @param userId   the current user id
     * @param friendId the user Id of the friend being added
     */
    public static void addFriend(String userId, String friendId) {
        final StringBuilder sb = new StringBuilder();
        if ("".equals(friendId)) {
            FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(mCurrentUserId).update(DATABASE_ADDED_USERS_FIELD, userId);
        } else {
            sb.append(friendId);
            sb.append(",").append(userId);
            FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(mCurrentUserId).update(DATABASE_ADDED_USERS_FIELD, sb.toString());
        }
    }


    //Returns the current users ID
    public static String getmCurrentUserId() {
        return mCurrentUserId;
    }

    //returns the current users picture
    public static String getmCurrentUserPicUrl() {
        return mCurrentUserPicUrl;
    }


    public interface firebaseDataCallback {
        void finishGettingUsersEatingHere(ArrayList<Users> users, RecyclerView.ViewHolder v);

        void datadownloadedcallback(ArrayList<Object> arrayList);

        void workUsersDataCallback(ArrayList<Users> arrayList, Object returnObject);

        void finishedGettingPlace(AddedUsersAdapter.MyviewHolder myviewHolder, String s, String placeId);

        void isItLikedCallback(boolean response);

        void finishedGettingLikedRestaurants(ArrayList<String> places);

        void isPlaceSelected(boolean currentUserSelectedPlace, boolean otherUsersSelectedPlace);
    }

    public interface chatData {
        void gotChatData(ArrayList<ChatObject> messagesSent, ArrayList<ChatObject> messagesRecieved);

        void refreshData();
    }
}



