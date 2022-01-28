package com.quispe.coagutest

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.quispe.coagutest.database.room.MyDatabase
import com.quispe.coagutest.database.room.RoomUserDataSource
import com.quispe.coagutest.databinding.ActivityLoginBinding
import com.quispe.coagutest.repository.UserRepository
import com.quispe.coagutest.ui.LoginViewModel
import com.quispe.coagutest.ui.LoginViewModelFactory
import com.quispe.coagutest.ui.initialActivity.InitialActivity

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