package com.jamesdavidmurphy.coffeeandcode;


import android.app.Fragment;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class WorkerFragment extends Fragment {

    // data object we want to retain
    private FirebaseAuth data;

    // this method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    public void setData(FirebaseAuth data) {
        this.data = data;
        if(data.getCurrentUser()==null){
            String test = "true";
        }
    }

    public FirebaseAuth getData() {
        if(data!=null) {
            if (data.getCurrentUser() == null) {
                String test = "true";
            }
        }
        return data;
    }
}
