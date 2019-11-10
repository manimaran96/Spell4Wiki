package com.manimaran.wikiaudio.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.manimaran.wikiaudio.BuildConfig;
import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.adapter.ListItemAdapter;
import com.manimaran.wikiaudio.model.ItemsModel;
import com.manimaran.wikiaudio.utils.GeneralUtils;

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
        // int icon, String name, String about, String url, String licenseUrl
        list.add(new ItemsModel(R.drawable.ic_spell4wiki_main, getString(R.string.spell4wiktionary), getString(R.string.spell4wiktionary), getString(R.string.link_vglug), getString(R.string.link_kaniyam)));
        list.add(new ItemsModel(R.drawable.ic_spell4wiki_main, getString(R.string.spell4wiktionary), getString(R.string.spell4wiktionary), getString(R.string.link_vglug), getString(R.string.link_kaniyam)));
        list.add(new ItemsModel(R.drawable.ic_spell4wiki_main, getString(R.string.spell4wiktionary), getString(R.string.spell4wiktionary), getString(R.string.link_vglug), getString(R.string.link_kaniyam)));
        list.add(new ItemsModel(R.drawable.ic_spell4wiki_main, getString(R.string.spell4wiktionary), getString(R.string.spell4wiktionary), getString(R.string.link_vglug), getString(R.string.link_kaniyam)));
        list.add(new ItemsModel(R.drawable.ic_spell4wiki_main, getString(R.string.spell4wiktionary), getString(R.string.spell4wiktionary), getString(R.string.link_vglug), getString(R.string.link_kaniyam)));
        list.add(new ItemsModel(R.drawable.ic_spell4wiki_main, getString(R.string.spell4wiktionary), getString(R.string.spell4wiktionary), getString(R.string.link_vglug), getString(R.string.link_kaniyam)));
        list.add(new ItemsModel(R.drawable.ic_spell4wiki_main, getString(R.string.spell4wiktionary), getString(R.string.spell4wiktionary), getString(R.string.link_vglug), getString(R.string.link_kaniyam)));

        return list;
    }

    private List<ItemsModel> getThirdPartyLibInfo(){
        List<ItemsModel> list = new ArrayList<>();

        list.add(new ItemsModel(getString(R.string.welcome_user), getString(R.string.app_intro_slide_1_description), getString(R.string.link_vglug)));
        list.add(new ItemsModel(getString(R.string.welcome_user), getString(R.string.app_intro_slide_1_description), getString(R.string.link_vglug)));
        list.add(new ItemsModel(getString(R.string.welcome_user), getString(R.string.app_intro_slide_1_description), getString(R.string.link_vglug)));
        list.add(new ItemsModel(getString(R.string.welcome_user), getString(R.string.app_intro_slide_1_description), getString(R.string.link_vglug)));
        list.add(new ItemsModel(getString(R.string.welcome_user), getString(R.string.app_intro_slide_1_description), getString(R.string.link_vglug)));
        list.add(new ItemsModel(getString(R.string.welcome_user), getString(R.string.app_intro_slide_1_description), getString(R.string.link_vglug)));
        list.add(new ItemsModel(getString(R.string.welcome_user), getString(R.string.app_intro_slide_1_description), getString(R.string.link_vglug)));
        list.add(new ItemsModel(getString(R.string.welcome_user), getString(R.string.app_intro_slide_1_description), getString(R.string.link_vglug)));
        list.add(new ItemsModel(getString(R.string.welcome_user), getString(R.string.app_intro_slide_1_description), getString(R.string.link_vglug)));
        list.add(new ItemsModel(getString(R.string.welcome_user), getString(R.string.app_intro_slide_1_description), getString(R.string.link_vglug)));
        list.add(new ItemsModel(getString(R.string.welcome_user), getString(R.string.app_intro_slide_1_description), getString(R.string.link_vglug)));

        return list;
    }

}
