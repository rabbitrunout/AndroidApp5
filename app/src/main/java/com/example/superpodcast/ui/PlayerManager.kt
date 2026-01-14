package com.example.superpodcast.ui

import android.content.Context
import android.media.MediaPlayer

class PlayerManager(context: Context) {

    private var mp: MediaPlayer? = null
    var currentUrl: String? = null
        private set

    // ✅ добавь это
    var currentKey: String? = null
        private set

    private var prepared = false

    // ✅ добавили key
    fun play(url: String, key: String? = null) {
        if (url.isBlank()) return

        // если тот же url — просто start
        if (currentUrl == url && prepared) {
            currentKey = key ?: currentKey
            mp?.start()
            return
        }

        stop()

        currentUrl = url
        currentKey = key

        mp = MediaPlayer().apply {
            setDataSource(url)
            setOnPreparedListener {
                prepared = true
                start()
            }
            setOnErrorListener { _, _, _ ->
                prepared = false
                false
            }
            prepareAsync()
        }
    }

    fun pause() { mp?.let { if (it.isPlaying) it.pause() } }

    fun stop() {
        mp?.let {
            try { it.stop() } catch (_: Exception) {}
            it.reset()
            it.release()
        }
        mp = null
        prepared = false
        currentUrl = null
        currentKey = null // ✅
    }

    fun release() = stop()
    fun isPlaying(): Boolean = mp?.isPlaying == true
    fun durationMs(): Long = runCatching { mp?.duration ?: 0 }.getOrDefault(0).toLong().coerceAtLeast(0)
    fun positionMs(): Long = runCatching { mp?.currentPosition ?: 0 }.getOrDefault(0).toLong().coerceAtLeast(0)
    fun seekTo(ms: Long) { runCatching { mp?.seekTo(ms.coerceAtLeast(0).toInt()) } }
}

