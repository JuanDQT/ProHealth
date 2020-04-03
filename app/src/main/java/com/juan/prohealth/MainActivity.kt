package com.juan.prohealth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.InputStream

class MainActivity : AppCompatActivity() {

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
    }

    fun calcularTodo(valor: Int) {
        // TODO
    }

    fun openJSON(): String? {
        var json: String? = null
        json = try {
            val inputStream: InputStream = com.juan.poesiadecine.Common.context.getAssets()
                .open(com.juan.poesiadecine.Common.JSON_CONFIG)
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
