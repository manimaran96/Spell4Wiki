package com.manimarank.spell4wiki.ui.about

import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.ui.common.BaseActivity
import com.manimarank.spell4wiki.data.model.ItemsModel
import com.manimarank.spell4wiki.utils.EdgeToEdgeUtils.setupEdgeToEdgeWithToolbar
import com.manimarank.spell4wiki.utils.constants.AppConstants
import com.manimarank.spell4wiki.utils.constants.Urls
import java.util.*

class ListItemActivity : BaseActivity() {
    private var listItems: List<ItemsModel> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_info)

        // No edge-to-edge setup needed - default action bar handles status bar spacing properly

        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        if (intent?.extras?.containsKey(AppConstants.TITLE) == true) {
            val title = intent.extras!!.getString(AppConstants.TITLE)
            setTitle(title)
            val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
            if (title == getString(R.string.credits)) listItems = creditsInfo else if (title == getString(R.string.third_party_libraries)) listItems = thirdPartyLibInfo
            val adapter = ListItemAdapter(this, listItems)
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(applicationContext)
        }
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(menuItem)
    }

    private val creditsInfo: List<ItemsModel>
        get() {
            val list: MutableList<ItemsModel> = ArrayList()
            list.add(ItemsModel("Translation icon", "Claudiu Antohi from the Noun Project", "https://thenounproject.com/term/translation/7368/", R.drawable.ic_language))
            list.add(ItemsModel("Dictionary icon", "Berkah Icon from the Noun Project", "https://thenounproject.com/term/dictionary/2298128/", R.drawable.ic_info))
            list.add(ItemsModel("Git icon", "WClarke from the wikimedia commons", "https://commons.wikimedia.org/wiki/File:Git-icon-black.svg", R.drawable.ic_git))
            list.add(ItemsModel( "Telegram icon", "CoreUI from the wikimedia commons", "https://commons.wikimedia.org/wiki/File:Cib-telegram-plane_(CoreUI_Icons_v1.0.0).svg", R.drawable.ic_telegram))
            list.add(ItemsModel("Analyzing website animation", "Osama Sarsar at lottiefiles.com", "https://lottiefiles.com/17784-analyzing-website", R.raw.check_file_availability, true))
            list.add(ItemsModel( "Empty state animation", "Rizwan Rasool19 at lottiefiles.com", "https://lottiefiles.com/16656-empty-state", R.raw.empty_state, true))
            list.add(ItemsModel( "Upload animation", "Esko Ahonen at lottiefiles.com", "https://lottiefiles.com/1683-cloud-upload", R.raw.uploading_file, true))
            list.add(ItemsModel("Web page error animation", "Arushi Saini at lottiefiles.com", "https://lottiefiles.com/3648-no-internet-connection", R.raw.web_page_load_error, true))
            list.add(ItemsModel("Commons Android App - Source code", "Upload pictures from Android to Wikimedia Commons", "https://github.com/commons-app/apps-android-commons"))
            list.add(ItemsModel("Wiki Audio Android App - Source code", "Upload audios from Android to Wikimedia Commons", "https://github.com/Atul22/wikiAudio"))
            return list
        }

    private val thirdPartyLibInfo: List<ItemsModel> get() {
            val list: MutableList<ItemsModel> = ArrayList()
            val gplV3 = Urls.GPL_V3
            val apache = Urls.APACHE
            val mit = Urls.MIT
            list.add(ItemsModel("org.jetbrains.kotlin:kotlin-stdlib:1.9.24", apache, "https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-stdlib"))
            list.add(ItemsModel("androidx.core:core-ktx:1.13.1", apache, "https://developer.android.com/jetpack/androidx/releases/core"))
            list.add(ItemsModel("androidx.constraintlayout:constraintlayout:2.1.4", apache, "https://developer.android.com/jetpack/androidx/releases/constraintlayout"))
            list.add(ItemsModel("androidx.appcompat:appcompat:1.7.0", apache, "https://developer.android.com/jetpack/androidx/releases/appcompat"))
            list.add(ItemsModel("com.google.android.material:material:1.12.0", apache, "https://developer.android.com/topic/libraries/support-library"))
            list.add(ItemsModel("androidx.recyclerview:recyclerview:1.3.2", apache, "https://developer.android.com/jetpack/androidx/releases/recyclerview"))
            list.add(ItemsModel("br.com.simplepass:loading-button-android:2.3.0", mit, "https://github.com/leandroBorgesFerreira/LoadingButtonAndroid"))
            list.add(ItemsModel("com.arthenica:ffmpeg-kit-audio:6.0-2", gplV3, "https://github.com/arthenica/ffmpeg-kit"))
            list.add(ItemsModel("com.github.bumptech.glide:glide:4.16.0", "https://github.com/bumptech/glide/blob/master/LICENSE", "https://github.com/bumptech/glide"))
            list.add(ItemsModel("com.squareup.retrofit2:retrofit:2.6.2", apache, "https://github.com/square/retrofit"))
            list.add(ItemsModel("com.squareup.retrofit2:converter-gson:2.6.2", apache, "https://github.com/square/retrofit/tree/master/retrofit-converters/gson"))
            list.add(ItemsModel("com.squareup.okhttp3:okhttp:4.2.1", apache, "https://github.com/square/okhttp"))
            list.add(ItemsModel("com.github.franmontiel:PersistentCookieJar:v1.0.1", apache, "https://github.com/franmontiel/PersistentCookieJar"))
            list.add(ItemsModel("android.arch.persistence.room:runtime:1.1.1", apache, "https://developer.android.com/jetpack/androidx/releases/room"))
            list.add(ItemsModel("android.arch.persistence.room:compiler:1.1.1", apache, "https://developer.android.com/jetpack/androidx/releases/room"))
            list.add(ItemsModel("com.airbnb.android:lottie:3.4.0", apache, "https://github.com/airbnb/lottie-android"))
            list.add(ItemsModel("com.gitlab.manimaran:crashreporter:v0.1", gplV3, "https://github.com/manimaran96/CrashReporter"))
            list.add(ItemsModel("uk.co.samuelwall:material-tap-target-prompt:3.0.0", apache, "https://github.com/sjwall/MaterialTapTargetPrompt"))
            list.add(ItemsModel("androidx.test.espresso:espresso-core:3.2.0", apache, "https://developer.android.com/training/testing/set-up-project"))
            return list
        }
}