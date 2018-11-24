package com.example.robmillaci.go4lunch.firebase;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.robmillaci.go4lunch.R;
import com.example.robmillaci.go4lunch.adapters.AddedUsersAdapter;
import com.example.robmillaci.go4lunch.adapters.UsersListAdapter;
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
import java.util.List;
import java.util.Map;

import static com.example.robmillaci.go4lunch.activities.RestaurantActivity.MARKER_UNSELECTED;

public class FirebaseHelper {
    private static final String mCurrentUserId; //the current users ID
    private static final String mCurrentUserPicUrl; //the current users picture URL
    private static int count; //the count of the added friends
    private static final DocumentReference userDoc; //the users document reference
    private static final String EXCLUDED_USER;


    //Database field values
    static final String DATABASE_TOKEN_PATH = "token";
    private static final String DATABASE_COLLECTION_PATH = "users";
    private static final String DATABASE_CHAT_COLLECTION = "chatdata";
    private static final String DATABASE_CHAT_NOTIFICATIONS = "chatNotifications";
    private static final String DATABASE_NAME_FIELD = "username";
    private static final String DATABASE_EMAIL_FIELD = "userEmail";
    private static final String DATABASE_PICTURE_FIELD = "picture";
    private static final String DATABASE_UNIQUE_ID_FIELD = "uniqueID";
    public static final String DATABASE_ADDED_USERS_FIELD = "addedUsers";
    public static final String DATABASE_SELECTED_RESTAURANT_FIELD = "selectedRestaurant";
    public static final String DATABASE_SELECTED_RESTAURANT_ID_FIELD = "selectedPlaceID";
    private static final String DATABASE_LIKED_RESTAURANTS_FIELD = "likedPlaces";
    private static final String NEW_MESSAGE_FIELD = "New message";

    private firebaseDataCallback mFirebaseDataCallback;
    private chatData mChatDataCallback;

    private UsersListAdapter.MyviewHolder mViewHolder;
    private ArrayList<Users> usersObjects = new ArrayList<>();


    /**
     * 3 constructors for this class one for different callbacks and for instantiation from a Recyclerview adapter class
     *
     * @param mCallback the callback for this instance of the class
     */

    public FirebaseHelper(firebaseDataCallback mCallback) {
        mFirebaseDataCallback = mCallback;
    }

    public FirebaseHelper(chatData mCallback) {
        mChatDataCallback = mCallback;
    }

    public FirebaseHelper(firebaseDataCallback mCallback, UsersListAdapter.MyviewHolder viewholder) {
        mFirebaseDataCallback = mCallback;
        this.mViewHolder = viewholder;
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
     * Returns the current users friends from the database and then calles {@link #createUserObjects(ArrayList)} to be used by the application
     */
    @SuppressWarnings("ConstantConditions")
    public void getMyWorkUsers() {
        FirebaseHelper.getCurrentUserData().get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<String> addedUsersArray = new ArrayList<>(); //arraylist to hold the added users
                if (task.isSuccessful()) {
                    try {
                        QuerySnapshot taskResults = task.getResult();
                        List<DocumentSnapshot> documents = taskResults.getDocuments();
                        String[] addedUsers = documents.get(0).get(DATABASE_ADDED_USERS_FIELD).toString().split(","); //split the returned "added users" string

                        //noinspection unchecked
                        addedUsersArray.addAll(Arrays.asList(addedUsers)); //add the added users to the arraylist
                    } catch (Exception e) {
                        e.printStackTrace();
                        createUserObjects(null);
                    }
                }
                createUserObjects(addedUsersArray); //create the users objects from the arraylist of added users
            }
        });
    }


    /**
     * From a string of user Id's this method will extract the relevant user details from Firebase and create {@link Users}
     *
     * @param addedUsersArray the arraylist of user IDs to lookup in the database
     */
    private void createUserObjects(ArrayList<String> addedUsersArray) {
        if (addedUsersArray == null) {
            mFirebaseDataCallback.workUsersDataCallback(null);
        } else {
            usersObjects = new ArrayList<>();
            count = addedUsersArray.size(); //count to determine when we have looped through each user
            for (String s : addedUsersArray) { //for each user ID in the addedUsersArray, loop through and extract their relevant information to create Users objects
                Query docRef = FirebaseHelper.getQueryDocSnapshot(s);
                docRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot taskResults = task.getResult();
                            List<DocumentSnapshot> documents = taskResults.getDocuments();
                            String userName = "";
                            String email = "";
                            String id = "";
                            String picture = "";
                            try {
                                DocumentSnapshot d = documents.get(0);
                                userName = (String) d.get(DATABASE_NAME_FIELD);
                                email = (String) d.get(DATABASE_EMAIL_FIELD);
                                id = (String) d.get(DATABASE_UNIQUE_ID_FIELD);
                                picture = (String) d.get(DATABASE_PICTURE_FIELD);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (!userName.equals("") && !email.equals("") && !id.equals("")) {
                                Users user = new Users(userName, id, email, picture);
                                usersObjects.add(user);
                                count--;

                                if (count == 0) { //if the count is 0, we have looped through every user ID so we can now callback with the resulting user objects
                                    mFirebaseDataCallback.workUsersDataCallback(usersObjects);
                                }
                            } else {
                                mFirebaseDataCallback.workUsersDataCallback(null);
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

                            Users user = new Users(name, uniqueID, email, picture);
                            usersArrayList.add(user);
                        }
                        mFirebaseDataCallback.datadownloadedcallback(usersArrayList); //callback with the results
                    }
                }
            }
        });
    }


    /**
     * Gets the users 'added friends' and callsback with the results
     */
    public void getAddedUsers() {
        FirebaseHelper.getCurrentUserData().get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        try {
                            QuerySnapshot taskResults = task.getResult();
                            @SuppressWarnings("ConstantConditions") List<DocumentSnapshot> documents = taskResults.getDocuments();
                            DocumentSnapshot d = documents.get(0); //There will only be one document returned in this case (the current users doc) so we can safely .get(0)
                            String addedUIDs = (String) d.get(DATABASE_ADDED_USERS_FIELD); //get the added users id's from the users document
                            assert addedUIDs != null;
                            String[] addedUsersUniqueID = addedUIDs.split(","); //split the CSV's into a string array
                            mFirebaseDataCallback.finishedGettingUsers(addedUsersUniqueID, mViewHolder); //callback with the results

                        } catch (Exception e) {
                            mFirebaseDataCallback.finishedGettingUsers(new String[]{""}, mViewHolder); //on an exception, we callback with a string[] containing ""
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
     * Add the selected place to the database - firstly deleting the old selected place data and then adding the new infromation
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
            m.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marker_orange));
            m.setTag(MARKER_UNSELECTED);
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
                StringBuilder sb = new StringBuilder(); //Stringbuilder to create the new liked places CSV string

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
     * @param users   the list of friends of the user, this is important because we dont want to display information about 'non friend' users
     */
    public void isPlaceSelected(final String placeId, final String[] users) {
        FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
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
                            //check wether the current user has selected this place and wether otherusers in the app have also selected this place
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
                            if (!s.equals(id)) {//if the liked place id doesnt = the id of the place to remove
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
     * @param id
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
                final ArrayList<String> addedUsersArray = new ArrayList<>(); //To hold all added users
                final ArrayList<Users> friendsEatingHere = new ArrayList<>(); //To hold those users that are eating at this place
                if (task.isSuccessful()) {
                    try {
                        final QuerySnapshot taskResults = task.getResult();
                        List<DocumentSnapshot> documents = taskResults.getDocuments();
                        String[] addedUsers = documents.get(0).get(DATABASE_ADDED_USERS_FIELD).toString().split(",");
                        //noinspection unchecked
                        addedUsersArray.addAll(Arrays.asList(addedUsers));
                        final int[] count = new int[]{addedUsersArray.size()};

                        //Now we have the list of added friends, loop through each one and see if they have selected the specific place
                        for (String s : addedUsersArray) {
                            FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH)
                                    .document(s)
                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() { //get the friends document

                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    count[0]--; //reduce the count so we know when we have checked every user

                                    if (task.isSuccessful() && task != null) {
                                        DocumentSnapshot taskResults = task.getResult();
                                        try {
                                            if (taskResults != null) {
                                                String id = taskResults.get(DATABASE_SELECTED_RESTAURANT_ID_FIELD).toString(); //extract the friends ID
                                                String name = taskResults.get(DATABASE_NAME_FIELD).toString(); //extract the friends name
                                                String email = taskResults.get(DATABASE_EMAIL_FIELD).toString(); //extract the friends email
                                                String picture = taskResults.get(DATABASE_PICTURE_FIELD).toString(); //extract the friends picture
                                                String selectedPlaceId = taskResults.get(DATABASE_SELECTED_RESTAURANT_ID_FIELD).toString(); //extract the friends selected place id

                                                if (selectedPlaceId.equals(placeId)) { //if the friends selected place is equal to the place we are checking against, add to the arraylist
                                                    friendsEatingHere.add(new Users(name, id, email, picture));
                                                }
                                                if (count[0] == 0) { //if we have looped through every friend, callback the results
                                                    mFirebaseDataCallback.finishGettingUsersEatingHere(friendsEatingHere, v);
                                                }
                                            }
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Log.d("FIREBASE", "onComplete: task failed");
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else { //if the task isnt successful, callback with an Array of size 0
                    mFirebaseDataCallback.finishGettingUsersEatingHere(friendsEatingHere, v);
                }
            }
        });
    }


    public void addChatData(final Map<String, Object> chatData, final String chattingTo) {
        final DocumentReference dbRef = FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(mCurrentUserId);

        FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(mCurrentUserId).collection(DATABASE_CHAT_COLLECTION)
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

    public void getCurrentUserChatData(final String userId, final String chattingToId) {

        FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(userId).collection(DATABASE_CHAT_COLLECTION)
                .document(chattingToId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot taskResults = task.getResult();

                if (taskResults != null) {
                    Map<String, Object> userChatData = taskResults.getData();

                    ArrayList<ChatObject> chatObjects = new ArrayList<>();
                    if (userChatData != null) {
                        Object[] keySet = userChatData.keySet().toArray();

                        for (int i = 0; i < userChatData.size(); i++) {
                            String key = (String) keySet[i];
                            String messageBody = (String) userChatData.get(key);
                            chatObjects.add(new ChatObject(key, messageBody, false));
                        }
                    }
                    getChattingUserData(chattingToId, userId, chatObjects);
                }
            }
        });
    }

    private void getChattingUserData(String chattingToId, String userId, final ArrayList<ChatObject> userChatData) {
        FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(chattingToId).collection(DATABASE_CHAT_COLLECTION)
                .document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot taskResults = task.getResult();

                if (taskResults != null) {
                    Map<String, Object> chattingToUserData = taskResults.getData();

                    ArrayList<ChatObject> ChattingTochatObjects = new ArrayList<>();
                    if (chattingToUserData != null) {
                        Object[] keySet = chattingToUserData.keySet().toArray();

                        for (int i = 0; i < userChatData.size(); i++) {
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


    public static void newMessage(final String messageFromUserId) {
        final DocumentReference dbRef = FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(mCurrentUserId);

        FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(mCurrentUserId).collection(DATABASE_CHAT_NOTIFICATIONS)
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

    public void checkNewNotifications(final String userID, final RecyclerView.ViewHolder viewHolder) {

        FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(mCurrentUserId).collection(DATABASE_CHAT_NOTIFICATIONS)
                .document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    if (task.getResult().getData() != null) {
                        //we have a new notification from this user
                        ArrayList<Object> response = new ArrayList<>();
                        response.add(true);
                        response.add(viewHolder);
                        mFirebaseDataCallback.datadownloadedcallback(response);

                    }
                }
            }
        });
    }


    public static void removeChatNotification(String chattingToUserId) {
        FirebaseFirestore.getInstance().collection(DATABASE_COLLECTION_PATH).document(mCurrentUserId).collection(DATABASE_CHAT_NOTIFICATIONS)
                .document(chattingToUserId).delete();
    }

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




    public static String getmCurrentUserId() {
        return mCurrentUserId;
    }

    public static String getmCurrentUserPicUrl() {
        return mCurrentUserPicUrl;
    }




    public interface firebaseDataCallback {
        void finishGettingUsersEatingHere(ArrayList<Users> users, RecyclerView.ViewHolder v);

        void datadownloadedcallback(ArrayList<Object> arrayList);

        void workUsersDataCallback(ArrayList<Users> arrayList);

        void finishedGettingUsers(String[] users, UsersListAdapter.MyviewHolder viewHolder);

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



