package com.manimarank.spell4wiki.ui.categoryselector

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.model.CategoryItem
import com.manimarank.spell4wiki.ui.listerners.OnCategorySelectionListener
import java.util.*

class CategoryAdapter(private var mList: ArrayList<CategoryItem>, listener: OnCategorySelectionListener?) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {
    private val mListener: OnCategorySelectionListener? = listener
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pos = holder.adapterPosition
        val model = mList[pos]
        holder.txtCategory.text = model.title ?: ""
        holder.txtCategory.setOnClickListener {
            mListener?.onCallBackListener(model.title)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category_row, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    /* adapter view holder */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtCategory: TextView = itemView.findViewById(R.id.txtCategory)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun loadData(mResData: List<CategoryItem>,) {
        mList.clear()
        mList.addAll(mResData)
        notifyDataSetChanged()
    }

}