package com.manimarank.spell4wiki.ui.about

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.model.CoreContributors
import com.manimarank.spell4wiki.utils.GeneralUtils.openUrlInBrowser

class CoreContributorsAdapter(private val mContext: Context, private val mList: List<CoreContributors>) : RecyclerView.Adapter<CoreContributorsAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contributor = mList[position]
        holder.txtName.text = contributor.name
        Glide.with(mContext).load(contributor.imgLink).placeholder(R.drawable.ic_contributors).apply(RequestOptions.circleCropTransform()).into(holder.imgIcon)
        holder.txtContribution.text = contributor.contribution
        holder.txtAbout.text = HtmlCompat.fromHtml(contributor.about, HtmlCompat.FROM_HTML_MODE_LEGACY)
        holder.btnOption.setOnClickListener { openUrlInBrowser(mContext, contributor.link) }
        holder.itemView.setOnClickListener { holder.txtAbout.visibility = if (holder.txtAbout.visibility != View.VISIBLE) View.VISIBLE else View.GONE }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_item_core_contributors, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    /* adapter view holder */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txtName: TextView = itemView.findViewById(R.id.txtName)
        var txtAbout: TextView = itemView.findViewById(R.id.txtAbout)
        var txtContribution: TextView = itemView.findViewById(R.id.txtContribution)
        var imgIcon: ImageView = itemView.findViewById(R.id.imgIcon)
        var btnOption: ImageView = itemView.findViewById(R.id.btn_option)
    }
}