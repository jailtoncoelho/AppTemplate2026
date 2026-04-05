package com.ifpr.androidapptemplate.ui.configuracoes

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.ifpr.androidapptemplate.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val settingsToolbar = findViewById<Toolbar>(R.id.settingsToolbar)
        setSupportActionBar(settingsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        
        settingsToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val btnConta = findViewById<LinearLayout>(R.id.btnConta)
        btnConta.setOnClickListener {
            startActivity(Intent(this, AccountSettingsActivity::class.java))
        }
    }
}
