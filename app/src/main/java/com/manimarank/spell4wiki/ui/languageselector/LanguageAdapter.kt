package com.manimarank.spell4wiki.ui.languageselector;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.manimarank.spell4wiki.R;
import com.manimarank.spell4wiki.data.db.entities.WikiLang;
import com.manimarank.spell4wiki.ui.listerners.OnLanguageSelectionListener;
import com.manimarank.spell4wiki.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.ViewHolder> implements Filterable {

    private Activity mActivity;
    private List<WikiLang> mList;
    private List<WikiLang> mBackUpList;
    private String existLangCode;
    private OnLanguageSelectionListener mListener;

    public LanguageAdapter(Activity activity, List<WikiLang> list, OnLanguageSelectionListener listener, String existLangCode) {
        this.mActivity = activity;
        this.mList = list;
        this.mBackUpList = list;
        this.existLangCode = existLangCode;
        this.mListener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        int pos = holder.getAdapterPosition();
        final WikiLang model = mList.get(pos);

        String localName = model.getLocalName() + " : " + model.getCode();
        holder.txtLanguage.setText(model.getName());
        holder.txtLocalName.setText(localName);
        holder.txtLocalName.setGravity(model.getIsLeftDirection() ? Gravity.START : Gravity.END);
        holder.radioSelect.setChecked(existLangCode != null && existLangCode.equals(model.getCode()));

        holder.layout.setOnClickListener(view1 -> {
            holder.radioSelect.setChecked(true);
            ToastUtils.INSTANCE.showLong(String.format(mActivity.getString(R.string.select_language_response_msg), model.getName()));
            mListener.onCallBackListener(model.getCode());
        });

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_language_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                if (constraint != null && constraint.length() > 0) {
                    List<WikiLang> filterList = new ArrayList<>();
                    for (WikiLang l : mBackUpList) {
                        if ((l.getName().toLowerCase() + " " + l.getLocalName().toLowerCase() + " " + l.getCode().toLowerCase()).contains(constraint.toString().toLowerCase())) {
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
                mList = castList(results.values, WikiLang.class);
                notifyDataSetChanged();
            }
        };
    }

    public static <T> List<T> castList(Object obj, Class<T> clazz) {
        List<T> result = new ArrayList<>();
        if(obj instanceof List<?>) {
            for (Object o : (List<?>) obj) {
                result.add(clazz.cast(o));
            }
            return result;
        }
        return result;
    }

    /* adapter view holder */
    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView txtLanguage, txtLocalName;
        private RadioButton radioSelect;
        private View layout;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtLanguage = itemView.findViewById(R.id.txtLanguage);
            txtLocalName = itemView.findViewById(R.id.txtLocalName);
            radioSelect = itemView.findViewById(R.id.radioSelect);
            layout = itemView.findViewById(R.id.layoutItem);
        }

    }

}
