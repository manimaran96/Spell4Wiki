package com.manimarank.spell4wiki.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.manimarank.spell4wiki.R;
import com.manimarank.spell4wiki.activities.base.BaseActivity;
import com.manimarank.spell4wiki.adapters.ListItemAdapter;
import com.manimarank.spell4wiki.utils.constants.AppConstants;
import com.manimarank.spell4wiki.utils.constants.Urls;
import com.manimarank.spell4wiki.models.ItemsModel;

import java.util.ArrayList;
import java.util.List;


public class ListItemActivity extends BaseActivity {

    private List<ItemsModel> listItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_info);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(AppConstants.TITLE)) {
            String title = getIntent().getExtras().getString(AppConstants.TITLE);
            setTitle(title);

            RecyclerView recyclerView = findViewById(R.id.recyclerView);
            assert title != null;
            if (title.equals(getString(R.string.credits)))
                listItems = getCreditsInfo();
            else if (title.equals(getString(R.string.third_party_libraries)))
                listItems = getThirdPartyLibInfo();
            ListItemAdapter adapter = new ListItemAdapter(this, listItems);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    private List<ItemsModel> getCreditsInfo() {
        List<ItemsModel> list = new ArrayList<>();
        list.add(new ItemsModel(R.drawable.ic_language, "Translation icon", "Claudiu Antohi from the Noun Project", "https://thenounproject.com/term/translation/7368/"));
        list.add(new ItemsModel(R.drawable.ic_info, "Dictionary icon", "Berkah Icon from the Noun Project", "https://thenounproject.com/term/dictionary/2298128/"));
        list.add(new ItemsModel(R.drawable.ic_git, "Git icon", "WClarke from the wikimedia commons", "https://commons.wikimedia.org/wiki/File:Git-icon-black.svg"));
        list.add(new ItemsModel(R.drawable.ic_telegram, "Telegram icon", "CoreUI from the wikimedia commons", "https://commons.wikimedia.org/wiki/File:Cib-telegram-plane_(CoreUI_Icons_v1.0.0).svg"));
        list.add(new ItemsModel(R.raw.check_file_availability, "Analyzing website animation", "Osama Sarsar at lottiefiles.com", "https://lottiefiles.com/17784-analyzing-website", true));
        list.add(new ItemsModel(R.raw.empty_state, "Empty state animation", "Rizwan Rasool19 at lottiefiles.com", "https://lottiefiles.com/16656-empty-state", true));
        list.add(new ItemsModel(R.raw.uploading_file, "Upload animation", "Esko Ahonen at lottiefiles.com", "https://lottiefiles.com/1683-cloud-upload", true));
        list.add(new ItemsModel(R.raw.web_page_load_error, "Web page error animation", "Arushi Saini at lottiefiles.com", "https://lottiefiles.com/3648-no-internet-connection", true));
        list.add(new ItemsModel( "Commons Android App - Source code", "Upload pictures from Android to Wikimedia Commons","https://github.com/commons-app/apps-android-commons"));
        list.add(new ItemsModel( "Wiki Audio Android App - Source code", "Upload audios from Android to Wikimedia Commons","https://github.com/Atul22/wikiAudio"));
        return list;
    }

    private List<ItemsModel> getThirdPartyLibInfo() {
        List<ItemsModel> list = new ArrayList<>();
        String gplV3 = Urls.GPL_V3;
        String apache = Urls.APACHE;
        String mit = Urls.MIT;
        list.add(new ItemsModel("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.72", apache, "https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-stdlib-jdk7"));
        list.add(new ItemsModel("androidx.core:core-ktx:1.2.0", apache, "https://developer.android.com/jetpack/androidx/releases/core"));
        list.add(new ItemsModel("androidx.constraintlayout:constraintlayout:1.1.3", apache, "https://androidstudio.googleblog.com/2018/08/constraintlayout-113.html"));
        list.add(new ItemsModel("androidx.appcompat:appcompat:1.1.0", apache, "https://developer.android.com/jetpack/androidx/releases/appcompat"));
        list.add(new ItemsModel("com.google.android.material:material:1.0.0", apache, "https://developer.android.com/topic/libraries/support-library"));
        list.add(new ItemsModel("androidx.recyclerview:recyclerview:1.1.0", apache, "https://developer.android.com/jetpack/androidx/releases/recyclerview"));
        list.add(new ItemsModel("br.com.simplepass:loading-button-android:1.14.0", mit, "https://github.com/leandroBorgesFerreira/LoadingButtonAndroid"));
        list.add(new ItemsModel("com.arthenica:mobile-ffmpeg-audio:4.3.1.LTS", gplV3, "https://github.com/tanersener/mobile-ffmpeg"));
        list.add(new ItemsModel("com.github.bumptech.glide:glide:4.11.0", "https://github.com/bumptech/glide/blob/master/LICENSE", "https://github.com/bumptech/glide"));
        list.add(new ItemsModel("com.squareup.retrofit2:retrofit:2.6.2", apache, "https://github.com/square/retrofit"));
        list.add(new ItemsModel("com.squareup.retrofit2:converter-gson:2.6.2", apache, "https://github.com/square/retrofit/tree/master/retrofit-converters/gson"));
        list.add(new ItemsModel("com.squareup.okhttp3:okhttp:4.2.1", apache, "https://github.com/square/okhttp"));
        list.add(new ItemsModel("com.github.franmontiel:PersistentCookieJar:v1.0.1", apache, "https://github.com/franmontiel/PersistentCookieJar"));
        list.add(new ItemsModel("android.arch.persistence.room:runtime:1.1.1", apache, "https://developer.android.com/jetpack/androidx/releases/room"));
        list.add(new ItemsModel("android.arch.persistence.room:compiler:1.1.1", apache, "https://developer.android.com/jetpack/androidx/releases/room"));
        list.add(new ItemsModel("com.airbnb.android:lottie:3.4.0", apache, "https://github.com/airbnb/lottie-android"));
        list.add(new ItemsModel("com.gitlab.manimaran:crashreporter:v0.1", gplV3, "https://github.com/manimaran96/CrashReporter"));
        list.add(new ItemsModel("uk.co.samuelwall:material-tap-target-prompt:3.0.0", apache, "https://github.com/sjwall/MaterialTapTargetPrompt"));
        list.add(new ItemsModel("androidx.test.espresso:espresso-core:3.2.0", apache, "https://developer.android.com/training/testing/set-up-project"));
        return list;
    }

}
