package com.juan.prohealth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import kotlinx.android.synthetic.main.activity_calendario.*

// https://github.com/prolificinteractive/material-calendarview
class CalendarioActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendario)

        btnPuntos.setOnClickListener {
            pintarPuntos()
        }

        btnRango.setOnClickListener {
            pintarRango()
        }
    }

    fun pintarPuntos() {
        calendarView.clearSelection()
        calendarView.selectionMode = MaterialCalendarView.SELECTION_MODE_MULTIPLE

        // Itera 7 veces y por cada itearcion marca un dia aleatorio en el calendario
        for (x in 0 until 7) {
            //calendarView.selectedDate = CalendarDay.from(2020, 3, (1 until 31).random())
            calendarView.setDateSelected(CalendarDay.from(2020, 3, (1 until 31).random()), true)
        }

    }

    // Coge una fecha aleatoria del mes actual, y marca en el calendario los dias siguientes(aleatorio 1 - 5)
    fun pintarRango() {
        calendarView.clearSelection()
        calendarView.selectionMode = MaterialCalendarView.SELECTION_MODE_RANGE
        val diaAleatorio = (1 until 25).random()
        calendarView.selectRange(CalendarDay.from(2020,3, diaAleatorio), CalendarDay.from(2020,3, diaAleatorio + (1 until 5).random()))
    }

}
