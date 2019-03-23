package com.manimaran.wikiaudio.lang_selection;

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
import com.manimaran.wikiaudio.listerner.OnLangSelectListener;
import com.manimaran.wikiaudio.model.WikiLanguage;
import com.manimaran.wikiaudio.util.PrefManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LangAdapter extends BaseAdapter implements Filterable{

    private Activity mActivity;
    private List<WikiLanguage> mList;
    private List<WikiLanguage> mBackUpList;
    private FilterVal valueFilter;
    private String existLangCode;
    private OnLangSelectListener mListener;

    public LangAdapter(Activity activity, List<WikiLanguage> list, OnLangSelectListener listener, String existLangCode) {
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
    public WikiLanguage getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view == null){
            view = mActivity.getLayoutInflater().inflate(R.layout.lang_list_item, viewGroup, false);
            holder = new ViewHolder();
            holder.layout = (RelativeLayout) view.findViewById(R.id.layout_lang_item);
            holder.textLangName = (TextView)view.findViewById(R.id.txt_lang_name);
            holder.textLocalName = (TextView)view.findViewById(R.id.txt_local_name);
            holder.radioSelect = (RadioButton) view.findViewById(R.id.radio_select);
            view.setTag(holder);
        }else
            holder = (ViewHolder)view.getTag();

        final WikiLanguage model = mList.get(i);

        holder.textLangName.setText(model.getName());
        holder.textLocalName.setText(model.getLocal());
        /*if(model.getTitleWordsNoAudio() != null && model.getTitleWordsNoAudio().length() > 0)
            holder.textLocalName.append(" - " + model.getTitleWordsNoAudio());*/
        holder.textLocalName.setGravity(model.getIsLeftDirection() ? Gravity.START : Gravity.END);
        holder.radioSelect.setChecked(existLangCode.equals(model.getCode()));

        final ViewHolder finalHolder = holder;
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalHolder.radioSelect.setChecked(true);
                mListener.OnClickListener(model.getCode(), model.getName(), model.getTitleWordsNoAudio());
            }
        });

        return view;

    }

    private class ViewHolder{
        TextView textLangName, textLocalName;
        RadioButton radioSelect;
        RelativeLayout layout;
    }

    @Override
    public Filter getFilter() {
        if (valueFilter == null) {
            valueFilter = new FilterVal();
        }
        return valueFilter;
    }

    private class FilterVal extends Filter
    {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                List<WikiLanguage> filterList = new ArrayList<>();
                for (WikiLanguage l : mBackUpList) {
                    if ((l.getName().toLowerCase() + " " + l.getLocal().toLowerCase()).contains(constraint.toString().toLowerCase())) {
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
            mList = (List<WikiLanguage>) results.values;
            notifyDataSetChanged();
        }
    }
}