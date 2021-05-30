package com.juan.prohealth.ui.initialActivity

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.juan.prohealth.*
import com.juan.prohealth.data.local.SharedPreference
import com.juan.prohealth.data.local.StorageValidationDataSource
import com.juan.prohealth.databinding.ActivityInitialBinding
import com.juan.prohealth.repository.ValidationRepository
import com.juan.prohealth.ui.mainActiviy.MainActivity
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class InitialActivity : AppCompatActivity() {

    private lateinit var validationRepository: ValidationRepository
    private lateinit var viewModel: InitialMainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityInitialBinding.inflate(layoutInflater)
        buildDependencies()
        viewModel = buildViewModel()
        setContentView(binding.root)
        // Comprobamos que ya esta guardado
        if (viewModel.isInSharedPreferences()) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            startActivity(intent)
            return
        }

        binding.btnINR.setOnClickListener {

            //Guardamos valores en String
            val inputNivelSangreText = binding.etNivel.text.toString()
            //Validamos estos valores
            if (AppContext.validarInputNivel(inputNivelSangreText)) {
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

                                val fechaFin =
                                    SimpleDateFormat("yyyy-MM-dd").parse(it.getString("fechaFin"))
                                MySharedPreferences.shared.setFechaFinPrueba(fechaFin.clearTime().time)


                                MySharedPreferences.shared.addString(
                                    "nivel",
                                    binding.etNivel.text.toString()
                                )
                                val intent =
                                    Intent(this@InitialActivity, MainActivity::class.java)
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

    private fun buildViewModel(): InitialMainViewModel {
        val factory = InitialModelFactory(validationRepository)
        return ViewModelProvider(this, factory).get(InitialMainViewModel::class.java)
    }

    private fun buildDependencies() {
        val sharedPreference = SharedPreference.getInstance(this.applicationContext)
        validationRepository = ValidationRepository(StorageValidationDataSource(sharedPreference))
    }

    private fun isExistInSharedPreferences(): Boolean {
        // Comprobamos que los valores existan en el sharedPreferences.
        return MySharedPreferences.shared.exists(arrayOf("nivel"))
    }
}
