package com.juan.prohealth.ui.ajustesActivity

import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.juan.prohealth.AppContext
import com.juan.prohealth.MySharedPreferences
import com.juan.prohealth.R
import com.juan.prohealth.database.Control
import com.juan.prohealth.database.User
import com.juan.prohealth.database.room.MyDatabase
import com.juan.prohealth.database.room.RoomControlDataSource
import com.juan.prohealth.database.room.RoomUserDataSource
import com.juan.prohealth.databinding.ActivityAjustesBinding
import com.juan.prohealth.repository.ControlRepository
import com.juan.prohealth.repository.UserRepository
import com.juan.prohealth.ui.common.alert
import io.github.lucasfsc.html2pdf.Html2Pdf
import java.io.File


class AjustesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAjustesBinding
    private lateinit var viewModel: AjustesViewModel
    private lateinit var controlRepository: ControlRepository
    private lateinit var userRepository: UserRepository

    private fun buildDependencies() {
        val db = MyDatabase.getDatabase(this)
        val controlLocal = RoomControlDataSource(db)
        val userLocal = RoomUserDataSource(db)
        controlRepository = ControlRepository(controlLocal)
        userRepository = UserRepository(userLocal)
    }

    private fun buildViewModel(): AjustesViewModel {
        val factory = AjustesViewModelFactory(controlRepository, userRepository)
        return ViewModelProvider(this, factory).get(AjustesViewModel::class.java)
    }

    private fun setUpUI() {
        binding = ActivityAjustesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvContactos.setOnClickListener { doAskMail() }
        binding.frameNotificaciones.setOnClickListener { doConfigNotification() }
        binding.tvExportar.setOnClickListener { doExportarMail() }
        binding.tvReconfigurarInr.setOnClickListener { doReconfigurarINR() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buildDependencies()
        viewModel = buildViewModel()
        setUpUI()
        subscribeUI()
    }

    private fun subscribeUI() {
        viewModel.userSchedule.observe(this) { schedule ->
            if (schedule.size > 1)
                setViewScheduleTime(schedule)
        }

        viewModel.currentActiveControls.observe(this) { activeControls ->
            if (!activeControls.isNullOrEmpty() && activeControls.count() > 0)
                binding.tvReconfigurarInr.visibility = View.VISIBLE
            else
                binding.tvReconfigurarInr.visibility = View.GONE
        }
    }

    private fun doUpdateSchedule(hour: Int, minute: Int) {
        viewModel.updateUserSchedule(hour, minute)
    }

    private fun setViewScheduleTime(time: Array<Int>) {
        binding.tvHora.text = AppContext.getDateFormat(time)
    }

    fun doConfigNotification() {
        val hour: Int = User.getCurrentTimeNotification()[0] // viewModel.userHour
        val minute: Int = User.getCurrentTimeNotification()[1]
        val mTimePicker: TimePickerDialog
        mTimePicker = TimePickerDialog(
            this, android.R.style.Theme_Holo_Dialog,
//            mTimePicker = TimePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog,
            OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
                Toast.makeText(
                    this,
                    "Seleccionado: $selectedHour:$selectedMinute",
                    Toast.LENGTH_SHORT
                ).show()

                doUpdateSchedule(selectedHour, selectedMinute)
                setViewScheduleTime(arrayOf(selectedHour, selectedMinute))

                if (Control.hasPendingControls()) {
                    //MyWorkManager.setWorkers(Control.getActiveControlList(onlyPendings = true))
                    Toast.makeText(this, "Alarmas actualizadas", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "No hay alarma programada", Toast.LENGTH_SHORT).show()
                }

            }, hour, minute, true
        ) //Yes 24 hour time

        mTimePicker.setTitle(getString(R.string.select_time))
        mTimePicker.show()
    }

    fun doAskMail() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Emails")
        builder.setMessage(getString(R.string.introduce_emails))
        val editText = EditText(this)
        editText.setText(MySharedPreferences.shared.getString("emails"))
        editText.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        builder.setView(editText)

        builder.setPositiveButton(getString(R.string.accept), DialogInterface.OnClickListener { dialogInterface, i ->
            if (!editText.text.isNullOrEmpty()) {
                MySharedPreferences.shared.addString("emails", editText.text.toString())
            }
        })

        builder.create().show()
    }

    fun generateExportFile(callback: (String?) -> Unit) {

        val tempFile = File(externalCacheDir, "resultado.pdf")

        // Lets to transorm to PDF
        Html2Pdf.Companion.Builder()
            .context(this)
            .html(Control.exportDataMail())
            .file(tempFile)
            .build().convertToPdf(object : Html2Pdf.OnCompleteConversion {
                override fun onFailed() {
                    return callback(null)
                }

                override fun onSuccess() {
                    return callback(tempFile.absolutePath)
                }
            })
    }

    fun doReconfigurarINR() {
        alert(getString(R.string.alerta), getString(R.string.alert_message_restart_inr), getString(R.string.yes),
            { dialogInterface, i ->
                viewModel.deleteLastGroupControl()
            }, getString(R.string.no),
            { dialogInterface, i ->
                dialogInterface.dismiss()
            })
    }

    fun doExportarMail() {
        generateExportFile {
            it?.let {
                val emailIntent = Intent(
                    Intent.ACTION_SENDTO,
                    Uri.fromParts("mailto", MySharedPreferences.shared.getString("emails"), null)
                )
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.send_email_subject))
                emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + it))
                emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.send_email_body))
                startActivity(Intent.createChooser(emailIntent, getString(R.string.send_email)))
                return@generateExportFile
            }
            Toast.makeText(
                this@AjustesActivity,
                getString(R.string.send_email_error),
                Toast.LENGTH_SHORT
            ).show()
        }

    }
}