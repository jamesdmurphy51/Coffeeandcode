package com.jamesdavidmurphy.coffeeandcode;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUname, etPwrd, etEmail;
    private Button btnRegister, btnLogin;
    private DatabaseReference databaseAuth;
    //private DatabaseReference databaseUser;
    private FirebaseAuth firebaseAuth;

    String strUname;
    String strPwrd;
    String strEmail;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        progressDialog = new ProgressDialog(this);

        //******************************************************************************************

        //******************************************************************************************
        //NOW ONTO BODY
        etUname = (EditText) findViewById(R.id.etUname);
        etPwrd = (EditText) findViewById(R.id.etPwrd);
        etEmail = (EditText) findViewById(R.id.etEmail);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseAuth = FirebaseDatabase.getInstance().getReference().child("auth");

        //------------------------------------------------------
        //handle login button click
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        }); //end setOnClickListener Login
        //------------------------------------------------------


        //handle register button click
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //---------------------------------------------------------------
                //if nameOnly is true, then registration is already complete....
                //...we just need to add to auth DB
                if(MainActivity.missingUname!=null) {
                    if (MainActivity.missingUname) {
                        //overwrite username
                        strUname = etUname.getText().toString().trim();
                        if (TextUtils.isEmpty(strUname)) {
                            Toast.makeText(getApplicationContext(), "Please add user name", Toast.LENGTH_LONG).show();
                        } else {
                            checkUnameAndAddToDb();
                        }
                        return;
                    }
                }
                //---------------------------------------------------------------

                strUname = etUname.getText().toString().trim();
                strPwrd = etPwrd.getText().toString().trim();
                strEmail = etEmail.getText().toString().trim();

                if(TextUtils.isEmpty(strUname) || TextUtils.isEmpty(strPwrd) || TextUtils.isEmpty(strEmail)) {
                    Toast.makeText(getApplicationContext(), "Please complete all 3 fields", Toast.LENGTH_LONG).show();
                    return;
                }else{
                    if(strPwrd.length()<6){
                        Toast.makeText(getApplicationContext(), "Password must be min 6 characters", Toast.LENGTH_LONG).show();
                        return;
                    }

                    //THEN CONTINUE WITH REGISTRATION
                    progressDialog.setMessage("Registering user...");
                    progressDialog.show();

                    //--------------------------------------------------------------------------------------------------
                    //--------------------------------------------------------------------------------------------------
                    //create FB user auth account
                    firebaseAuth.createUserWithEmailAndPassword(strEmail, strPwrd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                //get name & user id into auth db
                                MainActivity.myUserId = firebaseAuth.getCurrentUser().getUid();

                                checkUnameAndAddToDb();

                            }else{
                                Log.e("Error", "Auth failed!!: ", task.getException());
                                Toast.makeText(getApplicationContext(), "Auth failed!!:\n\n" + task.getException(), Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            } //end if
                        }
                    }); // end createUserWithEmailAndPassword
                    //--------------------------------------------------------------------------------------------------
                    //--------------------------------------------------------------------------------------------------
                } //end if

            }
        }); //end setOnClickListener Register

        if(MainActivity.missingUname!=null) {
            if (MainActivity.missingUname == true) {
                etPwrd.setVisibility(View.GONE);
                etEmail.setVisibility(View.GONE);
                btnLogin.setVisibility(View.GONE);
                etUname.setText("");
            }
        }


    } //end onCreate!!!


    //******************************************************************************************************
    //******************************************************************************************************

    private void checkUnameAndAddToDb(){

        //-------------------------------------------------------------------
        //CHECK IF USERNAME EXISTS!!
        databaseAuth.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(strUname)) {
                    Toast.makeText(getApplicationContext(), "Your email & password are succesfully resistered\n....however we just need a new username from you", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                    etPwrd.setVisibility(View.GONE);
                    etEmail.setVisibility(View.GONE);
                    btnLogin.setVisibility(View.GONE);
                    etUname.setText("");
                    MainActivity.missingUname = true;
                }else{
                    addToAuthDB();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Uname check failed!! (OnCancelled):\n\n" + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        }); //end addListenerForSingleValueEvent
        //-------------------------------------------------------------------



    }



    private void addToAuthDB(){

        //create child under "auth" collection reflecting userId
        //DatabaseReference current_user_db = databaseAuth.child(strUname);
        //---------------------------------------------------------------------
        //add name/value pair under userId child
        databaseAuth.child(strUname).setValue(MainActivity.myUserId).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "registration successfull", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                    MainActivity.missingUname = false;
                    MainActivity.myUserName = strUname;
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                } else {
                    Toast.makeText(getApplicationContext(), "Database registration failed:\n\n" + task.getException(), Toast.LENGTH_LONG).show();
                    Log.d("sweet", "registration:failure " + task.getException());
                    progressDialog.dismiss();
                }
            }
        }); // end addOnCompleteListener
        //---------------------------------------------------------------------



    }

} // end class!!!
