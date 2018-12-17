package com.manimaran.wikiaudio.adpter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.manimaran.wikiaudio.R;
import com.manimaran.wikiaudio.acticity.ViewPagerActivity;
import com.manimaran.wikiaudio.model.Words;

import java.util.List;

public class WordsAdapter extends RecyclerView.Adapter<WordsAdapter.MyViewHolder> {
 
    private List<Words> wordsList;
    private Context context;
 
    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView word, info;
        private RelativeLayout layout;

 
        public MyViewHolder(View view) {
            super(view);
            word = (TextView) view.findViewById(R.id.word);
            info = (TextView) view.findViewById(R.id.info);
            layout = (RelativeLayout) view.findViewById(R.id.layout);
        }
    }
 
 
    public WordsAdapter(List<Words> wordsList) {
        this.wordsList = wordsList;
    }
 
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        context = parent.getContext();
        return new MyViewHolder(itemView);
    }
 
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Words master = wordsList.get(holder.getAdapterPosition());
        holder.word.setText((holder.getAdapterPosition() + 1) + ". " + master.getWord());
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent next = new Intent(context, ViewPagerActivity.class);
                next.putExtra("pos", holder.getAdapterPosition());
                next.putExtra("word", master.getWord());
                context.startActivity(next);
            }
        });
    }
 
    @Override
    public int getItemCount() {
        return wordsList.size();
    }
}