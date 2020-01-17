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
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.adapter.EndlessAdapter;
import com.manimaran.wikiaudio.utils.GeneralUtils;
import com.manimaran.wikiaudio.utils.RealPathUtil;
import com.manimaran.wikiaudio.view.EndlessListView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class Spell4WordListActivity extends AppCompatActivity {


    private static final int EDIT_REQUEST_CODE = 42;


    private Button btnSelectFile, btnDirectContent, btnDone;
    private EditText editFile;
    private TextView txtFileInfo;
    private View layoutEdit, layoutSelect;
    private EndlessListView resultListView;
    private EndlessAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spell_4_wordlist);

        initUI();


    }

    private void initUI() {

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.spell4wordlist));
        }

        btnSelectFile = findViewById(R.id.btnSelectFile);
        btnDirectContent = findViewById(R.id.btnDirectContent);
        btnDone = findViewById(R.id.btnDone);
        editFile = findViewById(R.id.editFile);
        txtFileInfo = findViewById(R.id.txtFileInfo);
        resultListView = findViewById(R.id.listView);
        layoutSelect = findViewById(R.id.layoutSelect);
        layoutEdit = findViewById(R.id.layoutEdit);


        btnSelectFile.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                editDocument();
            }
        });

        btnDirectContent.setOnClickListener(v -> showDirectContentAlignMode());

        btnDone.setOnClickListener(v -> {
            GeneralUtils.hideKeyboard(Spell4WordListActivity.this);
            if(!TextUtils.isEmpty(editFile.getText())) {
                List<String> items = getWordListFromString(editFile.getText().toString());
                showWordsInRecordMode(items);
            }
            else
                GeneralUtils.showSnack(editFile, "Enter valid content");

        });

        resultListView.setLoadingView(R.layout.loading_row);

        layoutSelect.setVisibility(View.VISIBLE);
        layoutEdit.setVisibility(View.GONE);
        resultListView.setVisibility(View.GONE);

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
                File file = new File(RealPathUtil.getRealPath(getApplicationContext(), uri));
                String TAG = "Spell4WordListActivity";
                openFileInAlignMode(file.getAbsolutePath(), file.getName());

            }
        }
    }

    private void openFileInAlignMode(String filePath, String fileName) {

        layoutSelect.setVisibility(View.GONE);
        layoutEdit.setVisibility(View.VISIBLE);
        resultListView.setVisibility(View.GONE);

        txtFileInfo.setText(("Align the file \n" + fileName));
        editFile.setText(getContentFromFile(filePath));
    }

    private void showDirectContentAlignMode(){
        layoutSelect.setVisibility(View.GONE);
        layoutEdit.setVisibility(View.VISIBLE);
        resultListView.setVisibility(View.GONE);

        txtFileInfo.setText(("Paste and Align the content\n"));
        editFile.setText("");
    }

    private void showWordsInRecordMode(List<String> items) {

        layoutSelect.setVisibility(View.GONE);
        layoutEdit.setVisibility(View.GONE);
        resultListView.setVisibility(View.VISIBLE);


        adapter = new EndlessAdapter(this, items, R.layout.search_result_row, true);
        resultListView.setAdapter(adapter);
        resultListView.setVisibility(View.VISIBLE);
    }

    private List<String> getWordListFromString(String data) {

        List<String> list = new ArrayList<>();
        try {
            if(data != null && data.length() > 0) {
                String[] l = data.split("\n");

                if(l.length > 0){
                    for(String s : l){
                        if(s != null)
                        {
                            String word = s.trim();
                            if(word.length() > 0){
                                list.add(word);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result = null;
        Cursor cursor = null;//ww  w.j  a  va2  s. c om
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = getContentResolver().query(contentURI, proj,
                    null, null, null);
            int column_index = cursor != null ? cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA) : 0;
            if(cursor != null && cursor.moveToFirst())
                result = cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return result;
    }

    private String getContentFromFile(String fileName){
        String data = "";
        try {
            FileInputStream inputStream = new FileInputStream(fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);

            String line;

            // Reading line by line from the
            // file until a null is returned
            StringBuilder stringBuilder = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

            inputStream.close();
            data = stringBuilder.toString().trim();

            /*while ((line = reader.readLine()) != null) {
                String s = line.trim();
                if (s.length() > 0)
                    list.add(line);
            }*/

        } catch (Exception e) {
            e.printStackTrace();
        }


        return data;
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
