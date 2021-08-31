package com.juan.prohealth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.lifecycle.ViewModelProvider
import com.juan.prohealth.data.local.SharedPreference
import com.juan.prohealth.data.local.StorageValidationDataSource
import com.juan.prohealth.database.User
import com.juan.prohealth.database.room.MyDatabase
import com.juan.prohealth.database.room.RoomUserDataSource
import com.juan.prohealth.databinding.ActivityInitialBinding
import com.juan.prohealth.databinding.ActivitySplashScreenBinding
import com.juan.prohealth.repository.UserRepository
import com.juan.prohealth.repository.ValidationRepository
import com.juan.prohealth.ui.initialActivity.*
import com.juan.prohealth.ui.mainActiviy.MainActivity

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var userRepository: UserRepository
    private lateinit var viewModel: SplashScreenViewModel
    private lateinit var binding: ActivitySplashScreenBinding

    lateinit var handler: Handler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buildDependencies()
        viewModel = buildViewModel()
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        subscribeUI()
    }

    private fun subscribeUI() {
        viewModel.existsCurrentUser.observe(this) { existsCurrentUser ->
            val activityDestination = if (existsCurrentUser) MainActivity::class.java else LoginActivity::class.java

            handler = Handler()
            handler.postDelayed({
                val intent = Intent(this, activityDestination)
                startActivity(intent)
                finish()
            },2000)//delay
        }
    }

    private fun buildViewModel(): SplashScreenViewModel {
        val factory = SplashScreenFactory(userRepository)
        return ViewModelProvider(this, factory).get(SplashScreenViewModel::class.java)
    }

    private fun buildDependencies() {
        val database = MyDatabase.getDatabase(this)
        val userLocal = RoomUserDataSource(database)
        userRepository = UserRepository(userLocal)
    }
}
