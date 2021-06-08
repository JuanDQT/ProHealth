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
import com.juan.prohealth.ui.common.*
import com.juan.prohealth.ui.mainActiviy.MainActivity
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class InitialActivity : AppCompatActivity() {

    private lateinit var validationRepository: ValidationRepository
    private lateinit var viewModel: InitialMainViewModel
    private lateinit var binding: ActivityInitialBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildDependencies()
        viewModel = buildViewModel()
        binding = ActivityInitialBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Comprobamos que ya esta guardado
        if (viewModel.isInSharedPreferences(arrayOf(DOSE_LEVEL_TEXT))) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            startActivity(intent)
            return
        }

        binding.btnSaveDose.setOnClickListener { setFirstDoseLevel() }
    }

    private fun setFirstDoseLevel() {
        viewModel.addString(
            DOSE_LEVEL_TEXT,
            binding.etNivel.text.toString()
        )
        val intent =
            Intent(this@InitialActivity, MainActivity::class.java)
        startActivity(intent)
    }

    private fun setFirstDoseWithVerificationFromServerOld() {
        val inputBloodLevel = binding.etNivel.text.toString()

        if (AppContext.validarInputNivel(inputBloodLevel)) {
            // Guardamos los valores en sharedPrederences

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

                            val finalDate =
                                SimpleDateFormat("yyyy-MM-dd").parse(it.getString("fechaFin"))

                            val value = finalDate.clearTime().time

                            viewModel.setFinalDate(value)
                            //MySharedPreferences.shared.setFechaFinPrueba(value)

                            viewModel.addString(
                                DOSE_LEVEL_TEXT,
                                binding.etNivel.text.toString()
                            )
                            /*       MySharedPreferences.shared.addString(
                                            "nivel",
                                            binding.etNivel.text.toString()
                                        )*/
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

    private fun buildViewModel(): InitialMainViewModel {
        val factory = InitialModelFactory(validationRepository)
        return ViewModelProvider(this, factory).get(InitialMainViewModel::class.java)
    }

    private fun buildDependencies() {
        val sharedPreference = SharedPreference.getInstance(this.applicationContext)
        validationRepository = ValidationRepository(StorageValidationDataSource(sharedPreference))
    }

    private fun isExistInSharedPreferencesOld(): Boolean {
        // Comprobamos que los valores existan en el sharedPreferences.
        return MySharedPreferences.shared.exists(arrayOf(DOSE_LEVEL_TEXT))
    }

    companion object {
        private const val DOSE_LEVEL_TEXT: String = "nivel"
    }
}