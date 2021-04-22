package com.juan.prohealth.adapters;

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.juan.prohealth.*
import com.juan.prohealth.database.Control
import com.juan.prohealth.databinding.ItemDosisBinding
import java.util.*

class DoseAdapter(var controls: ArrayList<Control>, var context: Context) :
    RecyclerView.Adapter<DoseAdapter.ViewHolder>() {
    //private val localView = R.layout.item_dosis

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDosisBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        //val view = LayoutInflater.from(context).inflate(localView, parent, false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(controls[position])

        controls[position].fecha?.let {
            holder.tvText?.text = "${it.customFormat("dd/MM")}"
//        holder.tvText.setText(list.get(position).getNivelDosis());

            if (it == Date().clearTime()) {
                holder.tvCurrent?.visibility = View.VISIBLE
                holder.tvCurrent?.typeface = Typeface.DEFAULT_BOLD
                holder.tvText?.typeface = Typeface.DEFAULT_BOLD
            } else {
                // dia antes, despues
                if (it.isTomorrow())
                    holder.tvCurrent?.text = context.getString(R.string.manana)
                else if (it.isYesterday())
                    holder.tvCurrent?.text = context.getString(R.string.ayer)
                else
                    holder.tvCurrent?.visibility = View.GONE
            }
        }


        controls[position].recurso?.let {
            holder.ivSrc?.setBackgroundResource(AppContext.getImageNameByJSON(it))
        }

//        holder.itemView.background.alpha = 2
    }

    class ViewHolder(private val binding: ItemDosisBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(control: Control) {
            binding.apply {
                if (control.fecha != null) {
                    tvText.text = control.fecha!!.customFormat("dd/MM")
                }

                if (control.recurso != null) {
                    ivSrc.setBackgroundResource(AppContext.getImageNameByJSON(control.recurso!!))
                    tvCurrent
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return if (controls == null) {
            0
        } else {
            controls!!.count()
        }
    }
}