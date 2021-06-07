package com.juan.prohealth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.juan.prohealth.databinding.ActivitySplashScreenBinding
import com.juan.prohealth.ui.firstActivity.PreinicioActivity

class SplashScreenActivity : AppCompatActivity() {

    lateinit var handler: Handler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val activityDestination = if (User2.isLogged()) PreinicioActivity::class.java else LoginActivity::class.java

        handler = Handler()
        handler.postDelayed({
            val intent = Intent(this, activityDestination)
            intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            startActivity(intent)
            finish()
        },2000)//delay
    }
}
