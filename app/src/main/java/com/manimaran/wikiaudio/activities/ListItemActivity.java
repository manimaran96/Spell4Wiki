package com.manimaran.wikiaudio.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.MenuItem;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.adapters.ListItemAdapter;
import com.manimaran.wikiaudio.models.ItemsModel;

import java.util.ArrayList;
import java.util.List;


public class ListItemActivity extends AppCompatActivity {

    private List<ItemsModel> listItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_info);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if(getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey("title")){
            String title = getIntent().getExtras().getString("title");
            setTitle(title);

            RecyclerView recyclerView = findViewById(R.id.recyclerView);
            assert title != null;
            if(title.equals(getString(R.string.credits)))
                listItems = getCreditsInfo();
            else if(title.equals(getString(R.string.third_party_libraries)))
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

    private List<ItemsModel> getCreditsInfo(){
        List<ItemsModel> list = new ArrayList<>();
        list.add(new ItemsModel(R.drawable.ic_language, "Translation icon", "Claudiu Antohi from the Noun Project", "https://thenounproject.com/claudiu.antohi/"));
        list.add(new ItemsModel(R.drawable.ic_info, "Dictionary icon","Berkah Icon from the Noun Project", "https://thenounproject.com/berkahicon/"));
        list.add(new ItemsModel(R.drawable.ic_git, "Git icon","WClarke from the wiki commons", "https://commons.wikimedia.org/wiki/File:Git-icon-black.svg"));
        return list;
    }

    private List<ItemsModel> getThirdPartyLibInfo(){
        List<ItemsModel> list = new ArrayList<>();
        String gplV3 = getString(R.string.url_license_gpl_v3);
        String apache = "http://www.apache.org/licenses/LICENSE-2.0.txt";
        String mit = "https://opensource.org/licenses/MIT";
        list.add(new ItemsModel("androidx.constraintlayout:constraintlayout:1.1.3", apache, "https://androidstudio.googleblog.com/2018/08/constraintlayout-113.html"));
        list.add(new ItemsModel("androidx.appcompat:appcompat:1.1.0", apache, "https://developer.android.com/jetpack/androidx/releases/appcompat"));
        list.add(new ItemsModel("com.google.android.material:material:1.0.0", apache, "https://developer.android.com/topic/libraries/support-library"));
        list.add(new ItemsModel("androidx.recyclerview:recyclerview:1.1.0", apache, "https://developer.android.com/jetpack/androidx/releases/recyclerview"));
        list.add(new ItemsModel("br.com.simplepass:loading-button-android:1.14.0", mit, "https://github.com/leandroBorgesFerreira/LoadingButtonAndroid"));
        list.add(new ItemsModel("com.arthenica:mobile-ffmpeg-full:4.3.1.LTS", gplV3, "https://github.com/tanersener/mobile-ffmpeg"));
        list.add(new ItemsModel("com.github.bumptech.glide:glide:4.11.0", "https://github.com/bumptech/glide/blob/master/LICENSE", "https://github.com/bumptech/glide"));
        list.add(new ItemsModel("com.squareup.retrofit2:retrofit:2.6.2", apache, "https://github.com/square/retrofit"));
        list.add(new ItemsModel("com.squareup.okhttp3:okhttp:4.2.1", apache, "https://github.com/square/okhttp"));
        list.add(new ItemsModel("com.github.franmontiel:PersistentCookieJar:v1.0.1", apache, "https://github.com/franmontiel/PersistentCookieJar"));
        list.add(new ItemsModel("com.google.code.gson:gson:2.8.5", apache, "https://github.com/google/gson"));
        list.add(new ItemsModel("com.github.tiagohm.MarkdownView:library:0.19.0", apache, "https://github.com/tiagohm/MarkdownView"));
        list.add(new ItemsModel("com.github.AppIntro:AppIntro:4.2.3", apache, "https://github.com/AppIntro/AppIntro"));
        return list;
    }

}
