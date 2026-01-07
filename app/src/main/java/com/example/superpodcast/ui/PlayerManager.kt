package com.example.superpodcast.ui

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class PlayerManager(context: Context) {
    private val player = ExoPlayer.Builder(context).build()

    var currentUrl: String? = null
        private set

    fun play(url: String) {
        if (currentUrl != url) {
            currentUrl = url
            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()
        }
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun stop() {
        player.stop()
        currentUrl = null
    }

    fun isPlaying(): Boolean = player.isPlaying

    fun release() {
        player.release()
    }
}
