package com.manimarank.spell4wiki.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.manimarank.spell4wiki.adapters.EndlessRecyclerAdapter;
import com.manimarank.spell4wiki.utils.NetworkUtils;

import java.util.List;

public class EndlessRecyclerView extends RecyclerView {


    private boolean isLoading = false;
    private EndlessListener listener;
    private EndlessRecyclerAdapter adapter;
    private boolean isLastPage = false;
    private boolean loadMoreEnabled = true;


    public EndlessRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public EndlessRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EndlessRecyclerView(Context context) {
        super(context);
    }

    public void setAdapter(EndlessRecyclerAdapter mAdapter, @NonNull LinearLayoutManager layoutManager) {
        super.setAdapter(mAdapter);
        this.adapter = mAdapter;
        removeLoader();

        this.addOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && loadMoreEnabled) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0 && listener != null) {
                        if (isLastPage || !NetworkUtils.INSTANCE.isConnected(getContext()))
                            listener.loadFail();
                        else if (listener.loadData())
                            addLoaded();
                    }
                }
            }
        });
    }

    public void addNewData(List<String> data) {
        removeLoader();
        this.adapter.addItems(data);
    }

    public boolean isLastPage() {
        return isLastPage;
    }

    public void removeLoader() {
        isLoading = false;
        this.adapter.removeLoading();
    }

    private void addLoaded() {
        isLoading = true;
        this.adapter.addLoading();
    }


    public void reset() {
        this.adapter.clear();
        addLoaded();
        isLastPage = false;
        enableLoadMore();
        super.setAdapter(adapter);
    }

    public void setListener(EndlessListener listener) {
        this.listener = listener;
    }

    public void setLastPage() {
        this.isLastPage = true;
    }

    public void disableLoadMore() {
        loadMoreEnabled = false;
    }

    public void enableLoadMore() {
        loadMoreEnabled = true;
    }


    public interface EndlessListener {
        boolean loadData();

        void loadFail();
    }

}
