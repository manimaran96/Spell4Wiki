package com.manimaran.wikiaudio.adapters;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.constants.UrlType;
import com.manimaran.wikiaudio.models.ItemsModel;
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
            holder.imgIcon.setVisibility(View.VISIBLE);
            holder.imgIcon.setImageDrawable(ContextCompat.getDrawable(mContext, model.getIcon()));
        }else{
            holder.imgIcon.setVisibility(View.GONE);
        }
        holder.txtAbout.setText(model.getAbout());

        holder.btnOption.setOnClickListener(v -> {
            Activity activity = (Activity) mContext;
            if(model.getLicenseUrl() != null) {
                //creating a popup menu
                PopupMenu popup = new PopupMenu(mContext, holder.btnOption);
                //inflating menu from xml resource
                popup.inflate(R.menu.list_item_menu);
                //adding click listener
                popup.setOnMenuItemClickListener(item -> {
                    int i = item.getItemId();
                    String url = null;
                    if (i == R.id.web_page) {//handle web page
                        url = model.getUrl();
                    } else if (i == R.id.license_url) {//handle license url
                        url = model.getLicenseUrl();
                    }

                    GeneralUtils.openUrl(activity, url, UrlType.EXTERNAL, null);
                    return false;
                });
                //displaying the popup
                popup.show();
            }else {
                GeneralUtils.openUrl(activity, model.getUrl(), UrlType.EXTERNAL, null);
            }
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
