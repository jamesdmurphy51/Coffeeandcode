package com.jamesdavidmurphy.coffeeandcode;


import android.app.DialogFragment;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SearchDialog extends DialogFragment {

    ImageView iv;
    TextView tvName;
    TextView tvDesc;
    TextView tvLang;
    TextView tvExp;
    TextView tvDays;
    TextView tvTimes;
    TextView tvAge;
    TextView tvLoc;

    private DatabaseReference databaseUser;

    private Geocoder geocoder;
    private List<Address> addresses;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_search, null);

        //------------------------------------------------------------
        //listen for BACK button click
        Button btnBack = (Button) view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });
        //------------------------------------------------------------

        //------------------------------------------------------------
        //reference all views
        iv = (ImageView) view.findViewById(R.id.iv4SDialog_img);
        tvName = (TextView) view.findViewById(R.id.tv4SDialog_name);
        tvDesc = (TextView) view.findViewById(R.id.tv4SDialog_desc);
        tvLang = (TextView) view.findViewById(R.id.tv4SDialog_lang);
        tvExp = (TextView) view.findViewById(R.id.tv4SDialog_exp);
        tvDays = (TextView) view.findViewById(R.id.tv4SDialog_days);
        tvTimes = (TextView) view.findViewById(R.id.tv4SDialog_times);
        tvAge = (TextView) view.findViewById(R.id.tv4SDialog_age);
        tvLoc = (TextView) view.findViewById(R.id.tv4SDialog_loc);

        //get firebase data to populate views
        databaseUser = FirebaseDatabase.getInstance().getReference().child("users");

        databaseUser.orderByKey().equalTo(MainActivity.usernameClicked).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String strImageUri;
                String name;
                String desc;
                String exp;
                String age;
                String lang = "";
                String days = "";
                String times = "";
                Double latitude;
                Double longitude;

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    //pull data to variables user object
                    User objUser = child.getValue(User.class);

                    //pull FB values into variables
                    strImageUri = objUser.getStrImageUri();
                    name = objUser.getStrUserName();
                    desc = objUser.getStrAboutMe();
                    exp = objUser.getStrExp();
                    age = objUser.getStrAge();

                    if(objUser.getStrJsc().equals("Y"))
                        lang += "Javascript, ";
                    if(objUser.getStrJva().equals("Y"))
                        lang += "Java, ";
                    if(objUser.getStrPhp().equals("Y"))
                        lang += "PHP, ";
                    if(objUser.getStrPyt().equals("Y"))
                        lang += "Python, ";
                    if(objUser.getStrCsh().equals("Y"))
                        lang += "C#, ";
                    if(objUser.getStrCpl().equals("Y"))
                        lang += "C++, ";
                    if(objUser.getStrRub().equals("Y"))
                        lang += "Ruby, ";
                    if(objUser.getStrCss().equals("Y"))
                        lang += "CSS, ";
                    if(objUser.getStrC().equals("Y"))
                        lang += "C, ";
                    if(objUser.getStrObj().equals("Y"))
                        lang += "Objective-C, ";
                    lang = lang.substring(0, lang.length() - 2);

                    if(objUser.getStrMon().equals("Y"))
                        days += "Mon, ";
                    if(objUser.getStrTues().equals("Y"))
                        days += "Tues, ";
                    if(objUser.getStrWeds().equals("Y"))
                        days += "Weds, ";
                    if(objUser.getStrThur().equals("Y"))
                        days += "Thurs, ";
                    if(objUser.getStrFri().equals("Y"))
                        days += "Fri, ";
                    if(objUser.getStrSat().equals("Y"))
                        days += "Sat, ";
                    if(objUser.getStrSun().equals("Y"))
                        days += "Sun, ";
                    days = days.substring(0, days.length() - 2);

                    if(objUser.getStrMorn().equals("Y"))
                        times += "Morn, ";
                    if(objUser.getStrAnoon().equals("Y"))
                        times += "A/noon, ";
                    if(objUser.getStrEve().equals("Y"))
                        times += "Eve, ";

                    latitude = Double.parseDouble(objUser.getStrLat());
                    longitude = Double.parseDouble(objUser.getStrLong());
                    geocoder = new Geocoder(getActivity(), Locale.getDefault());
                    try {
                        addresses = geocoder.getFromLocation(latitude, longitude,1);}
                    catch (IOException e)
                    {
                        Log.e("Error"," e.printStackTrace(): " + e.getMessage());
                        Snackbar.make(getActivity().findViewById(android.R.id.content), "Error finding location, please try again", Snackbar.LENGTH_LONG);
                        return;
                    }

                    String strCity = addresses.get(0).getLocality();
                    String strState = addresses.get(0).getAdminArea();

                    //populate views
                    Uri uriImageUri = Uri.parse(strImageUri);
                    Picasso.with(getActivity()).load(uriImageUri).fit().centerCrop().into(iv);

                    tvName.setText(name);
                    tvDesc.setText(desc);
                    tvExp.setText((exp));
                    tvAge.setText(age);
                    tvLang.setText(lang);
                    tvDays.setText(days);
                    tvTimes.setText(times);
                    tvLoc.setText(strCity + ", " + strState + ".");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        }); //end addListenerForSingleValueEvent

        return view;
    }//end onCreateView


}
