package com.manimarank.spell4wiki.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.models.AppIntroData
import kotlinx.android.synthetic.main.app_intro_slide.*

class AppIntroSlideFragment : Fragment() {
    companion object {
        const val APP_INTRO_DATA = "app_intro_data"
        fun newInstance(appIntroData : AppIntroData): AppIntroSlideFragment {
            val fragment = AppIntroSlideFragment()
            val args = Bundle()
            args.putSerializable(APP_INTRO_DATA, appIntroData)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.app_intro_slide, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (arguments != null && arguments?.getSerializable(APP_INTRO_DATA) != null) {
            val appIntroData = (arguments?.getSerializable(APP_INTRO_DATA)) as AppIntroData
            txtAppIntroTitle.text = appIntroData.title
            imgAppIntro.setImageResource(appIntroData.imgId)
            txtAppIntroDescription.text = appIntroData.description
        }
    }

}
