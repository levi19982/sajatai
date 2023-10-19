package com.example.sajatai

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
class hangfelvetelek_activity : AppCompatActivity() {
    private var retrievedKey: String? = null
    private val recordings = mutableListOf<Recording>() // Make this a member variable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hangfelvetelek)

        val emailaddress = intent.getStringExtra("USEREMAIL")

        val rootRef = FirebaseDatabase.getInstance("https://sajatai-default-rtdb.europe-west1.firebasedatabase.app/").reference
        val query = rootRef.child("users").orderByChild("email").equalTo(emailaddress)

        val valueEventListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds in dataSnapshot.children) {
                    val key = ds.key
                    retrievedKey = key
                }
                fetchAudioFiles()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("DEBUG", databaseError.message)
            }
        }

        query.addListenerForSingleValueEvent(valueEventListener)

        // Initialize the RecyclerView with empty data
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView_recordings)
        val adapter = RecordingsAdapter(recordings)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun fetchAudioFiles() {
        Log.d("DEBUG", "Retrieved Key: $retrievedKey")
        if (retrievedKey == null) return

        val rootRef = FirebaseDatabase.getInstance("https://sajatai-default-rtdb.europe-west1.firebasedatabase.app/").reference
        val audioFilesRef = rootRef.child("users").child(retrievedKey!!).child("audioFiles")

        audioFilesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("DEBUG", "Audio Files Snapshot: $dataSnapshot")
                recordings.clear() // Clear the old data
                for (audioFileSnapshot in dataSnapshot.children) {
                    val fileName = audioFileSnapshot.child("fileName").getValue(String::class.java)
                    val fileUrl = audioFileSnapshot.child("fileURL").getValue(String::class.java)
                    if (fileName != null && fileUrl != null) {
                        recordings.add(Recording(fileName, fileUrl))
                    }
                }
                updateRecyclerView()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("DEBUG", "Error fetching data: ${databaseError.message}")
            }

        })
    }

    private fun updateRecyclerView() {
        Log.d("DEBUG", "Updating RecyclerView with ${recordings.size} recordings.")
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView_recordings)
        recyclerView.adapter?.notifyDataSetChanged() // Notify the adapter about the data changes
    }

}
