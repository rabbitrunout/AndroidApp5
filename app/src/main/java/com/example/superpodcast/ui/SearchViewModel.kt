package com.example.superpodcast.ui

import androidx.lifecycle.ViewModel
import com.example.superpodcast.model.PodcastSummaryViewData
import com.example.superpodcast.repository.ItunesRepo
import com.example.superpodcast.service.RetrofitProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class SearchViewModel : ViewModel() {

    private val repo = ItunesRepo(RetrofitProvider.itunes)
    private val http = OkHttpClient()

    suspend fun search(term: String, regex: String, minWords: Int): List<PodcastSummaryViewData> {
        return withContext(Dispatchers.IO) {
            val response = repo.searchByTerm(term)

            val safeRegex = runCatching { Regex(regex, RegexOption.IGNORE_CASE) }
                .getOrElse { Regex(".*") }

            response.results
                .mapNotNull { dto ->
                    val title = dto.collectionName ?: return@mapNotNull null
                    PodcastSummaryViewData(
                        id = dto.collectionId,
                        title = title,
                        author = dto.artistName ?: "",
                        imageUrl = dto.artworkUrl100 ?: "",
                        feedUrl = dto.feedUrl ?: "",
                        releaseDate = dto.releaseDate,
                        genre = dto.primaryGenreName,
                        country = dto.country,
                        trackCount = dto.trackCount
                    )
                }
                .filter { item ->
                    (safeRegex.containsMatchIn(item.title) || safeRegex.containsMatchIn(item.author)) &&
                            item.title.trim().split("\\s+".toRegex()).size >= minWords
                }
        }
    }

    /**
     * ✅ Tries multiple RSS patterns to find audio URL:
     *  - <enclosure url="...">
     *  - <media:content url="...">
     *  - Atom: <link rel="enclosure" href="...">
     */
    suspend fun getLatestEpisodeAudioUrl(feedUrl: String): String? {
        return withContext(Dispatchers.IO) {
            if (feedUrl.isBlank()) return@withContext null

            val xml = fetch(feedUrl) ?: return@withContext null

            // 1) enclosure (most common)
            findFirstUrl(xml, Regex("""<enclosure[^>]*url="([^"]+)"""", RegexOption.IGNORE_CASE))
                ?.let { return@withContext it }

            // 2) media:content
            findFirstUrl(xml, Regex("""<media:content[^>]*url="([^"]+)"""", RegexOption.IGNORE_CASE))
                ?.let { return@withContext it }

            // 3) atom link rel="enclosure"
            findFirstUrl(xml, Regex("""<link[^>]*rel="enclosure"[^>]*href="([^"]+)"""", RegexOption.IGNORE_CASE))
                ?.let { return@withContext it }

            // 4) sometimes enclosure without quotes variations (rare) -> skip

            null
        }
    }

    private fun fetch(url: String): String? {
        val req = Request.Builder()
            .url(url)
            .header("User-Agent", "SuperPodcast/1.0")
            .build()

        http.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return null
            return resp.body?.string()
        }
    }

    private fun findFirstUrl(xml: String, pattern: Regex): String? {
        val m = pattern.find(xml) ?: return null
        val url = m.groupValues.getOrNull(1)?.trim().orEmpty()
        if (url.isBlank()) return null

        // Some feeds give http redirects; ExoPlayer/MediaPlayer can handle it.
        // Filter only audio-like:
        val lower = url.lowercase()
        val looksAudio = lower.contains(".mp3") || lower.contains(".m4a") || lower.contains("audio")
        return if (looksAudio) url else url // оставляем даже если без расширения (часто так)
    }
}
