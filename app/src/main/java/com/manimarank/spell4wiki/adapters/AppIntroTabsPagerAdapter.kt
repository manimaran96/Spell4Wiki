package com.manimarank.spell4wiki.adapters

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import com.manimarank.spell4wiki.fragments.AppIntroSlideFragment
import com.manimarank.spell4wiki.models.AppIntroData

@SuppressLint("WrongConstant")
class AppIntroTabsPagerAdapter(fm: FragmentManager?, list: List<AppIntroData>) :
        FragmentStatePagerAdapter(fm!!, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    var data = list

    override fun getItem(position: Int): Fragment {
        return AppIntroSlideFragment.newInstance(data[position])
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return ""
    }

    override fun getCount(): Int {
        return data.size
    }
}