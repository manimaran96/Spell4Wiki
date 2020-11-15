package com.manimarank.spell4wiki.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.manimarank.spell4wiki.R;
import com.manimarank.spell4wiki.activities.base.BaseActivity;
import com.manimarank.spell4wiki.adapters.EndlessRecyclerAdapter;
import com.manimarank.spell4wiki.databases.DBHelper;
import com.manimarank.spell4wiki.databases.dao.WordsHaveAudioDao;
import com.manimarank.spell4wiki.databases.entities.WordsHaveAudio;
import com.manimarank.spell4wiki.fragments.LanguageSelectionFragment;
import com.manimarank.spell4wiki.listerners.OnLanguageSelectionListener;
import com.manimarank.spell4wiki.utils.ExtensionsKt;
import com.manimarank.spell4wiki.utils.GeneralUtils;
import com.manimarank.spell4wiki.utils.PrefManager;
import com.manimarank.spell4wiki.utils.RealPathUtil;
import com.manimarank.spell4wiki.utils.ShowCasePref;
import com.manimarank.spell4wiki.utils.SnackBarUtils;
import com.manimarank.spell4wiki.utils.constants.AppConstants;
import com.manimarank.spell4wiki.views.EndlessRecyclerView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetSequence;
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal;

import static com.manimarank.spell4wiki.utils.constants.EnumTypeDef.ListMode.SPELL_4_WORD_LIST;


public class Spell4WordListActivity extends BaseActivity {


    private EditText editFile;
    private TextView txtFileInfo;
    private View layoutEdit, layoutSelect, layoutEmpty;

    private EndlessRecyclerView recyclerView;
    private EndlessRecyclerAdapter adapter;
    private String languageCode = "";
    private WordsHaveAudioDao wordsHaveAudioDao;

    private PrefManager pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spell_4_wordlist);

        pref = new PrefManager(getApplicationContext());
        languageCode = pref.getLanguageCodeSpell4WordList();

        initUI();
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
        // Remove styles after paste/added content
        ExtensionsKt.removeStyleAfterPaste(editFile);
        txtFileInfo = findViewById(R.id.txtFileInfo);
        recyclerView = findViewById(R.id.recyclerView);
        layoutSelect = findViewById(R.id.layoutSelect);
        layoutEdit = findViewById(R.id.layoutEdit);
        layoutEmpty = findViewById(R.id.layoutEmpty);


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
                SnackBarUtils.INSTANCE.showLong(editFile, getString(R.string.provide_valid_content));

        });


        layoutSelect.setVisibility(View.VISIBLE);
        layoutEdit.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);

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

        startActivityForResult(intent, AppConstants.RC_EDIT_REQUEST_CODE);
    }

    public void updateList(String word) {
        if(!isDestroyed() && !isFinishing()) {
            if (adapter != null) {
                wordsHaveAudioDao.insert(new WordsHaveAudio(word, languageCode));
                adapter.addWordInWordsHaveAudioList(word);
                adapter.remove(word);
                if (adapter.getItemCount() == 0)
                    showEmptyView();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent data) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        super.onActivityResult(requestCode, resultCode, data);
        if(!isDestroyed() && !isFinishing()) {
            if (requestCode == AppConstants.RC_EDIT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
                // The document selected by the user won't be returned in the intent.
                // Instead, a URI to that document will be contained in the return intent
                // provided to this method as a parameter.
                // Pull that URI using resultData.getData().
                Uri uri;
                if (data != null) {
                    uri = data.getData();
                    assert uri != null;
                    File file = new File(RealPathUtil.getRealPath(getApplicationContext(), uri));
                    openFileInAlignMode(file.getAbsolutePath());
                }
            }

            if (requestCode == AppConstants.RC_UPLOAD_DIALOG) {
                if (data != null && data.hasExtra(AppConstants.WORD)) {
                    if (adapter != null) {
                        adapter.addWordInWordsHaveAudioList(data.getStringExtra(AppConstants.WORD));
                        adapter.remove(data.getStringExtra(AppConstants.WORD));
                        if (adapter.getItemCount() == 0)
                            showEmptyView();
                    }
                }
            }
        }
    }

    private void openFileInAlignMode(String filePath) {

        layoutSelect.setVisibility(View.GONE);
        layoutEdit.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);

        txtFileInfo.setText(getString(R.string.hint_select_file_next));
        editFile.setText(getContentFromFile(filePath));
    }

    private void showDirectContentAlignMode() {
        layoutSelect.setVisibility(View.GONE);
        layoutEdit.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);

        txtFileInfo.setText(getString(R.string.hint_direct_copy_next));
        editFile.setText("");
    }

    private void showWordsInRecordMode(List<String> items) {

        List<String> wordsAlreadyHaveAudio = wordsHaveAudioDao.getWordsAlreadyHaveAudioByLanguage(languageCode);
        items.removeAll(wordsAlreadyHaveAudio);
        if (items.size() > 0) {
            layoutSelect.setVisibility(View.GONE);
            layoutEdit.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);

            recyclerView.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);

            adapter = new EndlessRecyclerAdapter(this, items, SPELL_4_WORD_LIST);
            recyclerView.setAdapter(adapter, layoutManager);
            adapter.setWordsHaveAudioList(wordsHaveAudioDao.getWordsAlreadyHaveAudioByLanguage(languageCode));

            if (ShowCasePref.INSTANCE.isNotShowed(ShowCasePref.LIST_ITEM_SPELL_4_WIKI))
                new Handler().post(this::callShowCaseUI);
        } else {
            showEmptyView();
        }
    }

    private void showEmptyView() {
        layoutSelect.setVisibility(View.GONE);
        layoutEdit.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
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
            callBackPress();
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    private void loadLanguages() {
        OnLanguageSelectionListener callback = langCode -> {
            if (!languageCode.equals(langCode)) {
                languageCode = langCode;
                invalidateOptionsMenu();
                if (recyclerView.getVisibility() == View.VISIBLE || layoutEmpty.getVisibility() == View.VISIBLE) {
                    if (!TextUtils.isEmpty(editFile.getText())) {
                        List<String> items = getWordListFromString(editFile.getText().toString());
                        showWordsInRecordMode(items);
                    }
                }
            }
        };
        LanguageSelectionFragment languageSelectionFragment = new LanguageSelectionFragment(this);
        languageSelectionFragment.init(callback, SPELL_4_WORD_LIST);
        languageSelectionFragment.show(getSupportFragmentManager());
    }

    private void setupLanguageSelectorMenuItem(Menu menu) {
        MenuItem item = menu.findItem(R.id.menu_lang_selector);
        item.setVisible(true);
        View rootView = item.getActionView();
        TextView selectedLang = rootView.findViewById(R.id.txtSelectedLanguage);
        selectedLang.setText(this.languageCode.toUpperCase());
        rootView.setOnClickListener(v -> loadLanguages());
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        setupLanguageSelectorMenuItem(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        callBackPress();
    }

    private void callBackPress() {
        if (recyclerView.getVisibility() == View.VISIBLE || layoutEmpty.getVisibility() == View.VISIBLE) {
            layoutEdit.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.GONE);
        } else if (layoutEdit.getVisibility() == View.VISIBLE && pref.getAbortAlertStatus()) {
            if (!TextUtils.isEmpty(editFile.getText())) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.confirmation);
                builder.setMessage(R.string.confirm_to_back);
                builder.setCancelable(false);
                builder.setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    layoutSelect.setVisibility(View.VISIBLE);
                    layoutEdit.setVisibility(View.GONE);
                });
                builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                layoutSelect.setVisibility(View.VISIBLE);
                layoutEdit.setVisibility(View.GONE);
            }
        } else
            super.onBackPressed();
    }

    private void callShowCaseUI() {
        if (!isFinishing() && !isDestroyed()) {
            if (ShowCasePref.INSTANCE.isNotShowed(ShowCasePref.LIST_ITEM_SPELL_4_WIKI) && recyclerView != null && recyclerView.getVisibility() == View.VISIBLE && recyclerView.getChildAt(0) != null) {
                MaterialTapTargetSequence sequence = new MaterialTapTargetSequence().setSequenceCompleteListener(() -> ShowCasePref.INSTANCE.showed(ShowCasePref.LIST_ITEM_SPELL_4_WIKI));
                sequence.addPrompt(getPromptBuilder()
                        .setTarget(recyclerView.getChildAt(0))
                        .setPrimaryText(R.string.sc_t_spell4wiki_list_item)
                        .setSecondaryText(R.string.sc_d_spell4wiki_list_item));
                sequence.show();
            }
        }
    }

    private MaterialTapTargetPrompt.Builder getPromptBuilder() {
        return new MaterialTapTargetPrompt.Builder(Spell4WordListActivity.this)
                .setPromptFocal(new RectanglePromptFocal())
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .setFocalPadding(R.dimen.show_case_focal_padding);
    }
}
