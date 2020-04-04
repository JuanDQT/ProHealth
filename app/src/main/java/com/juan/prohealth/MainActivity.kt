package com.juan.prohealth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Comprobamos que ya esta guardado
        if (goInicioActivity()) {
            val intent = Intent(this, InicioActivity::class.java)
            startActivity(intent)
            return
        }

        btn_calcular.setOnClickListener {

            // TODO: Validar 2 edittext
            if(!et_sangre.text.toString().isNullOrEmpty() && false) {
                val valor = Integer.parseInt(et_sangre.text.toString())
                if(valor < 0) {
                    Toast.makeText(this, "El valor introducido es: " + et_sangre.text, Toast.LENGTH_LONG).show()
                    //calcularTodo(valor)
                    return@setOnClickListener;
                }
            }

            // Guardamos los valores en sharedPrederences
            MySharedPreferences.shared.addString("nivel", et_nivel.text.toString())
            MySharedPreferences.shared.addString("sangre", et_sangre.text.toString())

            val intent = Intent(this, InicioActivity::class.java)
            startActivity(intent)
        }
    }

    fun goInicioActivity(): Boolean {
        // Comprobamos que los valores existan en el sharedPreferences.
        return MySharedPreferences.shared.exists(arrayOf("nivel", "sangre"))
    }
}
