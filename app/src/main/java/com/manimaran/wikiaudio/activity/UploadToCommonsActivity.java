package com.manimaran.wikiaudio.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.adapter.EndlessAdapter;
import com.manimaran.wikiaudio.view.EndlessListView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class UploadToCommonsActivity extends AppCompatActivity {


    private static final int EDIT_REQUEST_CODE = 42;


    private EndlessListView resultListView;
    private EndlessAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_to_commons);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.spell4wordlist));
        }

        Button btnPickFile = findViewById(R.id.btn_pick_file);

        btnPickFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    editDocument();
                }
            }
        });

        resultListView = findViewById(R.id.search_result_list);
        resultListView.setLoadingView(R.layout.loading_row);
    }


    /**
     * Open a file for writing and append some text to it.
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void editDocument() {
        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's
        // file browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones).
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only text files.
        intent.setType("text/plain");

        startActivityForResult(intent, EDIT_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == EDIT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                assert uri != null;
                File file = new File(getRealPathFromURI(uri));
                String TAG = "UploadToCommonsActivity";
                adapter = new EndlessAdapter(this, readFromFile(file.getPath()), R.layout.search_result_row, true);
                resultListView.setAdapter(adapter);
                resultListView.setVisibility(View.VISIBLE);
            }
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    private List<String> readFromFile(String fileName) {
        List<String> list = new ArrayList<>();
        try {
            FileInputStream inputStream = new FileInputStream(fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);

            String line;

            // Reading line by line from the
            // file until a null is returned
            while ((line = reader.readLine()) != null) {
                String s = line.trim();
                if (s.length() > 0)
                    list.add(line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }


}
