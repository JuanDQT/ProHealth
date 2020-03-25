package com.juan.prohealth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.activity_calendario.*
import java.util.*


class CalendarioActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendario)

        btnRandom.setOnClickListener {
            // 1. Fecha que queremos mostrar
            val c = Calendar.getInstance()
            c.time = Date()
            c.add(Calendar.DATE, (1..15).random())
            val fechaRandom = c.time

            // 2. Convertirla a long
            //cvCalendario.date = fechaRandom.time

            pintarMasDias()
        }
    }

    fun pintarMasDias() {
        //cvCalendario.
    }
}
