package com.juan.prohealth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.btnINR

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

        btnINR.setOnClickListener {
            //Guardamos valores en String
            val inputValorSangreText = "No existe registro aun"
            val inputNivelSangreText = et_nivel.text.toString()
            //Validamos estos valores
            if(AppContext.validarInputNivel(inputNivelSangreText)){
                // Guardamos los valores en sharedPrederences
                MySharedPreferences.shared.addString("nivel", et_nivel.text.toString())
                MySharedPreferences.shared.addString("sangre", inputValorSangreText)
                val intent = Intent(this, InicioActivity::class.java)
                startActivity(intent)
            }

        }

    }

    fun goInicioActivity(): Boolean {
        // Comprobamos que los valores existan en el sharedPreferences.
        return MySharedPreferences.shared.exists(arrayOf("nivel", "sangre"))
    }
}
