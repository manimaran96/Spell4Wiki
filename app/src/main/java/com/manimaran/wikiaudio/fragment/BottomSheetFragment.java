package com.manimaran.wikiaudio.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import androidx.appcompat.widget.SearchView;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.adapter.LangAdapter;
import com.manimaran.wikiaudio.constant.UrlType;
import com.manimaran.wikiaudio.listerner.CallBackListener;
import com.manimaran.wikiaudio.listerner.OnLangSelectListener;
import com.manimaran.wikiaudio.model.WikiLanguage;
import com.manimaran.wikiaudio.utils.GeneralUtils;
import com.manimaran.wikiaudio.utils.PrefManager;
import com.manimaran.wikiaudio.wiki_api.MediaWikiClient;
import com.manimaran.wikiaudio.wiki_api.ServiceGenerator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BottomSheetFragment extends BottomSheetDialogFragment {

    private PrefManager pref;
    private CallBackListener callback;
    private List<WikiLanguage> wikiLanguageList = new ArrayList<>();
    private LangAdapter adapter;
    private Boolean isWiktionaryMode = false;

    public BottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getActivity() != null) {
            pref = new PrefManager(getContext());


            final BottomSheetDialog dialog = new BottomSheetDialog(getActivity());

            dialog.setContentView(R.layout.bottom_sheet_language_selection);

            final ProgressBar progressBar = dialog.findViewById(R.id.pb);
            final ListView listView = dialog.findViewById(R.id.list_view_lang);
            ImageView btnClose = dialog.findViewById(R.id.btn_close);
            final SearchView searchView = dialog.findViewById(R.id.search_view);


            if (progressBar != null)
                progressBar.setVisibility(View.VISIBLE);
            if (listView != null)
                listView.setVisibility(View.GONE);

            MediaWikiClient api = ServiceGenerator.createService(MediaWikiClient.class, getContext(), UrlType.WIKTIONARY_CONTRIBUTION);
            Call<ResponseBody> call = api.fetchWikiLangList();
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseStr = response.body().string();
                            List<WikiLanguage> langList = new ArrayList<>();
                            try {
                                JSONArray array = new JSONArray(responseStr);
                                int len = array.length();
                                int i;
                                for (i = 0; i < len; i++) {
                                    JSONObject obj = array.getJSONObject(i);
                                    WikiLanguage lang = new WikiLanguage();
                                    lang.setCode(obj.getString("code"));
                                    lang.setName(obj.getString("lang"));
                                    lang.setIsLeftDirection(obj.getString("dir").equals("ltr"));
                                    lang.setLocal(obj.getString("local_lang"));

                                    /*
                                     * Check Wiktionary mode or not
                                     * If wiktionary mode show all languages
                                     * If Contribution mode show only language have "title_words_without_audio" key-value
                                     * "title_words_without_audio" - category of words without audio in wiktionary
                                     */
                                    if (getIsWiktionaryMode()) {
                                        langList.add(lang);
                                    } else {
                                        if (obj.has("title_words_without_audio")) {
                                            lang.setTitleWordsNoAudio(obj.getString("title_words_without_audio"));
                                            langList.add(lang);
                                        }
                                    }
                                }

                                OnLangSelectListener listener = new OnLangSelectListener() {
                                    @Override
                                    public void OnClickListener(String langCode, String lang, String titleWordsWithoutAudio) {
                                        if (getIsWiktionaryMode())
                                            pref.setWiktionaryLangCode(langCode);
                                        else {
                                            pref.setContributionLangCode(langCode);
                                            if (titleWordsWithoutAudio != null)
                                                pref.setTitleWordsWithoutAudio(titleWordsWithoutAudio);
                                        }
                                        GeneralUtils.showToast(getContext(), String.format(getString(R.string.select_language_response_msg), lang));
                                        if (callback != null)
                                            callback.OnCallBackListener();
                                        dismiss();
                                    }
                                };

                                wikiLanguageList = langList;
                                String existLangCode = getIsWiktionaryMode() ? pref.getWiktionaryLangCode() : pref.getContributionLangCode();
                                adapter = new LangAdapter(getActivity(), wikiLanguageList, listener, existLangCode);
                                if (listView != null) {
                                    listView.setAdapter(adapter);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (progressBar != null)
                        progressBar.setVisibility(View.GONE);
                    if (listView != null)
                        listView.setVisibility(View.VISIBLE);

                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    t.printStackTrace();
                }
            });


            if (btnClose != null) {
                btnClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dismiss();
                    }
                });
            }

            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    BottomSheetDialog d = (BottomSheetDialog) dialog;

                    FrameLayout bottomSheet = (FrameLayout) d.findViewById(R.id.design_bottom_sheet);
                    if (bottomSheet != null) {
                        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
                        behavior.setHideable(false);
                        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                        // Full screen mode no collapse
                        DisplayMetrics displaymetrics = new DisplayMetrics();
                        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                        int screenHeight = displaymetrics.heightPixels;
                        behavior.setPeekHeight(screenHeight);
                    }

                }
            });

            if (searchView != null) {
                searchView.setQueryHint(getString(R.string.search_here));
                searchView.setQueryRefinementEnabled(true);
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        if (newText.length() > 0 && adapter != null)
                            adapter.getFilter().filter(newText);
                        return false;
                    }
                });
            }

            return dialog;
        } else
            return null;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

    }

    public void setCalBack(CallBackListener listener) {
        this.callback = listener;
    }

    public Boolean getIsWiktionaryMode() {
        return isWiktionaryMode;
    }

    public void setIsWiktionaryMode(Boolean wiktionary) {
        isWiktionaryMode = wiktionary;
    }
}