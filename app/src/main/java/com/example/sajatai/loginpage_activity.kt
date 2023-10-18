package com.example.sajatai

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import org.mindrot.jbcrypt.BCrypt
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.security.MessageDigest

class loginpage_activity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private val secretKey = "s3cr3tK3yF0rA3S!"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loginpage)

        database = FirebaseDatabase.getInstance("https://sajatai-default-rtdb.europe-west1.firebasedatabase.app/").reference

        val signupButton = findViewById<TextView>(R.id.sign_up)
        val loginButton = findViewById<Button>(R.id.login_btn)
        val emailText = findViewById<EditText>(R.id.email)
        val passwordText = findViewById<EditText>(R.id.password)

        loginButton.setOnClickListener {
            val email = emailText.text.toString()
            val password = passwordText.text.toString()

            val encryptedEmail = encrypt(email, secretKey)
            val hashedEmail = hashString(encryptedEmail)

            database.child("users").child(hashedEmail).get().addOnSuccessListener {
                val user = it.value as? HashMap<String, String> ?: return@addOnSuccessListener
                val storedHashedPassword = user["password"] ?: return@addOnSuccessListener

                if (BCrypt.checkpw(password, storedHashedPassword)) {
                    val hashedEmail = hashString(encrypt(email, secretKey))
                    val userRef = database.child("users").child(hashedEmail)
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val user = snapshot.getValue<Map<String, Any>>()
                            val encryptedName = user?.get("name") as? String ?: "No Name"
                            val userName = decrypt(encryptedName, "s3cr3tK3yF0rA3S!")
                            navigateToDashboard(userName)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle error
                        }
                    })
                } else {
                    // Password is incorrect
                    Toast.makeText(this, "Hibás email vagy jelszó", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                // Handle the error (e.g., user not found)
                Toast.makeText(this, "Hibás email vagy jelszó", Toast.LENGTH_SHORT).show()
            }
        }

        signupButton.setOnClickListener {
            val tosignup = Intent(this@loginpage_activity, signup_activity::class.java)
            startActivity(tosignup)
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

    private fun navigateToDashboard(userName: String) {
        val intent = Intent(this@loginpage_activity, dashboard_activity::class.java)
        intent.putExtra("USERNAME", userName)
        startActivity(intent)
    }

    private fun decrypt(data: String, secret: String): String {
        val cipher = Cipher.getInstance("AES")
        val secretKeySpec = SecretKeySpec(secret.toByteArray(), "AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
        val decodedValue = Base64.decode(data, Base64.DEFAULT)
        val decryptedValue = cipher.doFinal(decodedValue)
        return String(decryptedValue)
    }


}
