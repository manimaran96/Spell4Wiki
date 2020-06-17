package com.manimarank.spell4wiki.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.manimarank.spell4wiki.R;
import com.manimarank.spell4wiki.models.CoreContributors;
import com.manimarank.spell4wiki.utils.GeneralUtils;

import java.util.List;

public class CoreContributorsAdapter extends RecyclerView.Adapter<CoreContributorsAdapter.ViewHolder> {

    private List<CoreContributors> mList;
    private Context mContext;

    public CoreContributorsAdapter(Context context, List<CoreContributors> list) {
        this.mContext = context;
        this.mList = list;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        int pos = holder.getAdapterPosition();
        final CoreContributors model = mList.get(pos);
        holder.txtName.setText(model.getName());

        Glide.with(mContext).load(model.getImgLink()).placeholder(R.drawable.ic_contributors).apply(RequestOptions.circleCropTransform()).into(holder.imgIcon);

        holder.txtContribution.setText(model.getContribution());
        holder.txtAbout.setText(Html.fromHtml(model.getAbout()));

        holder.btnOption.setOnClickListener(v -> GeneralUtils.openUrlInBrowser(mContext, model.getLink()));

        holder.itemView.setOnClickListener(v -> holder.txtAbout.setVisibility(holder.txtAbout.getVisibility() != View.VISIBLE ? View.VISIBLE : View.GONE));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_core_contributors, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    /* adapter view holder */
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtName, txtAbout, txtContribution;
        ImageView imgIcon, btnOption;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            txtName = itemView.findViewById(R.id.txtName);
            txtContribution = itemView.findViewById(R.id.txtContribution);
            txtAbout = itemView.findViewById(R.id.txtAbout);
            btnOption = itemView.findViewById(R.id.btn_option);
        }

    }

}
