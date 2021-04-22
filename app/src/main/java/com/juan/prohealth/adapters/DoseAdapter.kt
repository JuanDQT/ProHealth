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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDosisBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
    }

    class ViewHolder(private val binding: ItemDosisBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(control: Control) {

            val resourceControl = control.recurso
            val dateControl = control.fecha

            if (dateControl != null) {

                binding.apply {

                    tvText.text = dateControl.customFormat("dd/MM")

                    if (dateControl == Date().clearTime()) {
                        tvCurrent.visibility = View.VISIBLE
                        tvCurrent.typeface = Typeface.DEFAULT_BOLD
                        tvText.typeface = Typeface.DEFAULT_BOLD
                    } else {
                        // dia antes, despues
                        if (dateControl.isTomorrow())
                            tvCurrent.text = context.getString(R.string.manana)
                        else if (dateControl.isYesterday())
                            tvCurrent.text = context.getString(R.string.ayer)
                        else
                            tvCurrent.visibility = View.GONE
                        //with WHEN
                        when {
                            dateControl.isTomorrow() -> tvCurrent.text = "MAÑANA"
                            dateControl.isYesterday() -> tvCurrent.text = "AYER"
                            else -> tvCurrent.visibility = View.GONE
                        }
                    }

                    if (resourceControl != null) {
                        ivSrc.setBackgroundResource(AppContext.getImageNameByJSON(resourceControl))
                    }
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