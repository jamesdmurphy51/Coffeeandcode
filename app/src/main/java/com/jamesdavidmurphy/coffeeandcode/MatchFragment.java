package com.jamesdavidmurphy.coffeeandcode;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;


public class MatchFragment extends Fragment{

    ListView list;
    //private DatabaseReference databaseUser;
    private DatabaseReference databaseMatches;

    ImageView lvImage;
    TextView lvName;
    TextView lvDesc;
    ImageButton ibtnMsg;
    ImageButton ibtnMatch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment (but dont return until listview created)
        View view = inflater.inflate(R.layout.fragment_match, container, false);

        //--------------------------------------------------------------------
        //FIREBASE
        //initialize list view
        databaseMatches = FirebaseDatabase.getInstance().getReference("matches");
        DatabaseReference databaseUserMatches = databaseMatches.child(MainActivity.myUserName);
        FirebaseListAdapter<User> fireAdapter = new FirebaseListAdapter<User>(
                getActivity(),
                User.class,
                R.layout.listview_row_match,
                databaseUserMatches
        ) {
            @Override
            protected void populateView(View v, User model, int position) {

                //reference all views
                lvImage = (ImageView)v.findViewById(R.id.imageView4LV2);
                lvName = (TextView) v.findViewById(R.id.tvListViewName2);
                lvDesc = (TextView) v.findViewById(R.id.tvListViewDesc2);
                ibtnMatch = (ImageButton) v.findViewById(R.id.btnMatch);
                ibtnMsg = (ImageButton) v.findViewById(R.id.btnMsg);

                //populate date for image/title/desc
                lvName.setText(model.getStrUserName());
                lvDesc.setText(model.getStrAboutMe());

                //....and image
                String strImageUri = model.getStrImageUri();
                Uri uriImageUri = Uri.parse(strImageUri);
                Picasso.with(getActivity()).load(uriImageUri).fit().centerCrop().into(lvImage);

                //color & tag button
                if(model.getStrMatch().equals("0")){
                    ibtnMatch.setImageResource(R.drawable.ic_cross);
                    ibtnMatch.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
                    ibtnMsg.setTag("N");
                }else{
                    ibtnMatch.setImageResource(R.drawable.ic_tick);
                    ibtnMatch.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP);
                    ibtnMsg.setTag(model.getStrUserName());
                }

                //--------------------------------------------------------------------
                //add listener for msg button
                ibtnMsg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //is the match reciprocal?
                        String reciprocal = view.getTag().toString();
                        if(reciprocal.equals("N")) {
                            Snackbar.make(getActivity().findViewById(android.R.id.content), "You cannot message until user has accepted your like", Snackbar.LENGTH_LONG).show();
                        }else{
                            //launch chat page
                            MainActivity.usernameClicked = reciprocal;
                            Intent i = new Intent(getActivity(), ChatActivity.class);
                            startActivity(i);
                        }
                    }
                });

                //--------------------------------------------------------------------


            } // end populateView!!!!
        }; // end populateView!!!!

        list = (ListView)view.findViewById(R.id.listViewMatch);
        list.setAdapter(fireAdapter);
        // END FIREBASE
        //--------------------------------------------------------------------


        //--------------------------------------------------------------------
        //navigate to dialog when user clicks list item
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //get username from textview
                MainActivity.usernameClicked = ((TextView) view.findViewById(R.id.tvListViewName2)).getText().toString();

                //navigate to search dialog
                SearchDialog searchDialog = new SearchDialog();
                searchDialog.show(getActivity().getFragmentManager(),"SearchDialog");
            }
        });
        //--------------------------------------------------------------------

        return view;
    }//end oncreateview

    //*********************************************************************************************
    //*********************************************************************************************

}



/*
//***********************************************
class ListViewAdapterMatch extends ArrayAdapter<String>
{
    Context context;
    int[] imageArray;
    String[] nameArray;
    String[] descArray;

    ListViewAdapterMatch(Context c, int[] imgs, String[] names, String[] descs) {
        super(c, R.layout.listview_row_match, R.id.tvListViewName2, names);
        this.context=c;
        this.imageArray = imgs;
        this.nameArray = names;
        this.descArray = descs;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.listview_row_match, parent, false);

        //reference all views
        ImageView lvImage = (ImageView)row.findViewById(R.id.imageView4LV2);
        TextView lvName = (TextView) row.findViewById(R.id.tvListViewName2);
        TextView lvDesc = (TextView) row.findViewById(R.id.tvListViewDesc2);
        ImageButton ibtnMsg = (ImageButton) row.findViewById(R.id.ibtnMsg);


        //populate date for image/title/desc
        lvImage.setImageResource(imageArray[position]);
        lvName.setText(nameArray[position]);
        lvDesc.setText(descArray[position]);

        //set action for button
        ibtnMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, ChatActivity.class);
                context.startActivity(i);
            }
        });

        return row;
    }
}
*/