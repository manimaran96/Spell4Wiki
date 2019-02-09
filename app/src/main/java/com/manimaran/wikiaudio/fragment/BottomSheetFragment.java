package com.manimaran.wikiaudio.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.lang_selection.LangAdapter;
import com.manimaran.wikiaudio.listerner.OnItemClickListener;
import com.manimaran.wikiaudio.model.Language;
import com.manimaran.wikiaudio.util.GeneralUtils;
import com.manimaran.wikiaudio.util.PrefManager;

import java.util.List;

public class BottomSheetFragment extends BottomSheetDialogFragment {

    PrefManager pref;
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
        if(getActivity() != null) {
            pref = new PrefManager(getContext());


            final BottomSheetDialog dialog = new BottomSheetDialog(getActivity());

            dialog.setContentView(R.layout.bottom_sheet_language_selection);

            ListView listView = dialog.findViewById(R.id.list_view_lang);
            ImageView btnClose = dialog.findViewById(R.id.btn_close);
            final SearchView searchView = dialog.findViewById(R.id.search_view);

            List<Language> languageList = GeneralUtils.getLanguageListFromJson(getContext());

            OnItemClickListener listener = new OnItemClickListener() {
                @Override
                public void OnClickListener(String langCode, String lang) {
                    pref.setLangCode(langCode);
                    GeneralUtils.showToast(getContext(), String.format(getString(R.string.select_language_response_msg), lang));
                    dismiss();
                }
            };

            final LangAdapter adapter = new LangAdapter(getActivity(), languageList, listener);
            if (listView != null) {
                listView.setAdapter(adapter);
            }

            if(btnClose != null) {
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

                    FrameLayout bottomSheet = (FrameLayout) d.findViewById(android.support.design.R.id.design_bottom_sheet);
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

            if(searchView != null)
            {
                searchView.setQueryHint(getString(R.string.search_here));
                searchView.setQueryRefinementEnabled(true);
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        adapter.getFilter().filter(newText);
                        return false;
                    }
                });
            }

            return dialog;
        }else
            return null;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

    }
}