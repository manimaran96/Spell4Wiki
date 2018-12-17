package com.manimaran.wikiaudio.adpter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.manimaran.wikiaudio.fragment.SwipeFragment;
import com.manimaran.wikiaudio.model.Words;

import java.util.List;

public class CustomPagerAdapter extends FragmentStatePagerAdapter {

    List<Words> list;
    SwipeFragment fragment;
    public CustomPagerAdapter(FragmentManager fm, List<Words> list) {
        super(fm);
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Fragment getItem(int position) {
        fragment = SwipeFragment.newInstance(position);
        fragment.setWord(list.get(position).getWord());
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        Log.e("Check ", "Frag Adapter destroy Item");
        super.destroyItem(container, position, object);
    }


}