package com.manimaran.wikiaudio.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.adapters.EndlessAdapter;
import com.manimaran.wikiaudio.constants.AppConstants;
import com.manimaran.wikiaudio.databases.DBHelper;
import com.manimaran.wikiaudio.databases.dao.WordsHaveAudioDao;
import com.manimaran.wikiaudio.fragments.LanguageSelectionFragment;
import com.manimaran.wikiaudio.listerners.OnLanguageSelectionListener;
import com.manimaran.wikiaudio.utils.GeneralUtils;
import com.manimaran.wikiaudio.utils.PrefManager;
import com.manimaran.wikiaudio.utils.RealPathUtil;
import com.manimaran.wikiaudio.views.EndlessListView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static com.manimaran.wikiaudio.constants.EnumTypeDef.ListMode.SPELL_4_WORD_LIST;


public class Spell4WordListActivity extends AppCompatActivity {


    private static final int EDIT_REQUEST_CODE = 42;
    EndlessAdapter adapter;
    private EditText editFile;
    private TextView txtFileInfo;
    private View layoutEdit, layoutSelect;
    private EndlessListView resultListView;
    private String languageCode = "";
    private WordsHaveAudioDao wordsHaveAudioDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spell_4_wordlist);

        initUI();

        PrefManager pref = new PrefManager(getApplicationContext());
        languageCode = pref.getLanguageCodeSpell4WordList();

    }

    private void initUI() {

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.spell4wordlist));
        }

        wordsHaveAudioDao = DBHelper.getInstance(getApplicationContext()).getAppDatabase().getWordsHaveAudioDao();

        Button btnSelectFile = findViewById(R.id.btnSelectFile);
        Button btnDirectContent = findViewById(R.id.btnDirectContent);
        Button btnDone = findViewById(R.id.btnDone);
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
            if (!TextUtils.isEmpty(editFile.getText())) {
                List<String> items = getWordListFromString(editFile.getText().toString());
                showWordsInRecordMode(items);
            } else
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
                                 Intent data) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (data != null) {
                uri = data.getData();
                assert uri != null;
                File file = new File(RealPathUtil.getRealPath(getApplicationContext(), uri));
                openFileInAlignMode(file.getAbsolutePath(), file.getName());
            }
        }

        if (requestCode == AppConstants.RC_UPLOAD_DIALOG) {
            if (data != null && data.hasExtra(AppConstants.WORD)) {
                if (adapter != null) {
                    adapter.remove(data.getStringExtra(AppConstants.WORD));
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    private void openFileInAlignMode(String filePath, String fileName) {

        layoutSelect.setVisibility(View.GONE);
        layoutEdit.setVisibility(View.VISIBLE);
        resultListView.setVisibility(View.GONE);

        txtFileInfo.setText(("Align the file content \n" + fileName));
        editFile.setText(getContentFromFile(filePath));
    }

    private void showDirectContentAlignMode() {
        layoutSelect.setVisibility(View.GONE);
        layoutEdit.setVisibility(View.VISIBLE);
        resultListView.setVisibility(View.GONE);

        txtFileInfo.setText(("Paste and Align the words\n"));
        editFile.setText("");
    }

    private void showWordsInRecordMode(List<String> items) {

        layoutSelect.setVisibility(View.GONE);
        layoutEdit.setVisibility(View.GONE);
        resultListView.setVisibility(View.VISIBLE);


        adapter = new EndlessAdapter(this, items, SPELL_4_WORD_LIST);
        resultListView.setAdapter(adapter);
        resultListView.setVisibility(View.VISIBLE);
        adapter.setWordsHaveAudioList(wordsHaveAudioDao.getWordsAlreadyHaveAudioByLanguage(languageCode));
    }

    private List<String> getWordListFromString(String data) {

        List<String> list = new ArrayList<>();
        try {
            if (data != null && data.length() > 0) {
                String[] l = data.split("\n");

                if (l.length > 0) {
                    for (String s : l) {
                        if (s != null) {
                            String word = s.trim();
                            if (word.length() > 0) {
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
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = getContentResolver().query(contentURI, proj,
                    null, null, null);
            int column_index = cursor != null ? cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA) : 0;
            if (cursor != null && cursor.moveToFirst())
                result = cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return result;
    }

    private String getContentFromFile(String fileName) {
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.spell4wiki_view_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    private void loadLanguages() {
        OnLanguageSelectionListener callback = langCode -> {
            languageCode = langCode;
            invalidateOptionsMenu();
        };
        LanguageSelectionFragment languageSelectionFragment = new LanguageSelectionFragment(this);
        languageSelectionFragment.init(callback, SPELL_4_WORD_LIST);
        languageSelectionFragment.show(getSupportFragmentManager(), languageSelectionFragment.getTag());
    }

    private void setupLanguageSelectorMenuItem(Menu menu) {
        MenuItem item = menu.findItem(R.id.menu_lang_selector);
        item.setVisible(true);
        View rootView = item.getActionView();
        TextView selectedLang = rootView.findViewById(R.id.txtSelectedLanguage);
        selectedLang.setText(this.languageCode.toUpperCase());
        rootView.setOnClickListener(v -> {
            loadLanguages();
        });

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        setupLanguageSelectorMenuItem(menu);
        return super.onPrepareOptionsMenu(menu);
    }

}
