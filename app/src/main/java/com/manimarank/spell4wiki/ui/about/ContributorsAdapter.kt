package com.manimarank.spell4wiki.ui.about

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.model.CodeContributors
import com.manimarank.spell4wiki.utils.GeneralUtils.openUrlInBrowser

class ContributorsAdapter(private val mContext: Context, private val mList: List<CodeContributors>) : RecyclerView.Adapter<ContributorsAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contributor = mList[position]
        holder.txtName.text = contributor.name
        Glide.with(mContext).load(contributor.avatarUrl).apply(RequestOptions.circleCropTransform()).into(holder.imgIcon)
        holder.txtAbout.text = String.format(mContext.getString(R.string.contributions), contributor.contributions.toString() + "")
        holder.txtName.setOnClickListener { openUrlInBrowser(mContext, contributor.htmlUrl) }
        holder.imgIcon.setOnClickListener { openUrlInBrowser(mContext, contributor.htmlUrl) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contributors_row, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    /* adapter view holder */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtName: TextView = itemView.findViewById(R.id.txt_name)
        var txtAbout: TextView = itemView.findViewById(R.id.txt_about)
        var imgIcon: ImageView = itemView.findViewById(R.id.imgIcon)
    }
}