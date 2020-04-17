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
            //Guardamos valores en String
            val inputValorSangreText = et_sangre.text.toString()
            val inputNivelSangreText = et_nivel.text.toString()
            //Validamos estos valores
            if(validarInputText(inputValorSangreText) && validarInputNivel(inputNivelSangreText)){
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

    /**
     * Aqui validamos el campo Valor de Sangre, que no este vacio ni tampoco sea nullo, y que contenga el formato correcto, es decir
     * que contenga comas y puntos. Tambien que sus digitos no sean superiores a 4.
     */
    fun validarInputText(inputText: String): Boolean{

        if(!inputText.isNullOrEmpty()){
            if(inputText.length <= 4){
                if(inputText.contains(".") || inputText.contains(",")){
                    return  true;
                }else {
                    Toast.makeText(
                        this,
                        "El el formato del valor en sangre es: (1.00 ó 1,00 a 7.00 ó 7,00)",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }else {
                Toast.makeText(
                    this,
                    "El valor de sangre no puede ser superior a 4 digitos de tamaño",
                    Toast.LENGTH_LONG
                ).show()
            }
        }else {
            Toast.makeText(this, "El valor de sangre introducido no puede estar vacio", Toast.LENGTH_LONG)
                .show()
        }

        return false;
    }

    /**
     * Aqui validamos el valor del Nivel de Dosis (1-54)
     * Que no este vacio, que los digitos no sean superiores a 2, y que se encuentre entre los valores 1 y 54
     */
    fun validarInputNivel(inputText: String): Boolean{

        if(!inputText.isNullOrEmpty()){
            if(inputText.length <= 2){
                var nivelNumero= inputText.toInt()
                if(nivelNumero in 1..54){// sugiere un rango de 1..54 es posible?
                    return  true;
                }else {
                    Toast.makeText(
                        this,
                        "El nivel ha de estar entre los rangos 1-54",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }else {
                Toast.makeText(
                    this,
                    "El nivel no puede ser superior a 2 digitos de tamaño",
                    Toast.LENGTH_LONG
                ).show()
            }
        }else {
            Toast.makeText(this, "El nivel introducido no puede estar vacio", Toast.LENGTH_LONG)
                .show()
        }

        return false;
    }
}
