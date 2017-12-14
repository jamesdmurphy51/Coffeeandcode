package com.jamesdavidmurphy.coffeeandcode;

import android.content.Intent;
import android.database.DataSetObserver;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private EditText chatText;
    private Button btnSend;
    private boolean side = false;
    private DatabaseReference databaseMessagesSender;
    private DatabaseReference databaseMessagesReceiver;
    private RecyclerView rvMsgList;

    private FirebaseUser fbUser;
    private FirebaseAuth fbAuth;
    private DatabaseReference databaseAuthUser;

    //******************************************************************************************
    //******************************************************************************************
    //******************************************************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //FIRST INSTATIATE ACTION BAR
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        ActionBar ab = getSupportActionBar();
        ab.setTitle("Message " + MainActivity.usernameClicked);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);

        //override setDisplayHomeAsUpEnabled default action as we just want to backout, not go to home
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatActivity.super.onBackPressed();
            }
        });
        //******************************************************************************************
        //******************************************************************************************
        //NOW ONTO BODY

        //------------------------------------------------------------------------------------------
        //get references to all objects
        rvMsgList = (RecyclerView) findViewById(R.id.messageRec);
        databaseMessagesSender = FirebaseDatabase.getInstance().getReference().child("messages").child(MainActivity.myUserName).child(MainActivity.usernameClicked);
        databaseMessagesReceiver = FirebaseDatabase.getInstance().getReference().child("messages").child(MainActivity.usernameClicked).child(MainActivity.myUserName);
        chatText = (EditText) findViewById(R.id.etChat);
        btnSend = (Button) findViewById(R.id.btnSend);
        fbAuth = FirebaseAuth.getInstance();
        //------------------------------------------------------------------------------------------

        //------------------------------------------------------------------------------------------
        //format recylcler view
        rvMsgList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        rvMsgList.setLayoutManager(linearLayoutManager);
        //------------------------------------------------------------------------------------------

        //------------------------------------------------------------------------------------------
        //2 x listeners for sending message
        //handle event where user hits enter (calls sendChatMessage)
        chatText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if((event.getAction()==KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    return sendChatMessage();
                }
                return false;
            }
        });
        //handle event where user clicks btnSend (also calls sendChatMessage)
        btnSend.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                sendChatMessage();
            }}
        );
        //------------------------------------------------------------------------------------------

    }//end onCreate!!!!


    @Override
    protected void onStart() {
        super.onStart();

        //populate RV with FB data
        FirebaseRecyclerAdapter<Message, MessageViewHolder> FBRA = new FirebaseRecyclerAdapter<Message, MessageViewHolder>(
                Message.class,
                R.layout.listview_row_chat,
                MessageViewHolder.class,
                databaseMessagesSender
        ) {
            @Override
            protected void populateViewHolder(MessageViewHolder viewHolder, Message model, int position) {
                viewHolder.setContent(model.getUserName() + ": " + model.getContent());

            }
        }; // end FirebaseRecyclerAdapter

        //here we populate rv with FB message data (ALL MESSAGES!!!)
        rvMsgList.setAdapter(FBRA);

    } //end onStart()

    //******************************************************************************************
    //******************************************************************************************

    private boolean sendChatMessage(){
        //prep to get FB userName from auth collection (using auth ID)
        //fbUser = fbAuth.getCurrentUser();
        //databaseAuthUser = FirebaseDatabase.getInstance().getReference().child("auth").child(fbUser.getUid());

        //get value from et
        final String chatMsgText = chatText.getText().toString().trim();
        if(!TextUtils.isEmpty(chatMsgText)){
            //-----------------------------------------
            //FIRST ADD TO CURRENT USERS MSG COLLECTION
            //First add PK to messages DB
            final DatabaseReference newPost = databaseMessagesSender.push();
            //then add message object as child
            Message msg = new Message(MainActivity.myUserName, chatMsgText);

            newPost.setValue(msg).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                       //nothing required
                    } else {
                        Toast.makeText(getApplicationContext(), "Database failure:\n\n" + task.getException(), Toast.LENGTH_LONG).show();
                        Log.d("sweet", "login:failure", task.getException());
                    }
                }
            });
            //-----------------------------------------
            //THEN ADD TO RECEIVING USERS MSG COLLECTION
            //First add PK to messages DB
            final DatabaseReference newPost2 = databaseMessagesReceiver.push();

            newPost2.setValue(msg).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        //scroll to last entry of recycler view
                        rvMsgList.scrollToPosition(rvMsgList.getAdapter().getItemCount());
                    } else {
                        Toast.makeText(getApplicationContext(), "Database failure:\n\n" + task.getException(), Toast.LENGTH_LONG).show();
                        Log.d("sweet", "login:failure", task.getException());
                    }
                }
            });
        } //end  if(!TextUtils.isEmpty(chatMsgText)){

        chatText.setText("");


        return true;
    } //end sendChatMessage


    //********************************************************************
    //********************************************************************
    //********************************************************************
    public static class MessageViewHolder extends RecyclerView.ViewHolder{

        View mView;
        public MessageViewHolder(View itemView){
            super(itemView);
            mView = itemView;
        }

        public void setContent(String content){
            //get reference to bubble textView
            TextView singleMessage = (TextView) mView.findViewById(R.id.singleMessage);
            singleMessage.setText(content);
        }

    } //end child class

    //********************************************************************
    //********************************************************************

} //end parent class

