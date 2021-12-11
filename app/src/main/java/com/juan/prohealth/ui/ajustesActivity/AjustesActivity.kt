package com.juan.prohealth.ui.ajustesActivity

import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.juan.prohealth.AppContext
import com.juan.prohealth.MySharedPreferences
import com.juan.prohealth.MyWorkManager
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

        binding.tvContactos.setOnClickListener { viewModel.loadUserEmailInfo() }
        binding.frameNotificaciones.setOnClickListener { viewModel.loadUserAlarmForAlert() }
        binding.tvExportar.setOnClickListener { doExportarMail() }
        binding.tvReconfigurarInr.setOnClickListener { doReconfigurarINR() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buildDependencies()
        viewModel = buildViewModel()
        setUpUI()
        subscribeUI()
        viewModel.loadUserAlarm()
    }

    private fun subscribeUI() {
        viewModel.getUserAlarm().observe(this) { time ->
            drawTime(time)
        }

        viewModel.getUserAlarmForAlert().observe(this) { time ->
            showConfigAlarmAlert(time)
        }

        viewModel.currentActiveControls.observe(this) { activeControls ->
            if (!activeControls.isNullOrEmpty() && activeControls.count() > 0)
                binding.tvReconfigurarInr.visibility = View.VISIBLE
            else
                binding.tvReconfigurarInr.visibility = View.GONE
        }
        viewModel.getUserEmailInfo().observe(this) {
            doAskMail(it)
        }
    }

    private fun doUpdateSchedule(hour: Int, minute: Int) {
        viewModel.updateUserSchedule(hour, minute)
    }

    private fun drawTime(time: Pair<Int, Int>) {
        binding.tvHora.text = AppContext.getDateFormat(time)
    }

    fun showConfigAlarmAlert(time: Pair<Int, Int>) {
        val hour: Int = time.first
        val minute: Int = time.second
        val mTimePicker = TimePickerDialog(
            this, android.R.style.Theme_Holo_Dialog,
            { timePicker, selectedHour, selectedMinute ->
                doUpdateSchedule(selectedHour, selectedMinute)
                drawTime(Pair(selectedHour, selectedMinute))
                Toast.makeText(this, "Alarmas actualizadas", Toast.LENGTH_SHORT).show()
            }, hour, minute, true
        ) //Yes 24 hour time

        mTimePicker.setTitle(getString(R.string.select_time))
        mTimePicker.show()
    }

    fun doAskMail(emails: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Emails")
        val view: LinearLayout = layoutInflater.inflate(R.layout.ad_introducir_emails, null) as LinearLayout
        val editText = view.findViewById<EditText>(R.id.emails)
        editText.setText(emails ?: "")
        builder.setView(view)
        builder.setPositiveButton(getString(R.string.accept), { dialogInterface, i ->
            if (!editText.text.isNullOrEmpty()) {
                viewModel.updateUserEmails(editText.text.toString())
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