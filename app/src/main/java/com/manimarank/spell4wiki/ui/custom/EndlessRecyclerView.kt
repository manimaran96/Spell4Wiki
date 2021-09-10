package com.manimarank.spell4wiki.ui.custom

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.manimarank.spell4wiki.ui.spell4wiktionary.EndlessRecyclerAdapter
import com.manimarank.spell4wiki.utils.NetworkUtils.isConnected

/**
 * Endless Recycler View Widget component for lazy loading
 * @property isLoading Boolean
 * @property listener EndlessListener?
 * @property adapter EndlessRecyclerAdapter?
 * @property isLastPage Boolean
 * @property loadMoreEnabled Boolean
 */
class EndlessRecyclerView : RecyclerView {
    private var isLoading = false
    private var listener: EndlessListener? = null
    private var adapter: EndlessRecyclerAdapter? = null
    var isLastPage = false
        private set
    private var loadMoreEnabled = true

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context) : super(context)

    fun setAdapter(mAdapter: EndlessRecyclerAdapter?, layoutManager: LinearLayoutManager) {
        super.setAdapter(mAdapter)
        adapter = mAdapter
        removeLoader()
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                if (!isLoading && loadMoreEnabled) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && listener != null) {
                        if (isLastPage || !isConnected(context))
                            listener?.loadFail()
                        else if (listener?.loadData() == true)
                            addLoaded()
                    }
                }
            }
        })
    }

    fun addNewData(data: MutableList<String>?) {
        removeLoader()
        if (data != null)
            adapter?.addItems(data)
    }

    fun removeLoader() {
        isLoading = false
        adapter?.removeLoading()
    }

    private fun addLoaded() {
        isLoading = true
        adapter?.addLoading()
    }

    fun reset() {
        adapter?.clear()
        addLoaded()
        isLastPage = false
        enableLoadMore()
        super.setAdapter(adapter)
    }

    fun setListener(listener: EndlessListener?) {
        this.listener = listener
    }

    fun setLastPage() {
        isLastPage = true
    }

    fun disableLoadMore() {
        loadMoreEnabled = false
    }

    fun enableLoadMore() {
        loadMoreEnabled = true
    }

    interface EndlessListener {
        fun loadData(): Boolean
        fun loadFail()
    }
}