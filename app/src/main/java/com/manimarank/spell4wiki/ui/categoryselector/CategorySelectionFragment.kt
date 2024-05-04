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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CategorySelectionFragment(private val mActivity: Activity) : BottomSheetDialogFragment() {
    private lateinit var pref: PrefManager
    private var callback: OnCategorySelectionListener? = null
    private var categoryList = ArrayList<CategoryItem>()
    private var adapter: CategoryAdapter? = null

    @EnumListMode
    private var listMode = 0
    private var preSelectedLanguageCode: String? = null

    var recyclerView: RecyclerView? = null
    var loaderView: ProgressBar? = null
    var txtCategoryResInfo: TextView? = null

    @JvmOverloads
    fun init(callback: OnCategorySelectionListener?, @EnumListMode mode: Int, preSelectedLanguageCode: String? = null) {
        this.callback = callback
        listMode = mode
        this.preSelectedLanguageCode = preSelectedLanguageCode
        isCancelable = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        pref = PrefManager(context)
        val dialog = BottomSheetDialog(mActivity, R.style.AppTheme)
        dialog.setContentView(R.layout.bottom_sheet_category_selection)
        val txtTitle = dialog.findViewById<TextView>(R.id.text_select_category_sub_title)
        if (!TextUtils.isEmpty(subTitleInfo) && txtTitle != null) {
            txtTitle.makeVisible()
            txtTitle.text = subTitleInfo
        }
        recyclerView = dialog.findViewById(R.id.recyclerView)
        loaderView = dialog.findViewById(R.id.progressBar)
        txtCategoryResInfo = dialog.findViewById(R.id.txtCategorySearchInfo)
        val btnClose = dialog.findViewById<ImageView>(R.id.btn_close)
        val searchView = dialog.findViewById<SearchView>(R.id.search_view)

        categoryList.clear()

        val categorySelectionListener = object : OnCategorySelectionListener {
            override fun onCallBackListener(category: String?) {
                callback?.onCallBackListener(category)
                dismiss()
            }
        }

        adapter = CategoryAdapter(arrayListOf(), categorySelectionListener, preSelectedLanguageCode)
        recyclerView?.adapter = adapter
        btnClose?.setOnClickListener { dismiss() }
        dialog.setOnShowListener { dialog1: DialogInterface ->
            val d = dialog1 as BottomSheetDialog
            val bottomSheet = d.findViewById<FrameLayout>(R.id.design_bottom_sheet)
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

        searchView?.queryHint = getString(R.string.search)
        searchView?.isQueryRefinementEnabled = true
        val handler = Handler(Looper.getMainLooper())
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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

        //Wiktionary Categories list
        val api = ApiClient.getWiktionaryApi(mActivity, pref.languageCodeSpell4WikiAll ?: AppConstants.DEFAULT_LANGUAGE_CODE).create(ApiInterface::class.java)
        val call = api.fetchCategoryList(
            searchTerm,
            searchTerm,
            100,
            null
        )

        showLoader(true)

        call.enqueue(object : Callback<WikiCategoryListItemResponse?> {
            override fun onResponse(call: Call<WikiCategoryListItemResponse?>, response: Response<WikiCategoryListItemResponse?>) {
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
                showLoader(false, t.message ?: "")
                t.printStackTrace()

            }
        })
    }
    fun showLoader(show: Boolean?, resInfo: String? = null) {
        recyclerView.makeGone()
        loaderView.makeGone()
        txtCategoryResInfo.makeGone()

        if (resInfo != null) {
            txtCategoryResInfo?.text = resInfo
            txtCategoryResInfo.makeVisible()
        } else if (show == true) {
            loaderView.makeVisible()
        } else {
            recyclerView.makeVisible()
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
}