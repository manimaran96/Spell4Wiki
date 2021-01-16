package com.manimarank.spell4wiki.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.ColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.SimpleColorFilter;
import com.airbnb.lottie.model.KeyPath;
import com.airbnb.lottie.value.LottieValueCallback;
import com.manimarank.spell4wiki.R;
import com.manimarank.spell4wiki.data.model.ItemsModel;
import com.manimarank.spell4wiki.utils.GeneralUtils;

import java.util.List;

public class ListItemAdapter extends RecyclerView.Adapter<ListItemAdapter.ViewHolder> {

    private List<ItemsModel> mList;
    private Context mContext;

    public ListItemAdapter(Context context, List<ItemsModel> list) {
        this.mContext = context;
        this.mList = list;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        int pos = holder.getAdapterPosition();
        final ItemsModel model = mList.get(pos);
        holder.txtName.setText(model.getName());
        if (model.getIcon() != -1) {
            if (model.isLottie()) {
                holder.lottieAnimationView.setVisibility(View.VISIBLE);
                holder.lottieAnimationView.setAnimation(model.getIcon());
                try {
                    int filterColor = ContextCompat.getColor(mContext, model.getName().contains("Upload animation") ? R.color.w_blue : R.color.transparent);
                    SimpleColorFilter filter = new SimpleColorFilter(filterColor);
                    KeyPath keyPath = new KeyPath("**");
                    LottieValueCallback<ColorFilter> callback = new LottieValueCallback<>(filter);
                    holder.lottieAnimationView.addValueCallback(keyPath, LottieProperty.COLOR_FILTER, callback);
                }catch (Exception e){
                    e.printStackTrace();
                }
            } else {
                holder.imgIcon.setVisibility(View.VISIBLE);
                holder.imgIcon.setImageDrawable(ContextCompat.getDrawable(mContext, model.getIcon()));
            }
        } else {
            holder.imgIcon.setVisibility(View.GONE);
            holder.lottieAnimationView.setVisibility(View.GONE);
        }
        holder.txtAbout.setText(model.getAbout());

        holder.btnOption.setOnClickListener(v -> {
            Activity activity = (Activity) mContext;
            if (activity != null)
                GeneralUtils.openUrlInBrowser(activity, model.getUrl());
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    /* adapter view holder */
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtName, txtAbout;
        ImageView imgIcon, btnOption;
        LottieAnimationView lottieAnimationView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            lottieAnimationView = itemView.findViewById(R.id.lottieAnimationView);
            txtName = itemView.findViewById(R.id.txt_name);
            txtAbout = itemView.findViewById(R.id.txt_about);
            btnOption = itemView.findViewById(R.id.btn_option);
        }

    }

}
