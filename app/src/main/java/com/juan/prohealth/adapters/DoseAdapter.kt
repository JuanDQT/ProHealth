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

class DoseAdapter(var controls: ArrayList<Control>) :
    RecyclerView.Adapter<DoseAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDosisBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(controls[position], holder.itemView.context)
    }

    class ViewHolder(private val binding: ItemDosisBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(control: Control, context: Context) {

            val resourceControl = control.recurso
            val dateControl = control.fecha

            if (dateControl != null) {

                binding.apply {

                    tvDate.text = dateControl.customFormat("dd/MM")

                    if (dateControl == Date().clearTime()) {
                        tvCurrent.visibility = View.VISIBLE
                        tvCurrent.typeface = Typeface.DEFAULT_BOLD
                        tvDate.typeface = Typeface.DEFAULT_BOLD
                    } else {
                        when {
                            dateControl.isTomorrow() -> tvCurrent.text =
                                context.getString(R.string.manana)
                            dateControl.isYesterday() -> tvCurrent.text =
                                context.getString(R.string.ayer)
                            else -> tvCurrent.visibility = View.GONE
                        }
                    }

                    if (resourceControl != null) {
                        imageDose.setBackgroundResource(
                            AppContext.getImageNameByJSON(
                                resourceControl
                            )
                        )
                    }
                }
            }
        }

    }

    override fun getItemCount(): Int = if (controls.size > 0) controls.size else 0
}