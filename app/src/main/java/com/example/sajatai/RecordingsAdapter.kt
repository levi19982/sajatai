package com.example.sajatai

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
        // You can also set onClickListeners for the buttons here
    }

    override fun getItemCount(): Int = recordings.size
}
