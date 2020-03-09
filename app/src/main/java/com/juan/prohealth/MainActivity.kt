package com.juan.prohealth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvInit.text = "Proyecto ProHealth V1"

        btn_planificar.setOnClickListener {

            if(!et_valor.text.toString().isNullOrEmpty()) {
                val valor = Integer.parseInt(et_valor.text.toString())
                if(valor > 0) {
                    Toast.makeText(this, "El valor introducido es: " + et_valor.text, Toast.LENGTH_LONG).show()
                    calcularTodo(valor)
                }
            }
        }
    }

    fun calcularTodo(valor: Int) {
        // TODO
    }
}
