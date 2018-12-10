package com.manimaran.wikiaudio;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.manimaran.wikiaudio.model.Words;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] list = getResources().getStringArray(R.array.words);

        List<Words> wordsList = new ArrayList<>();
        for(String s : list)
            wordsList.add(new Words(s, "",false));

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        WordsAdapter mAdapter = new WordsAdapter(wordsList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
    }
}
