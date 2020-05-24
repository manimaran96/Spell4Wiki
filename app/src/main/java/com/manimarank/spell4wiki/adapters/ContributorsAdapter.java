package com.manimarank.spell4wiki.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.manimarank.spell4wiki.R;
import com.manimarank.spell4wiki.models.Contributors;
import com.manimarank.spell4wiki.utils.GeneralUtils;

import java.util.List;

public class ContributorsAdapter extends RecyclerView.Adapter<ContributorsAdapter.ViewHolder> {

    private List<Contributors> mList;
    private Context mContext;

    public ContributorsAdapter(Context context, List<Contributors> list) {
        this.mContext = context;
        this.mList = list;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        int pos = holder.getAdapterPosition();
        final Contributors model = mList.get(pos);
        holder.txtName.setText(model.getName());

        Glide.with(mContext).load(model.getAvatarUrl()).apply(RequestOptions.circleCropTransform()).into(holder.imgIcon);
        holder.txtAbout.setText(("Contributions : " + model.getContributions()));

        holder.btnOption.setOnClickListener(v -> {
            GeneralUtils.openUrlInBrowser(mContext, model.getHtmlUrl());
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_contributors, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    /* adapter view holder */
    class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtName, txtAbout;
        ImageView imgIcon, btnOption;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            txtName = itemView.findViewById(R.id.txt_name);
            txtAbout = itemView.findViewById(R.id.txt_about);
            btnOption = itemView.findViewById(R.id.btn_option);
        }

    }

}
