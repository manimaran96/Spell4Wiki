package com.manimaran.wikiaudio.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.lang_selection.LangAdapter;
import com.manimaran.wikiaudio.util.GeneralUtils;
import com.manimaran.wikiaudio.util.PrefManager;

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

            final LangAdapter adapter = new LangAdapter(getActivity(), GeneralUtils.getLanguageListFromJson(getContext()));
            if (listView != null) {
                listView.setAdapter(adapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        GeneralUtils.showToast(getContext(), "Click");
                        pref.setLangCode(adapter.getItem(i).getCode());
                        GeneralUtils.showToast(getContext(), "Code " + pref.getLangCode());
                        dismiss();
                    }
                });
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