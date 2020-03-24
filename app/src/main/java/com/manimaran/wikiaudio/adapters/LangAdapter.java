package com.manimaran.wikiaudio.adapters;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.databases.entities.WikiLang;
import com.manimaran.wikiaudio.listerners.OnLanguageSelectionListener;
import com.manimaran.wikiaudio.utils.GeneralUtils;

import java.util.ArrayList;
import java.util.List;

public class LangAdapter extends BaseAdapter implements Filterable {

    private Activity mActivity;
    private List<WikiLang> mList;
    private List<WikiLang> mBackUpList;
    private FilterVal valueFilter;
    private String existLangCode;
    private OnLanguageSelectionListener mListener;

    public LangAdapter(Activity activity, List<WikiLang> list, OnLanguageSelectionListener listener, String existLangCode) {
        this.mActivity = activity;
        this.mList = list;
        this.mBackUpList = list;
        this.existLangCode = existLangCode;
        this.mListener = listener;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public WikiLang getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view == null) {
            view = mActivity.getLayoutInflater().inflate(R.layout.lang_list_item, viewGroup, false);
            holder = new ViewHolder();
            holder.layout = view.findViewById(R.id.layout_lang_item);
            holder.textLangName = view.findViewById(R.id.txt_lang_name);
            holder.textLocalName = view.findViewById(R.id.txt_local_name);
            holder.radioSelect = view.findViewById(R.id.radio_select);
            view.setTag(holder);
        } else
            holder = (ViewHolder) view.getTag();

        final WikiLang model = mList.get(i);

        holder.textLangName.setText(model.getName());
        holder.textLocalName.setText(model.getLocalName());
        /*if(model.getTitleWordsNoAudio() != null && model.getTitleWordsNoAudio().length() > 0)
            holder.textLocalName.append(" - " + model.getTitleWordsNoAudio());*/
        holder.textLocalName.setGravity(model.getIsLeftDirection() ? Gravity.START : Gravity.END);
        holder.radioSelect.setChecked(existLangCode != null && existLangCode.equals(model.getCode()));

        final ViewHolder finalHolder = holder;
        holder.layout.setOnClickListener(view1 -> {
            finalHolder.radioSelect.setChecked(true);
            GeneralUtils.showToast(mActivity, String.format(mActivity.getString(R.string.select_language_response_msg), model.getName()));
            mListener.OnCallBackListener(model.getCode());
        });

        return view;

    }

    @Override
    public Filter getFilter() {
        if (valueFilter == null) {
            valueFilter = new FilterVal();
        }
        return valueFilter;
    }

    private static class ViewHolder {
        TextView textLangName, textLocalName;
        RadioButton radioSelect;
        RelativeLayout layout;
    }

    private class FilterVal extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                List<WikiLang> filterList = new ArrayList<>();
                for (WikiLang l : mBackUpList) {
                    if ((l.getName().toLowerCase() + " " + l.getLocalName().toLowerCase()).contains(constraint.toString().toLowerCase())) {
                        filterList.add(l);
                    }
                }
                results.count = filterList.size();
                results.values = filterList;
            } else {
                results.count = mBackUpList.size();
                results.values = mBackUpList;
            }
            return results;

        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mList = (List<WikiLang>) results.values;
            notifyDataSetChanged();
        }
    }
}