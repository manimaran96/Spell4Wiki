package com.manimarank.spell4wiki.ui.categoryselector

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.DisplayMetrics
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.apis.ApiClient
import com.manimarank.spell4wiki.data.apis.ApiInterface
import com.manimarank.spell4wiki.data.model.CategoryItem
import com.manimarank.spell4wiki.data.model.WikiCategoryListItemResponse
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.ui.listerners.OnCategorySelectionListener
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.constants.ListMode.Companion.EnumListMode
import com.manimarank.spell4wiki.utils.makeGone
import com.manimarank.spell4wiki.utils.makeVisible
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import com.manimarank.spell4wiki.databinding.BottomSheetCategorySelectionBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CategorySelectionFragment(private val mActivity: Activity) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetCategorySelectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var pref: PrefManager
    private var callback: OnCategorySelectionListener? = null
    private var categoryList = ArrayList<CategoryItem>()
    private var adapter: CategoryAdapter? = null

    @EnumListMode
    private var listMode = 0
    private var preSelectedLanguageCode: String? = null

    @JvmOverloads
    fun init(callback: OnCategorySelectionListener?, @EnumListMode mode: Int, preSelectedLanguageCode: String? = null) {
        this.callback = callback
        listMode = mode
        this.preSelectedLanguageCode = preSelectedLanguageCode
        isCancelable = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        pref = PrefManager(context)
        val dialog = BottomSheetDialog(mActivity, R.style.BottomSheetDialogTheme)
        _binding = BottomSheetCategorySelectionBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        if (!TextUtils.isEmpty(subTitleInfo)) {
            binding.textSelectCategorySubTitle.makeVisible()
            binding.textSelectCategorySubTitle.text = subTitleInfo
        }

        categoryList.clear()

        val categorySelectionListener = object : OnCategorySelectionListener {
            override fun onCallBackListener(category: String?) {
                callback?.onCallBackListener(category)
                dismiss()
            }
        }

        adapter = CategoryAdapter(arrayListOf(), categorySelectionListener)
        binding.recyclerView.adapter = adapter
        binding.btnClose.setOnClickListener { dismiss() }
        dialog.setOnShowListener { dialog1: DialogInterface ->
            val d = dialog1 as BottomSheetDialog
            val bottomSheet = d.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) {
                val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
                behavior.isHideable = false
                behavior.state = BottomSheetBehavior.STATE_EXPANDED

                // Full screen mode no collapse
                val displayMetrics = DisplayMetrics()
                mActivity.windowManager.defaultDisplay.getMetrics(displayMetrics)
                val screenHeight = displayMetrics.heightPixels
                behavior.peekHeight = screenHeight
            }
        }

        binding.searchView.queryHint = getString(R.string.search)
        binding.searchView.isQueryRefinementEnabled = true
        val handler = Handler(Looper.getMainLooper())
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                showLoader(null)
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed({
                    fetchCategory(newText)
                }, 400)
                return false
            }
        })

        return dialog
    }

    fun fetchCategory(searchTerm: String?) {
        if (searchTerm == null) {
            showLoader(null, "Not valid search term")
            return
        }

        // Check internet connectivity before making API call
        if (!isConnected(mActivity)) {
            showLoader(null, mActivity.getString(R.string.check_internet))
            return
        }

        //Wiktionary Categories list
        val api = ApiClient.getWiktionaryApi(mActivity, pref.languageCodeSpell4WikiAll ?: AppConstants.DEFAULT_LANGUAGE_CODE).create(ApiInterface::class.java)
        val call = api.fetchCategoryList(
            searchTerm,
            //searchTerm,
            100,
            null
        )

        showLoader(true)

        call.enqueue(object : Callback<WikiCategoryListItemResponse?> {
            override fun onResponse(call: Call<WikiCategoryListItemResponse?>, response: Response<WikiCategoryListItemResponse?>) {
                // Check if fragment is still attached to prevent crashes
                if (_binding == null || !isAdded) return

                var resList: List<CategoryItem> = listOf()
                if (response.isSuccessful && response.body() != null) {
                    resList = response.body()?.query?.allpages ?: listOf()
                }

                if (resList.isNotEmpty()) {
                    showLoader(false)
                    adapter?.loadData(resList)
                } else {
                    showLoader(null, mActivity.getString(R.string.result_not_found))
                }
            }

            override fun onFailure(call: Call<WikiCategoryListItemResponse?>, t: Throwable) {
                // Check if fragment is still attached to prevent crashes
                if (_binding == null || !isAdded) return

                showLoader(false, t.message ?: "")
                t.printStackTrace()

            }
        })
    }
    fun showLoader(show: Boolean?, resInfo: String? = null) {
        // Check if binding is still available to prevent NPE when fragment is destroyed
        if (_binding == null) return

        binding.recyclerView.makeGone()
        binding.progressBar.makeGone()
        binding.txtCategorySearchInfo.makeGone()

        if (resInfo != null) {
            binding.txtCategorySearchInfo.text = resInfo
            binding.txtCategorySearchInfo.makeVisible()
        } else if (show == true) {
            binding.progressBar.makeVisible()
        } else {
            binding.recyclerView.makeVisible()
        }
    }
    private val subTitleInfo: String?
        get() {
            return "Search Category by known key words"
        }

    fun show(fragmentManager: FragmentManager) {
        try {
            if (fragmentManager.findFragmentByTag(tagValue) != null) return
        } catch (ignore: Exception) {
        }
        show(fragmentManager, tagValue)
    }

    private val tagValue: String
        get() = "CATEGORY_SELECTION_FRAGMENT"

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}