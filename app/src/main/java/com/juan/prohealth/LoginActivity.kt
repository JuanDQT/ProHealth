package com.juan.prohealth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.juan.prohealth.database.User
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        tvModoInvitado.setOnClickListener {
            // La primera vez, tendra id -1 en modo invitado
            User.crearUsuarioInvitado()
            User.setLogged("-1")
            startActivity(Intent(this, MainActivity::class.java))
        }

        btnLogin.setOnClickListener {
            // TODO: conexion API, respuesta.. registramos en user y actualizamos User.setLogged() con el id del servidor
            // Algo parecido con el boton de Google, pero lo dejamos para el final
            // Ya que tiraremos del User.Id -1, que es lo mismo
        }

    }

    /**
     * Boton de ATRAS cerramos la app
     */
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}