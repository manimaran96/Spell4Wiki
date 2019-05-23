package com.manimaran.wikiaudio.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.manimaran.wikiaudio.adapter.EndlessAdapter;

import java.util.List;

public class EndlessListView extends ListView implements OnScrollListener {

    private View footer;
    private boolean isLoading;
    private EndlessListener listener;
    private EndlessAdapter adapter;

    public EndlessListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setOnScrollListener(this);
    }

    public EndlessListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnScrollListener(this);
    }

    public EndlessListView(Context context) {
        super(context);
        this.setOnScrollListener(this);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {

        if (getAdapter() == null)
            return;

        if (getAdapter().getCount() == 0)
            return;

        int l = visibleItemCount + firstVisibleItem;
        if (l >= totalItemCount && !isLoading) {
            // It is time to add new data. We call the listener
            isLoading = true;
            if (listener != null && listener.loadData() && this.getFooterViewsCount() == 0)
                this.addFooterView(footer);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    public void setLoadingView(int resId) {
        final LayoutInflater inflater = (LayoutInflater) super.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        footer = inflater.inflate(resId, null);
        this.addFooterView(footer);
    }

    public void setAdapter(EndlessAdapter adapter) {
        super.setAdapter(adapter);
        this.adapter = adapter;
        this.removeFooterView(footer);
    }

    public void addNewData(List<String> data) {
        this.removeFooterView(footer);

        adapter.addAll(data);
        adapter.notifyDataSetChanged();
        isLoading = false;
    }

    public void loadLaterOnScroll() {
        isLoading = false;
    }

    public void reset() {
        adapter.clear();
        adapter.notifyDataSetChanged();
        isLoading = true;
        if (this.getFooterViewsCount() == 0)
            this.addFooterView(footer);
    }

    public EndlessListener getListener() {
        return listener;
    }

    public void setListener(EndlessListener listener) {
        this.listener = listener;
    }


    public interface EndlessListener {
        boolean loadData();
    }

}
