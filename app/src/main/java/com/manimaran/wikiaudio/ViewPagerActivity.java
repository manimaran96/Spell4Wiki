package com.manimaran.wikiaudio;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.manimaran.wikiaudio.model.Words;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_pager);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

        Bundle bundle = getIntent().getExtras();
        Integer pos = 0;
        if(bundle!=null)
        {
            String word =  bundle.getString("word");
            pos =  bundle.getInt("pos");
        }
        String[] list = getResources().getStringArray(R.array.words);
        List<Words> wordsList = new ArrayList<>();
        for(String s : list)
            wordsList.add(new Words(s, "",false));

        viewPager.setAdapter(new CustomPagerAdapter(getSupportFragmentManager(), wordsList));
        viewPager.setCurrentItem(pos, true);
    }
}