package com.juan.prohealth

import android.app.Application
import android.content.Context
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
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }


}