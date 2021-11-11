package com.juan.prohealth

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.events.calendar.utils.EventsCalendarUtil
import com.events.calendar.views.EventsCalendar
import com.juan.prohealth.database.Control
import com.juan.prohealth.database.room.MyDatabase
import com.juan.prohealth.database.room.RoomControlDataSource
import com.juan.prohealth.databinding.ActivityCalendarioBinding
import com.juan.prohealth.repository.ControlRepository
import com.juan.prohealth.ui.CalendarioViewModel
import com.juan.prohealth.ui.GraphViewModel
import com.juan.prohealth.ui.common.clearTime
import com.juan.prohealth.ui.common.fromDate
import com.juan.prohealth.ui.common.setBackgroundResource
import java.util.*


class CalendarioActivity : AppCompatActivity(), EventsCalendar.Callback {

    private lateinit var binding: ActivityCalendarioBinding
    private lateinit var viewModel: CalendarioViewModel
    private lateinit var controlRepository: ControlRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildDependencies()
        setUpUI()
        viewModel = buildViewModel()
        subscribeUI()
    }

    private fun subscribeUI() {
        viewModel.controlList.observe(this) { calendarArray ->
            fillCalendarValues(calendarArray)
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

    override fun onDayLongPressed(selectedDate: Calendar?) {
        Log.e(
            "LONG CLICKED",
            EventsCalendarUtil.getDateString(selectedDate, EventsCalendarUtil.DD_MM_YYYY)
        )
    }

    override fun onDaySelected(selectedDate: Calendar?) {
        Log.e(
            "CLICKED",
            EventsCalendarUtil.getDateString(selectedDate, EventsCalendarUtil.DD_MM_YYYY)
        )
        selectedDate?.let {
            var fechaSeleccionada = it.time.clearTime()
            var registroDay = Control.getControlDay(fechaSeleccionada)
            if (registroDay == null) {
                binding.tvDosis.text = ""
                binding.tvSangre.text = ""
                binding.tvInfoDosis.text = ""
                binding.viewDosis.setBackgroundResource(0)

            } else {
                var nivelDosis = registroDay.nivelDosis.toString()
                var nivelSangreActual = registroDay.sangre.toString()
                var imagenDosisBaseDatos = registroDay.recurso.toString()
                var imagenNameRecurso = AppContext.getImageNameByJSON(imagenDosisBaseDatos)

                binding.tvDosis.text = "El nivel de dosis actual es: $nivelDosis"
                binding.tvSangre.text = "Tu nivel de sangre actual es: $nivelSangreActual"
                binding.tvInfoDosis.text = "Dosis de Sintrom para este dia: $imagenDosisBaseDatos"
                binding.viewDosis.setBackgroundResource(imagenNameRecurso)
            }
        }
    }

    override fun onMonthChanged(monthStartDate: Calendar?) {
    }

    // TODO: cambiar el idioma sera complicado, ya que esta asi implementado internamente
    // A no ser que creemos un modulo perosnalizado como la grafica, y lo modifiquemos in SITU
    // MonthView: val namesOfDays = DateFormatSymbols.getInstance().shortWeekdays
    // Por:  DateFormatSymbols symbols = new DateFormatSymbols(new Locale("es"));
    fun fillCalendarValues(calendarListValues: Array<Calendar>) {
        binding.evCalendario.addEvent(calendarListValues)
        binding.evCalendario.build()
    }

    private fun setUpCalendar() {
        val today = Calendar.getInstance()
        val end = Calendar.getInstance()
        end.add(Calendar.YEAR, 2)

        binding.evCalendario.setSelectionMode(binding.evCalendario.SINGLE_SELECTION) //set mode of Calendar
            .setIsBoldTextOnSelectionEnabled(true)
            .setToday(today) //set today's date [today: Calendar]
            .setMonthRange(
                today,
                end
            ) //set starting month [start: Calendar] and ending month [end: Calendar]
            .setWeekStartDay(
                Calendar.MONDAY,
                false
            ) //set start day of the week as you wish [startday: Int, doReset: Boolean]
            //.setCurrentSelectedDate(today) //set current date and scrolls the calendar to the corresponding month of the selected date [today: Calendar]
            .setDatesTypeface(Typeface.DEFAULT) //set font for dates
            .setDateTextFontSize(16f) //set font size for dates
            .setMonthTitleTypeface(Typeface.DEFAULT_BOLD) //set font for title of the calendar
            .setMonthTitleFontSize(25f) //set font size for title of the calendar
            .setWeekHeaderTypeface(Typeface.DEFAULT) //set font for week names
            .setWeekHeaderFontSize(16f) //set font size for week names
            .setCallback(this) //set the callback for EventsCalendar
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
