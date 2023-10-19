package com.example.sajatai

import android.app.AlertDialog
import android.content.ContentValues
import com.arthenica.mobileffmpeg.FFmpeg
import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import java.util.concurrent.TimeUnit
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.io.IOException
import android.Manifest
import android.content.pm.PackageManager
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import java.io.File
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okio.Okio
import okio.buffer
import okio.sink
import org.json.JSONObject


class recording_activity : AppCompatActivity() {

    private var isRecording = false
    private var elapsedTime = 0L
    private var MICROPHONE_PERMISSION_CODE = 200
    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var recordingUri: Uri? = null
    private var timer: CountDownTimer? = null
    private var isPaused = false
    private var pauseTime = 0L
    private lateinit var pauseResumeButton: Button
    private var retrievedKey: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recording)

        if(!hasPermissions()) {
            requestPermissions()
        }

        val micimage = findViewById<ImageView>(R.id.mic)
        val playbackbutton = findViewById<Button>(R.id.playback)
        pauseResumeButton = findViewById<Button>(R.id.pauseResume)
        val email = intent.getStringExtra("USEREMAIL") ?: "default@email.com"

        val rootRef = FirebaseDatabase.getInstance("https://sajatai-default-rtdb.europe-west1.firebasedatabase.app/").reference
        val query = rootRef.child("users").orderByChild("email").equalTo(email)

        val valueEventListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds in dataSnapshot.children) {
                    val key = ds.key
                    retrievedKey = key
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("DEBUG", databaseError.message)
            }
        }

        query.addListenerForSingleValueEvent(valueEventListener)



        pauseResumeButton.setOnClickListener {
            if (isPaused) {
                resumeRecordingProcess()
            } else {
                pauseRecordingProcess()
            }
        }

        micimage.setOnClickListener {
            if (isRecording) {
                stopRecordingProcess(micimage)
            } else {
                startRecordingProcess(micimage)
            }
        }

        playbackbutton.setOnClickListener {
            recordingUri?.let {
                startPlayback(it.toString())
            }
        }
    }

    private fun startRecordingProcess(micImage: ImageView) {
        isRecording = true
        micImage.setImageResource(R.drawable.xmic)
        startRecording()
        startTimer()
        pauseResumeButton.visibility = View.VISIBLE  // Show the pause/resume button
    }

    private fun stopRecordingProcess(micImage: ImageView) {
        isRecording = false
        isPaused = false
        micImage.setImageResource(R.drawable.mic)
        stopRecording()
        stopTimer()
        showRenameDialog()
        pauseResumeButton.visibility = View.GONE  // Hide the pause/resume button
    }
    private fun startTimer() {
        timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                elapsedTime++
                updateCountdownUI(elapsedTime)
            }

            override fun onFinish() {}
        }.start()
    }

    private fun stopTimer() {
        timer?.cancel()
        elapsedTime = 0
        updateCountdownUI(elapsedTime)
    }

    private fun updateCountdownUI(seconds: Long) {
        val countdowntext = findViewById<TextView>(R.id.countdown)
        countdowntext.text = seconds.toString()
    }

    private fun showRenameDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Rename Recording")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
            val newFileName = input.text.toString()   // Add the file extension
            renameRecording(newFileName)
        })
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, _ -> dialog.cancel() })

        builder.show()
    }

    private fun renameRecording(newFileName: String) {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, newFileName)

        recordingUri?.let { uri ->
            contentResolver.update(uri, contentValues, null, null)
            val email = intent.getStringExtra("USEREMAIL") ?: "default@email.com" // Get email, use a default if null
            uploadRecordingToFirebase(uri, email, newFileName)

            // Check if the file exists
            val filePath = getFilePathFromUri(uri)
            val file = File(filePath!!)
        }
    }





    private fun startRecording() {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "my_audio_file.mp3")
            put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MUSIC)
        }

        recordingUri = contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)

        // Show the file path as a toast
        showToastWithFilePath(recordingUri!!)

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            recordingUri?.let { uri ->
                val fileDescriptor = contentResolver.openFileDescriptor(uri, "w")?.fileDescriptor
                fileDescriptor?.let {
                    setOutputFile(it)
                }
            }

            try {
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            reset()
            release()
        }
        mediaRecorder = null
    }

    private fun startPlayback(uriString: String) {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(applicationContext, Uri.parse(uriString))
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun stopPlayback() {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null
    }

    private fun hasPermissions(): Boolean {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            MICROPHONE_PERMISSION_CODE
        )
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == MICROPHONE_PERMISSION_CODE) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted. You can start recording or other tasks
            } else {
                // Permissions denied. You should notify the user and potentially disable features.
            }
        }
    }

    private fun pauseRecordingProcess() {
        isPaused = true
        pauseTime = elapsedTime
        mediaRecorder?.pause()
        timer?.cancel()
        pauseResumeButton.text = "Resume"
    }

    private fun resumeRecordingProcess() {
        isPaused = false
        mediaRecorder?.resume()
        startTimer(pauseTime)
        pauseResumeButton.text = "Pause"
    }

    private fun startTimer(startFrom: Long = 0L) {
        elapsedTime = startFrom
        timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                elapsedTime++
                updateCountdownUI(elapsedTime)
            }

            override fun onFinish() {}
        }.start()
    }
    private fun uploadRecordingToFirebase(uri: Uri, email: String, recordingName: String) {
        val storageReference = FirebaseStorage.getInstance("gs://sajatai.appspot.com").reference

        // Set the metadata for the audio file
        val metadata = StorageMetadata.Builder()
            .setContentType("audio/mpeg")
            .build()

        // Define the path in Firebase Storage
        val childReference = storageReference.child("users/"+retrievedKey+"/audioFiles/"+recordingName+".mp3")

        val originalPath = getFilePathFromUri(uri)
        val convertedPath = "$originalPath.mp3"

        // Convert and upload
        if (convertToMp3(originalPath!!, convertedPath)) {
            val convertedUri = Uri.fromFile(File(convertedPath))
            childReference.putFile(convertedUri, metadata)
                .addOnSuccessListener { taskSnapshot ->
                    // After a successful upload, obtain the download URL
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                        val audioFilePath = downloadUri.toString()
                        transcribeAudio(audioFilePath) { transcription ->
                            // Use the downloadUri and transcription, e.g., save it to the Firebase Database
                            saveRecordingMetadataToDatabase(email, recordingName, audioFilePath, transcription)
                        }
                    }
                }
                .addOnFailureListener {
                    // Handle the error
                    Toast.makeText(this, "Failed to upload recording: ${it.message}", Toast.LENGTH_LONG).show()
                    it.printStackTrace() // This will print the full stack trace to your logcat.
                }
        } else {
            // Handle the conversion error here, if needed
            Toast.makeText(this, "Failed to convert the audio file", Toast.LENGTH_LONG).show()
        }
    }




    private fun saveRecordingMetadataToDatabase(email: String, recordingName: String, url: String, transcription: String?) {
        val databaseReference = FirebaseDatabase.getInstance("https://sajatai-default-rtdb.europe-west1.firebasedatabase.app/").reference.child("users/"+retrievedKey+"/audioFiles")
        val recordingData = hashMapOf<String, Any>(
            "fileName" to recordingName,
            "fileURL" to url,
            "transcription" to (transcription ?: "Transcription not available")
        )
        Log.d("DatabaseSave", "Saving: $recordingData")


        databaseReference.push().setValue(recordingData)
            .addOnSuccessListener {
                Toast.makeText(this, "Recording metadata saved successfully.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Handle the error
                Toast.makeText(this, "Failed to save recording metadata.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getFilePathFromUri(uri: Uri): String? {
        // Get the file path using ContentResolver
        val cursor = contentResolver.query(uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null)
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor?.moveToFirst()
        val filePath = columnIndex?.let { cursor.getString(it) }
        cursor?.close()
        return filePath
    }

    // Use this function wherever you need to toast the file path
    fun showToastWithFilePath(uri: Uri) {
        val filePath = getFilePathFromUri(uri)
    }

    private fun decrypt(data: String, secret: String): String {
        val cipher = Cipher.getInstance("AES")
        val secretKeySpec = SecretKeySpec(secret.toByteArray(), "AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
        val decodedValue = Base64.decode(data, Base64.DEFAULT)
        val decryptedValue = cipher.doFinal(decodedValue)
        return String(decryptedValue)
    }

    fun transcribeAudio(audioUrl: String, callback: (String?) -> Unit) {
        val API_URL = "https://api.openai.com/v1/audio/transcriptions"
        val API_KEY = ""

        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .build()

        // Download the audio file from Firebase Storage to a local temp file
        val requestDownload = Request.Builder()
            .url(audioUrl)
            .build()

        client.newCall(requestDownload).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val tempFile = File.createTempFile("temp_audio", ".mp3", cacheDir)
                val sink = tempFile.sink().buffer()
                sink.writeAll(response.body!!.source())
                sink.close()

                // Now, send this local file to the transcription API
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "audio.mp3", RequestBody.create("audio/mpeg".toMediaTypeOrNull(), tempFile))
                    .addFormDataPart("model", "whisper-1")
                    .build()

                val requestTranscribe = Request.Builder()
                    .url(API_URL)
                    .header("Authorization", "Bearer $API_KEY")
                    .post(requestBody)
                    .build()

                client.newCall(requestTranscribe).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        tempFile.delete()
                        Log.e("TranscriptionError", "Network error: ${e.message}")
                        callback(null)
                    }

                    override fun onResponse(call: Call, responseTranscribe: Response) {
                        if (responseTranscribe.isSuccessful) {
                            val jsonResponse = responseTranscribe.body?.string()
                            Log.d("TranscriptionSuccess", "Response: $jsonResponse")
                            val jsonObject = JSONObject(jsonResponse)
                            val transcription = jsonObject.optString("text")
                            callback(transcription)
                        } else {
                            Log.e("TranscriptionError", "Response error: ${responseTranscribe.code} - ${responseTranscribe.body?.string()}")
                            // Print the error response for debugging purposes
                                callback(null)
                        }
                        tempFile.delete()
                    }
                })
            }
        })
    }

    private fun convertToMp3(inputPath: String, outputPath: String): Boolean {
        val returnCode = FFmpeg.execute("-i $inputPath $outputPath")
        return returnCode == 0
    }

}
