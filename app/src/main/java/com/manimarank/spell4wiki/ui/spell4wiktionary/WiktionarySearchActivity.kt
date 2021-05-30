package com.manimarank.spell4wiki.ui.spell4wiktionary

import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.apis.ApiClient.getWiktionaryApi
import com.manimarank.spell4wiki.data.apis.ApiInterface
import com.manimarank.spell4wiki.data.db.DBHelper
import com.manimarank.spell4wiki.data.model.WikiSearchWords
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.data.prefs.ShowCasePref
import com.manimarank.spell4wiki.data.prefs.ShowCasePref.isNotShowed
import com.manimarank.spell4wiki.data.prefs.ShowCasePref.showed
import com.manimarank.spell4wiki.ui.common.BaseActivity
import com.manimarank.spell4wiki.ui.custom.EndlessRecyclerView.EndlessListener
import com.manimarank.spell4wiki.ui.languageselector.LanguageSelectionFragment
import com.manimarank.spell4wiki.ui.listerners.OnLanguageSelectionListener
import com.manimarank.spell4wiki.utils.GeneralUtils
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.constants.ListMode
import com.manimarank.spell4wiki.utils.makeGone
import com.manimarank.spell4wiki.utils.makeInVisible
import com.manimarank.spell4wiki.utils.makeVisible
import kotlinx.android.synthetic.main.activity_wiktionary_search.*
import kotlinx.android.synthetic.main.loading_info.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetSequence
import java.util.*

class WiktionarySearchActivity : BaseActivity(), EndlessListener {

    private var adapter: EndlessRecyclerAdapter? = null
    private lateinit var snackBar: Snackbar
    private var queryString: String? = null
    private var nextOffset: Int? = null
    private lateinit var api: ApiInterface
    private var languageCode: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wiktionary_search)
        init()
    }

    private fun init() {
        val pref = PrefManager(this@WiktionarySearchActivity)
        languageCode = pref.languageCodeWiktionary
        api = getWiktionaryApi(applicationContext, languageCode ?: AppConstants.DEFAULT_LANGUAGE_CODE).create(ApiInterface::class.java)

        // Views
        snackBar = Snackbar.make(search_bar, getString(R.string.something_went_wrong), Snackbar.LENGTH_LONG)
        search_bar.setIconifiedByDefault(false)
        search_bar.queryHint = resources.getString(R.string.search)
        search_bar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                submitQuery(s)
                return true
            }

            override fun onQueryTextChange(s: String): Boolean {
                return false
            }
        })
        recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        adapter = EndlessRecyclerAdapter(this, ArrayList(), ListMode.WIKTIONARY)
        recyclerView.setAdapter(adapter, layoutManager)
        recyclerView.setListener(this)
        recyclerView.makeInVisible()
        supportActionBar?.title = getString(R.string.wiktionary)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        if (intent.extras?.containsKey(AppConstants.SEARCH_TEXT) == true) {
            val text = intent.extras?.getString(AppConstants.SEARCH_TEXT)
            search_bar.setQuery(text, true)
        }

    }

    private fun submitQuery(s: String) {
        if (!isDestroyed && !isFinishing) {
            queryString = s
            nextOffset = 0
            txtNotFound.makeGone()
            layoutProgress.makeVisible()
            recyclerView.makeVisible()
            recyclerView.reset()
            search(queryString)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.spell4wiki_view_menu, menu)
        Handler().post { callShowCaseUI() }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        setupLanguageSelectorMenuItem(menu)
        return super.onPrepareOptionsMenu(menu)
    }

    private fun loadLanguages() {
        val callback = object : OnLanguageSelectionListener {
            override fun onCallBackListener(langCode: String?) {
                if (languageCode != langCode) {
                    languageCode = langCode
                    invalidateOptionsMenu()
                    api = getWiktionaryApi(applicationContext, languageCode!!).create(ApiInterface::class.java)
                    queryString?.let { qs -> submitQuery(qs) }
                }
            }
        }
        val languageSelectionFragment = LanguageSelectionFragment(this)
        languageSelectionFragment.init(callback, ListMode.WIKTIONARY)
        languageSelectionFragment.show(supportFragmentManager)
    }

    private fun setupLanguageSelectorMenuItem(menu: Menu) {
        val item = menu.findItem(R.id.menu_lang_selector)
        item.isVisible = true
        val rootView = item.actionView
        val selectedLang = rootView.findViewById<TextView>(R.id.txtSelectedLanguage)
        selectedLang.text = languageCode?.toUpperCase(Locale.ROOT) ?: ""
        rootView.setOnClickListener {
            if (isNotShowed(ShowCasePref.WIKTIONARY_PAGE))
                return@setOnClickListener
            loadLanguages()
        }
    }

    private fun search(query: String?) {
        if (!isDestroyed && !isFinishing) {
            if (isConnected(applicationContext)) {
                val call = api.fetchRecords(query, nextOffset)
                call.enqueue(object : Callback<WikiSearchWords?> {
                    override fun onResponse(
                        call: Call<WikiSearchWords?>,
                        response: Response<WikiSearchWords?>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            processSearchResult(response.body())
                        } else searchFailed(getString(R.string.something_went_wrong))
                    }

                    override fun onFailure(call: Call<WikiSearchWords?>, t: Throwable) {
                        searchFailed(getString(R.string.something_went_wrong))
                    }
                })
            } else searchFailed(getString(R.string.check_internet))
        }
    }

    private fun searchFailed(msg: String) {
        if (!isDestroyed && !isFinishing) {
            if (isConnected(applicationContext)) {
                snackBar.setText(msg)
                if (layoutProgress.visibility == View.VISIBLE)
                    layoutProgress.makeGone()
                if (recyclerView != null && adapter?.itemCount ?: 0 < 1) {
                    recyclerView.makeInVisible()
                    txtNotFound.text = getString(R.string.result_not_found)
                    txtNotFound.makeVisible()
                } else txtNotFound.makeGone()
            } else snackBar.setText(getString(R.string.check_internet))
            if (msg != getString(R.string.result_not_found) && !snackBar.isShown)
                snackBar.show()
        }
    }

    private fun processSearchResult(wikiSearchWords: WikiSearchWords?) {
        if (!isDestroyed && !isFinishing) {
            val titleList = ArrayList<String?>()
            if (layoutProgress.visibility == View.VISIBLE) layoutProgress.makeGone()
            if (snackBar.isShown) snackBar.dismiss()
            if (wikiSearchWords != null) {
                nextOffset = if (wikiSearchWords.offset?.nextOffset != null) {
                        wikiSearchWords.offset?.nextOffset
                    } else {
                        recyclerView.setLastPage()
                        null
                    }

                wikiSearchWords.query?.wikiTitleList?.filter { it.title != null }?.forEach { wikiWord ->
                    titleList.add(wikiWord.title)
                }
                if (titleList.isEmpty()) {
                    searchFailed(getString(R.string.result_not_found))
                } else {
                    recyclerView.addNewData(titleList)
                }
            } else searchFailed(getString(R.string.something_went_wrong))
        }
    }

    override fun loadData(): Boolean {
        /*
         * Triggered only when new data needs to be appended to the list
         * Return true if loading is in progress, false if there is no more data to load
         */
        return if (nextOffset != null) {
            search(queryString)
            true
        } else false
    }

    override fun loadFail() {
        if (!isConnected(applicationContext))
            searchFailed(getString(R.string.check_internet))
        else if (recyclerView != null && recyclerView.isLastPage && adapter?.itemCount ?: 0 > 10)
            searchFailed(getString(R.string.no_more_data_found))
    }

    private fun callShowCaseUI() {
        if (!isFinishing && !isDestroyed && isNotShowed(ShowCasePref.WIKTIONARY_PAGE)) {
            val wikiLangDao = DBHelper.getInstance(applicationContext).appDatabase.wikiLangDao
            val sequence = MaterialTapTargetSequence().setSequenceCompleteListener { showed(ShowCasePref.WIKTIONARY_PAGE) }
            sequence.addPrompt(
                GeneralUtils.getPromptBuilder(this)
                    .setTarget(R.id.layoutSelectLanguage)
                    .setPrimaryText(R.string.sc_t_wiktionary_page_language)
                    .setSecondaryText(String.format(getString(R.string.sc_d_wiktionary_page_language), wikiLangDao.getWikiLanguageWithCode(languageCode).name))
            )
            sequence.show()
        }
    }
}