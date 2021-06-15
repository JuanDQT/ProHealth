package com.juan.prohealth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.juan.prohealth.database.room.MyDatabase
import com.juan.prohealth.database.room.RoomUserDataSource
import com.juan.prohealth.database.room.User
import com.juan.prohealth.databinding.ActivityLoginBinding
import com.juan.prohealth.repository.UserRepository
import com.juan.prohealth.ui.LoginViewModel
import com.juan.prohealth.ui.LoginViewModelFactory
import com.juan.prohealth.ui.initialActivity.InitialActivity
import com.juan.prohealth.database.User as RealmUser

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        buildDependencies()
        viewModel = buildViewModel()

        binding.tvModoInvitado.setOnClickListener {
            // La primera vez, tendra id -1 en modo invitado
            RealmUser.crearUsuarioInvitado()
            RealmUser.setLogged("-1")
            startActivity(Intent(this, InitialActivity::class.java))
            var userRandom = User()
            viewModel.createInvitedUser(userRandom)
        }

        binding.btnLogin.setOnClickListener {
            // TODO: conexion API, respuesta.. registramos en user y actualizamos User.setLogged() con el id del servidor
            // Algo parecido con el boton de Google, pero lo dejamos para el final
            // Ya que tiraremos del User.Id -1, que es lo mismo
        }

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

    /**
     * Boton de ATRAS cerramos la app
     */
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}