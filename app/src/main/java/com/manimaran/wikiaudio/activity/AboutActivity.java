package com.manimaran.wikiaudio.activity;

import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.manimaran.wikiaudio.R;

import br.tiagohm.markdownview.css.styles.Github;


public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setTitle(getString(R.string.about));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        /*
         * Load about markdown file from  - https://raw.githubusercontent.com/manimaran96/Spell4Wiki/master/README.md
         */
        br.tiagohm.markdownview.MarkdownView mMarkdownView = findViewById(R.id.markdown_view);
        Github style = new Github();
        style.addRule("img", "width:0px", "height:0px"); // Markdown view not load images so view make 0px.
        mMarkdownView.addStyleSheet(style);
        mMarkdownView.loadMarkdownFromUrl(getString(R.string.url_about));

        final ProgressBar progressBar = findViewById(R.id.pb);
        final LinearLayout layoutAbout = findViewById(R.id.layout_about);
        layoutAbout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                layoutAbout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        }, 2000);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }
}
