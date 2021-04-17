package com.juan.prohealth

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.juan.prohealth.security.UniqueDeviceID
import kotlinx.android.synthetic.main.activity_preinicio.*
import kotlinx.android.synthetic.main.activity_preinicio.btnINR
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class PreinicioActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preinicio)

        // Comprobamos que ya esta guardado
        if (goInicioActivity()) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            startActivity(intent)
            return
        }

        btnINR.setOnClickListener {

            //Guardamos valores en String
            val inputNivelSangreText = et_nivel.text.toString()
            //Validamos estos valores
            if(AppContext.validarInputNivel(inputNivelSangreText)){
                // Guardamos los valores en sharedPrederences


                //
                val pdLoading = ProgressDialog(this)
                pdLoading.setMessage(getString(R.string.validando))
                pdLoading.show()

                //
                SyncData.validateDevice(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject?) {

                        response?.let {
                            val status = it.getInt("status")
                            pdLoading.dismiss()

                            if (status == 1) {

                                val fechaFin = SimpleDateFormat("yyyy-MM-dd").parse(it.getString("fechaFin"))
                                MySharedPreferences.shared.setFechaFinPrueba(fechaFin.clearTime().time)


                                MySharedPreferences.shared.addString("nivel", et_nivel.text.toString())
                                val intent = Intent(this@PreinicioActivity, MainActivity::class.java)
                                startActivity(intent)
                            } else
                                alert(getString(R.string.alerta), "Vuelvelo a intentar mas tarde")
                        }
                    }

                    override fun onError(anError: ANError?) {
                        pdLoading.dismiss()
                        alert(getString(R.string.alerta), getString(R.string.error_verificacion))
                    }
                })
            }



            //

            // respuesta, guardar en fecha fin..

            // continue

        }

    }

    fun goInicioActivity(): Boolean {
        // Comprobamos que los valores existan en el sharedPreferences.
        return MySharedPreferences.shared.exists(arrayOf("nivel"))
    }
}
