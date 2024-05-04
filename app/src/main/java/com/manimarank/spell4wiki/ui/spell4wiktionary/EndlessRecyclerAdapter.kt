package com.manimarank.spell4wiki.ui.spell4wiktionary

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.manimarank.spell4wiki.BuildConfig
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.prefs.PrefManager
import com.manimarank.spell4wiki.data.prefs.ShowCasePref
import com.manimarank.spell4wiki.data.prefs.ShowCasePref.isNotShowed
import com.manimarank.spell4wiki.ui.common.BaseViewHolder
import com.manimarank.spell4wiki.ui.webui.CommonWebActivity
import com.manimarank.spell4wiki.utils.GeneralUtils.checkPermissionGranted
import com.manimarank.spell4wiki.utils.GeneralUtils.permissionDenied
import com.manimarank.spell4wiki.utils.GeneralUtils.showRecordDialog
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected
import com.manimarank.spell4wiki.utils.SnackBarUtils.showLong
import com.manimarank.spell4wiki.utils.ToastUtils.showLong
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.constants.ListMode
import com.manimarank.spell4wiki.utils.constants.ListMode.Companion.EnumListMode
import com.manimarank.spell4wiki.utils.constants.Urls
import com.manimarank.spell4wiki.utils.makeVisible

class EndlessRecyclerAdapter(
    private val mContext: Context,
    wordItems: MutableList<String>,
    @property:EnumListMode @EnumListMode private val mode: Int
) : RecyclerView.Adapter<BaseViewHolder>() {
    private var isLoaderVisible = false
    private val mItems: MutableList<String> = wordItems
    private val mActivity: Activity = mContext as Activity
    private var rootView: View? = null
    private var wordsAlreadyHaveAudio: MutableList<String> = ArrayList()
    private val pref: PrefManager = PrefManager(mContext)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            VIEW_TYPE_NORMAL -> DataViewHolder(
                LayoutInflater.from(
                    parent.context
                ).inflate(R.layout.item_result_row, parent, false)
            )
            VIEW_TYPE_LOADING -> ProgressViewHolder(
                LayoutInflater.from(
                    parent.context
                ).inflate(R.layout.item_loading_row, parent, false)
            )
            else -> ProgressViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_loading_row, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemViewType(position: Int): Int {
        return if (isLoaderVisible) {
            if (position == mItems.size - 1) VIEW_TYPE_LOADING else VIEW_TYPE_NORMAL
        } else {
            VIEW_TYPE_NORMAL
        }
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    fun addItems(postItems: MutableList<String>) {
        if (postItems.isNotEmpty()) {
            postItems.forEach {item ->
                if (!mItems.contains(item))
                    mItems.add(item)
            }
            notifyDataSetChanged()
        }
    }

    fun remove(word: String?) {
        try {
            val pos = mItems.indexOf(word)
            if (pos >= 0 && mItems.size > pos) {
                mItems.remove(word)
                notifyItemRemoved(pos)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun getList(): MutableList<String> {
        return mItems
    }

    fun addLoading() {
        if (!isLoaderVisible && mItems.size > 0) {
            isLoaderVisible = true
            mItems.add("")
            try {
                Handler().post { notifyItemInserted(mItems.size - 1) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeLoading() {
        val position = mItems.size - 1
        if (position >= 0 && isLoaderVisible) {
            isLoaderVisible = false
            mItems.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun clear() {
        mItems.clear()
        notifyDataSetChanged()
    }

    fun setWordsHaveAudioList(wordsAlreadyHaveAudio: MutableList<String>?) {
        if (wordsAlreadyHaveAudio != null) {
            this.wordsAlreadyHaveAudio = wordsAlreadyHaveAudio
        }
    }

    fun addWordInWordsHaveAudioList(wordsAlreadyHaveAudio: String?) {
        wordsAlreadyHaveAudio?.let { this.wordsAlreadyHaveAudio.add(it) }
    }

    private fun showNetworkProblem() {
        if (rootView != null)
            showLong(rootView!!, mActivity.getString(R.string.check_internet)
        ) else
            showLong(mActivity.getString(R.string.check_internet))
    }

    private fun openWiktionaryWebView(word: String) {
        if (isConnected(mActivity)) {
            val intent = Intent(mContext, CommonWebActivity::class.java)
            var langCode = languageCode
            if (langCode == null) langCode = AppConstants.DEFAULT_LANGUAGE_CODE
            val url = String.format(Urls.WIKTIONARY_WEB, langCode, word)
            intent.putExtra(AppConstants.TITLE, word)
            intent.putExtra(AppConstants.URL, url)
            intent.putExtra(AppConstants.IS_WIKTIONARY_WORD, true)
            intent.putExtra(AppConstants.LANGUAGE_CODE, langCode)
            mActivity.startActivity(intent)
        } else {
            showNetworkProblem()
        }
    }

    private val languageCode: String?
        get() = when (mode) {
            ListMode.TEMP -> null
            else -> pref.languageCodeSpell4WikiAll
        }

    internal class ProgressViewHolder(itemView: View) : BaseViewHolder(itemView) {
        override fun clear() {}
    }

    internal inner class DataViewHolder(itemView: View) : BaseViewHolder(itemView) {
        private val txtWord: TextView
        private val btnWikMeaning: View
        override fun clear() {}
        override fun onBind(position: Int) {
            super.onBind(position)
            val word = mItems[position]
            txtWord.text = word
            btnWikMeaning.makeVisible()
            btnWikMeaning.setOnClickListener { openWiktionaryWebView(word) }
            val isHaveAudio = wordsAlreadyHaveAudio.contains(word)
            itemView.setBackgroundColor(ContextCompat.getColor(mContext, if (isHaveAudio) R.color.record_have_audio else R.color.record_normal))
            txtWord.setOnClickListener {
                when (mode) {
                    ListMode.SPELL_4_WIKI, ListMode.SPELL_4_WORD_LIST, ListMode.SPELL_4_WORD -> {
                        if (isNotShowed(ShowCasePref.LIST_ITEM_SPELL_4_WIKI)) return@setOnClickListener
                        if (!isHaveAudio) {
                            if (checkPermissionGranted(mActivity)) {
                                if (isConnected(mActivity))
                                    showRecordDialog(mActivity, word, languageCode)
                                else showNetworkProblem()
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                permissionToRecordAudio
                            }
                        } else showLong(
                            itemView,
                            String.format(mActivity.getString(R.string.audio_file_already_exist), word)
                        )
                    }
                    ListMode.WIKTIONARY -> openWiktionaryWebView(word)
                    ListMode.TEMP -> {
                    }
                    else -> openWiktionaryWebView(word)
                }
            }
        }

        private val permissionToRecordAudio: Unit
            get() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissionDenied(mActivity)) {
                    showAppSettingsPageHint()
                    mActivity.requestPermissions(
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ), AppConstants.RC_PERMISSIONS
                    )
                }
            }

        private fun showAppSettingsPageHint() {
            Snackbar.make(itemView, mActivity.getString(R.string.permission_required), Snackbar.LENGTH_LONG)
                .setAction(mActivity.getString(R.string.go_settings)) {
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    mActivity.startActivity(intent)
                }
                .show()
        }

        init {
            rootView = itemView
            txtWord = itemView.findViewById(R.id.txtWord)
            btnWikMeaning = itemView.findViewById(R.id.btnWikiMeaning)
        }
    }

    companion object {
        private const val VIEW_TYPE_LOADING = 0
        private const val VIEW_TYPE_NORMAL = 1
    }

}