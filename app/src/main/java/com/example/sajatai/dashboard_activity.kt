package com.example.sajatai

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.cardview.widget.CardView

class dashboard_activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val userName = intent.getStringExtra("USERNAME")
        val emailaddress = intent.getStringExtra("USEREMAIL")
        val userNameTextView = findViewById<TextView>(R.id.nev_id)
        userNameTextView.text = userName

        val hangfelvetel = findViewById<CardView>(R.id.hangfelvetel_id)
        val hangfelvetelek_tablazat = findViewById<CardView>(R.id.hangfelvetelek)

        hangfelvetel.setOnClickListener{
            val torecording = Intent(this@dashboard_activity, recording_activity::class.java)
            torecording.putExtra("USEREMAIL", emailaddress)
            startActivity(torecording)
        }

        hangfelvetelek_tablazat.setOnClickListener{
            val tohangfelvetelek = Intent(this@dashboard_activity, hangfelvetelek_activity::class.java)
            tohangfelvetelek.putExtra("USEREMAIL", emailaddress)
            startActivity(tohangfelvetelek)
        }
    }
}