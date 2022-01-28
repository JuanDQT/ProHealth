package com.quispe.coagutest.ui.ajustesActivity

import android.app.TimePickerDialog
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
import com.quispe.coagutest.AppContext
import com.quispe.coagutest.MySharedPreferences
import com.quispe.coagutest.R
import com.quispe.coagutest.database.room.MyDatabase
import com.quispe.coagutest.database.room.RoomControlDataSource
import com.quispe.coagutest.database.room.RoomUserDataSource
import com.quispe.coagutest.databinding.ActivityAjustesBinding
import com.quispe.coagutest.repository.ControlRepository
import com.quispe.coagutest.repository.UserRepository
import com.quispe.coagutest.ui.common.alert


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
        binding.tvExportar.setOnClickListener { viewModel.prepareExportControls(externalCacheDir, this) }
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

        viewModel.getExportControls().observe(this) {
            doExportarMail(it)
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

    fun doReconfigurarINR() {
        alert(getString(R.string.alerta), getString(R.string.alert_message_restart_inr), getString(R.string.yes),
            { dialogInterface, i ->
                viewModel.deleteLastGroupControl()
            }, getString(R.string.no),
            { dialogInterface, i ->
                dialogInterface.dismiss()
            })
    }

    fun doExportarMail(generateExportFile: String?) {
        generateExportFile?.let {
            val emailIntent = Intent(
                Intent.ACTION_SENDTO,
                Uri.fromParts("mailto", MySharedPreferences.shared.getString("emails"), null)
            )
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.send_email_subject))
            emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + it))
            emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.send_email_body))
            startActivity(Intent.createChooser(emailIntent, getString(R.string.send_email)))
        } ?: kotlin.run {
            Toast.makeText(
                this@AjustesActivity,
                getString(R.string.send_email_error),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}