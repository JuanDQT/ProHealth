package com.quispe.coagutest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.quispe.coagutest.database.room.MyDatabase
import com.quispe.coagutest.database.room.RoomUserDataSource
import com.quispe.coagutest.databinding.ActivitySplashScreenBinding
import com.quispe.coagutest.repository.UserRepository
import com.quispe.coagutest.ui.initialActivity.*
import com.quispe.coagutest.ui.mainActiviy.MainActivity

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var userRepository: UserRepository
    private lateinit var viewModel: SplashScreenViewModel
    private lateinit var binding: ActivitySplashScreenBinding

    lateinit var handler: Handler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
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
