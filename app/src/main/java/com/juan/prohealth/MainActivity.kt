package com.juan.prohealth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.btn_calcular
import kotlinx.android.synthetic.main.activity_main.et_sangre
import kotlinx.android.synthetic.main.inicio_main.*

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

            // TODO: Validar 2 edittext -> Comprobamos que NO esa VACIO, que NO CONTENGA el simbolo negativo - y que su tamaño no sea mas de 4.(1.00 - 7.00) para abarcar 2 decimales
            if(!et_sangre.text.toString().isNullOrEmpty() && !et_nivel.text.toString().isNullOrEmpty()){
                if(et_sangre.text.toString().length <= 4 && et_nivel.text.toString().length <= 2){
                    if(!et_sangre.text.toString().contains("-")){
                        if(et_sangre.text.toString().contains(".")||et_sangre.text.toString().contains(",")){

                            Toast.makeText(this, "El valor introducido es: " + et_sangre.text, Toast.LENGTH_LONG).show()
                            // Guardamos los valores en sharedPrederences
                            MySharedPreferences.shared.addString("nivel", et_nivel.text.toString())
                            MySharedPreferences.shared.addString("sangre", et_sangre.text.toString())
                            //calcularTodo(valor)
                            //return@setOnClickListener;
                            val intent = Intent(this, InicioActivity::class.java)
                            startActivity(intent)
                        }else{
                            Toast.makeText(this, "Es necesario que la cifra contenga punto o coma(pj: nivel de sangre: 1,00", Toast.LENGTH_LONG).show()
                        }
                    }else{
                        Toast.makeText(this, "No se adminten valores negativos", Toast.LENGTH_LONG).show()
                    }
                }else{
                    Toast.makeText(this, "El valor no puede ser superior a 4 digitos de tamaño", Toast.LENGTH_LONG).show()
                }
            }else{
                Toast.makeText(this, "El valor introducido no puede estar vacio", Toast.LENGTH_LONG).show()
            }


        }
    }

    fun goInicioActivity(): Boolean {
        // Comprobamos que los valores existan en el sharedPreferences.
        return MySharedPreferences.shared.exists(arrayOf("nivel", "sangre"))
    }
}
