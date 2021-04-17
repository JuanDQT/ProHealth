package com.juan.prohealth.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
        holder.tvText?.text = "${list[position].fecha?.customFormat("dd/MM")}"
//        holder.tvText.setText(list.get(position).getNivelDosis());

        if (list[position].fecha == Date().clearTime()) {
            holder.tvCurrent?.visibility = View.VISIBLE
        } else {
            holder.tvCurrent?.visibility = View.GONE
        }

        list[position].recurso?.let {
            holder.ivSrc?.setBackgroundResource(AppContext.getImageNameByJSON(it))
        }
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