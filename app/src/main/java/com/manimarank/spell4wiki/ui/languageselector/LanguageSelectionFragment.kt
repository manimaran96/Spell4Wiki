package com.manimarank.spell4wiki.ui.languageselector

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.db.DBHelper
import com.manimarank.spell4wiki.data.db.entities.WikiLang
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.ui.listerners.OnLanguageSelectionListener
import com.manimarank.spell4wiki.utils.GeneralUtils.openUrlInBrowser
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import com.manimarank.spell4wiki.utils.SnackBarUtils.showNormal
import com.manimarank.spell4wiki.utils.constants.ListMode
import com.manimarank.spell4wiki.utils.constants.ListMode.Companion.EnumListMode
import com.manimarank.spell4wiki.utils.constants.Urls
import com.manimarank.spell4wiki.utils.makeGone
import com.manimarank.spell4wiki.utils.makeVisible

class LanguageSelectionFragment(private val mActivity: Activity) : BottomSheetDialogFragment() {
    private lateinit var pref: PrefManager
    private var callback: OnLanguageSelectionListener? = null
    private var wikiLanguageList = ArrayList<WikiLang>()
    private var adapter: LanguageAdapter? = null

    @EnumListMode
    private var listMode = 0
    private var preSelectedLanguageCode: String? = null

    @JvmOverloads
    fun init(callback: OnLanguageSelectionListener?, @EnumListMode mode: Int, preSelectedLanguageCode: String? = null) {
        this.callback = callback
        listMode = mode
        this.preSelectedLanguageCode = preSelectedLanguageCode
        isCancelable = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        pref = PrefManager(context)
        if (TextUtils.isEmpty(preSelectedLanguageCode)) preSelectedLanguageCode = existingLanguageCode
        val dialog = BottomSheetDialog(mActivity, R.style.AppTheme)
        dialog.setContentView(R.layout.bottom_sheet_language_selection)
        val txtTitle = dialog.findViewById<TextView>(R.id.text_select_lang_title)
        if (!TextUtils.isEmpty(subTitleInfo) && txtTitle != null) {
            txtTitle.makeVisible()
            txtTitle.text = subTitleInfo
        }
        val recyclerView = dialog.findViewById<RecyclerView>(R.id.recyclerView)
        val btnClose = dialog.findViewById<ImageView>(R.id.btn_close)
        val searchView = dialog.findViewById<SearchView>(R.id.search_view)
        val layoutAddLanguage = dialog.findViewById<View>(R.id.layoutAddLanguage)
        val btnAddMyLanguage = dialog.findViewById<Button>(R.id.btnAddMyLanguage)
        val dbHelper = DBHelper.getInstance(requireContext())

        /*
         * Check Wiktionary mode or not
         * If wiktionary mode show all languages
         * If Contribution mode show only language have "title_words_without_audio" key-value
         * "title_words_without_audio" - category of words without audio in wiktionary
         */
        wikiLanguageList.clear()
        if (listMode == ListMode.SPELL_4_WIKI) {
            dbHelper.appDatabase.wikiLangDao?.wikiLanguageListForWordsWithoutAudio?.filterNotNull()?.forEach { item -> wikiLanguageList.add(item) }
            layoutAddLanguage.makeVisible()
            btnAddMyLanguage?.setOnClickListener {
                if (isAdded && isConnected(requireContext())) {
                    openUrlInBrowser(requireContext(), Urls.FORM_ADD_MY_LANGUAGE)
                } else showNormal(btnAddMyLanguage, getString(R.string.check_internet))
            }
        } else {
            dbHelper.appDatabase.wikiLangDao?.wikiLanguageList?.filterNotNull()?.forEach { item -> wikiLanguageList.add(item) }
            layoutAddLanguage.makeGone()
        }
        val languageSelectionListener = object : OnLanguageSelectionListener {
            override fun onCallBackListener(langCode: String?) {
                when (listMode) {

                    ListMode.WIKTIONARY -> pref.languageCodeWiktionary = langCode
                    ListMode.SPELL_4_WIKI_CONTRIBUTION_LANGUAGE -> pref.languageCodeSpell4Wikicontributelang = langCode

                    ListMode.TEMP -> {
                    }
                }
                callback?.onCallBackListener(langCode)
                dismiss()
            }
        }
        adapter = LanguageAdapter(wikiLanguageList, languageSelectionListener, preSelectedLanguageCode)
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
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter?.filter?.filter(newText)
                return false
            }
        })

        return dialog
    }

    private val existingLanguageCode: String?
        get() = when (listMode) {

            ListMode.WIKTIONARY -> pref.languageCodeWiktionary
            ListMode.SPELL_4_WIKI_CONTRIBUTION_LANGUAGE -> pref.languageCodeSpell4Wikicontributelang

            ListMode.TEMP -> null
            else -> null
        }
    private val subTitleInfo: String?
        get() {
            var info: String? = null
            when (listMode) {

                                ListMode.SPELL_4_WIKI_CONTRIBUTION_LANGUAGE -> info = getString(R.string.Spell_4_wiktionary_Contribution)

                ListMode.TEMP -> info = getString(R.string.temporary)
            }
            if (info != null) {
                info = String.format(getString(R.string.language_for_note), info)
            }
            return info
        }

    fun show(fragmentManager: FragmentManager) {
        try {
            if (fragmentManager.findFragmentByTag(tagValue) != null) return
        } catch (ignore: Exception) {
        }
        show(fragmentManager, tagValue)
    }

    private val tagValue: String
        get() = "LANGUAGE_FRAGMENT"
}