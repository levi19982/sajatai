package com.example.sajatai

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.mindrot.jbcrypt.BCrypt
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.security.MessageDigest

class signup_activity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private val secretKey = "s3cr3tK3yF0rA3S!"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        database = FirebaseDatabase.getInstance("https://sajatai-default-rtdb.europe-west1.firebasedatabase.app/").reference

        val name = findViewById<TextInputEditText>(R.id.name_id)
        val email = findViewById<TextInputEditText>(R.id.email_id)
        val username = findViewById<TextInputEditText>(R.id.username_id)
        val password = findViewById<TextInputEditText>(R.id.password_id)
        val signup_button = findViewById<MaterialButton>(R.id.signupbutton)

        signup_button.setOnClickListener {
            val encryptedName = encrypt(name.text.toString(), secretKey)
            val encryptedEmail = encrypt(email.text.toString(), secretKey)
            val encryptedUsername = encrypt(username.text.toString(), secretKey)
            val hashedPassword = BCrypt.hashpw(password.text.toString(), BCrypt.gensalt())

            writeToDatabase(encryptedName, encryptedEmail, encryptedUsername, hashedPassword)
        }
    }

    private fun writeToDatabase(name: String, email: String, username: String, passwordHash: String) {
        val userId = database.push().key
        val user = hashMapOf(
            "name" to name,
            "email" to email,
            "username" to username,
            "password" to passwordHash
        )

        if (userId != null) {
            val hashedEmail = hashString(email)
            database.child("users").child(hashedEmail).setValue(user)
                .addOnSuccessListener {
                    Toast.makeText(this, "Sikeres regisztráció", Toast.LENGTH_SHORT).show()
                    val tologin = Intent(this@signup_activity, loginpage_activity::class.java)
                    startActivity(tologin)
                }
                .addOnFailureListener {
                    // Handle error
                }
        }
    }

    private fun encrypt(data: String, secret: String): String {
        val cipher = Cipher.getInstance("AES")
        val secretKeySpec = SecretKeySpec(secret.toByteArray(), "AES")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
        val encryptedValue = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(encryptedValue, Base64.DEFAULT)
    }

    private fun hashString(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

}

