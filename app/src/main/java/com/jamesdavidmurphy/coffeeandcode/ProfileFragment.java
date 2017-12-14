package com.jamesdavidmurphy.coffeeandcode;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.LOCATION_SERVICE;
import static com.facebook.FacebookSdk.getApplicationContext;


public class ProfileFragment extends Fragment
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    //below is used if you want to LISTEN
    //, com.google.android.gms.location.LocationListener

    private View view;
    private DatabaseReference databaseUser;
    private StorageReference mStorageRef;

    private EditText etName, etAddress, etWebsite, etGithub, etAboutMe;
    private String strWebsite, strGithub, strAboutMe, strMatch;

    private CheckBox chkJsc, chkJva, chkPhp, chkPyt, chkCsh, chkCpl, chkRub, chkCss, chkC, chkObj,
            chkMon, chkTues, chkWeds, chkThur, chkFri, chkSat, chkSun, chkMorn, chkAnoon, chkEve;
    private String strJsc, strJva, strPhp, strPyt, strCsh, strCpl, strRub, strCss, strC, strObj,
            strMon, strTues, strWeds, strThur, strFri, strSat, strSun, strMorn, strAnoon, strEve;

    private Spinner spnAge, spnExp;
    private String strAge, strExp;

    private ImageView imvProfilePic;
    private Uri uriLocalImageFilePath;
    private String strImageUri;

    private Button btnLocation;

    private static final int GALLERY_INTENT = 2;
    private ProgressDialog mProgressDialog;

    private Boolean usernameExists;

    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    //below is used if you want to LISTEN
    //private LocationRequest locationRequest;
    //private FusedLocationProviderApi locationProviderApi;

    private Geocoder geocoder;
    private List<Address> addresses;
    private String strAddress;
    private double dblTempLat, dblTempLong;

    private static final int PERMIS_REQ_LOC = 101;
    //**********************************************************************************************
    //**********************************************************************************************
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_profile, container, false);

        etName = (EditText) view.findViewById(R.id.etName);
        etName.setText(MainActivity.myUserName);

        //---------------------------------------------------------------
        //listener for imageView
        imvProfilePic = (ImageView) view.findViewById(R.id.imvProfilePic);
        Button btnGallery = (Button) view.findViewById(R.id.btnGallery);

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_INTENT);
            }
        }); //end btnGallery.setOnClickListener
        //---------------------------------------------------------------


        //---------------------------------------------------------------
        //listener for Geolocation button
        btnLocation = (Button) view.findViewById(R.id.btnLoc);
        etAddress = (EditText) view.findViewById(R.id.etAddress);

        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(getActivity().findViewById(android.R.id.content), "We are finding you!...Please be patient", Snackbar.LENGTH_LONG).show();
                getLocation();
            }
        });
        //---------------------------------------------------------------


        //---------------------------------------------------------------
        //listener save button
        //FIREBASE
        databaseUser = FirebaseDatabase.getInstance().getReference().child("users");
        //databaseMatches = FirebaseDatabase.getInstance().getReference().child("matches");
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mProgressDialog = new ProgressDialog(getActivity()); //getActivity() instead of 'this'

        Button btnSave = (Button) view.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //first validate user is happy with address (if we dont have their lat/long
                if(MainActivity.myLat==null && MainActivity.myLong==null) { //this means that user has entered address manually!!
                    final String locMessage = validateUserManualLocation();

                    //check user is happy with address before saving to DB
                    final Snackbar snackBar = Snackbar.make((getActivity()).findViewById(android.R.id.content), "Your address will be added as: " + locMessage + "\nAre you Sure?", Snackbar.LENGTH_LONG);
                    //--------------------------------------
                    View snackbarView = snackBar.getView();
                    TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setMaxLines(5);  // show multiple line
                    //--------------------------------------
                    snackBar.setAction("I'm Sure", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackBar.dismiss();
                            strAddress = locMessage;
                            etAddress.setText(strAddress);
                            //confirm lat/long to module variables
                            MainActivity.myLat = Double.toString(dblTempLat);
                            MainActivity.myLong = Double.toString(dblTempLong);
                            validateAndSaveProfile();
                        }
                    });
                    snackBar.show();

                }else{
                    //we have lat/long from 'Find Me"....so can go straight to validate/save
                    validateAndSaveProfile();
                } //end if(MainActivity.myLat==null && MainActivity.myLong==null)

            }//end onClick
        }); //end btnSave.setOnClickListener
        //---------------------------------------------------------------

        //---------------------------------------------------------------
        //listener for reset button (TBC)
        //TBC
        //---------------------------------------------------------------

        return view;

    }//end onCreateView
    //**********************************************************************************************
    //**********************************************************************************************


    private void validateAndSaveProfile(){
        //***************************************
        //NOW WE CONTINUE WITH REST OF VALIDATION
        String errorMsg = validateUserInput();
        if (errorMsg != "") {
            Snackbar.make(getActivity().findViewById(android.R.id.content), errorMsg, Snackbar.LENGTH_LONG).show();
            return;
        }
        //now save to FB
        saveProfileToFBUsers();
        //***************************************
    }





    //**********************************************************************************************
    //**********************************************************************************************
    //GEOLOCATION!!!
    private void getLocation(){

        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        /*
        //below is used if you want to LISTEN
        locationProviderApi = LocationServices.FusedLocationApi;
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        */
        googleApiClient.connect();
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //if user not granted persmission......
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //....then MUST request
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMIS_REQ_LOC);
        }

        //now we can get lcoation (hopefully)
        lastLocation =  LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        //below is used if you want to LISTEN
        //LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        if (lastLocation != null) {
            double myLat = lastLocation.getLatitude();
            double myLong = lastLocation.getLongitude();
            //popualate GLOBAL LEVEL VARIABLES to be populated to d/base
            MainActivity.myLat = Double.toString(myLat);
            MainActivity.myLong = Double.toString(myLong);

            //now get text address to show user
            geocoder = new Geocoder(getActivity(), Locale.getDefault());
            try {
                addresses = geocoder.getFromLocation(myLat, myLong,1);}
            catch (IOException e)
            {
                Log.d("sweet"," e.printStackTrace(): " + e.getMessage());
                Snackbar.make(getActivity().findViewById(android.R.id.content), "Error finding location, please try again", Snackbar.LENGTH_LONG);
                return;
            }

            //String address = addresses.get(0).getAddressLine(0);
            //String postalCode = addresses.get(0).getPostalCode();
            String strCity = addresses.get(0).getLocality();
            String strState = addresses.get(0).getAdminArea();
            String strCountry = addresses.get(0).getCountryName();
            strAddress = strCity + ", " + strState + "."; //MODULE LEVEL VARIABLE!!
            etAddress.setText(strAddress);

        }else{
            Snackbar.make(getActivity().findViewById(android.R.id.content), "Couldn't get the location. Make sure location is enabled on the device", Snackbar.LENGTH_LONG).show();
        }

        googleApiClient.disconnect();

    } //end onConnected

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("sweet", "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    /*
    @Override
    public void onStart() {
        super.onStart();
        googleApiClient.connect();
    }
    @Override
    public void onResume() {
        super.onResume();
        if(googleApiClient.isConnected())
            requestLocationUpdates();
    }
    @Override
    public void onPause() {
        super.onPause();
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }
    @Override
    public void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }
    @Override
    public void onLocationChanged(Location location) {
        Double myLat = location.getLatitude();
        Double myLong = location.getLongitude();

        etAddress.setText(myLat.toString());

        googleApiClient.disconnect();
    }
    */
    //END GEOLOCATION
    //**********************************************************************************************
    //**********************************************************************************************







    //**********************************************************************************************
    //**********************************************************************************************
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_INTENT && resultCode == RESULT_OK) {
            //get uri of gallery image & add to imageView
            uriLocalImageFilePath = data.getData();
            Picasso.with(getActivity()).load(uriLocalImageFilePath).fit().centerCrop().into(imvProfilePic);
        }
    } // end onActivityResult
    //**********************************************************************************************
    //**********************************************************************************************


    //**********************************************************************************************
    //**********************************************************************************************
    private void saveProfileToFBUsers(){

        //now replace local (GoogleImages) uri for Firebase uri by uploading image
        //FIREBASE CODE
        mProgressDialog.setMessage("Uploading data....");
        mProgressDialog.show();
        StorageReference filepath = mStorageRef.child("Photos").child(MainActivity.myUserName);
        //add to firebase storage
        filepath.putFile(uriLocalImageFilePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                @SuppressWarnings("VisibleForTests") Uri uriFirebaseUri =  taskSnapshot.getDownloadUrl();
                //replace string for local image uri with firebase uri string
                strImageUri = uriFirebaseUri.toString();

                //now create user object to upload to FB d/base
                User user = new User(strJsc,  strJva,  strPhp,  strPyt,  strCsh,  strCpl,  strRub,  strCss,  strC,  strObj,
                        strMon,  strTues,  strWeds,  strThur,  strFri,  strSat,  strSun,
                        strMorn, strAnoon,  strEve,
                        MainActivity.myUserName,  MainActivity.myUserId,  MainActivity.myLat,  MainActivity.myLong, strAddress, strAge,  strExp,  strWebsite,  strGithub,  strImageUri,  strAboutMe,  strMatch);

                //commit profile to Firebase here
                databaseUser.child(MainActivity.myUserName).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mProgressDialog.dismiss();
                            Snackbar.make(getActivity().findViewById(android.R.id.content), "Profile info has saved", Snackbar.LENGTH_LONG).show();
                        } else {
                            mProgressDialog.dismiss();
                            Snackbar.make(getActivity().findViewById(android.R.id.content), "Profile info failed to load:\n\n" + task.getException(), Snackbar.LENGTH_LONG).show();
                            Log.e("Error", "login:failure", task.getException());
                        }
                    }
                });// end addOnCompleteListener
            }
        });
    }//end saveProfileToFBUsers
    //**********************************************************************************************
    //**********************************************************************************************


    //**********************************************************************************************
    //**********************************************************************************************
    private String validateUserManualLocation(){
        String strInputtedAddress;
        String strCheckedAddress = "";

        etAddress = (EditText) view.findViewById(R.id.etAddress);
        strInputtedAddress = etAddress.getText().toString().trim();
        if (strInputtedAddress.length() == 0) {
            return "Please enter address or click 'FIND ME!'";
        } else {
            //need to get LAT/LONG from address to populate to d/base
            geocoder = new Geocoder(getActivity(), Locale.getDefault());
            try {
                addresses = geocoder.getFromLocationName(strInputtedAddress, 1);
            } catch (IOException e) {
                Log.e("Error", " e.printStackTrace(): " + e.getMessage());
                return "Unable to locate address inputted, please try again";
            }
            if (addresses.size() > 0) {
                dblTempLat = addresses.get(0).getLatitude();
                //MainActivity.myLat = Double.toString(latitude);...dont want to populat until user approves address
                dblTempLong = addresses.get(0).getLongitude();
                //MainActivity.myLong = Double.toString(longitude);

                //now get REG ADDRESS to check user is happy.....then repopulate edit textn\
                geocoder = new Geocoder(getActivity(), Locale.getDefault());
                try {
                    addresses = geocoder.getFromLocation(dblTempLat, dblTempLong,1);}
                catch (IOException e)
                {
                    Log.e("Error"," e.printStackTrace(): " + e.getMessage());
                    return "Unable to locate address inputted, please try again";
                }

                //finally we have variables for our return statement
                String strCity = addresses.get(0).getLocality();
                String strState = addresses.get(0).getAdminArea();
                //String strCountry = addresses.get(0).getCountryName();
                strCheckedAddress = strCity + ", " + strState + ".";

            } else{
                return "Unable to locate address inputted, please try again";
            } // end if(addresses.size() > 0)

        } // end if strInputtedAddress.length() == 0

        //assuming we made is past all the if's!!
        return strCheckedAddress;

    }; //end validate user location


    private String validateUserInput() {
        //name (PK)
        //MainActivity.myUserName = MainActivity.myUserName;
        //MainActivity.myUserId = MainActivity.myUserId;

        //age
        spnAge = (Spinner) view.findViewById(R.id.spnAge);
        strAge = spnAge.getSelectedItem().toString();

        //experience
        spnExp = (Spinner) view.findViewById(R.id.spnExp);
        strExp = spnExp.getSelectedItem().toString();

        //languages
        chkJsc = (CheckBox) view.findViewById(R.id.chkJsc);
        chkJva = (CheckBox) view.findViewById(R.id.chkJva);
        chkPhp = (CheckBox) view.findViewById(R.id.chkPhp);
        chkPyt = (CheckBox) view.findViewById(R.id.chkPyt);
        chkCsh = (CheckBox) view.findViewById(R.id.chkCsh);
        chkCpl = (CheckBox) view.findViewById(R.id.chkCpl);
        chkRub = (CheckBox) view.findViewById(R.id.chkRub);
        chkCss = (CheckBox) view.findViewById(R.id.chkCss);
        chkC = (CheckBox) view.findViewById(R.id.chkC);
        chkObj = (CheckBox) view.findViewById(R.id.chkObj);
        if (!chkJsc.isChecked() && !chkJva.isChecked() && !chkPhp.isChecked() && !chkPyt.isChecked() && !chkCsh.isChecked() && !chkCpl.isChecked() && !chkRub.isChecked() && !chkCss.isChecked() && !chkC.isChecked() && !chkObj.isChecked())
            return "Please choose at least one programming language";

        strJsc = (chkJsc.isChecked() ? "Y" : "N");
        strJva = (chkJva.isChecked() ? "Y" : "N");
        strPhp = (chkPhp.isChecked() ? "Y" : "N");
        strPyt = (chkPyt.isChecked() ? "Y" : "N");
        strCsh = (chkCsh.isChecked() ? "Y" : "N");
        strCpl = (chkCpl.isChecked() ? "Y" : "N");
        strRub = (chkRub.isChecked() ? "Y" : "N");
        strCss = (chkCss.isChecked() ? "Y" : "N");
        strC = (chkC.isChecked() ? "Y" : "N");
        strObj = (chkObj.isChecked() ? "Y" : "N");


        //website
        etWebsite = (EditText) view.findViewById(R.id.etWebsite);
        strWebsite = etWebsite.getText().toString().trim();
        if (strWebsite.length() != 0 && !Patterns.WEB_URL.matcher(strWebsite).matches())
            return "Please enter a valid URL for website address";

        //git
        etGithub = (EditText) view.findViewById(R.id.etGithub);
        strGithub = etGithub.getText().toString().trim();
        if (strGithub.length() != 0 && !Patterns.WEB_URL.matcher(strGithub).matches())
            return "Please enter a valid URL for GitHub address";

        //days free
        chkMon = (CheckBox) view.findViewById(R.id.chkMon);
        chkTues = (CheckBox) view.findViewById(R.id.chkTues);
        chkWeds = (CheckBox) view.findViewById(R.id.chkWeds);
        chkThur = (CheckBox) view.findViewById(R.id.chkThur);
        chkFri = (CheckBox) view.findViewById(R.id.chkFri);
        chkSat = (CheckBox) view.findViewById(R.id.chkSat);
        chkSun = (CheckBox) view.findViewById(R.id.chkSun);
        if (!chkMon.isChecked() && !chkTues.isChecked() && !chkWeds.isChecked() && !chkThur.isChecked() && !chkFri.isChecked() && !chkSat.isChecked() && !chkSun.isChecked())
            return "Please choose at least one day that you are free";

        strMon = (chkMon.isChecked() ? "Y" : "N");
        strTues = (chkTues.isChecked() ? "Y" : "N");
        strWeds = (chkWeds.isChecked() ? "Y" : "N");
        strThur = (chkThur.isChecked() ? "Y" : "N");
        strFri = (chkFri.isChecked() ? "Y" : "N");
        strSat = (chkSat.isChecked() ? "Y" : "N");
        strSun = (chkSun.isChecked() ? "Y" : "N");


        //times free
        chkMorn = (CheckBox) view.findViewById(R.id.chkMorn);
        chkAnoon = (CheckBox) view.findViewById(R.id.chkAnoon);
        chkEve = (CheckBox) view.findViewById(R.id.chkEve);
        if (!chkMorn.isChecked() && !chkAnoon.isChecked() && !chkEve.isChecked())
            return "Please choose at least one time of day that you are free";

        strMorn = (chkMorn.isChecked() ? "Y" : "N");
        strAnoon = (chkAnoon.isChecked() ? "Y" : "N");
        strEve = (chkEve.isChecked() ? "Y" : "N");


        //image
        if (uriLocalImageFilePath == null || uriLocalImageFilePath.toString().isEmpty()) {
            return "Please choose profile picture or avatar";
        } else {
            strImageUri = uriLocalImageFilePath.toString();
        }

        //about
        etAboutMe = (EditText) view.findViewById(R.id.etAboutMe);
        strAboutMe = etAboutMe.getText().toString().trim();
        if (strAboutMe.length() == 0) {
            return "Please enter a few lines in the 'about me' section";
        }

        //match
        strMatch = "0";

        //else if all is well we return empty string
        return "";
    }

    //**********************************************************************************************
    //**********************************************************************************************


} //end Class

