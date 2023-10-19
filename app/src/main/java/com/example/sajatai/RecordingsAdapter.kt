package com.example.sajatai

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecordingsAdapter(private val recordings: List<Recording>) : RecyclerView.Adapter<RecordingsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recordingName: TextView = itemView.findViewById(R.id.textView_recordingName)
        val playButton: ImageButton = itemView.findViewById(R.id.button_play)
        val settingsButton: ImageButton = itemView.findViewById(R.id.button_settings)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recording, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val recording = recordings[position]
        holder.recordingName.text = recording.name
        holder.playButton.setOnClickListener {
            playAudio(recording.fileUrl)
        }
    }

    private fun playAudio(url: String) {
        val mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(url)
            setOnPreparedListener { start() }
            prepareAsync()
        }
    }




    override fun getItemCount(): Int = recordings.size
}
