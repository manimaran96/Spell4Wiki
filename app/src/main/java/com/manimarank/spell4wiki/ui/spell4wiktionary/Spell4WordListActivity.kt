package com.manimarank.spell4wiki.ui.spell4wiktionary

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.db.DBHelper
import com.manimarank.spell4wiki.data.db.dao.WordsHaveAudioDao
import com.manimarank.spell4wiki.data.db.entities.WordsHaveAudio
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.data.prefs.ShowCasePref
import com.manimarank.spell4wiki.data.prefs.ShowCasePref.isNotShowed
import com.manimarank.spell4wiki.data.prefs.ShowCasePref.showed
import com.manimarank.spell4wiki.ui.common.BaseActivity
import com.manimarank.spell4wiki.ui.dialogs.showConfirmBackDialog
import com.manimarank.spell4wiki.ui.languageselector.LanguageSelectionFragment
import com.manimarank.spell4wiki.ui.listerners.OnLanguageSelectionListener
import com.manimarank.spell4wiki.utils.GeneralUtils.getPromptBuilder
import com.manimarank.spell4wiki.utils.GeneralUtils.hideKeyboard
import com.manimarank.spell4wiki.utils.RealPathUtil.getRealPath
import com.manimarank.spell4wiki.utils.SnackBarUtils.showLong
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.constants.ListMode
import com.manimarank.spell4wiki.utils.makeGone
import com.manimarank.spell4wiki.utils.makeVisible
import kotlinx.android.synthetic.main.activity_spell_4_wordlist.*
import kotlinx.android.synthetic.main.empty_state_ui.*
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetSequence
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.*

class Spell4WordListActivity : BaseActivity() {
    private var adapter: EndlessRecyclerAdapter? = null
    private var languageCode: String? = ""
    private var wordsHaveAudioDao: WordsHaveAudioDao? = null
    private lateinit var pref: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spell_4_wordlist)
        pref = PrefManager(applicationContext)
        languageCode = pref.languageCodeSpell4WordList
        initUI()
    }

    private fun initUI() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.spell4wordlist)

        wordsHaveAudioDao = DBHelper.getInstance(applicationContext).appDatabase.wordsHaveAudioDao

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
        recyclerView.makeGone()
        layoutEmpty.makeGone()
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
        recyclerView.makeGone()
        layoutEmpty.makeGone()
        txtFileInfo.text = getString(R.string.hint_select_file_next)
        editFile.setText(getContentFromFile(filePath))
    }

    private fun showDirectContentAlignMode() {
        layoutSelect.makeGone()
        layoutEdit.makeVisible()
        recyclerView.makeGone()
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
            recyclerView.makeVisible()
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
        recyclerView.makeGone()
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
                        if (word.isNotEmpty()) {
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
                    invalidateOptionsMenu()
                    if (recyclerView.visibility == View.VISIBLE || layoutEmpty.visibility == View.VISIBLE) {
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
        languageSelectionFragment.init(callback, ListMode.SPELL_4_WORD_LIST)
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
        if (recyclerView.visibility == View.VISIBLE || layoutEmpty.visibility == View.VISIBLE) {
            layoutEdit.makeVisible()
            recyclerView.makeGone()
            layoutEmpty.makeGone()
        } else if (layoutEdit.visibility == View.VISIBLE) {
            if (!TextUtils.isEmpty(editFile.text) || pref.abortAlertStatus == true) {
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