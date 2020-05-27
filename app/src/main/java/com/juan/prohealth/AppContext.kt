package com.juan.prohealth

import android.app.Application
import android.content.Context
import android.widget.Toast
import com.juan.prohealth.database.CustomMigration
import io.realm.Realm
import io.realm.RealmConfiguration
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import kotlin.collections.ArrayList

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


        fun getNivelFromFichero(fileName: String, nivel: String, dias: String, variacionNivelDosis: Boolean): ArrayList<String> {
            var json = JSONObject(openJSON(fileName))
            var dataFourDays = arrayListOf<String>()
            var dataSevenDays = arrayListOf<String>()

            if(dias == "4") {
                return getDateFourDays(fileName,nivel,dataFourDays, json)
            }
            if (dias == "7") {
                // de 0 > 4 guarda una array de 3 (4 numeros) json.getJSONArray(nivel).length()

                /*for (i in 0 until json.getJSONArray(nivel).length()) {
                    if (json.getJSONArray(nivel).getString(i).isNullOrEmpty() && dataForDays.size<4) {
                        var diasFaltantes = json.getJSONArray(nivel).length().minus(i)
                        for (k in 0 until diasFaltantes) {
                            if(!json.getJSONArray(nivel).getString(k).isNullOrEmpty()) {
                                dataForDays.add(json.getJSONArray(nivel).getString(k))
                            }
                        }
                    }else{
                        if(dataForDays.size<4) {
                            dataForDays.add(json.getJSONArray(nivel).getString(i))
                        }
                    }
                }
                */
                //llamamos la funcion para obtener una iteracion COMPLETA de 4 dias sin ""
                dataFourDays = getDateFourDays(fileName,nivel,dataFourDays, json)
                var diasRestantes = json.getJSONArray(nivel).length().minus(1)// 3 espacios faltantes a rellenar
                //TODO Datos a arreglar Nivel 19 - 4.8 sangre
                for (i in 0 until 4){
                    dataSevenDays.add(dataFourDays.get(i))
                }

                var condicionA = json.getJSONArray(nivel).getString(1).isNullOrEmpty()
                var condicionB = json.getJSONArray(nivel).getString(2).isNullOrEmpty()
                var condicionC = json.getJSONArray(nivel).getString(3).isNullOrEmpty()

                    for (i in 0 until diasRestantes) {
                        //Unico caso en el que necesita una logica especial, cuando el ultimo campo(3) del JSON esta vacio ""
                        if(dataSevenDays.size<7 && condicionC &&!condicionA &&!condicionB) {
                            var reajuste = i + 1
                            dataSevenDays.add(dataFourDays.get(reajuste))
                        }else{
                            dataSevenDays.add(dataFourDays.get(i))
                        }
                    }
                return dataSevenDays
                }

            return dataFourDays
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

        fun getDateFourDays(fileName: String, nivel: String, dataForDays: ArrayList<String>, json: JSONObject): ArrayList<String>{
            //json.getJSONArray(nivel).length())
            //(dias.toInt())
            for (i in 0 until json.getJSONArray(nivel).length()) {
                if (json.getJSONArray(nivel).getString(i).isNullOrEmpty()&& dataForDays.size<4) {
                    var diasRestantes = json.getJSONArray(nivel).length().minus(i)
                    for (k in 0 until diasRestantes) {
                        if(!json.getJSONArray(nivel).getString(k).isNullOrEmpty()){
                            dataForDays.add(json.getJSONArray(nivel).getString(k))
                        }
                    }
                }else{
                    if(dataForDays.size<4) {
                        dataForDays.add(json.getJSONArray(nivel).getString(i))
                    }
                }
            }
            return dataForDays
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        Realm.init(context)
        val realmConfiguration: RealmConfiguration = RealmConfiguration.Builder().name("control").migration(CustomMigration()).build()
        Realm.setDefaultConfiguration(realmConfiguration)
    }

}