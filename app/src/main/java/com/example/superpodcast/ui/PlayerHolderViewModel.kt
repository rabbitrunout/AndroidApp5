package com.example.superpodcast.ui

import androidx.lifecycle.ViewModel
import com.example.superpodcast.model.EpisodeUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class NowPlayingUi(
    val title: String = "",
    val author: String = "",
    val url: String? = null,        // audioUrl
    val key: String? = null,        // идентификатор подкаста (лучше feedUrl)
    val isPlaying: Boolean = false,
    val episodeId: String? = null,
    val episodeTitle: String? = null
)

class PlayerHolderViewModel : ViewModel() {

    private val _nowPlaying = MutableStateFlow(NowPlayingUi())
    val nowPlaying: StateFlow<NowPlayingUi> = _nowPlaying

    private var queue: List<EpisodeUi> = emptyList()
    private var index: Int = -1

    fun setNowPlaying(
        title: String,
        author: String,
        url: String?,
        key: String? = null,
        isPlaying: Boolean = true,
        episodeId: String? = null,
        episodeTitle: String? = null
    ) {
        _nowPlaying.update {
            it.copy(
                title = title,
                author = author,
                url = url,
                key = key,
                isPlaying = isPlaying,
                episodeId = episodeId,
                episodeTitle = episodeTitle
            )
        }
    }

    fun setPlaying(isPlaying: Boolean) {
        _nowPlaying.update { it.copy(isPlaying = isPlaying) }
    }

    fun clear() {
        queue = emptyList()
        index = -1
        _nowPlaying.value = NowPlayingUi()
    }

    fun setEpisodeQueue(episodes: List<EpisodeUi>, startIndex: Int) {
        queue = episodes
        index = startIndex.coerceIn(0, (episodes.size - 1).coerceAtLeast(0))
    }

    fun setEpisodeQueue(episodes: List<EpisodeUi>, currentEpisodeId: String?) {
        queue = episodes
        index = episodes.indexOfFirst { it.id == currentEpisodeId }
        if (index < 0) index = 0
    }

    fun playNextEpisode(player: PlayerManager) {
        // ✅ защита: если очереди нет — просто остановим play state
        if (queue.isEmpty() || index < 0) {
            setPlaying(false)
            return
        }

        val nextIndex = index + 1
        if (nextIndex !in queue.indices) {
            setPlaying(false)
            return
        }

        index = nextIndex
        val next = queue[index]
        val url = next.audioUrl.trim()

        if (url.isBlank()) {
            // пропускаем пустые
            playNextEpisode(player)
            return
        }

        // ✅ стартуем следующий эпизод
        player.play(url, key = nowPlaying.value.key)

        setNowPlaying(
            title = nowPlaying.value.title,
            author = nowPlaying.value.author,
            url = url,
            key = nowPlaying.value.key,
            isPlaying = true,
            episodeId = next.id,
            episodeTitle = next.title
        )
    }
}
