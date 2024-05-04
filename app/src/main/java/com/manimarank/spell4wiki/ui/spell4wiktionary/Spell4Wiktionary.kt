package com.manimarank.spell4wiki.ui.spell4wiktionary

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.apis.ApiClient.getWiktionaryApi
import com.manimarank.spell4wiki.data.apis.ApiInterface
import com.manimarank.spell4wiki.data.db.DBHelper
import com.manimarank.spell4wiki.data.db.dao.WikiLangDao
import com.manimarank.spell4wiki.data.db.dao.WordsHaveAudioDao
import com.manimarank.spell4wiki.data.db.entities.WordsHaveAudio
import com.manimarank.spell4wiki.data.model.WikiWordsWithoutAudio
import com.manimarank.spell4wiki.data.prefs.AppPref.INSTANCE.getFetchBy
import com.manimarank.spell4wiki.data.prefs.AppPref.INSTANCE.getFetchDir
import com.manimarank.spell4wiki.data.prefs.AppPref.INSTANCE.getFetchLimit
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.data.prefs.ShowCasePref
import com.manimarank.spell4wiki.data.prefs.ShowCasePref.isNotShowed
import com.manimarank.spell4wiki.data.prefs.ShowCasePref.showed
import com.manimarank.spell4wiki.ui.categoryselector.CategorySelectionFragment
import com.manimarank.spell4wiki.ui.common.BaseActivity
import com.manimarank.spell4wiki.ui.custom.EndlessRecyclerView.EndlessListener
import com.manimarank.spell4wiki.ui.dialogs.CommonDialog.openRunFilterInfoDialog
import com.manimarank.spell4wiki.ui.dialogs.showConfirmBackDialog
import com.manimarank.spell4wiki.ui.languageselector.LanguageSelectionFragment
import com.manimarank.spell4wiki.ui.listerners.OnCategorySelectionListener
import com.manimarank.spell4wiki.ui.listerners.OnLanguageSelectionListener
import com.manimarank.spell4wiki.utils.GeneralUtils
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import com.manimarank.spell4wiki.utils.SnackBarUtils
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.constants.ListMode
import com.manimarank.spell4wiki.utils.extensions.makeNullIfEmpty
import com.manimarank.spell4wiki.utils.makeGone
import com.manimarank.spell4wiki.utils.makeInVisible
import com.manimarank.spell4wiki.utils.makeVisible
import kotlinx.android.synthetic.main.activity_spell_4_wiktionary.btnAddCategory
import kotlinx.android.synthetic.main.activity_spell_4_wiktionary.recyclerView
import kotlinx.android.synthetic.main.activity_spell_4_wiktionary.refreshLayout
import kotlinx.android.synthetic.main.activity_spell_4_wiktionary.spinnerCategory
import kotlinx.android.synthetic.main.empty_state_ui.layoutEmpty
import kotlinx.android.synthetic.main.layout_run_filter_action.btnRunFilter
import kotlinx.android.synthetic.main.layout_run_filter_action.btnRunFilterInfo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetSequence
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal
import java.util.Locale
import java.util.concurrent.TimeUnit


class Spell4Wiktionary : BaseActivity(), EndlessListener {
    private var wikiLangDao: WikiLangDao? = null
    private var wordsHaveAudioDao: WordsHaveAudioDao? = null
    private var wordsListAlreadyHaveAudio: MutableList<String> = ArrayList()

    // Views
    private lateinit var adapter: EndlessRecyclerAdapter
    private var nextOffsetObj: String? = null
    private lateinit var pref: PrefManager
    private var languageCode: String? = ""
    private lateinit var snackBar: Snackbar

    // For track multiple api calls based on continues time, success but no valid data and fail retry
    private var apiResultTime = 0L
    private var apiRetryCount = 0
    private var apiFailRetryCount = 0
    private var wiktionaryTitleOfWordsWithoutAudio: String? = null

    private val filterRemovedWords = ArrayList<String>()

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spell_4_wiktionary)
        pref = PrefManager(applicationContext)
        languageCode = pref.languageCodeSpell4WikiAll
        init()
        loadDataFromServer()
    }

    /**
     * Init views
     */
    private fun init() {
        wikiLangDao = DBHelper.getInstance(applicationContext).appDatabase.wikiLangDao

        // Title & Sub title
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val wikiLang = wikiLangDao?.getWikiLanguageWithCode(languageCode)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.spell4wiktionary)
        supportActionBar?.subtitle = GeneralUtils.getLanguageInfo(applicationContext, wikiLang)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        snackBar = Snackbar.make(recyclerView, getString(R.string.record_fetch_fail), Snackbar.LENGTH_LONG)
        recyclerView.setHasFixedSize(true)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setupFilterWordOption()

        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        adapter = EndlessRecyclerAdapter(this@Spell4Wiktionary, ArrayList(), ListMode.SPELL_4_WIKI)
        recyclerView.setAdapter(adapter, layoutManager)
        recyclerView.setListener(this)
        recyclerView.makeVisible()
        refreshLayout.setOnRefreshListener { loadDataFromServer() }
        loadCategoriesData()
    }

    private fun setupFilterWordOption() {
        val dialog = Dialog(this, R.style.RecordAudioDialogTheme)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.loading_file_availability)
        val txtInfo = dialog.findViewById<TextView>(R.id.txtFileName)
        val txtProgress = dialog.findViewById<TextView>(R.id.txtProgress)
        txtProgress.makeVisible()
        txtInfo.text = getFilterText("")
        dialog.setCancelable(false)

        var itemList: List<String> = ArrayList()
        filterRemovedWords.clear()

        viewModel.progressForFilter.observe(this) { index ->
            // val progress = ((index+1) * 100) / itemList.size
            txtProgress.text = ("${index + 1}/${itemList.size}")
            txtInfo.text = getFilterText(itemList[index])
        }

        viewModel.wordAlreadyHaveAudio.observe(this) { word ->
            updateList(word)
        }

        viewModel.wordsWithoutAudioList.observe(this) { list ->
            val diff = itemList.size - list.size
            SnackBarUtils.showLong(
                recyclerView,
                if (diff > 0) getString(
                    R.string.words_filter_success,
                    diff
                ) else getString(R.string.no_words_filtered)
            )
            filterRemovedWords.addAll(list)
            dialog.dismiss()
        }

        btnRunFilter.setOnClickListener {
            val runFilterNoOfWordsCheckCount = pref.runFilterNumberOfWordsToCheck ?: AppConstants.RUN_FILTER_NO_OF_WORDS_CHECK_COUNT
            itemList = adapter.getList().filter { filterRemovedWords.contains(it).not() }.take(runFilterNoOfWordsCheckCount)
            if (itemList.isNotEmpty() && languageCode != null) {
                txtProgress.text = ("0/${itemList.size}")
                dialog.show()
                viewModel.checkWordsAvailability(itemList, languageCode!!, runFilterNoOfWordsCheckCount)
            } else
                SnackBarUtils.showLong(recyclerView, getString(R.string.no_words_scroll_to_get_new_words))
        }

        btnRunFilterInfo.setOnClickListener { this.openRunFilterInfoDialog() }
    }

    private fun getFilterText(word: String?): String {
        val fileName = "$languageCode-$word.ogg"
        return String.format(getString(R.string.checking_file_availability), fileName)
    }

    private fun resetApiResultTime() {
        apiResultTime = System.currentTimeMillis()
        apiRetryCount = 0
    }

    /**
     * Getting words from wiktionary without audio
     */
    private fun loadDataFromServer() {
        if (!isFinishing && !isDestroyed) {
            if (isConnected(applicationContext)) {
                /*
                 * Check if user reach the end of api data.
                 * Show empty UI If no data exist. Otherwise show message.
                 */
                if (recyclerView.isLastPage) {
                    searchFailed(getString(R.string.no_more_data_found))
                    return
                }

                /*
                 * Set basic information on both very first time and after language change
                 */
                if (nextOffsetObj == null) {
                    if (!refreshLayout.isRefreshing)
                        refreshLayout.isRefreshing = true
                    resetApiResultTime()
                    apiFailRetryCount = 0
                    recyclerView?.reset()
                    val wikiLang = wikiLangDao?.getWikiLanguageWithCode(languageCode)
                    if (wikiLang != null && !TextUtils.isEmpty(wikiLang.titleOfWordsWithoutAudio) && wiktionaryTitleOfWordsWithoutAudio?.makeNullIfEmpty() == null)
                        wiktionaryTitleOfWordsWithoutAudio = wikiLang.titleOfWordsWithoutAudio

                    wordsListAlreadyHaveAudio.clear()
                    wordsHaveAudioDao?.getWordsAlreadyHaveAudioByLanguage(languageCode)?.forEach { word ->
                        wordsListAlreadyHaveAudio.add(word)
                    }
                }


                /*
                 * For Avoiding multiple api calls
                 * Time limit for loop api call max 30 to 40 secs.
                 * Checked cases
                 *      1. Words count below the view port(approximate 15) & apiRetryCount reach 3 and more -> Lot of Words already have audios
                 *      2. apiRetryCount only 3 and above -> After getting some data may fail, Some user recorded words between records limits
                 *      3. apiFailRetryCount only 3 and above -> Continuous api fail
                 */
                val duration = System.currentTimeMillis() - apiResultTime
                if (TimeUnit.MILLISECONDS.toSeconds(duration) > AppConstants.API_LOOP_MAX_SECS || apiFailRetryCount >= AppConstants.API_MAX_FAIL_RETRY) {
                    if (adapter.itemCount < AppConstants.API_LOOP_MINIMUM_COUNT_IN_LIST && apiRetryCount >= AppConstants.API_MAX_RETRY || apiRetryCount >= AppConstants.API_MAX_RETRY || apiFailRetryCount >= AppConstants.API_MAX_FAIL_RETRY) {
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle(R.string.do_you_want_continue)
                        val isFail = apiFailRetryCount >= AppConstants.API_MAX_FAIL_RETRY
                        builder.setMessage(if (isFail) R.string.spell4wiktionary_load_more_failed_confirmation else R.string.spell4wiktionary_load_more_confirmation)
                        builder.setCancelable(false)
                        builder.setPositiveButton(R.string.yes_continue) { _: DialogInterface?, _: Int ->
                            recyclerView.enableLoadMore()
                            resetApiResultTime()
                            apiFailRetryCount = 0
                            loadDataFromServer()
                        }
                        builder.setNegativeButton(R.string.later) { dialog: DialogInterface, _: Int ->
                            recyclerView.disableLoadMore()
                            dialog.dismiss()
                        }
                        val dialog = builder.create()
                        recyclerView.disableLoadMore()
                        if (refreshLayout.isRefreshing) refreshLayout.isRefreshing = false
                        dialog.show()
                        return
                    }
                }


                // DB Clear or Sync Issue
                if (wiktionaryTitleOfWordsWithoutAudio == null) {
                    //wiktionaryTitleOfWordsWithoutAudio = AppConstants.DEFAULT_TITLE_FOR_WITHOUT_AUDIO
                    //languageCode = AppConstants.DEFAULT_LANGUAGE_CODE
                    //invalidateOptionsMenu()
                    //pref.languageCodeSpell4Wiki = languageCode
                }

                //wiktionaryTitleOfWordsWithoutAudio = "பகுப்பு:அரிசமய. உள்ள பக்கங்கள்"; // https://ta.wiktionary.org/wiki/பகுப்பு:சென்னைப்_பேரகரமுதலியின்_சொற்சுருக்கப்_பகுப்புகள்-தமிழ்
                val api = getWiktionaryApi(applicationContext, languageCode ?: AppConstants.DEFAULT_LANGUAGE_CODE).create(ApiInterface::class.java)
                val call = api.fetchUnAudioRecords(
                    wiktionaryTitleOfWordsWithoutAudio ?: "null",
                    nextOffsetObj,
                    getFetchLimit(),
                    getFetchBy(),
                    getFetchDir()
                )
                call.enqueue(object : Callback<WikiWordsWithoutAudio?> {
                    override fun onResponse(
                        call: Call<WikiWordsWithoutAudio?>,
                        response: Response<WikiWordsWithoutAudio?>
                    ) {
                        if (response.isSuccessful && response.body() != null && response.body()?.error == null) {
                            processSearchResultAudio(response.body())
                        } else
                            searchFailed(response?.body()?.error?.info ?: getString(R.string.something_went_wrong))
                    }

                    override fun onFailure(call: Call<WikiWordsWithoutAudio?>, t: Throwable) {
                        searchFailed(getString(R.string.something_went_wrong))
                    }
                })
            } else {
                searchFailed(getString(R.string.check_internet))
            }
        }
    }

    private fun processSearchResultAudio(wikiWordsWithoutAudio: WikiWordsWithoutAudio?) {
        if (!isDestroyed && !isFinishing) {
            val titleList = ArrayList<String>()
            if (recyclerView.visibility != View.VISIBLE) recyclerView.makeVisible()
            if (layoutEmpty.visibility == View.VISIBLE) layoutEmpty.makeGone()
            if (snackBar.isShown) snackBar.dismiss()
            if (wikiWordsWithoutAudio != null) {
                nextOffsetObj = if (wikiWordsWithoutAudio.offset?.nextOffset != null) {
                        wikiWordsWithoutAudio.offset?.nextOffset
                    } else {
                        recyclerView.setLastPage()
                        null
                    }

                wikiWordsWithoutAudio.query?.wikiTitleList?.forEach { wt ->
                    if (wt.title != null)
                        titleList.add(wt.title!!)
                }
                if (titleList.isNotEmpty()) {
                    apiFailRetryCount = 0
                    // Remove already recorded words
                    titleList.removeAll(wordsListAlreadyHaveAudio)
                    if (titleList.size > 0) {
                        if (refreshLayout.isRefreshing)
                            refreshLayout.isRefreshing = false
                        resetApiResultTime()
                        adapter.setWordsHaveAudioList(wordsListAlreadyHaveAudio)
                        recyclerView.addNewData(titleList)
                        if (isNotShowed(ShowCasePref.LIST_ITEM_SPELL_4_WIKI) || isNotShowed(ShowCasePref.SPELL_4_WIKI_PAGE))
                            Handler().post { callShowCaseUI() }
                    } else {
                        if (!refreshLayout.isRefreshing)
                            refreshLayout.isRefreshing = true
                        apiRetryCount += 1
                        loadDataFromServer()
                    }
                } else {
                    searchFailed(getString(R.string.something_went_wrong))
                }
            } else searchFailed(getString(R.string.something_went_wrong))
        }
    }

    private fun searchFailed(msg: String) {
        if (!isDestroyed && !isFinishing) {
            resetApiResultTime()
            if (isConnected(applicationContext)) {
                apiFailRetryCount += 1
                snackBar.setText(msg)
                if (recyclerView != null && adapter.itemCount < 1) {
                    recyclerView.makeInVisible()
                    layoutEmpty.makeVisible()
                }
            } else {
                snackBar.setText(getString(if (adapter.itemCount < 15) R.string.record_fetch_fail else R.string.check_internet))
            }
            if (recyclerView != null) recyclerView.removeLoader()
            if (refreshLayout.isRefreshing) refreshLayout.isRefreshing = false
            if (!snackBar.isShown) snackBar.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.spell4wiki_view_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            callBackPress()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadLanguages() {
        val callback = object : OnLanguageSelectionListener {
            override fun onCallBackListener(langCode: String?) {
                if (languageCode != langCode) {
                    languageCode = langCode
                    supportActionBar?.subtitle = GeneralUtils.getLanguageInfo(applicationContext, wikiLangDao?.getWikiLanguageWithCode(langCode))
                    invalidateOptionsMenu()
                    recyclerView.reset()
                    nextOffsetObj = null
                    loadCategoriesData()
                    loadDataFromServer()
                }
            }
        }
        try {
            val languageSelectionFragment = LanguageSelectionFragment(this)
            languageSelectionFragment.init(callback, ListMode.SPELL_4_WIKI_ALL)

            languageSelectionFragment.show(supportFragmentManager)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupLanguageSelectorMenuItem(menu: Menu) {
        val item = menu.findItem(R.id.menu_lang_selector)
        item.isVisible = true
        val rootView = item.actionView
        val selectedLang = rootView.findViewById<TextView>(R.id.txtSelectedLanguage)
        selectedLang.text = languageCode?.toUpperCase(Locale.ROOT) ?: ""
        rootView.setOnClickListener {
            if (isNotShowed(ShowCasePref.SPELL_4_WIKI_PAGE)) return@setOnClickListener
            loadLanguages()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        setupLanguageSelectorMenuItem(menu)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun loadData(): Boolean {
        /*
         * Triggered only when new data needs to be appended to the list
         * Return true if loading is in progress, false if there is no more data to load
         */
        return if (nextOffsetObj != null) {
            loadDataFromServer()
            true
        } else false
    }

    override fun loadFail() {
        if (!isConnected(applicationContext))
            searchFailed(getString(R.string.check_internet))
        else if (recyclerView != null && recyclerView.isLastPage)
            searchFailed(getString(R.string.no_more_data_found))
    }

    fun updateList(word: String?) {
        if (!isDestroyed && !isFinishing) {
            wordsHaveAudioDao?.insert(WordsHaveAudio(word, languageCode))
            adapter.addWordInWordsHaveAudioList(word)
            adapter.remove(word)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (!isDestroyed && !isFinishing) {
            if (requestCode == AppConstants.RC_UPLOAD_DIALOG) {
                if (data != null && data.hasExtra(AppConstants.WORD)) {
                    adapter.addWordInWordsHaveAudioList(data.getStringExtra(AppConstants.WORD))
                    adapter.remove(data.getStringExtra(AppConstants.WORD))
                }
            }
        }
    }

    private fun callShowCaseUI() {
        if (!isFinishing && !isDestroyed) {
            if (isNotShowed(ShowCasePref.LIST_ITEM_SPELL_4_WIKI) || isNotShowed(ShowCasePref.SPELL_4_WIKI_PAGE)) {
                val sequence = MaterialTapTargetSequence().setSequenceCompleteListener {
                    showed(ShowCasePref.LIST_ITEM_SPELL_4_WIKI)
                    showed(ShowCasePref.SPELL_4_WIKI_PAGE)
                }
                if (isNotShowed(ShowCasePref.SPELL_4_WIKI_PAGE)) {
                    sequence.addPrompt(
                        promptBuilder
                            .setTarget(R.id.layoutSelectLanguage)
                            .setPrimaryText(R.string.sc_t_spell4wiki_page_language)
                            .setSecondaryText(
                                String.format(
                                    getString(R.string.sc_d_spell4wiki_page_language),
                                    wikiLangDao?.getWikiLanguageWithCode(languageCode)?.name ?: ""
                                )
                            )
                    )
                }
                if (isNotShowed(ShowCasePref.LIST_ITEM_SPELL_4_WIKI) && recyclerView != null && recyclerView.getChildAt(
                        0
                    ) != null
                ) {
                    sequence.addPrompt(
                        promptBuilder
                            .setTarget(recyclerView.getChildAt(0))
                            .setPrimaryText(R.string.sc_t_spell4wiki_list_item)
                            .setSecondaryText(R.string.sc_d_spell4wiki_list_item)
                    )
                }
                sequence.show()
            }
        }
    }

    private val promptBuilder: MaterialTapTargetPrompt.Builder
        get() = MaterialTapTargetPrompt.Builder(this@Spell4Wiktionary)
            .setPromptFocal(RectanglePromptFocal())
            .setAnimationInterpolator(FastOutSlowInInterpolator())
            .setFocalPadding(R.dimen.show_case_focal_padding)

    override fun onBackPressed() {
        callBackPress()
    }

    private fun callBackPress() {
        if (adapter.itemCount > 0) {
            this.showConfirmBackDialog { super.onBackPressed() }
        } else super.onBackPressed()
    }

    private fun loadCategoriesData() {
        val categoryTitleFromApi: String? = wikiLangDao?.getWikiLanguageWithCode(languageCode)?.titleOfWordsWithoutAudio
        var categoryDataList = pref.getWordsCategoryList(languageCode)
        if (categoryTitleFromApi?.makeNullIfEmpty() != null && !categoryDataList.contains(categoryTitleFromApi))
            setUpCategoryData(categoryTitleFromApi)
        categoryDataList = pref.getWordsCategoryList(languageCode)
        setupCategorySpinnerData(categoryDataList)
        btnAddCategory.setOnClickListener {
            showCategorySearchSelectionScreen()
        }
    }

    private fun setupCategorySpinnerData(categoryDataList: MutableList<String>) {
        val spinnerAdapter = ArrayAdapter(applicationContext, R.layout.item_category, categoryDataList.toTypedArray())
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = spinnerAdapter

        val selectedPos = categoryDataList.indexOfFirst { it == pref.getSelectedWordsCategory(languageCode) }

        spinnerCategory.setSelection(selectedPos)

        wiktionaryTitleOfWordsWithoutAudio = categoryDataList.elementAtOrNull(selectedPos)
        if (categoryDataList.isNotEmpty()) {
            spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    id: Long
                ) {
                    pref.setSelectedWordsCategory(languageCode, parent?.getItemAtPosition(pos)?.toString())
                    wiktionaryTitleOfWordsWithoutAudio = pref.getSelectedWordsCategory(languageCode)
                    nextOffsetObj = null
                    recyclerView?.reset()
                    loadDataFromServer()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        }
    }

    private fun showCategorySearchSelectionScreen() {

        val categorySelectionListener = object : OnCategorySelectionListener {
            override fun onCallBackListener(category: String?) {
                setUpCategoryData(category, refreshSpinner = true)
            }
        }

        try {
            val categorySelectionFragment = CategorySelectionFragment(this)
            categorySelectionFragment.init(categorySelectionListener, ListMode.SPELL_4_WIKI_ALL)
            categorySelectionFragment.show(supportFragmentManager)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun setUpCategoryData(category: String?, refreshSpinner: Boolean = false) {
        if (category?.makeNullIfEmpty() != null) {
            val catList: MutableList<String> = pref.getWordsCategoryList(languageCode)
            catList.add(category)
            pref.setSelectedWordsCategory(languageCode, category)
            pref.setWordsCategoryList(languageCode, catList.toMutableSet())
            if (refreshSpinner) {
               setupCategorySpinnerData(catList)
            }
        }
    }
}