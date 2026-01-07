package com.example.superpodcast.ui

import androidx.lifecycle.ViewModel
import com.example.superpodcast.model.PodcastSummaryViewData
import com.example.superpodcast.repository.ItunesRepo
import com.example.superpodcast.service.RetrofitProvider
import com.prof18.rssparser.RssParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SearchViewModel : ViewModel() {

    private val repo = ItunesRepo(RetrofitProvider.itunes)

    /**
     * Search podcasts on iTunes and apply advanced filters:
     * - regex: matches title OR author (case-insensitive)
     * - minWords: minimum words in title
     */
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
                        feedUrl = dto.feedUrl ?: ""
                    )
                }
                .filter { item ->
                    val titleWords = item.title.trim().split("\\s+".toRegex()).size
                    val matches = safeRegex.containsMatchIn(item.title) || safeRegex.containsMatchIn(item.author)
                    matches && titleWords >= minWords
                }
        }
    }

    /**
     * For playback we need a direct audio URL (mp3).
     * iTunes gives only RSS feed URL, so we parse RSS and take audio from the latest episode.
     */
    suspend fun getLatestEpisodeAudioUrl(feedUrl: String): String? {
        if (feedUrl.isBlank()) return null

        return withContext(Dispatchers.IO) {
            val parser = RssParser()
            val channel = parser.getRssChannel(feedUrl)
            channel.items.firstOrNull()?.audio
        }
    }
}
