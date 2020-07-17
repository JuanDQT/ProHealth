package com.juan.prohealth

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.events.calendar.utils.EventsCalendarUtil
import com.events.calendar.views.EventsCalendar
import com.juan.prohealth.database.Control
import kotlinx.android.synthetic.main.activity_calendario.*
import java.util.*


class CalendarioActivity : AppCompatActivity(), EventsCalendar.Callback{

    override fun onDayLongPressed(selectedDate: Calendar?) {
        Log.e("LONG CLICKED", EventsCalendarUtil.getDateString(selectedDate, EventsCalendarUtil.DD_MM_YYYY))
    }

    override fun onDaySelected(selectedDate: Calendar?) {
        Log.e("CLICKED", EventsCalendarUtil.getDateString(selectedDate, EventsCalendarUtil.DD_MM_YYYY))
        selectedDate?.let {
            var fechaSeleccionada = it.time.clearTime()
            var registroDay = Control.getControlDay(fechaSeleccionada)
            if(registroDay==null){
                Toast.makeText(this@CalendarioActivity,"No hay registros guardados",Toast.LENGTH_LONG).show()
                tv_Dosis.setText("")
                tv_Sangre.setText("")
                tv_infoDosis.setText("")
                viewDosis.setBackgroundResource(0)
            }else{
                var nivelDosis = registroDay.nivelDosis.toString()
                var nivelSangreActual = registroDay.sangre.toString()
                var imagenDosisBaseDatos = registroDay.recurso.toString()
                var imagenNameRecurso = AppContext.getImageNameByJSON(imagenDosisBaseDatos)
                val recursoID = resources.getIdentifier(imagenNameRecurso,"drawable", packageName)
                //Toast.makeText(this@CalendarioActivity,"Hay 1 registro guardados ",Toast.LENGTH_LONG).show()
                tv_Dosis.setText("El nivel de dosis actual es: " + nivelDosis)
                tv_Sangre.setText("Tu nivel de sangre actual es: " + nivelSangreActual)
                tv_infoDosis.setText("Dosis de Sintrom para este dia : " + imagenDosisBaseDatos)
                viewDosis.setBackgroundResource(recursoID)
            }
        }
    }

    override fun onMonthChanged(monthStartDate: Calendar?) {
        Log.e("MON", "CHANGED")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendario)

        val today = Calendar.getInstance()
        val end = Calendar.getInstance()
        end.add(Calendar.YEAR, 2)

        eventsCalendar.setSelectionMode(eventsCalendar.MULTIPLE_SELECTION) //set mode of Calendar
            .setIsBoldTextOnSelectionEnabled(true)
            .setToday(today) //set today's date [today: Calendar]
            .setMonthRange(today, end) //set starting month [start: Calendar] and ending month [end: Calendar]
            .setWeekStartDay(Calendar.SUNDAY, false) //set start day of the week as you wish [startday: Int, doReset: Boolean]
            //.setCurrentSelectedDate(today) //set current date and scrolls the calendar to the corresponding month of the selected date [today: Calendar]
            .setDatesTypeface(Typeface.DEFAULT) //set font for dates
            .setDateTextFontSize(16f) //set font size for dates
            .setMonthTitleTypeface(Typeface.DEFAULT_BOLD) //set font for title of the calendar
            .setMonthTitleFontSize(16f) //set font size for title of the calendar
            .setWeekHeaderTypeface(Typeface.DEFAULT) //set font for week names
            .setWeekHeaderFontSize(16f) //set font size for week names
            .setCallback(this) //set the callback for EventsCalendar
            //.addEvent() //set events on the EventsCalendar [c: Calendar]
           // .disableDate(dc) //disable a specific day on the EventsCalendar [c: Calendar]
            //.disableDaysInWeek(Calendar.SATURDAY, Calendar.SUNDAY) //disable days in a week on the whole EventsCalendar [varargs days: Int]
            .build()

        val c = Calendar.getInstance()
        println("INFORMACION DEV: "+ Control.getHistoric().get(0).fechaInicio.toString())
        //Fri Jul 17 00:00:00 GMT+02:00 2020 esto queda al hacer un toString de una fecha
        /*var historico = Control.getHistoric()
        for (i in 0 until historico.size){
            var day = historico.get(i).fechaInicio
        }*/
        c.add(Calendar.DAY_OF_MONTH, 1)
        eventsCalendar.addEvent(c)
        eventsCalendar.addEvent(c)

    }

}
