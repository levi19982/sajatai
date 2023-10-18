package com.example.sajatai

import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.os.SystemClock
import android.text.InputType
import android.widget.Button
import android.widget.EditText
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
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recording)

        if(!hasPermissions()) {
            requestPermissions()
        }

        val micimage = findViewById<ImageView>(R.id.mic)
        val playbackbutton = findViewById<Button>(R.id.playback)
        pauseResumeButton = findViewById<Button>(R.id.pauseResume)

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
            val newFileName = input.text.toString() + ".3gp"  // Add the file extension
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
        }
    }


    private fun startRecording() {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "my_audio_file.3gp")
            put(MediaStore.MediaColumns.MIME_TYPE, "audio/3gpp")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MUSIC)
        }

        recordingUri = contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

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


}
