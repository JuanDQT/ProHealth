package com.juan.prohealth

import android.app.Application
import android.content.Context
import android.provider.Settings
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import com.juan.prohealth.database.CustomMigration
import io.realm.Realm
import io.realm.RealmConfiguration
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class AppContext : Application() {

    companion object {
        lateinit  var context: Context

        fun getFileContentFromAssets(fileName: String): String? {
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

        /**
         * En esta funcion abarcamos las 3 posibles opciones para iterar correctamente
         * en caso de planificar dosis para 3, 4, y 7 dias. En caso de que haya algun
         * nivel espacios vacios de dosis, se procedera a volver a empezar hasta abarcar las
         * dosis que se han de planificar empezando desde el principio. Devolviendo un arrayList
         * con los 3, 4 o 7 dias sin VACIOS con las dosis que corresponden.
         */
        fun getNivelFromFichero(fileName: String, nivel: String, dias: String): ArrayList<String> {
            var json = JSONObject(getFileContentFromAssets(fileName))
            var dataThreeDays = arrayListOf<String>()
            var dataFourDays = arrayListOf<String>()
            var dataSevenDays = arrayListOf<String>()
            var dataDosisOut = arrayListOf<String>()

            when(dias){
                "3"->{
                    dataThreeDays = getDateFourDays(fileName,nivel,dataFourDays, json)
                    for (i in 0 until 3){
                        dataDosisOut.add(dataThreeDays.get(i))
                    }
                }
               "4"->{
                   dataDosisOut = getDateFourDays(fileName,nivel,dataFourDays, json)
               }
                "7"->{
                    dataFourDays = getDateFourDays(fileName,nivel,dataFourDays, json)
                    var diasRestantesRellenar = json.getJSONArray(nivel).length().minus(1)
                    // 3 espacios faltantes a rellenar
                    for (i in 0 until 4){
                        dataSevenDays.add(dataFourDays.get(i))
                    }
                    dataDosisOut = getDateSevenDays(fileName, nivel, dataSevenDays, dataFourDays, json, diasRestantesRellenar)
                }
            }
            //TODO revisar que sucede al poner 4.9 en sangre ya que saca vacios
            return dataDosisOut
        }

        /**
         * Validamos el campo de texto de nivel de sangre
         * Que no este vacio, que contenga coma o punto(formato de dato), y
         * tambien que tenga un tamaño maximo de 4 Y ESTE ENTRE 0 y 7
         *
         */
        fun validarInputTextSangre(inputText: String): Float? {

            inputText.toFloatOrNull()?.let {
                if(it in 1..7)
                    return (it)
            }

            return null
        }

        fun getIMEI(): String {
            return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
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

        /**
         * Funcion que rellenar un arrayList de tamaño 4 que son los que corresponden
         * para planificar la dosis sin VACIOS, y en caso de haberlos rellenarlos
         * de la manera correcta, volviendo a empezar desde el nivel hasta que se
         * rellenen los 4 espacios.
         */
        fun getDateFourDays(fileName: String, nivel: String, dataForDays: ArrayList<String>, json: JSONObject): ArrayList<String>{
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

        /**
         * Funcion para rellenar una arrayList de tamaño 7, para ello
         * nos apoyamos en la que utilizabamos de 4, e iteramos correctamente
         * para rellenar los 3 dias que faltan por rellenar. Solo existe un caso concreto
         * en el que si cumplen los 3 requisitos se aplica un reajuste para rellenar
         * correctamente la arrayList de 7 con un correcto rellenado de dosis.
         */
        fun getDateSevenDays(fileName: String, nivel: String, dataSevenDays: ArrayList<String>, dataFourDays: ArrayList<String>, json: JSONObject, diasRestantesRellenar: Int): ArrayList<String>{
            var condicioA = json.getJSONArray(nivel).getString(1).isNullOrEmpty()
            var condicioB = json.getJSONArray(nivel).getString(2).isNullOrEmpty()
            var condicioC = json.getJSONArray(nivel).getString(3).isNullOrEmpty()
            for (i in 0 until diasRestantesRellenar) {
                //Unico caso en el que necesita una logica especial, cuando el ultimo campo(3) del JSON esta vacio ""
                if(dataSevenDays.size<7 && condicioC &&!condicioA &&!condicioB) {
                    var reajuste = i + 1
                    dataSevenDays.add(dataFourDays.get(reajuste))
                }else{
                    dataSevenDays.add(dataFourDays.get(i))
                }
            }
            return dataSevenDays

        }

        fun getImageNameByJSON(jsonData: String): String {
            when (jsonData) {
                "0" -> return "notoca"
                "1/8" -> return "un_octavo"
                "1/4" -> return "un_cuarto"
                "1/2" -> return "medio"
                "3/4" -> return "tres_cuartos"
                "1" -> return "entero"
                "1+1/4" -> return "entero_un_cuarto"
                "1+1/2" -> return "entero_un_medio"
                "1+3/4" -> return "entero_tres_cuartos"
                "2" -> return "dos_enteros"
                "2+1/4" -> return "dos_enteros_un_cuarto"
                else  -> return ""
            }
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