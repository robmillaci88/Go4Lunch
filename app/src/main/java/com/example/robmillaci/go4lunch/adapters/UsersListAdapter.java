package com.example.robmillaci.go4lunch.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.robmillaci.go4lunch.R;
import com.example.robmillaci.go4lunch.data_objects.Users;
import com.example.robmillaci.go4lunch.firebase.FirebaseHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static com.example.robmillaci.go4lunch.firebase.FirebaseHelper.DATABASE_ADDED_USERS_FIELD;

/**
 * This class is responsible for creating the adaptor to display all users of this app
 */
public class UsersListAdapter extends BaseAdapterClass implements
        Filterable {

    private ArrayList<Object> mUsersArrayList; //Arraylist containing all the users
    private ArrayList<Object> filteredUsersList; //Arraylist to hold the filtered users
    private final ArrayList<Object> originalArray; //Arraylist to keep a copy of the origional list of users - used when filtering
    private String addedUIds; // the users 'added friends'
    private final Context mContext; //the context of the calling class

    public UsersListAdapter(ArrayList<Object> usersArrayList, Context context) {
        mUsersArrayList = usersArrayList;
        originalArray = usersArrayList;
        this.mContext = context;
    }


    @NonNull
    @Override
    public UsersListAdapter.MyviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_recycler_view, parent, false); //inflate and return the view
        return new MyviewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        final MyviewHolder myviewHolder = (MyviewHolder) holder;

        final Users user = (Users) mUsersArrayList.get(position); //get the user related to this position
        final String name = user.getUsername(); //get the users name
        final String email = user.getUserEmail(); //get the users email
        final String uId = user.getUserID(); //get the users ID
        final String pic = user.getUserPic() == null ? "" : user.getUserPic(); //get the users picture

        myviewHolder.uniqueId = uId; //set the unique id of the user in this position
        myviewHolder.userEmail.setText(email); //set the users email
        myviewHolder.username.setText(name); //set the users name

        new FirebaseHelper(this).getMyWorkUsers(holder);

        //load the users picture into the holder view
        if (!pic.equals("")) {
            Picasso.get().load(pic).into(myviewHolder.userPicture);
        } else {
            myviewHolder.userPicture.setImageResource(R.drawable.com_facebook_profile_picture_blank_portrait);
        }


        //get the current users addedUsers. The results are called back to this classes workUsersDataCallback method

        //set the addFriend on click listener. First retrieve the list of currently added users and then pass this to the addFriend method in FireBaseHelper class
        //Updates the UI to reflect the adding of a user and displays a Toast message to confirm 'Friend added'
        myviewHolder.addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseHelper.getCurrentUserData().get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot taskResults = task.getResult();
                            if (taskResults != null) {
                                List<DocumentSnapshot> documents = taskResults.getDocuments();

                                try {
                                    DocumentSnapshot d = documents.get(0);
                                    addedUIds = d.get(DATABASE_ADDED_USERS_FIELD) == null ? "" : (String) d.get(DATABASE_ADDED_USERS_FIELD); //get the added users UIDs from the database
                                } catch (Exception e) {
                                    addedUIds = "";
                                }

                                FirebaseHelper.addFriend(uId, addedUIds);

                                myviewHolder.addFriend.setImageResource(R.drawable.checked);
                                myviewHolder.addFriend.setClickable(false);
                                Toast.makeText(mContext, R.string.friend_added, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        });
    }


    @Override
    public int getItemCount() {
        return mUsersArrayList == null ? 0 : mUsersArrayList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    filteredUsersList = originalArray;
                } else {
                    ArrayList<Object> queryfilteredList = new ArrayList<>();
                    for (Object userObj : originalArray) {
                        Users row = (Users)userObj;
                        Log.d("filtercheck", "performFiltering: charstring is  " + charString.toLowerCase());

                        Log.d("filtercheck", "performFiltering: user name is " + row.getUsername());
                        Log.d("filtercheck", "performFiltering: user name is " + row.getUserEmail());


                        // name match condition. if the username or user email matches the filter constraint
                            if (row.getUsername().toLowerCase().contains(charString.toLowerCase()) || row.getUserEmail().toLowerCase().contains(charString.toLowerCase())) {
                                Log.d("performFiltering", "performFiltering: added user " + row.getUsername());
                                queryfilteredList.add(row);
                            }
                        }

                    filteredUsersList = queryfilteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredUsersList;
                return filterResults;

            }


            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                //noinspection unchecked
                mUsersArrayList = (ArrayList<Object>) results.values;
                notifyDataSetChanged();

            }
        };
    }




    /**
     * called after {@link FirebaseHelper#createUserObjects(ArrayList, Object)}
     * If the viewholders current user's ID is within the returned users Array, this user has been added and so we make UI changes to reflect this
     *
     * @param addedUsersReturned the current users added users (friends)
     * @param viewHolder         the returned Viewholder object that we need to make UI changes to depending on the results of @param addedUsersReturned
     */
    @Override
    public void workUsersDataCallback(ArrayList<Users> addedUsersReturned, Object viewHolder) {
        if (viewHolder != null) {
            MyviewHolder myviewHolder = (MyviewHolder) viewHolder;

            String viewHolderId = myviewHolder.uniqueId == null? "" : myviewHolder.uniqueId;

            for (Users u : addedUsersReturned) {
                if (u.getUserID().equals(viewHolderId)) {
                    myviewHolder.addFriend.setImageResource(R.drawable.checked);
                    myviewHolder.addFriend.setClickable(false);
                }
            }
        }
    }


    public static class MyviewHolder extends RecyclerView.ViewHolder {

        final TextView username;
        final TextView userEmail;
        final ImageView userPicture;
        final ImageView addFriend;
        String uniqueId;

        private MyviewHolder(View itemView) {
            super(itemView);
            this.username = itemView.findViewById(R.id.uname);
            this.userEmail = itemView.findViewById(R.id.uEmail);
            this.userPicture = itemView.findViewById(R.id.profPic);
            this.addFriend = itemView.findViewById(R.id.addFriend);
            this.uniqueId = "";
        }
    }
}
