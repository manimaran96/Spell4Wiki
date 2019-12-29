package com.manimaran.wikiaudio.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.constant.UrlType;
import com.manimaran.wikiaudio.model.ItemsModel;
import com.manimaran.wikiaudio.utils.GeneralUtils;

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
        if(model.getIcon() != -1) {
            holder.imgIcon.setImageDrawable(ContextCompat.getDrawable(mContext, model.getIcon()));
        }
        holder.txtAbout.setText(model.getAbout());

        holder.btnOption.setOnClickListener(v -> {
            //creating a popup menu
            PopupMenu popup = new PopupMenu(mContext, holder.btnOption);
            //inflating menu from xml resource
            popup.inflate(R.menu.item_menu);
            //adding click listener
            popup.setOnMenuItemClickListener(item -> {
                int i = item.getItemId();
                String url = null;
                if (i == R.id.web_page) {//handle web page
                    url = model.getUrl();
                } else if (i == R.id.license_url) {//handle license url
                    url = model.getLicenseUrl();
                }
                Activity activity = (Activity) mContext;
                GeneralUtils.openUrl(activity, url, UrlType.EXTERNAL, null);
                return false;
            });
            //displaying the popup
            popup.show();
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_list, parent, false);
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
