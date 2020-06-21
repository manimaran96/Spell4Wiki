package com.manimarank.spell4wiki.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.manimarank.spell4wiki.R;
import com.manimarank.spell4wiki.adapters.LanguageAdapter;
import com.manimarank.spell4wiki.databases.DBHelper;
import com.manimarank.spell4wiki.databases.entities.WikiLang;
import com.manimarank.spell4wiki.listerners.OnLanguageSelectionListener;
import com.manimarank.spell4wiki.utils.GeneralUtils;
import com.manimarank.spell4wiki.utils.NetworkUtils;
import com.manimarank.spell4wiki.utils.PrefManager;
import com.manimarank.spell4wiki.utils.SnackBarUtils;
import com.manimarank.spell4wiki.utils.constants.EnumTypeDef.ListMode;
import com.manimarank.spell4wiki.utils.constants.Urls;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class LanguageSelectionFragment extends BottomSheetDialogFragment {

    private PrefManager pref;
    private OnLanguageSelectionListener callback;
    private List<WikiLang> wikiLanguageList = new ArrayList<>();
    private LanguageAdapter adapter;
    @ListMode
    private int listMode;
    private String preSelectedLanguageCode = null;

    private Activity mActivity;

    public LanguageSelectionFragment(Activity activity) {
        this.mActivity = activity;
    }

    public void init(OnLanguageSelectionListener callback, @ListMode int mode) {
        init(callback, mode, null);
    }

    public void init(OnLanguageSelectionListener callback, @ListMode int mode, String preSelectedLanguageCode) {
        this.callback = callback;
        this.listMode = mode;
        this.preSelectedLanguageCode = preSelectedLanguageCode;
        setCancelable(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        pref = new PrefManager(getContext());
        if (TextUtils.isEmpty(preSelectedLanguageCode))
            preSelectedLanguageCode = getExistingLanguageCode();

        final BottomSheetDialog dialog = new BottomSheetDialog(mActivity, R.style.AppTheme);

        dialog.setContentView(R.layout.bottom_sheet_language_selection);

        TextView txtTitle = dialog.findViewById(R.id.text_select_lang_title);
        if (!TextUtils.isEmpty(getSubTitleInfo()) && txtTitle != null) {
            txtTitle.setVisibility(View.VISIBLE);
            txtTitle.setText(getSubTitleInfo());
        }
        final RecyclerView recyclerView = dialog.findViewById(R.id.recyclerView);
        ImageView btnClose = dialog.findViewById(R.id.btn_close);
        final SearchView searchView = dialog.findViewById(R.id.search_view);
        View layoutAddLanguage = dialog.findViewById(R.id.layoutAddLanguage);
        Button btnAddMyLanguage = dialog.findViewById(R.id.btnAddMyLanguage);

        DBHelper dbHelper = DBHelper.getInstance(getContext());

        /*
         * Check Wiktionary mode or not
         * If wiktionary mode show all languages
         * If Contribution mode show only language have "title_words_without_audio" key-value
         * "title_words_without_audio" - category of words without audio in wiktionary
         */
        wikiLanguageList.clear();
        if (listMode == ListMode.SPELL_4_WIKI) {
            wikiLanguageList = dbHelper.getAppDatabase().getWikiLangDao().getWikiLanguageListForWordsWithoutAudio();
            if (layoutAddLanguage != null) {
                layoutAddLanguage.setVisibility(View.VISIBLE);
            }
            if (btnAddMyLanguage != null) {
                btnAddMyLanguage.setOnClickListener(v -> {
                    if (getActivity() != null && NetworkUtils.INSTANCE.isConnected(getActivity())) {
                        GeneralUtils.openUrlInBrowser(getActivity(), Urls.FORM_ADD_MY_LANGUAGE);
                    } else
                        SnackBarUtils.INSTANCE.showNormal(btnAddMyLanguage, getString(R.string.check_internet));
                });
            }
        } else {
            wikiLanguageList = dbHelper.getAppDatabase().getWikiLangDao().getWikiLanguageList();
            if (layoutAddLanguage != null) {
                layoutAddLanguage.setVisibility(View.GONE);
            }
        }

        OnLanguageSelectionListener languageSelectionListener = langCode -> {
            switch (listMode) {
                case ListMode.SPELL_4_WIKI:
                    pref.setLanguageCodeSpell4Wiki(langCode);
                    break;
                case ListMode.SPELL_4_WORD_LIST:
                    pref.setLanguageCodeSpell4WordList(langCode);
                    break;
                case ListMode.SPELL_4_WORD:
                    pref.setLanguageCodeSpell4Word(langCode);
                    break;
                case ListMode.WIKTIONARY:
                    pref.setLanguageCodeWiktionary(langCode);
                    break;
                case ListMode.TEMP:
                    break;
            }

            if (callback != null)
                callback.OnCallBackListener(langCode);
            dismiss();
        };

        adapter = new LanguageAdapter(getActivity(), wikiLanguageList, languageSelectionListener, preSelectedLanguageCode);
        if (recyclerView != null) {
            recyclerView.setAdapter(adapter);
        }

        if (btnClose != null) {
            btnClose.setOnClickListener(view -> dismiss());
        }

        dialog.setOnShowListener(dialog1 -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog1;

            FrameLayout bottomSheet = d.findViewById(R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setHideable(false);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                // Full screen mode no collapse
                DisplayMetrics displaymetrics = new DisplayMetrics();
                mActivity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                int screenHeight = displaymetrics.heightPixels;
                behavior.setPeekHeight(screenHeight);
            }

        });

        if (searchView != null) {
            searchView.setQueryHint(getString(R.string.search));
            searchView.setQueryRefinementEnabled(true);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    if (adapter != null)
                        adapter.getFilter().filter(newText);
                    return false;
                }
            });
        }
        return dialog;
    }

    private String getExistingLanguageCode() {
        switch (listMode) {
            case ListMode.SPELL_4_WIKI:
                return pref.getLanguageCodeSpell4Wiki();
            case ListMode.SPELL_4_WORD_LIST:
                return pref.getLanguageCodeSpell4WordList();
            case ListMode.SPELL_4_WORD:
                return pref.getLanguageCodeSpell4Word();
            case ListMode.WIKTIONARY:
                return pref.getLanguageCodeWiktionary();
            case ListMode.TEMP:
            default:
                return null;
        }
    }

    private String getSubTitleInfo() {
        String info = null;
        switch (listMode) {
            case ListMode.SPELL_4_WIKI:
                info = getString(R.string.spell4wiktionary);
                break;
            case ListMode.SPELL_4_WORD_LIST:
                info = getString(R.string.spell4wordlist);
                break;
            case ListMode.SPELL_4_WORD:
                info = getString(R.string.spell4word);
                break;
            case ListMode.WIKTIONARY:
                info = getString(R.string.wiktionary);
                break;
            case ListMode.TEMP:
                info = getString(R.string.temporary);
        }
        if (info != null) {
            info = String.format(getString(R.string.language_for_note), info);
        }
        return info;
    }

    @Override
    public void onCancel(@NotNull DialogInterface dialog) {
        super.onCancel(dialog);
    }

    public void show(FragmentManager fragmentManager) {
        try {
            if (fragmentManager.findFragmentByTag(getTagValue()) != null)
                return;
        } catch (Exception ignore) {

        }
        show(fragmentManager, getTagValue());
    }

    public String getTagValue() {
        return "LANGUAGE_FRAGMENT";
    }
}