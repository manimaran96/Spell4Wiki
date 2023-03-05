package com.manimarank.spell4wiki.ui.spell4wiktionary

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.db.DBHelper
import com.manimarank.spell4wiki.data.db.dao.WikiLangDao
import com.manimarank.spell4wiki.data.db.dao.WordsHaveAudioDao
import com.manimarank.spell4wiki.data.db.entities.WordsHaveAudio
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.data.prefs.ShowCasePref
import com.manimarank.spell4wiki.data.prefs.ShowCasePref.isNotShowed
import com.manimarank.spell4wiki.data.prefs.ShowCasePref.showed
import com.manimarank.spell4wiki.ui.common.BaseActivity
import com.manimarank.spell4wiki.ui.dialogs.CommonDialog.openRunFilterInfoDialog
import com.manimarank.spell4wiki.ui.dialogs.showConfirmBackDialog
import com.manimarank.spell4wiki.ui.languageselector.LanguageSelectionFragment
import com.manimarank.spell4wiki.ui.listerners.OnLanguageSelectionListener
import com.manimarank.spell4wiki.utils.GeneralUtils
import com.manimarank.spell4wiki.utils.GeneralUtils.getPromptBuilder
import com.manimarank.spell4wiki.utils.GeneralUtils.hideKeyboard
import com.manimarank.spell4wiki.utils.RealPathUtil.getRealPath
import com.manimarank.spell4wiki.utils.SnackBarUtils
import com.manimarank.spell4wiki.utils.SnackBarUtils.showLong
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.constants.ListMode
import com.manimarank.spell4wiki.utils.makeGone
import com.manimarank.spell4wiki.utils.makeVisible
import kotlinx.android.synthetic.main.activity_spell_4_wiktionary.*
import kotlinx.android.synthetic.main.activity_spell_4_wordlist.*
import kotlinx.android.synthetic.main.activity_spell_4_wordlist.recyclerView
import kotlinx.android.synthetic.main.empty_state_ui.*
import kotlinx.android.synthetic.main.layout_run_filter_action.*
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetSequence
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.*

class Spell4WordListActivity : BaseActivity() {
    private var adapter: EndlessRecyclerAdapter? = null
    private var languageCode: String? = ""
    private var wikiLangDao: WikiLangDao? = null
    private var wordsHaveAudioDao: WordsHaveAudioDao? = null
    private lateinit var pref: PrefManager
    private lateinit var viewModel: MainViewModel
    private val filterRemovedWords = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spell_4_wordlist)
        pref = PrefManager(applicationContext)
        languageCode = pref.languageCodeSpell4Wikicontributelang
        initUI()
    }

    private fun initUI() {
        wikiLangDao = DBHelper.getInstance(applicationContext).appDatabase.wikiLangDao
        wordsHaveAudioDao = DBHelper.getInstance(applicationContext).appDatabase.wordsHaveAudioDao

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val wikiLang = wikiLangDao?.getWikiLanguageWithCode(languageCode)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.spell4wordlist)
        supportActionBar?.subtitle = GeneralUtils.getLanguageInfo(applicationContext, wikiLang)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        btnSelectFile.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                editDocument()
            }
        }
        btnDirectContent.setOnClickListener { showDirectContentAlignMode() }
        btnDone.setOnClickListener {
            hideKeyboard(this@Spell4WordListActivity)
            if (!TextUtils.isEmpty(editFile.text)) {
                val items = getWordListFromString(editFile.text.toString())
                showWordsInRecordMode(items)
            } else showLong(editFile, getString(R.string.provide_valid_content))
        }
        layoutSelect.makeVisible()
        layoutEdit.makeGone()
        layoutList.makeGone()
        layoutEmpty.makeGone()

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        setupFilterWordOption()
    }

    /**
     * Open a file for writing and append some text to it.
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private fun editDocument() {
        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's
        // file browser.
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones).
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        // Filter to show only text files.
        intent.type = "text/plain"
        startActivityForResult(intent, AppConstants.RC_EDIT_REQUEST_CODE)
    }

    fun updateList(word: String?) {
        if (!isDestroyed && !isFinishing) {
            if (adapter != null) {
                wordsHaveAudioDao?.insert(WordsHaveAudio(word, languageCode))
                adapter?.addWordInWordsHaveAudioList(word)
                adapter?.remove(word)
                if (adapter?.itemCount == 0) showEmptyView()
            }
        }
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
            showLong(
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
            itemList = adapter?.getList()?.filter { filterRemovedWords.contains(it).not() }?.take(runFilterNoOfWordsCheckCount) ?: listOf()
            if (itemList.isNotEmpty() && languageCode != null) {
                txtProgress.text = ("0/${itemList.size}")
                dialog.show()
                viewModel.checkWordsAvailability(itemList, languageCode!!, runFilterNoOfWordsCheckCount)
            } else
                showLong(recyclerView, getString(R.string.no_words_scroll_to_get_new_words))
        }

        btnRunFilterInfo.setOnClickListener { this.openRunFilterInfoDialog() }
    }

    private fun getFilterText(word: String?): String {
        val fileName = "$languageCode-$word.ogg"
        return String.format(getString(R.string.checking_file_availability), fileName)
    }

    public override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        data: Intent?
    ) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.
        super.onActivityResult(requestCode, resultCode, data)
        if (!isDestroyed && !isFinishing) {
            if (requestCode == AppConstants.RC_EDIT_REQUEST_CODE && resultCode == RESULT_OK) {
                // The document selected by the user won't be returned in the intent.
                // Instead, a URI to that document will be contained in the return intent
                // provided to this method as a parameter.
                // Pull that URI using resultData.getData().
                if (data?.data != null) {
                    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                    val file = File(getRealPath(applicationContext, data.data!!))
                    openFileInAlignMode(file.absolutePath)
                }
            }
            if (requestCode == AppConstants.RC_UPLOAD_DIALOG) {
                if (data != null && data.hasExtra(AppConstants.WORD)) {
                    adapter?.addWordInWordsHaveAudioList(data.getStringExtra(AppConstants.WORD))
                    adapter?.remove(data.getStringExtra(AppConstants.WORD))
                    if (adapter?.itemCount == 0)
                        showEmptyView()
                }
            }
        }
    }

    private fun openFileInAlignMode(filePath: String) {
        layoutSelect.makeGone()
        layoutEdit.makeVisible()
        layoutList.makeGone()
        layoutEmpty.makeGone()
        txtFileInfo.text = getString(R.string.hint_select_file_next)
        editFile.setText(getContentFromFile(filePath))
    }

    private fun showDirectContentAlignMode() {
        layoutSelect.makeGone()
        layoutEdit.makeVisible()
        layoutList.makeGone()
        layoutEmpty.makeGone()
        txtFileInfo.text = getString(R.string.hint_direct_copy_next)
        editFile.setText("")
    }

    private fun showWordsInRecordMode(items: MutableList<String>) {
        val wordsAlreadyHaveAudio = wordsHaveAudioDao?.getWordsAlreadyHaveAudioByLanguage(languageCode)
        wordsAlreadyHaveAudio?.let {
            items.removeAll(wordsAlreadyHaveAudio)
        }
        if (items.size > 0) {
            layoutSelect.makeGone()
            layoutEdit.makeGone()
            layoutList.makeVisible()
            layoutEmpty.makeGone()
            recyclerView.setHasFixedSize(true)
            val layoutManager = LinearLayoutManager(this)
            recyclerView.layoutManager = layoutManager
            adapter = EndlessRecyclerAdapter(this, items, ListMode.SPELL_4_WORD_LIST)
            recyclerView.setAdapter(adapter, layoutManager)
            adapter?.setWordsHaveAudioList(wordsHaveAudioDao?.getWordsAlreadyHaveAudioByLanguage(languageCode)?.toMutableList())
            if (isNotShowed(ShowCasePref.LIST_ITEM_SPELL_4_WIKI))
                Handler().post { callShowCaseUI() }
        } else {
            showEmptyView()
        }
    }

    private fun showEmptyView() {
        layoutSelect.makeGone()
        layoutEdit.makeGone()
        layoutList.makeGone()
        layoutEmpty.makeVisible()
    }

    private fun getWordListFromString(data: String?): MutableList<String> {
        val list: MutableList<String> = ArrayList()
        try {
            if (data != null && data.isNotEmpty()) {
                val l = data.split("\n").toTypedArray()
                if (l.isNotEmpty()) {
                    for (s in l) {
                        val word = s.trim { it <= ' ' }
                        if (word.isNotEmpty() && !list.contains(word)) {
                            list.add(word)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun getContentFromFile(fileName: String): String {
        var data = ""
        try {
            val inputStream = FileInputStream(fileName)
            val inputStreamReader = InputStreamReader(inputStream)
            val reader = BufferedReader(inputStreamReader)
            var line: String?

            // Reading line by line from the
            // file until a null is returned
            val stringBuilder = StringBuilder()
            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line).append("\n")
            }
            inputStream.close()
            data = stringBuilder.toString().trim { it <= ' ' }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return data
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.spell4wiki_view_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            callBackPress()
        }
        return super.onOptionsItemSelected(menuItem)
    }

    private fun loadLanguages() {
        val callback = object : OnLanguageSelectionListener {
            override fun onCallBackListener(langCode: String?) {
                if (languageCode != langCode) {
                    languageCode = langCode
                    supportActionBar?.subtitle = GeneralUtils.getLanguageInfo(applicationContext, wikiLangDao?.getWikiLanguageWithCode(langCode))
                    invalidateOptionsMenu()
                    if (layoutList.visibility == View.VISIBLE || layoutEmpty.visibility == View.VISIBLE) {
                        if (!TextUtils.isEmpty(editFile.text)) {
                            val items = getWordListFromString(
                                editFile.text.toString()
                            )
                            showWordsInRecordMode(items)
                        }
                    }
                }
            }
        }
        val languageSelectionFragment = LanguageSelectionFragment(this)
        languageSelectionFragment.init(callback, ListMode.SPELL_4_WIKI_CONTRIBUTION_LANGUAGE)
        languageSelectionFragment.show(supportFragmentManager)
    }

    private fun setupLanguageSelectorMenuItem(menu: Menu) {
        val item = menu.findItem(R.id.menu_lang_selector)
        item.isVisible = true
        val rootView = item.actionView
        val selectedLang = rootView.findViewById<TextView>(R.id.txtSelectedLanguage)
        selectedLang.text = languageCode?.toUpperCase(Locale.ROOT) ?: ""
        rootView.setOnClickListener { loadLanguages() }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        setupLanguageSelectorMenuItem(menu)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onBackPressed() {
        callBackPress()
    }

    private fun callBackPress() {
        if (layoutList.visibility == View.VISIBLE || layoutEmpty.visibility == View.VISIBLE) {
            layoutEdit.makeVisible()
            layoutList.makeGone()
            layoutEmpty.makeGone()
        } else if (layoutEdit.visibility == View.VISIBLE) {
            if (!TextUtils.isEmpty(editFile.text)) {
                this.showConfirmBackDialog {
                    layoutSelect.makeVisible()
                    layoutEdit.makeGone()
                }
            } else {
                layoutSelect.makeVisible()
                layoutEdit.makeGone()
            }
        } else super.onBackPressed()
    }

    private fun callShowCaseUI() {
        if (!isFinishing && !isDestroyed) {
            if (isNotShowed(ShowCasePref.LIST_ITEM_SPELL_4_WIKI) && recyclerView != null && recyclerView.visibility == View.VISIBLE && recyclerView.getChildAt(0) != null) {
                val sequence = MaterialTapTargetSequence().setSequenceCompleteListener { showed(ShowCasePref.LIST_ITEM_SPELL_4_WIKI) }
                sequence.addPrompt(
                    getPromptBuilder(this@Spell4WordListActivity)
                        .setTarget(recyclerView.getChildAt(0))
                        .setPrimaryText(R.string.sc_t_spell4wiki_list_item)
                        .setSecondaryText(R.string.sc_d_spell4wiki_list_item)
                )
                sequence.show()
            }
        }
    }
}