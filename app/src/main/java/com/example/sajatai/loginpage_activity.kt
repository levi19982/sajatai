package com.example.sajatai

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class loginpage_activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loginpage)

        val signupbutton = findViewById<TextView>(R.id.sign_up)

        signupbutton.setOnClickListener{
            val tosignup = Intent(this@loginpage_activity, signup_activity::class.java)
            startActivity(tosignup)
        }
    }
}