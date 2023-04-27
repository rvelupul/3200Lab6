package com.example.radiostationapp.services

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import com.example.radiostationapp.ui.home.HomeFragment.Companion.currentTrackIndex
import com.example.radiostationapp.ui.home.HomeFragment.Companion.stationsList

class MediaService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {
    private lateinit var mediaPlayer: MediaPlayer
    var isPlaying = false

    override fun onBind(intent: Intent?): IBinder? {
        return MediaBinder()
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
        mediaPlayer.setOnPreparedListener(this)
        mediaPlayer.setOnCompletionListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    override fun onPrepared(mp: MediaPlayer?) {
        mediaPlayer.start()
        isPlaying = true
    }

    override fun onCompletion(mp: MediaPlayer?) {
        playNext()
    }

    fun play() {
        if (isPlaying) {
            mediaPlayer.pause()
            isPlaying = false

        } else {
            mediaPlayer.start()
            isPlaying = true
        }
    }

     fun playNext() {
        if (currentTrackIndex < stationsList?.size?.minus(1) ?: 0) {
            //  currentTrackIndex++
            mediaPlayer.reset()
            mediaPlayer.setDataSource(stationsList?.get(currentTrackIndex)?.url)
            mediaPlayer.prepareAsync()
        }
    }

    inner class MediaBinder : Binder() {
        fun getService(): MediaService = this@MediaService
    }
}
