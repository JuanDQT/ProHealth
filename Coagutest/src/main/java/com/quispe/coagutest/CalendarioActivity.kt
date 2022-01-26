package com.quispe.coagutest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quispe.coagutest.database.room.Control
import com.quispe.coagutest.database.room.MyDatabase
import com.quispe.coagutest.database.room.RoomControlDataSource
import com.quispe.coagutest.databinding.ActivityCalendarioBinding
import com.quispe.coagutest.repository.ControlRepository
import com.quispe.coagutest.ui.CalendarioViewModel
import com.quispe.coagutest.ui.common.customFormat
import com.quispe.coagutest.ui.common.setBackgroundResource
import java.util.*


class CalendarioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarioBinding
    private lateinit var viewModel: CalendarioViewModel
    private lateinit var controlRepository: ControlRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildDependencies()
        viewModel = buildViewModel()
        setUpUI()
        subscribeUI()
    }

    private fun subscribeUI() {
        viewModel.controlList.observe(this) { calendarArray ->
            fillCalendarValues(calendarArray)
        }

        viewModel.controlSelected.observe(this) { controlSelected ->
            drawControlSelectedDate(controlSelected)
        }
    }

    private fun setUpUI() {
        binding = ActivityCalendarioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpCalendar()
    }

    private fun buildViewModel(): CalendarioViewModel {
        val factory = CalendarioViewModelFactory(controlRepository)
        return ViewModelProvider(this, factory).get(CalendarioViewModel::class.java)
    }

    private fun buildDependencies() {
        val database = MyDatabase.getDatabase(this)
        val controlLocal = RoomControlDataSource(database)
        controlRepository = ControlRepository(controlLocal)
    }

    private fun drawControlSelectedDate(control: Control?) {
        control?.let {
            binding.tvFecha.text = it.executionDate.customFormat("dd/MM/yyyyy")
            binding.tvDosis.text = String.format(getString(R.string.nivel_de_dosis_actual), it.doseLevel)
            binding.tvSangre.text = String.format(getString(R.string.nivel_de_sangre_actual), it.blood)
            binding.tvInfoDosis.text = String.format(getString(R.string.dosis_sintrom_este_dia), it.resource)
            binding.viewDosis.setBackgroundResource(AppContext.getImageNameByJSON(it.resource))
        } ?: run {
            binding.tvFecha.text = getString(R.string.nada_planificado_fecha)
            binding.tvDosis.text = ""
            binding.tvSangre.text = ""
            binding.tvInfoDosis.text = ""
            binding.viewDosis.setBackgroundResource(0)
        }
    }

    fun fillCalendarValues(calendarListValues: Array<Calendar>) {
        binding.evCalendario.addEvent(calendarListValues)
        binding.evCalendario.build()
    }

    private fun setUpCalendar() {
        val today = Calendar.getInstance()
        val end = Calendar.getInstance()
        end.add(Calendar.YEAR, 2)
        binding.evCalendario.setSelectionMode(binding.evCalendario.SINGLE_SELECTION) //set mode of Calendar
            .setToday(today) //set today's date [today: Calendar]
            .setMonthRange(today, end) //set starting month [start: Calendar] and ending month [end: Calendar]
            .setCallback(viewModel) //set the callback for EventsCalendar
    }

    @Suppress("UNCHECKED_CAST")
    class CalendarioViewModelFactory(
        private val controlRepository: ControlRepository
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            CalendarioViewModel(controlRepository) as T
    }
}
