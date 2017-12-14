package com.jamesdavidmurphy.coffeeandcode;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class LoginActivity extends AppCompatActivity {

    private EditText etPwrd, etEmail;
    private Button btnLogin;
    private DatabaseReference databaseAuth;
    private FirebaseAuth fbAuth;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressDialog = new ProgressDialog(this);

        //******************************************************************************************
        //NOW ONTO BODY
        etPwrd = (EditText) findViewById(R.id.etPwrd2);
        etEmail = (EditText) findViewById(R.id.etEmail2);
        btnLogin = (Button) findViewById(R.id.btnLogin2);
        fbAuth = FirebaseAuth.getInstance();
        databaseAuth = FirebaseDatabase.getInstance().getReference().child("auth");

        //handle login button click
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strPwrd = etPwrd.getText().toString().trim();
                String strEmail = etEmail.getText().toString().trim();

                if(!TextUtils.isEmpty(strPwrd) && !TextUtils.isEmpty(strEmail)){
                    progressDialog.setMessage("Logging in user...");
                    progressDialog.show();
                    fbAuth.signInWithEmailAndPassword(strEmail, strPwrd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                getUserCredentials();
                            }else{
                                Toast.makeText(getApplicationContext(), "Authorization failed:\n\n" + task.getException(), Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                                return;
                            } //end if
                        }
                    }); // end addOnCompleteListener
                }else{
                    Toast.makeText(getApplicationContext(), "Please enter both email and password", Toast.LENGTH_LONG).show();
                } // end if
            }
        }); //end setOnClickListener Register

    } //end onCreate!!!

    public void getUserCredentials(){
        MainActivity.myUserId = fbAuth.getCurrentUser().getUid();
        //query "auth" collection to see if Uid exists.....
        databaseAuth.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //lookup username using Uid
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    String Uid = (String) childDataSnapshot.getValue();
                    if(Uid.equals(MainActivity.myUserId))
                        MainActivity.myUserName = (String) childDataSnapshot.getKey();
                }
                //------------------------------------------------------------------------
                //if no userName found then we need to send them back to registration page
                if(MainActivity.myUserName==null){
                    MainActivity.missingUname = true;
                    Toast.makeText(getApplicationContext(), "Your email & password are succesfully resistered\n....however we just need a new username from you", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                    return;
                }



                //------------------------------------------------------------------------
                //set lat/long to null in case 2 diff users using same app
                MainActivity.myLong = null;
                MainActivity.myLat = null;

                Intent loginIntent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(loginIntent);

            } //end onDataChange

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Toast.makeText(getApplicationContext(), "Database error: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("sweet", "Database error: " + databaseError.getMessage());
            }
        }); //end addValueEventListener

    } // end checkUserExists

} // end class!!!
