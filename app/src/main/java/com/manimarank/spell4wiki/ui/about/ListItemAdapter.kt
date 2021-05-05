package com.manimarank.spell4wiki.ui.about

import android.app.Activity
import android.content.Context
import android.graphics.ColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.model.ItemsModel
import com.manimarank.spell4wiki.utils.GeneralUtils.openUrlInBrowser
import com.manimarank.spell4wiki.utils.makeVisible

class ListItemAdapter(private val mContext: Context, private val mList: List<ItemsModel>) : RecyclerView.Adapter<ListItemAdapter.ViewHolder>() {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mList[position]
        holder.txtName.text = item.name
        if (item.icon != -1) {
            if (item.isLottie) {
                holder.lottieAnimationView.makeVisible()
                holder.lottieAnimationView.setAnimation(item.icon)
                try {
                    val filterColor = ContextCompat.getColor(mContext, if (item.name.contains("Upload animation")) R.color.w_blue else R.color.transparent)
                    val filter = SimpleColorFilter(filterColor)
                    val keyPath = KeyPath("**")
                    val callback = LottieValueCallback<ColorFilter>(filter)
                    holder.lottieAnimationView.addValueCallback(keyPath, LottieProperty.COLOR_FILTER, callback)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                holder.imgIcon.visibility = View.VISIBLE
                holder.imgIcon.setImageDrawable(ContextCompat.getDrawable(mContext, item.icon))
            }
        } else {
            holder.imgIcon.visibility = View.GONE
            holder.lottieAnimationView.visibility = View.GONE
        }
        holder.txtAbout.text = item.about
        holder.btnOption.setOnClickListener { v: View? ->
            (mContext as? Activity)?.let { activity ->
                openUrlInBrowser(activity, item.url)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list_row, parent, false)
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
        var btnOption: ImageView = itemView.findViewById(R.id.btn_option)
        var lottieAnimationView: LottieAnimationView = itemView.findViewById(R.id.lottieAnimationView)
    }
}