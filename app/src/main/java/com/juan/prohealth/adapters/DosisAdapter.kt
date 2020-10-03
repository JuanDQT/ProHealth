package com.juan.prohealth.adapters;

import android.content.Context;
import android.text.format.DateUtils
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.graphics.alpha

import androidx.recyclerview.widget.RecyclerView;
import com.juan.prohealth.*

import com.juan.prohealth.database.Control;
import java.util.*

class DosisAdapter(var list: ArrayList<Control>, var context: Context): RecyclerView.Adapter<DosisAdapter.PalabraViewHolder2>() {
    private val localView = R.layout.item_dosis

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PalabraViewHolder2 {
        val view = LayoutInflater.from(context).inflate(localView, parent, false);
        return PalabraViewHolder2(view)
    }

    override fun onBindViewHolder(holder: PalabraViewHolder2, position: Int) {
        list[position].fecha?.let {
            holder.tvText?.text = "${it.customFormat("dd/MM")}"
//        holder.tvText.setText(list.get(position).getNivelDosis());

            if (it == Date().clearTime()) {
                holder.tvCurrent?.visibility = View.VISIBLE
            } else {
                // dia antes, despues
                if(it.isTomorrow())
                    holder.tvCurrent?.text = context.getString(R.string.manana)
                else if(it.isYesterday())
                    holder.tvCurrent?.text = context.getString(R.string.ayer)
                else
                    holder.tvCurrent?.visibility = View.GONE
            }
        }


        list[position].recurso?.let {
            holder.ivSrc?.setBackgroundResource(AppContext.getImageNameByJSON(it))
        }

//        holder.itemView.background.alpha = 2
    }

    class PalabraViewHolder2(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvCurrent: TextView? = null
        var tvText: TextView? = null
        var ivSrc: ImageView? = null

        init {
            tvCurrent = itemView.findViewById(R.id.tv_current)
            tvText = itemView.findViewById(R.id.tv_text)
            ivSrc = itemView.findViewById(R.id.iv_src)
        }
    }

    override fun getItemCount(): Int {
        return if (list == null) { 0 } else { list!!.count() }
    }
}