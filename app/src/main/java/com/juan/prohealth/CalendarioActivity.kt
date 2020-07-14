package com.juan.prohealth

import android.os.Bundle
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.juan.prohealth.database.Control
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import kotlinx.android.synthetic.main.activity_calendario.*

// https://github.com/prolificinteractive/material-calendarview
class CalendarioActivity : AppCompatActivity(), OnDateSelectedListener{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendario)
        calendarView.setOnDateChangedListener(this)
       /* btnPuntos.setOnClickListener {
            pintarPuntos()
        }

        btnRango.setOnClickListener {
            pintarRango()
        }*/
    }

    override fun onDateSelected(
        @NonNull widget: MaterialCalendarView,
        @NonNull date: CalendarDay,
        selected: Boolean
    ) {
        if(selected){
           widget.clearSelection()
          //widget.currentDate
        }
        Control.getControlDay()
        Toast.makeText(this, date.toString().replace("CalendarDay","").replace("{","").replace("}","")
                + " " + selected.toString(), Toast.LENGTH_LONG).show()
    }
/*
    fun pintarPuntos() {
        calendarView.clearSelection()
        calendarView.selectionMode = MaterialCalendarView.SELECTION_MODE_MULTIPLE
        // Itera 7 veces y por cada itearcion marca un dia aleatorio en el calendario
        for (x in 0 until 7) {
            //calendarView.selectedDate = CalendarDay.from(2020, 3, (1 until 31).random())
            calendarView.setDateSelected(CalendarDay.from(2020, 4, (1 until 31).random()), true)
        }

    }

    // Coge una fecha aleatoria del mes actual, y marca en el calendario los dias siguientes(aleatorio 1 - 5)
    fun pintarRango() {
        calendarView.clearSelection()
        calendarView.selectionMode = MaterialCalendarView.SELECTION_MODE_RANGE
        val diaAleatorio = (1 until 25).random()
        calendarView.selectRange(CalendarDay.from(2020,4, diaAleatorio), CalendarDay.from(2020,4, diaAleatorio + (1 until 5).random()))
    }
*/

}
