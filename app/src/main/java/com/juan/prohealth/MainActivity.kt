package com.juan.prohealth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.nio.charset.StandardCharsets.UTF_8

class MainActivity : AppCompatActivity() {

    val RANGO_AZUL = "rangoBajoAzul.json"
    val RANGO_ROJO = "rangoAltoRojo.json"

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

        btnCambiarVentana.setOnClickListener {
            val calendarioActivity = Intent(this, CalendarioActivity::class.java)
            startActivity(calendarioActivity)
        }
        val randomTest = openJSON()

        randomTest?.let {
            print(it)
            val json = JSONObject(it);

            //val nivel30 = json.get("30");
            val datosArray = json.getJSONArray("30")

            Toast.makeText(this, "[30][0] " + datosArray[0], Toast.LENGTH_LONG).show()

        }
    }

    fun calcularTodo(valor: Int) {
        // TODO
    }

    fun openJSON(): String? {
        var json: String? = null
        json = try {
            val inputStream = this.assets.open(RANGO_AZUL)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, UTF_8)
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
        return json
    }
}
