package com.example.sajatai

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class hangfelvetelek_activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hangfelvetelek)



        // Dummy data for testing
        val recordings = listOf(
            Recording("Recording 1"),
            Recording("Recording 2")
            // ... add more recordings for testing
        )

        // Set up the RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView_recordings)
        val adapter = RecordingsAdapter(recordings)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

    }

}