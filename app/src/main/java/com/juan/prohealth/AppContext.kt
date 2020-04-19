package com.juan.prohealth

import android.app.Application
import android.content.Context
import android.widget.Toast
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class AppContext : Application() {

    companion object {
        lateinit  var context: Context

        private fun openJSON(fileName: String): String? {
            var json: String? = null
            json = try {
                val inputStream = context.assets.open(fileName)
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()
                String(buffer, StandardCharsets.UTF_8)
            } catch (ex: Exception) {
                ex.printStackTrace()
                return null
            }
            return json
        }


        fun getNivelFromFichero(fileName: String, nivel: String, dias: String, isSumar: Boolean): ArrayList<String> {
            val json = JSONObject(openJSON(fileName));
            val data = arrayListOf<String>()
            for (i in 0 until json.getJSONArray(nivel).length()) {
                //if (json.getJSONArray(nivel).getString(i).isNullOrEmpty())
                  //  continue;
                data.add(json.getJSONArray(nivel).getString(i))
            }

            if (dias == "7") {
                    // Control de 7 dias, anadimos los controles '3' restantes (nivelyDias["nivel"]?:0)  = MySharedPreferences.shared.getNivel().toInt()
                val moveNivel = nivel.toInt() + if (isSumar) 0 else -1//En control de 7 dias, solo existen 2 posibilidades, que se reste 1 nivel o que se mantenga IGUAL (0)

                    for (i in 0 until json.getJSONArray(moveNivel.toString()).length() - 1) {// -1 porque asi coge los 3 restantes, no todos
                        for (i in 0 until json.getJSONArray(moveNivel.toString()).length() - 1) {

                        }
                       // if (json.getJSONArray(moveNivel.toString()).getString(i).isNullOrEmpty())
                         //   continue;
                        data.add(json.getJSONArray(moveNivel.toString()).getString(i))
                }
            }
            return data
        }

        /**
         * Validamos el campo de texto de nivel de sangre
         * Que no este vacio, que contenga coma o punto(formato de dato), y
         * tambien que tenga un tamaño maximo de 4
         *
         */
        fun validarInputTextSangre(inputText: String): Boolean{

            if(!inputText.isNullOrEmpty()){
                if(inputText.length <= 4){
                    if(inputText.contains(".") || inputText.contains(",")){
                        return  true;
                    }else {
                        Toast.makeText(
                            context,
                            "El el formato del valor en sangre es: (1.00 ó 1,00 a 7.00 ó 7,00)",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }else {
                    Toast.makeText(
                        context,
                        "El valor de sangre no puede ser superior a 4 digitos de tamaño",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }else {
                Toast.makeText(context, "El valor de sangre introducido no puede estar vacio", Toast.LENGTH_LONG)
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
                            context,
                            "El nivel ha de estar entre los rangos 1-54",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }else {
                    Toast.makeText(
                        context,
                        "El nivel no puede ser superior a 2 digitos de tamaño",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }else {
                Toast.makeText(context, "El nivel introducido no puede estar vacio", Toast.LENGTH_LONG)
                    .show()
            }

            return false;
        }

    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }


}