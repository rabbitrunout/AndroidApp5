package com.example.superpodcast.ui

import android.content.Context
import android.media.MediaPlayer

class PlayerManager(context: Context) {

    private var mp: MediaPlayer? = null
    var currentUrl: String? = null
        private set

    private var prepared = false

    fun play(url: String) {
        if (url.isBlank()) return

        // если тот же url — просто start
        if (currentUrl == url && prepared) {
            mp?.start()
            return
        }

        stop() // освободить предыдущий

        currentUrl = url
        mp = MediaPlayer().apply {
            setDataSource(url)
            setOnPreparedListener {
                prepared = true
                start()
            }
            setOnCompletionListener {
                // можно оставить prepared=true, но isPlaying станет false
            }
            setOnErrorListener { _, _, _ ->
                prepared = false
                false
            }
            prepareAsync()
        }
    }

    fun pause() {
        mp?.let {
            if (it.isPlaying) it.pause()
        }
    }

    fun stop() {
        mp?.let {
            try {
                it.stop()
            } catch (_: Exception) { }
            it.reset()
            it.release()
        }
        mp = null
        prepared = false
        currentUrl = null
    }

    fun release() = stop()

    fun isPlaying(): Boolean = mp?.isPlaying == true

    fun durationMs(): Long {
        val d = runCatching { mp?.duration ?: 0 }.getOrDefault(0)
        return d.toLong().coerceAtLeast(0)
    }

    fun positionMs(): Long {
        val p = runCatching { mp?.currentPosition ?: 0 }.getOrDefault(0)
        return p.toLong().coerceAtLeast(0)
    }

    fun seekTo(ms: Long) {
        val safe = ms.coerceAtLeast(0).toInt()
        runCatching { mp?.seekTo(safe) }
    }
}
