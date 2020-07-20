package com.juan.prohealth

import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Data
import com.juan.prohealth.database.Control
import com.juan.prohealth.database.User
import kotlinx.android.synthetic.main.activity_ajustes.*
import java.util.*


class AjustesActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ajustes)

        tv_hora.text = getDateFormat(User.getCurrentTimeNotification())

        tv_contactos.setOnClickListener(this)
        frame_notificaciones.setOnClickListener(this)
        tv_exportar.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        v?.let {
            when (it.id) {
                R.id.tv_contactos -> doAskMail()
                R.id.frame_notificaciones -> doConfigNotification()
                R.id.tv_exportar -> doExportarMail()
            }
        }
    }


    fun getDateFormat(dateArray: Array<Int>): String {
        return "${dateArray[0]}:${dateArray[1].toString().padStart(2, '0')}"
    }

    fun doConfigNotification() {
        val hour: Int = User.getCurrentTimeNotification()[0]
        val minute: Int = User.getCurrentTimeNotification()[1]
        val mTimePicker: TimePickerDialog
        mTimePicker = TimePickerDialog(this, android.R.style.Theme_Holo_Dialog,
//            mTimePicker = TimePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog,
            OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
                Toast.makeText(this, "Seleccionado: $selectedHour:$selectedMinute", Toast.LENGTH_SHORT).show()
                User.settCurrentTimeNotification(selectedHour, selectedMinute)
                tv_hora.text = getDateFormat(arrayOf(selectedHour, selectedMinute))

                if (Control.hasPendingControls()) {
                    MyWorkManager.setWorkers(Control.getActiveControlList(onlyPendings = true))
                    Toast.makeText(this, "Alarmas actualizadas", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "No hay alarma programada", Toast.LENGTH_SHORT).show()
                }

            }, hour, minute, true
        ) //Yes 24 hour time

        mTimePicker.setTitle("Seleccionar hora")
        mTimePicker.show()
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

    fun doExportarMail() {
        val data = Html.fromHtml(Control.exportDataMail())
        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", MySharedPreferences.shared.getString("emails"), null))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Planificacion IRN")
        emailIntent.putExtra(Intent.EXTRA_TEXT, data)
        startActivity(Intent.createChooser(emailIntent, "Enviar mail..."))
    }
}