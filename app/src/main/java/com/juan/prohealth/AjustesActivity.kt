package com.juan.prohealth

import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.DialogInterface
import android.os.Bundle
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_ajustes.*
import java.util.*


class AjustesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ajustes)

        tv_contactos.setOnClickListener{
            doAskMail()
        }

        tv_notificaciones.setOnClickListener {
            val mcurrentTime: Calendar = Calendar.getInstance()
            val hour: Int = mcurrentTime.get(Calendar.HOUR_OF_DAY)
            val minute: Int = mcurrentTime.get(Calendar.MINUTE)
            val mTimePicker: TimePickerDialog
            mTimePicker = TimePickerDialog(this, android.R.style.Theme_Holo_Dialog,
//            mTimePicker = TimePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog,
                OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
                    Toast.makeText(this, "Seleccionado: $selectedHour:$selectedMinute", Toast.LENGTH_SHORT).show()
                }, hour, minute, false
            ) //Yes 24 hour time

            mTimePicker.setTitle("Seleccionar hora")
            mTimePicker.show()
        }

        tv_exportar.setOnClickListener {
            Toast.makeText(this, "Pendiente...", Toast.LENGTH_SHORT).show()
        }
    }

    fun doAskMail() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Emails")
        builder.setMessage("Introduce los emails. Puedes separarlos por ;")
        val editText = EditText(this)
        editText.setText(MySharedPreferences.shared.getString("emails"))
        editText.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        builder.setView(editText)

        builder.setPositiveButton("Aceptar", DialogInterface.OnClickListener { dialogInterface, i ->
            if (!editText.text.isNullOrEmpty()) {
                MySharedPreferences.shared.addString("emails", editText.text.toString())
            }
        })

        builder.create().show()
    }

}