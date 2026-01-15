package com.example.superpodcast.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.superpodcast.model.EpisodeUi
import com.example.superpodcast.model.PodcastSummaryViewData
import com.example.superpodcast.repository.ItunesRepo
import com.example.superpodcast.service.RetrofitProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.util.concurrent.TimeUnit

class SearchViewModel : ViewModel() {

    companion object {
        private const val TAG = "SearchVM"
    }

    private val repo = ItunesRepo(RetrofitProvider.itunes)

    // ✅ чуть более "живучий" клиент: таймауты + редиректы
    private val http = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * @param onlyPlayable если true — оставляем только с feedUrl (как у тебя было),
     *                    если false — возвращаем всё (рекомендую для UI).
     */
    suspend fun search(
        term: String,
        regex: String,
        minWords: Int,
        onlyPlayable: Boolean = false
    ): List<PodcastSummaryViewData> {
        return withContext(Dispatchers.IO) {
            val q = term.trim().ifEmpty { "podcast" } // ✅ чтобы поиск работал даже пустым

            val response = repo.searchByTerm(q)

            val safeRegex = runCatching { Regex(regex.trim().ifEmpty { ".*" }, RegexOption.IGNORE_CASE) }
                .getOrElse { Regex(".*") }

            val raw = response.results.mapNotNull { dto ->
                val title = dto.collectionName ?: return@mapNotNull null
                val feed = dto.feedUrl?.trim()

                PodcastSummaryViewData(
                    id = dto.collectionId,
                    title = title,
                    author = dto.artistName ?: "",
                    imageUrl = dto.artworkUrl100 ?: "",
                    feedUrl = feed,
                    releaseDate = dto.releaseDate,
                    genre = dto.primaryGenreName,
                    country = dto.country,
                    trackCount = dto.trackCount,
                    collectionUrl = dto.collectionViewUrl
                )
            }

            val filtered = raw.filter { item ->
                val wordsOk = item.title.trim()
                    .split("\\s+".toRegex())
                    .filter { it.isNotBlank() }
                    .size >= (minWords.coerceAtLeast(1))

                val textOk = safeRegex.containsMatchIn(item.title) || safeRegex.containsMatchIn(item.author)

                wordsOk && textOk
            }

            val playableCount = filtered.count { !it.feedUrl.isNullOrBlank() }
            Log.d(TAG, "search q='$q' raw=${raw.size} afterFilter=${filtered.size} playable=$playableCount onlyPlayable=$onlyPlayable")

            if (onlyPlayable) filtered.filter { !it.feedUrl.isNullOrBlank() } else filtered
        }
    }

    /**
     * Берём первый playable audio url из RSS.
     */
    suspend fun getLatestEpisodeAudioUrl(feedUrl: String): String? {
        return withContext(Dispatchers.IO) {
            val url = feedUrl.trim()
            if (url.isBlank()) return@withContext null

            val req = Request.Builder()
                .url(url)
                .header("User-Agent", "SuperPodcast/1.0")
                .build()

            http.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    Log.w(TAG, "RSS request failed code=${resp.code} url=$url")
                    return@withContext null
                }
                val body = resp.body ?: return@withContext null
                body.byteStream().use { stream ->
                    parseFirstEpisodeAudioUrl(stream)
                }
            }
        }
    }

    suspend fun getEpisodes(feedUrl: String): List<EpisodeUi> {
        return withContext(Dispatchers.IO) {
            val url = feedUrl.trim()
            if (url.isBlank()) return@withContext emptyList()

            val req = Request.Builder()
                .url(url)
                .header("User-Agent", "SuperPodcast/1.0")
                .build()

            http.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) {
                    Log.w(TAG, "RSS episodes failed code=${resp.code} url=$url")
                    return@withContext emptyList()
                }
                val body = resp.body ?: return@withContext emptyList()

                body.byteStream().use { stream ->
                    parseEpisodes(stream)
                }
            }
        }
    }

    // ---------------------------
    // XML parsing helpers
    // ---------------------------

    private fun newParser(input: InputStream): XmlPullParser {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        return factory.newPullParser().apply { setInput(input, null) }
    }

    private fun parseFirstEpisodeAudioUrl(input: InputStream): String? {
        val parser = newParser(input)
        var inItem = false

        while (true) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    val name = parser.name?.lowercase().orEmpty()

                    if (name == "item") inItem = true

                    if (inItem) {
                        if (name == "enclosure") {
                            val url = parser.getAttributeValue(null, "url")?.trim().orEmpty()
                            if (url.isNotBlank()) return url
                        }
                        if (name.endsWith("content")) {
                            val url = parser.getAttributeValue(null, "url")?.trim().orEmpty()
                            if (url.isNotBlank()) return url
                        }
                        if (name == "link") {
                            val rel = parser.getAttributeValue(null, "rel")?.lowercase().orEmpty()
                            if (rel == "enclosure") {
                                val href = parser.getAttributeValue(null, "href")?.trim().orEmpty()
                                if (href.isNotBlank()) return href
                            }
                        }
                    }
                }

                XmlPullParser.END_DOCUMENT -> return null
            }
            parser.next()
        }
    }

    private fun parseEpisodes(input: InputStream): List<EpisodeUi> {
        val parser = newParser(input)
        val out = ArrayList<EpisodeUi>(64)

        var inItem = false
        var currentTitle = ""
        var currentPubDate = ""
        var currentDesc = ""
        var currentAudioUrl = ""

        fun flushEpisode() {
            if (currentTitle.isBlank()) return

            val cleanedDesc = decodeEntities(cleanHtml(currentDesc)).trim()
            val title = decodeEntities(currentTitle).trim()
            val pub = decodeEntities(currentPubDate).trim()

            val id = "${title.hashCode()}_${pub.hashCode()}_${out.size}"

            out.add(
                EpisodeUi(
                    id = id,
                    title = title,
                    pubDateText = pub,
                    audioUrl = currentAudioUrl.trim(),
                    description = cleanedDesc
                )
            )
        }

        while (true) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    val name = parser.name?.lowercase().orEmpty()

                    if (name == "item") {
                        inItem = true
                        currentTitle = ""
                        currentPubDate = ""
                        currentDesc = ""
                        currentAudioUrl = ""
                    }

                    if (inItem) {
                        when (name) {
                            "title" -> currentTitle = readText(parser)
                            "pubdate" -> currentPubDate = readText(parser)
                            "description" -> currentDesc = readText(parser)

                            "enclosure" -> {
                                val url = parser.getAttributeValue(null, "url")?.trim().orEmpty()
                                if (url.isNotBlank()) currentAudioUrl = url
                            }

                            else -> {
                                if (name.endsWith("content")) {
                                    val url = parser.getAttributeValue(null, "url")?.trim().orEmpty()
                                    if (currentAudioUrl.isBlank() && url.isNotBlank()) currentAudioUrl = url
                                }
                                if (name == "link") {
                                    val rel = parser.getAttributeValue(null, "rel")?.lowercase().orEmpty()
                                    if (rel == "enclosure") {
                                        val href = parser.getAttributeValue(null, "href")?.trim().orEmpty()
                                        if (currentAudioUrl.isBlank() && href.isNotBlank()) currentAudioUrl = href
                                    }
                                }
                            }
                        }
                    }
                }

                XmlPullParser.END_TAG -> {
                    val name = parser.name?.lowercase().orEmpty()
                    if (name == "item") {
                        flushEpisode()
                        inItem = false
                    }
                }

                XmlPullParser.END_DOCUMENT -> return out
            }
            parser.next()
        }
    }

    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text ?: ""
            parser.nextTag()
        }
        return result
    }

    private fun cleanHtml(text: String): String {
        return text
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<.*?>", RegexOption.DOT_MATCHES_ALL), "")
            .trim()
    }

    private fun decodeEntities(s: String): String {
        return s
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
    }
}
