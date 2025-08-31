package com.manimarank.spell4wiki.ui.spell4wiktionary

import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.apis.ApiClient.getWiktionaryApi
import com.manimarank.spell4wiki.data.apis.ApiInterface
import com.manimarank.spell4wiki.data.db.DBHelper
import com.manimarank.spell4wiki.data.db.dao.WikiLangDao
import com.manimarank.spell4wiki.data.model.WikiSearchWords
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.data.prefs.ShowCasePref
import com.manimarank.spell4wiki.data.prefs.ShowCasePref.isNotShowed
import com.manimarank.spell4wiki.data.prefs.ShowCasePref.showed
import com.manimarank.spell4wiki.databinding.ActivityWiktionarySearchBinding
import com.manimarank.spell4wiki.ui.common.BaseActivity
import com.manimarank.spell4wiki.ui.custom.EndlessRecyclerView.EndlessListener
import com.manimarank.spell4wiki.ui.languageselector.LanguageSelectionFragment
import com.manimarank.spell4wiki.ui.listerners.OnLanguageSelectionListener
import com.manimarank.spell4wiki.utils.EdgeToEdgeUtils.setupEdgeToEdgeWithToolbar
import com.manimarank.spell4wiki.utils.GeneralUtils
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.constants.ListMode
import com.manimarank.spell4wiki.utils.makeGone
import com.manimarank.spell4wiki.utils.makeInVisible
import com.manimarank.spell4wiki.utils.makeVisible
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetSequence
import java.util.Locale

class WiktionarySearchActivity : BaseActivity(), EndlessListener {

    private lateinit var binding: ActivityWiktionarySearchBinding
    private var wikiLangDao: WikiLangDao? = null
    private var adapter: EndlessRecyclerAdapter? = null
    private lateinit var snackBar: Snackbar
    private var queryString: String? = null
    private var nextOffset: Int? = null
    private lateinit var api: ApiInterface
    private var languageCode: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWiktionarySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        val pref = PrefManager(this@WiktionarySearchActivity)
        languageCode = pref.languageCodeSpell4WikiAll
        api = getWiktionaryApi(applicationContext, languageCode ?: AppConstants.DEFAULT_LANGUAGE_CODE).create(ApiInterface::class.java)

        // Views
        snackBar = Snackbar.make(binding.searchBar, getString(R.string.something_went_wrong), Snackbar.LENGTH_LONG)
        binding.searchBar.setIconifiedByDefault(false)
        binding.searchBar.queryHint = resources.getString(R.string.search)
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                submitQuery(s)
                return true
            }

            override fun onQueryTextChange(s: String): Boolean {
                return false
            }
        })
        binding.recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        binding.recyclerView.layoutManager = layoutManager
        adapter = EndlessRecyclerAdapter(this, ArrayList(), ListMode.WIKTIONARY)
        binding.recyclerView.setAdapter(adapter, layoutManager)
        binding.recyclerView.setListener(this)
        binding.recyclerView.makeInVisible()

        wikiLangDao = DBHelper.getInstance(applicationContext).appDatabase.wikiLangDao

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val wikiLang = wikiLangDao?.getWikiLanguageWithCode(languageCode)

        // Setup proper status bar handling and edge-to-edge
        setupEdgeToEdgeWithToolbar(
            rootView = binding.root,
            toolbar = toolbar
        )

        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.wiktionary)
        supportActionBar?.subtitle = GeneralUtils.getLanguageInfo(applicationContext, wikiLang)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        if (intent.extras?.containsKey(AppConstants.SEARCH_TEXT) == true) {
            val text = intent.extras?.getString(AppConstants.SEARCH_TEXT)
            binding.searchBar.setQuery(text, true)
        }

    }

    private fun submitQuery(s: String) {
        if (!isDestroyed && !isFinishing) {
            queryString = s
            nextOffset = 0
            binding.txtNotFound.makeGone()
            binding.root.findViewById<View>(R.id.layoutProgress).makeVisible()
            binding.recyclerView.makeVisible()
            binding.recyclerView.reset()
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
                    supportActionBar?.subtitle = GeneralUtils.getLanguageInfo(applicationContext, wikiLangDao?.getWikiLanguageWithCode(langCode))
                    invalidateOptionsMenu()
                    api = getWiktionaryApi(applicationContext, languageCode!!).create(ApiInterface::class.java)
                    queryString?.let { qs -> submitQuery(qs) }
                }
            }
        }
        val languageSelectionFragment = LanguageSelectionFragment(this)
        languageSelectionFragment.init(callback, ListMode.SPELL_4_WIKI_ALL)
        languageSelectionFragment.show(supportFragmentManager)
    }

    private fun setupLanguageSelectorMenuItem(menu: Menu) {
        val item = menu.findItem(R.id.menu_lang_selector)
        item.isVisible = true
        val rootView = item.actionView ?: return
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
                        } else {
                            searchFailed(getString(R.string.something_went_wrong))
                        }
                    }

                    override fun onFailure(call: Call<WikiSearchWords?>, t: Throwable) {
                        error("Search network failure: ${t.message}")
                        t.printStackTrace()
                        searchFailed(getString(R.string.something_went_wrong_try_again))
                    }
                })
            } else {
                searchFailed(getString(R.string.check_internet))
            }
        }
    }

    private fun searchFailed(msg: String) {
        if (!isDestroyed && !isFinishing) {
            if (isConnected(applicationContext)) {
                snackBar.setText(msg)
                val layoutProgress = binding.root.findViewById<View>(R.id.layoutProgress)
                if (layoutProgress.visibility == View.VISIBLE)
                    layoutProgress.makeGone()
                if (adapter?.itemCount ?: 0 < 1) {
                    binding.recyclerView.makeInVisible()
                    binding.txtNotFound.text = getString(R.string.result_not_found)
                    binding.txtNotFound.makeVisible()
                } else binding.txtNotFound.makeGone()
            } else snackBar.setText(getString(R.string.check_internet))
            if (msg != getString(R.string.result_not_found) && !snackBar.isShown)
                snackBar.show()
        }
    }

    private fun processSearchResult(wikiSearchWords: WikiSearchWords?) {
        if (!isDestroyed && !isFinishing) {
            val titleList: MutableList<String> = ArrayList()
            val layoutProgress = binding.root.findViewById<View>(R.id.layoutProgress)
            if (layoutProgress.visibility == View.VISIBLE) layoutProgress.makeGone()
            if (snackBar.isShown) snackBar.dismiss()
            if (wikiSearchWords != null) {
                nextOffset = if (wikiSearchWords.offset?.nextOffset != null) {
                        wikiSearchWords.offset?.nextOffset
                    } else {
                        binding.recyclerView.setLastPage()
                        null
                    }

                wikiSearchWords.query?.wikiTitleList?.filter { it.title != null }?.forEach { wikiWord ->
                    titleList.add(wikiWord.title!!)
                }
                if (titleList.isEmpty()) {
                    searchFailed(getString(R.string.result_not_found))
                } else {
                    binding.recyclerView.addNewData(titleList)
                }
            } else {
                // wikiSearchWords is null
                error("Search result data is null")
                searchFailed(getString(R.string.result_not_found))
            }
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
        else if (binding.recyclerView.isLastPage && adapter?.itemCount ?: 0 > 10)
            searchFailed(getString(R.string.no_more_data_found))
    }

    private fun callShowCaseUI() {
        if (!isFinishing && !isDestroyed && isNotShowed(ShowCasePref.WIKTIONARY_PAGE)) {
            val sequence = MaterialTapTargetSequence().setSequenceCompleteListener { showed(ShowCasePref.WIKTIONARY_PAGE) }
            sequence.addPrompt(
                GeneralUtils.getPromptBuilder(this)
                    .setTarget(R.id.layoutSelectLanguage)
                    .setPrimaryText(R.string.sc_t_wiktionary_page_language)
                    .setSecondaryText(String.format(getString(R.string.sc_d_wiktionary_page_language), wikiLangDao?.getWikiLanguageWithCode(languageCode)?.name ?: ""))
            )
            sequence.show()
        }
    }
}