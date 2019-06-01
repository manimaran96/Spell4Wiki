package com.manimaran.wikiaudio.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.acticity.SearchActivity;
import com.manimaran.wikiaudio.acticity.Spell4Wiktionary;
import com.manimaran.wikiaudio.acticity.UploadToCommonsActivity;
import com.manimaran.wikiaudio.utils.GeneralUtils;

import org.jetbrains.annotations.NotNull;

public class OptionAdapter extends RecyclerView.Adapter<OptionAdapter.ViewHolder> {

    // Context
    private Context mContext;
    private GeneralUtils generalUtils;

    // Variables
    private int[] optionNames = {R.string.spell4explore, R.string.spell4wiktionary, R.string.spell4wordlist, R.string.spell4word};
    private int[] optionIcons = {R.drawable.ic_spell4explore, R.drawable.ic_spell4wiki, R.drawable.ic_spell4wordlist, R.drawable.ic_spell4word};

    public OptionAdapter(Context applicationContext) {
        this.mContext = applicationContext;
        this.generalUtils = new GeneralUtils();
    }

    @NotNull
    @Override
    public OptionAdapter.ViewHolder onCreateViewHolder(@NotNull ViewGroup viewGroup, int i) {

        mContext = viewGroup.getContext();
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_main_option, viewGroup, false);
        ViewHolder viewHolder = new OptionAdapter.ViewHolder(v);

        int dummyHeight = 300;

        v.getLayoutParams().height = (generalUtils.getScreenHeight(mContext) - dummyHeight) / 3;

       /* v.getLayoutParams().height = generalUtils.getScreenWidth(mContext) / 2;
        v.getLayoutParams().width = generalUtils.getScreenWidth(mContext) / 2;*/

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NotNull ViewHolder viewHolder, final int pos) {

        final int i = viewHolder.getAdapterPosition();

        viewHolder.imgView.setImageResource(optionIcons[i]);
        viewHolder.textTitle.setText(optionNames[i]);

        viewHolder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Class nextClass = SearchActivity.class;
                switch (i) {
                    case 0:
                        nextClass = SearchActivity.class;
                        break;
                    case 1:
                        nextClass = Spell4Wiktionary.class;
                        break;
                    case 2:
                        nextClass = UploadToCommonsActivity.class;
                        break;
                }
                mContext.startActivity(new Intent(mContext, nextClass));
            }
        });

    }


    @Override
    public int getItemCount() {
        return optionIcons.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public CardView layout;
        private TextView textTitle;
        private ImageView imgView;

        ViewHolder(View itemView) {
            super(itemView);

            textTitle = itemView.findViewById(R.id.txt_option_name);
            imgView = itemView.findViewById(R.id.img_option_icon);
            layout = itemView.findViewById(R.id.card_layout);
        }
    }
}
