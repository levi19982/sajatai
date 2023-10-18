package com.example.sajatai

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class dashboard_activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val userName = intent.getStringExtra("USERNAME")
        val userNameTextView = findViewById<TextView>(R.id.nev_id)
        userNameTextView.text = userName
    }
}