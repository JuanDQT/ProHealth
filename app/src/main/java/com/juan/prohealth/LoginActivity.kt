package com.juan.prohealth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.juan.prohealth.database.room.MyDatabase
import com.juan.prohealth.database.room.RoomUserDataSource
import com.juan.prohealth.databinding.ActivityLoginBinding
import com.juan.prohealth.repository.UserRepository
import com.juan.prohealth.ui.LoginViewModel
import com.juan.prohealth.ui.LoginViewModelFactory
import com.juan.prohealth.ui.initialActivity.InitialActivity

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var viewModel: LoginViewModel
    private lateinit var userRepository: UserRepository
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        buildDependencies()
        viewModel = buildViewModel()

        binding.tvModoInvitado.setOnClickListener(this)
        binding.btnLogin.setOnClickListener(this)
    }

    private fun buildViewModel(): LoginViewModel {
        val factory = LoginViewModelFactory(userRepository)
        return ViewModelProvider(this, factory).get(LoginViewModel::class.java)
    }

    private fun buildDependencies() {
        val database = MyDatabase.getDatabase(this)
        val userLocal = RoomUserDataSource(database)

        userRepository = UserRepository(userLocal)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.tvModoInvitado.id -> startActivity(Intent(this, InitialActivity::class.java))
            // TODO: conexion API, respuesta..
            binding.btnLogin.id -> return
        }
    }
}