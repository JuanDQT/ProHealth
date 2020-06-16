package com.juan.prohealth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.btnINR
import kotlinx.android.synthetic.main.activity_main.et_sangre

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
            val inputValorSangreText = et_sangre.text.toString()
            val inputNivelSangreText = et_nivel.text.toString()
            //Validamos estos valores
            if(AppContext.validarInputTextSangre(inputValorSangreText) != null && AppContext.validarInputNivel(inputNivelSangreText)){
                val valorSangreNumerico = inputValorSangreText.replace(",", ".").toFloat()
                //Validamos que el valor de sangre este entre 1.00 y 7.00 siendo Float
                if(valorSangreNumerico >= 1.00 && valorSangreNumerico <= 7.00){

                    // Guardamos los valores en sharedPrederences
                    MySharedPreferences.shared.addString("nivel", et_nivel.text.toString())
                    MySharedPreferences.shared.addString("sangre", et_sangre.text.toString())
                    //calcularTodo(valor)
                    //return@setOnClickListener;
                    val intent = Intent(this, InicioActivity::class.java)
                    startActivity(intent)

                } else {
                    Toast.makeText(
                        this,
                        "Ha de ser un valor entre 1.00 y 7.00",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        }
    }

    fun goInicioActivity(): Boolean {
        // Comprobamos que los valores existan en el sharedPreferences.
        return MySharedPreferences.shared.exists(arrayOf("nivel", "sangre"))
    }

}
