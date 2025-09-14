package com.manimarank.spell4wiki.ui.languageselector

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.manimarank.spell4wiki.R
import com.manimarank.spell4wiki.data.db.entities.WikiLang
import com.manimarank.spell4wiki.ui.listerners.OnLanguageSelectionListener
import com.manimarank.spell4wiki.utils.ToastUtils.showLong
import java.util.*

class LanguageAdapter(private var mList: List<WikiLang>, listener: OnLanguageSelectionListener, private val existLangCode: String?) : RecyclerView.Adapter<LanguageAdapter.ViewHolder>(), Filterable {
    private val mBackUpList: List<WikiLang> = mList
    private val mListener: OnLanguageSelectionListener = listener
    private var onFilterResultListener: ((isEmpty: Boolean) -> Unit)? = null
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pos = holder.adapterPosition
        val model = mList[pos]
        val localName = model.localName + " : " + model.code
        holder.txtLanguage.text = model.name
        holder.txtLocalName.text = localName
        holder.txtLocalName.gravity = if (model.isLeftDirection) Gravity.START else Gravity.END
        holder.radioSelect.isChecked = existLangCode == model.code
        holder.layout.setOnClickListener {
            holder.radioSelect.isChecked = true
            showLong(String.format(it.context.getString(R.string.select_language_response_msg), model.name))
            mListener.onCallBackListener(model.code)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_language_row, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val results = FilterResults()
                if (constraint.isNotEmpty()) {
                    val filterList: MutableList<WikiLang> = ArrayList()
                    mBackUpList.forEach { l ->
                        if ((l.name.toLowerCase(Locale.ROOT) + " " + l.localName.toLowerCase(Locale.ROOT) + " " + l.code.toLowerCase(Locale.ROOT)).contains(constraint.toString().toLowerCase(Locale.ROOT))) {
                            filterList.add(l)
                        }
                    }
                    results.count = filterList.size
                    results.values = filterList
                } else {
                    results.count = mBackUpList.size
                    results.values = mBackUpList
                }
                return results
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                mList = castList(results.values, WikiLang::class.java).filterNotNull()
                notifyDataSetChanged()

                // Notify about empty results when searching
                if (constraint.isNotEmpty()) {
                    onFilterResultListener?.invoke(mList.isEmpty())
                } else {
                    onFilterResultListener?.invoke(false) // Not empty when no search constraint
                }
            }
        }
    }

    fun setOnFilterResultListener(listener: (isEmpty: Boolean) -> Unit) {
        onFilterResultListener = listener
    }

    /* adapter view holder */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtLanguage: TextView = itemView.findViewById(R.id.txtLanguage)
        val txtLocalName: TextView = itemView.findViewById(R.id.txtLocalName)
        val radioSelect: RadioButton = itemView.findViewById(R.id.radioSelect)
        val layout: View = itemView.findViewById(R.id.layoutItem)
    }

    companion object {
        fun <T> castList(obj: Any?, clazz: Class<T>): List<T?> {
            val result: MutableList<T?> = ArrayList()
            if (obj is List<*>) {
                for (o in obj) {
                    result.add(clazz.cast(o))
                }
                return result
            }
            return result
        }
    }

}