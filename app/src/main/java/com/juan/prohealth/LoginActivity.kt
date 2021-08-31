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
            val guestUser = User()
            instanteUser(guestUser)
            goToInitialActivity()
        }

        // TODO: conexion API, respuesta..
        binding.btnLogin.setOnClickListener {
        }
    }

    // Instance user from Local or FireBase
    private fun instanteUser(user: User) {
        viewModel.createUser(user)
    }

    private fun goToInitialActivity() {
        startActivity(Intent(this, InitialActivity::class.java))
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
}