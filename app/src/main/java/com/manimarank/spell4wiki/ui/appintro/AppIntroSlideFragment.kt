package com.manimarank.spell4wiki.ui.appintro

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.model.AppIntroData
import com.manimarank.spell4wiki.databinding.AppIntroSlideBinding

class AppIntroSlideFragment : Fragment() {

    private var _binding: AppIntroSlideBinding? = null
    private val binding get() = _binding!!
    companion object {
        const val APP_INTRO_DATA = "app_intro_data"
        fun newInstance(appIntroData: AppIntroData): AppIntroSlideFragment {
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
        _binding = AppIntroSlideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (arguments != null && arguments?.getSerializable(APP_INTRO_DATA) != null) {
            val appIntroData = (arguments?.getSerializable(APP_INTRO_DATA)) as AppIntroData
            binding.txtAppIntroTitle.text = appIntroData.title
            binding.imgAppIntro.setImageResource(appIntroData.imgId)
            binding.txtAppIntroDescription.text = appIntroData.description
            binding.txtAppIntroDescription.movementMethod = ScrollingMovementMethod()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
