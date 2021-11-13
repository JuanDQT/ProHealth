package com.juan.prohealth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.juan.prohealth.database.room.Control
import com.juan.prohealth.database.room.MyDatabase
import com.juan.prohealth.database.room.RoomControlDataSource
import com.juan.prohealth.databinding.ActivityCalendarioBinding
import com.juan.prohealth.repository.ControlRepository
import com.juan.prohealth.ui.CalendarioViewModel
import com.juan.prohealth.ui.common.setBackgroundResource
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
            binding.tvDosis.text = "El nivel de dosis actual es: ${it.doseLevel}"
            binding.tvSangre.text = "Tu nivel de sangre actual es: ${it.blood}"
            binding.tvInfoDosis.text = "Dosis de Sintrom para este dia: ${it.resource}"
            binding.viewDosis.setBackgroundResource(AppContext.getImageNameByJSON(it.resource))
        } ?: run {
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
