package com.juan.prohealth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.juan.prohealth.database.User

class SplashScreenActivity : AppCompatActivity() {

    lateinit var handler: Handler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val activityDestination = if (User.isLogged()) PreinicioActivity::class.java else LoginActivity::class.java

        handler = Handler()
        handler.postDelayed({
            val intent = Intent(this, activityDestination)
            startActivity(intent)
            finish()
        },2000)//delay
    }
}
