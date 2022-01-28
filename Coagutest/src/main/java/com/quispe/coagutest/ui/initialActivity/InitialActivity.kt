package com.quispe.coagutest.ui.initialActivity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.quispe.coagutest.database.room.MyDatabase
import com.quispe.coagutest.database.room.RoomUserDataSource
import com.quispe.coagutest.database.room.User
import com.quispe.coagutest.databinding.ActivityInitialBinding
import com.quispe.coagutest.repository.UserRepository
import com.quispe.coagutest.ui.mainActiviy.MainActivity

class InitialActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var userRepository: UserRepository
    private lateinit var viewModel: InitialMainViewModel
    private lateinit var binding: ActivityInitialBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildDependencies()
        viewModel = buildViewModel()
        binding = ActivityInitialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSaveDose.setOnClickListener(this)
    }

    private fun instanceUser(): Boolean {
        val doseInput = binding.etNivel.text.toString().toIntOrNull()
        if (doseInput != null) {
            val guestUser = User(level = doseInput)
            viewModel.createUser(guestUser)
            return true
        }
        return false
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun buildViewModel(): InitialMainViewModel {
        val factory = InitialModelFactory(userRepository)
        return ViewModelProvider(this, factory).get(InitialMainViewModel::class.java)
    }

    private fun buildDependencies() {
        val database = MyDatabase.getDatabase(this)
        val userLocal = RoomUserDataSource(database)
        userRepository = UserRepository(userLocal)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.btnSaveDose.id -> {
                if (instanceUser())
                    goToMainActivity()
            }
        }
    }
}
