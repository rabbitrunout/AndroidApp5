package com.example.superpodcast.ui

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy

class PlayerManager(context: Context) {

    private val appContext = context.applicationContext

    private val player: ExoPlayer = ExoPlayer.Builder(appContext)
        .setMediaSourceFactory(
            DefaultMediaSourceFactory(appContext)
                .setLoadErrorHandlingPolicy(DefaultLoadErrorHandlingPolicy())
        )
        .build()

    var currentUrl: String? = null
        private set

    var currentKey: String? = null
        private set

    // callbacks наружу
    var onEnded: (() -> Unit)? = null
    var onError: ((Throwable) -> Unit)? = null
    var onPlayingChanged: ((Boolean) -> Unit)? = null

    init {
        player.addListener(object : Player.Listener {

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                onPlayingChanged?.invoke(isPlaying)
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    onEnded?.invoke()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                onError?.invoke(error)
            }
        })
    }

    fun play(url: String, key: String? = null) {
        val safeUrl = url.trim()
        if (safeUrl.isBlank()) return

        // тот же трек — продолжить
        if (currentUrl == safeUrl) {
            player.playWhenReady = true
            player.play()
            return
        }

        currentUrl = safeUrl
        currentKey = key

        val item = MediaItem.fromUri(safeUrl)
        player.setMediaItem(item)
        player.prepare()
        player.playWhenReady = true
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun stop() {
        player.stop()
        currentUrl = null
        currentKey = null
    }

    fun release() {
        player.release()
    }

    fun isPlaying(): Boolean = player.isPlaying

    fun durationMs(): Long = player.duration.coerceAtLeast(0L)
    fun positionMs(): Long = player.currentPosition.coerceAtLeast(0L)

    fun seekTo(ms: Long) {
        player.seekTo(ms.coerceAtLeast(0L))
    }
}
