package com.manimaran.wikiaudio.fragment;

import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.manimaran.wikiaudio.R;

public class MyCustomDialogFragment extends DialogFragment {
 @Override
 public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 }
 
 @Override
 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.pop_up_record_ui, container, false);
        
        // Do all the stuff to initialize your custom view
        
        return v;
    } 
}