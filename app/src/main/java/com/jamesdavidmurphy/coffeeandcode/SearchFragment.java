package com.jamesdavidmurphy.coffeeandcode;

import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


public class SearchFragment extends Fragment{

    ListView list;
    private DatabaseReference databaseUser;
    private DatabaseReference databaseMatches;

    ImageView lvImage;
    TextView lvName;
    TextView lvDesc;
    TextView lvAddress;
    ImageButton btnYes;
    ImageButton btnNo;

    TextView tvDistance;
    SeekBar sbDistance;

    private String userNameLiked;
    int intProgressValue;

    FirebaseListAdapter<User> fireAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment (but dont return until listview created)
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        //--------------------------------------------------------------------
        //FIREBASE
        //first populate global variables myLat/myLong
        databaseUser = FirebaseDatabase.getInstance().getReference("users");

        databaseUser.child(MainActivity.myUserName).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    String keyName = (String) childDataSnapshot.getKey();
                    if(keyName.equals("strLat"))
                        MainActivity.myLat = (String) childDataSnapshot.getValue();
                    if(keyName.equals("strLong"))
                        MainActivity.myLong = (String) childDataSnapshot.getValue();
                }
            } //end onDataChange

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        //initialize list view
        fireAdapter = new FirebaseListAdapter<User>(
                getActivity(),
                User.class,
                R.layout.listview_row_search,
                databaseUser
        ) {
            @Override
            protected void populateView(View v, User model, int position) {

                //reference all views
                lvImage = (ImageView)v.findViewById(R.id.imageView4LV);
                lvName = (TextView) v.findViewById(R.id.tvListViewName);
                lvDesc = (TextView) v.findViewById(R.id.tvListViewDesc);
                lvAddress = (TextView) v.findViewById(R.id.tvListViewLoc);
                btnYes = (ImageButton) v.findViewById(R.id.btnYes);
                //btnNo = (ImageButton) v.findViewById(R.id.btnNo);

                //populate date for image/title
                lvName.setText(model.getStrUserName());
                lvDesc.setText(model.getStrAboutMe());

                //....and image
                String strImageUri = model.getStrImageUri();
                Uri uriImageUri = Uri.parse(strImageUri);
                Picasso.with(getActivity()).load(uriImageUri).fit().centerCrop().into(lvImage);

                //get refererence for meUser & foreignUser...SO CAN HIDE WHERE NECESSARY
                String foreignUser = model.getStrUserName();
                String meUser = MainActivity.myUserName;


                //---------------------------------------------
                //add address and format based on how far away

                //in case we dont have coords/its meUser....we default to just providing address
                lvAddress.setText(model.getstrAddress());

                //but now we want distance
                Double foreignLat = Double.parseDouble(model.getStrLat());
                Double foreignLong = Double.parseDouble(model.getStrLong());

                Double dblMyLat;
                Double dblMyLong;
                float [] dist;
                if(MainActivity.myLat !=null && !foreignUser.equals(meUser)) {
                    dblMyLat = Double.parseDouble(MainActivity.myLat);
                    dblMyLong = Double.parseDouble(MainActivity.myLong);
                    dist = new float[1];

                    Location.distanceBetween(foreignLat, foreignLong, dblMyLat, dblMyLong, dist);
                    Float flDist = dist[0];

                    int intKmDist = Math.round(flDist/1000);
                    lvAddress.setText(intKmDist + "km away");
                    if(intProgressValue<=intKmDist) {
                        v.setLayoutParams(new AbsListView.LayoutParams(-1,1));
                        v.setVisibility(View.GONE);
                        //lvAddress.setTextColor(Color.RED);
                    }else{
                        lvAddress.setTextColor(Color.parseColor("#009933"));
                        lvAddress.setTypeface(null, Typeface.BOLD);
                        v.setVisibility(View.VISIBLE);
                        v.setLayoutParams(new AbsListView.LayoutParams(-1,-2));
                    }

                } //if(MainActivity.myLat !=null)
                //---------------------------------------------


                //add tag to thumbs up button
                btnYes.setTag(model.getStrUserName());

                //make thumb black if view is for yourself
                btnYes.setVisibility(View.VISIBLE);
                if (foreignUser.equals(meUser)){
                    btnYes.setVisibility(View.INVISIBLE);
                }
                //--------------------------------------------------------------------
                //add listener to thumbs up button.....to get foreign user object from users collection and
                //add as child to primary users collection
                btnYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //first get username clicked
                        userNameLiked = view.getTag().toString();

                        //then check that user has created a profile
                        databaseUser.child(MainActivity.myUserName).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(!dataSnapshot.hasChildren()) {
                                    Snackbar.make(getActivity().findViewById(android.R.id.content), "You must create a profile before you can 'like' someone", Snackbar.LENGTH_LONG).show();
                                    return;
                                }else{
                                    //first use snackbar to double-check user want to be friends
                                    final Snackbar snackBar = Snackbar.make((getActivity()).findViewById(android.R.id.content), "This will tell them you want to meet\nAre you Sure?", Snackbar.LENGTH_LONG);
                                    snackBar.setAction("I'm Sure", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            snackBar.dismiss();

                                            //NOW WE APPEND TO FB!!!
                                            appendForeignUser();
                                        }
                                    });
                                    snackBar.show();
                                } //end if
                            } //end onDataChange

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        }); //end addValueEventListener
                    }
                }); // btnYes.setOnClickListener
                //--------------------------------------------------------------------
            } // end populateView!!!!
        }; // end populateView!!!!

        list = (ListView)view.findViewById(R.id.listViewSearch);
        list.setAdapter(fireAdapter);
        // END FIREBASE
        //--------------------------------------------------------------------


        //--------------------------------------------------------------------
        //navigate to dialog when user clicks list item
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //get username from textview
                MainActivity.usernameClicked = ((TextView) view.findViewById(R.id.tvListViewName)).getText().toString();

                //navigate to search dialog
                SearchDialog searchDialog = new SearchDialog();
                searchDialog.show(getActivity().getFragmentManager(),"SearchDialog");
            }
        });
        //--------------------------------------------------------------------

        //seek bar
        sbDistance = (SeekBar) view.findViewById(R.id.sbDistance);
        tvDistance = (TextView) view.findViewById(R.id.tvDistance);
        handleSeekbar();

        return view;
    }//end oncreateview

    //*********************************************************************************************
    //*********************************************************************************************


    private void appendForeignUser(){
        //Toast.makeText(getActivity(), "Fuck you " + userNameLiked + "!!!", Toast.LENGTH_LONG).show();

        //pull foreign user object from database
        databaseUser.child(userNameLiked).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Log.d("sweet", user.getStrUserName());

                //post to matches database
                databaseMatches = FirebaseDatabase.getInstance().getReference().child("matches");
                databaseMatches.child(MainActivity.myUserName).child(userNameLiked).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            updateMatchField();
                            Snackbar.make(getActivity().findViewById(android.R.id.content), "Your like has been sent", Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(getActivity().findViewById(android.R.id.content), "Error: Profile info was not saved", Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    } //appendForeignUser

    private void updateMatchField(){
        //first check if foreign user has a collection in matches database with your username
        databaseMatches.child(userNameLiked).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(MainActivity.myUserName)){
                    //update match on foreign users database
                    databaseMatches.child(userNameLiked).child(MainActivity.myUserName).child("strMatch").setValue("1");
                    //update match on primary users database
                    databaseMatches.child(MainActivity.myUserName).child(userNameLiked).child("strMatch").setValue("1");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    } //end updateMatchField


    private void handleSeekbar(){

        sbDistance.setMax(300);
        sbDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                intProgressValue = progress;
                tvDistance.setText(progress + "km");
                //repopulate listview
                fireAdapter.notifyDataSetChanged();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        sbDistance.setProgress(300);
    } //end handleSeekbar



} //end class

//***********************************************
/*
class ListViewAdapterSearch extends ArrayAdapter<String>
{
    Context context;
    int[] imageArray;
    String[] nameArray;
    String[] descArray;

    ListViewAdapterSearch(Context c, int[] imgs, String[] names, String[] descs) {
        super(c, R.layout.listview_row_search, R.id.tvListViewName, names);
        this.context=c;
        this.imageArray = imgs;
        this.nameArray = names;
        this.descArray = descs;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.listview_row_search, parent, false);

        //reference all views
        ImageView lvImage = (ImageView)row.findViewById(R.id.imageView4LV);
        TextView lvName = (TextView) row.findViewById(R.id.tvListViewName);
        TextView lvDesc = (TextView) row.findViewById(R.id.tvListViewDesc);
        ImageButton btnYes = (ImageButton) row.findViewById(R.id.btnYes);
        ImageButton btnNo = (ImageButton) row.findViewById(R.id.btnNo);

        //populate date for image/title/desc
        lvImage.setImageResource(imageArray[position]);
        lvName.setText(nameArray[position]);
        lvDesc.setText(descArray[position]);

        //set action for buttons
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Snackbar snackBar = Snackbar.make(((MainActivity)context).findViewById(android.R.id.content), "This will tell them you want to meet\nAre you Sure?", Snackbar.LENGTH_LONG);
                snackBar.setAction("I'm Sure", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //code to record match here
                        snackBar.dismiss();
                    }
                });
                snackBar.show();
            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Snackbar snackBar = Snackbar.make(((MainActivity)context).findViewById(android.R.id.content), "This will remove them from your list\nAre you Sure?", Snackbar.LENGTH_LONG);
                snackBar.setAction("I'm sure", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //code to delete user here
                        snackBar.dismiss();
                    }
                });
                snackBar.show();
            }
        });

        return row;
    }
}
*/


