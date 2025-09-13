package com.manimarank.spell4wiki.ui.spell4wiktionary

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
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
import com.manimarank.spell4wiki.utils.EdgeToEdgeUtils.setupEdgeToEdgeWithToolbar
import com.manimarank.spell4wiki.utils.GeneralUtils
import com.manimarank.spell4wiki.utils.GeneralUtils.getPromptBuilder
import com.manimarank.spell4wiki.utils.GeneralUtils.hideKeyboard
import com.manimarank.spell4wiki.utils.SnackBarUtils.showLong
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.constants.ListMode
import com.manimarank.spell4wiki.utils.makeGone
import com.manimarank.spell4wiki.utils.makeVisible
import com.manimarank.spell4wiki.databinding.ActivitySpell4WordlistBinding
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetSequence
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.Locale


class Spell4WordListActivity : BaseActivity() {

    private lateinit var binding: ActivitySpell4WordlistBinding
    private var adapter: EndlessRecyclerAdapter? = null
    private var languageCode: String? = ""
    private var wikiLangDao: WikiLangDao? = null
    private var wordsHaveAudioDao: WordsHaveAudioDao? = null
    private lateinit var pref: PrefManager
    private lateinit var viewModel: MainViewModel
    private val filterRemovedWords = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpell4WordlistBinding.inflate(layoutInflater)
        setContentView(binding.root)
        pref = PrefManager(applicationContext)
        languageCode = pref.languageCodeSpell4WikiAll
        initUI()
    }

    private fun initUI() {
        wikiLangDao = DBHelper.getInstance(applicationContext).appDatabase.wikiLangDao
        wordsHaveAudioDao = DBHelper.getInstance(applicationContext).appDatabase.wordsHaveAudioDao

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val wikiLang = wikiLangDao?.getWikiLanguageWithCode(languageCode)

        // Setup proper status bar handling
        setupEdgeToEdgeWithToolbar(
            rootView = binding.root,
            toolbar = toolbar
        )

        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.spell4wordlist)
        supportActionBar?.subtitle = GeneralUtils.getLanguageInfo(applicationContext, wikiLang)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSelectFile.setOnClickListener {
            openTextFilePicker()
        }
        binding.btnDirectContent.setOnClickListener { showDirectContentAlignMode() }
        binding.btnDone.setOnClickListener {
            hideKeyboard(this@Spell4WordListActivity)
            if (!TextUtils.isEmpty(binding.editFile.text)) {
                val items = getWordListFromString(binding.editFile.text.toString())
                showWordsInRecordMode(items)
            } else showLong(binding.editFile, getString(R.string.provide_valid_content))
        }
        binding.layoutSelect.makeVisible()
        binding.layoutEdit.makeGone()
        binding.layoutList.makeGone()
        binding.root.findViewById<View>(R.id.layoutEmpty).makeGone()

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        setupFilterWordOption()
    }

    /**
     * Open a text file to read the words
     */
    private val openTextFileLauncher = registerForActivityResult( ActivityResultContracts.OpenDocument(), this::handleSelectedTextFile)

    // Handle the result of file selection
    private fun handleSelectedTextFile(uri: Uri?) {
        if (uri != null) {
            readTextFileFromUri(uri)
        }
    }

    // Read content of the selected text file
    private fun readTextFileFromUri(uri: Uri) {
        try {
            contentResolver.openInputStream(uri).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val fileContent = java.lang.StringBuilder()
                    var line: String?
                    while ((reader.readLine().also { line = it }) != null) {
                        fileContent.append(line).append("\n")
                    }
                    openFileContentInAlignMode(fileContent.toString())
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Launch SAF to pick a text file
    private fun openTextFilePicker() {
        openTextFileLauncher.launch(arrayOf("text/plain")) // MIME type for text files
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
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
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
                binding.recyclerView,
                if (diff > 0) getString(
                    R.string.words_filter_success,
                    diff
                ) else getString(R.string.no_words_filtered)
            )
            filterRemovedWords.addAll(list)
            dialog.dismiss()
        }

        viewModel.filterCancelled.observe(this) { cancelled ->
            if (cancelled) {
                showLong(binding.recyclerView, getString(R.string.filter_cancelled))
                dialog.dismiss()
            }
        }

        btnCancel.setOnClickListener {
            viewModel.cancelFilter()
        }

        binding.root.findViewById<View>(R.id.btnRunFilter).setOnClickListener {
            val runFilterNoOfWordsCheckCount = pref.runFilterNumberOfWordsToCheck ?: AppConstants.RUN_FILTER_NO_OF_WORDS_CHECK_COUNT
            itemList = adapter?.getList()?.filter { filterRemovedWords.contains(it).not() }?.take(runFilterNoOfWordsCheckCount) ?: listOf()
            if (itemList.isNotEmpty() && languageCode != null) {
                txtProgress.text = ("0/${itemList.size}")
                dialog.show()
                viewModel.checkWordsAvailability(itemList, languageCode!!, runFilterNoOfWordsCheckCount)
            } else
                showLong(binding.recyclerView, getString(R.string.no_words_scroll_to_get_new_words))
        }

        binding.root.findViewById<View>(R.id.btnRunFilterInfo).setOnClickListener { this.openRunFilterInfoDialog() }
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

    private fun openFileContentInAlignMode(fileContent: String) {
        binding.layoutSelect.makeGone()
        binding.layoutEdit.makeVisible()
        binding.layoutList.makeGone()
        binding.root.findViewById<View>(R.id.layoutEmpty).makeGone()
        binding.txtFileInfo.text = getString(R.string.hint_select_file_next)
        binding.editFile.setText(fileContent)
    }

    private fun showDirectContentAlignMode() {
        binding.layoutSelect.makeGone()
        binding.layoutEdit.makeVisible()
        binding.layoutList.makeGone()
        binding.root.findViewById<View>(R.id.layoutEmpty).makeGone()
        binding.txtFileInfo.text = getString(R.string.hint_direct_copy_next)
        binding.editFile.setText("")
    }

    private fun showWordsInRecordMode(items: MutableList<String>) {
        val wordsAlreadyHaveAudio = wordsHaveAudioDao?.getWordsAlreadyHaveAudioByLanguage(languageCode)
        wordsAlreadyHaveAudio?.let {
            items.removeAll(wordsAlreadyHaveAudio)
        }
        if (items.size > 0) {
            binding.layoutSelect.makeGone()
            binding.layoutEdit.makeGone()
            binding.layoutList.makeVisible()
            binding.root.findViewById<View>(R.id.layoutEmpty).makeGone()
            binding.recyclerView.setHasFixedSize(true)
            val layoutManager = LinearLayoutManager(this)
            binding.recyclerView.layoutManager = layoutManager
            adapter = EndlessRecyclerAdapter(this, items, ListMode.SPELL_4_WORD_LIST)
            binding.recyclerView.setAdapter(adapter, layoutManager)
            adapter?.setWordsHaveAudioList(wordsHaveAudioDao?.getWordsAlreadyHaveAudioByLanguage(languageCode)?.toMutableList())
            if (isNotShowed(ShowCasePref.LIST_ITEM_SPELL_4_WIKI))
                Handler().post { callShowCaseUI() }
        } else {
            showEmptyView()
        }
    }

    private fun showEmptyView() {
        binding.layoutSelect.makeGone()
        binding.layoutEdit.makeGone()
        binding.layoutList.makeGone()
        binding.root.findViewById<View>(R.id.layoutEmpty).makeVisible()
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
                    if (binding.layoutList.visibility == View.VISIBLE || binding.root.findViewById<View>(R.id.layoutEmpty).visibility == View.VISIBLE) {
                        if (!TextUtils.isEmpty(binding.editFile.text)) {
                            val items = getWordListFromString(
                                binding.editFile.text.toString()
                            )
                            showWordsInRecordMode(items)
                        }
                    }
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
        if (binding.layoutList.visibility == View.VISIBLE || binding.root.findViewById<View>(R.id.layoutEmpty).visibility == View.VISIBLE) {
            binding.layoutEdit.makeVisible()
            binding.layoutList.makeGone()
            binding.root.findViewById<View>(R.id.layoutEmpty).makeGone()
        } else if (binding.layoutEdit.visibility == View.VISIBLE) {
            if (!TextUtils.isEmpty(binding.editFile.text)) {
                this.showConfirmBackDialog {
                    binding.layoutSelect.makeVisible()
                    binding.layoutEdit.makeGone()
                }
            } else {
                binding.layoutSelect.makeVisible()
                binding.layoutEdit.makeGone()
            }
        } else super.onBackPressed()
    }

    private fun callShowCaseUI() {
        if (!isFinishing && !isDestroyed) {
            if (isNotShowed(ShowCasePref.LIST_ITEM_SPELL_4_WIKI) && binding.recyclerView.visibility == View.VISIBLE && binding.recyclerView.getChildAt(0) != null) {
                val sequence = MaterialTapTargetSequence().setSequenceCompleteListener { showed(ShowCasePref.LIST_ITEM_SPELL_4_WIKI) }
                sequence.addPrompt(
                    getPromptBuilder(this@Spell4WordListActivity)
                        .setTarget(binding.recyclerView.getChildAt(0))
                        .setPrimaryText(R.string.sc_t_spell4wiki_list_item)
                        .setSecondaryText(R.string.sc_d_spell4wiki_list_item)
                )
                sequence.show()
            }
        }
    }
}