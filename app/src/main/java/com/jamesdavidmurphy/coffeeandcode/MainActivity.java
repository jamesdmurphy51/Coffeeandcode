package com.jamesdavidmurphy.coffeeandcode;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FloatingActionButton fabLogout;
    private CoordinatorLayout rootLayout;

    public static String myUserName;
    public static String myUserId;
    public static String myLat;
    public static String myLong;
    public static String usernameClicked;
    public  static Boolean missingUname = false;

    private FirebaseAuth fbAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* removed toolbar as we have TabLayout
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        */

        //-----------------------------------------------------------
        //if user is not logged in, then direct to registration/login page

        fbAuth = FirebaseAuth.getInstance();
        FirebaseUser fbUser = fbAuth.getCurrentUser();

        //if its STILL null then user must log in
        if(fbUser==null) {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        }
        //-----------------------------------------------------------



        //setupViewPager custom method creates/populates ViewPagerAdapter using addFragment method of FragmentPagerAdapter class
        // and sets (attach) it to viewPager
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        //setupWithViewPager is simple method of tabLayout class that attaches it to Viewpager
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        //run setIcon method for each tab
        setupTabIcons();

        //add listener to tab changes
        rootLayout = (CoordinatorLayout) findViewById(R.id.mainLayout);
        fabLogout = (FloatingActionButton) findViewById(R.id.fabLogout);

        //add snackbar when tab is selected
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: Snackbar.make(rootLayout, "Home Page", Snackbar.LENGTH_LONG).show();
                        fabLogout.show();
                        break;
                    case 1: Snackbar.make(rootLayout, "Edit Your Profile, then hit 'Save' button.\nmake sure you scroll " +
                            "all the way to the bottom!", Snackbar.LENGTH_LONG).show();
                        fabLogout.show();
                        break;
                    case 2: Snackbar.make(rootLayout, "Click on suggestions to see more detail.\n\"Thumbs up\" means you want to message/meet.", Snackbar.LENGTH_LONG).show();
                        fabLogout.show();
                        break;
                    case 3: Snackbar.make(rootLayout, "Message with your matches", Snackbar.LENGTH_LONG).show();
                        fabLogout.show();
                        break;
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                this.onTabSelected(tab);
            }
        });


        //add listener for fab
        fabLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //first use snackbar to double-check user want to be friends
                final Snackbar snackBar = Snackbar.make(rootLayout, "This will log out out of the app\nAre you Sure?", Snackbar.LENGTH_LONG);
                snackBar.setAction("I'm Sure", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackBar.dismiss();
                        //signout of FBAuth and clear globals
                        FirebaseAuth.getInstance().signOut();
                        MainActivity.myUserName=null;
                        MainActivity.myUserName=null;
                        MainActivity.myLat=null;
                        MainActivity.myLong=null;
                        MainActivity.missingUname=null;
                        MainActivity.usernameClicked=null;

                        //punt user back to register page
                        Intent i = new Intent(MainActivity.this, RegisterActivity.class);
                        // set the new task and clear flags (so user cant back-button to MainActivity
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);

                    }
                });
                snackBar.show();
            }
        });
    }//end onCreate

    @Override
    public void onBackPressed() {
        //dont want user backing into the login page
    }


    //*********************************************************************************************
    /*
    private static final String SELECTED_ITEM_POSITION = "ItemPosition";
    private int mPosition;

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the state of item position
        outState.putInt(SELECTED_ITEM_POSITION, mPosition);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Read the state of item position
        mPosition = savedInstanceState.getInt(SELECTED_ITEM_POSITION);
    }
    */
    //*********************************************************************************************




    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_home);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_profile);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_people);
        tabLayout.getTabAt(3).setIcon(R.drawable.ic_message);
    }

    public void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new HomeFragment());
        adapter.addFragment(new ProfileFragment());
        adapter.addFragment(new SearchFragment());
        adapter.addFragment(new MatchFragment());
        //now ArrayList (mFragmentList) is populated in FragmentPagerAdapter class, we can attach to
        //our ViewPager
        viewPager.setAdapter(adapter);
    }


    //SEPERATE CLASS
    public class ViewPagerAdapter extends FragmentPagerAdapter {
        public final List<Fragment> mFragmentList = new ArrayList<>();
        //private final List<String> mFragmentTitleList = new ArrayList<>();

        //base constructor
        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        //***key method used above in custom setupViewPager method
        //adds fragment to ArrayList of fragments and string to ArrayList of strings
        public void addFragment(Fragment fragment) {
            mFragmentList.add(fragment);
            //mFragmentTitleList.add(title);
        }

        @Override
        //called when adapter needs fragment but it does not exist in memory
        //returns the fragment at the specified position
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        /*
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
        */
    } //end  public class ViewPagerAdapter
}