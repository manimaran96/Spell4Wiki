package com.manimaran.wikiaudio.acticity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.wiki_api.ServiceGenerator;

import org.billthefarmer.markdown.MarkdownView;


public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setTitle(getString(R.string.about));

        /*
         * Load about markdown file from  - https://raw.githubusercontent.com/manimaran96/Spell4Wiki/master/README.md
         */
        MarkdownView markdownView = findViewById(R.id.markdown);
        markdownView.loadMarkdownFile(getString(R.string.url_about));
    }
}
