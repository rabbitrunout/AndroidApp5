package com.example.superpodcast.ui

import androidx.lifecycle.ViewModel
import com.example.superpodcast.model.EpisodeUi
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

                        // ✅ Details fields
                        releaseDate = dto.releaseDate,
                        genre = dto.primaryGenreName,
                        country = dto.country,
                        trackCount = dto.trackCount,
                        collectionUrl = dto.collectionViewUrl
                    )
                }
                .filter { item ->
                    val words = item.title.trim().split("\\s+".toRegex()).size
                    val matches = safeRegex.containsMatchIn(item.title) || safeRegex.containsMatchIn(item.author)
                    matches && words >= minWords
                }
        }
    }

    /**
     * ✅ Get latest episode audio URL from RSS/Atom
     */
    suspend fun getLatestEpisodeAudioUrl(feedUrl: String): String? {
        return withContext(Dispatchers.IO) {
            if (feedUrl.isBlank()) return@withContext null
            val xml = fetch(feedUrl) ?: return@withContext null

            // Search in whole document (usually first item has enclosure)
            firstAudioUrlFromXml(xml)
        }
    }

    /**
     * ✅ Episodes list
     */
    suspend fun getEpisodes(feedUrl: String): List<EpisodeUi> {
        return withContext(Dispatchers.IO) {
            if (feedUrl.isBlank()) return@withContext emptyList()
            val xml = fetch(feedUrl) ?: return@withContext emptyList()

            // <item> ... </item>
            val items = Regex("""<item\b.*?</item>""",
                setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
            )
                .findAll(xml)
                .map { it.value }
                .toList()

            items.mapIndexedNotNull { index, itemXml ->
                val titleRaw = findTagText(itemXml, "title")
                val title = decodeEntities(titleRaw).trim()
                if (title.isBlank()) return@mapIndexedNotNull null

                val pubDate = decodeEntities(findTagText(itemXml, "pubDate")).trim()

                // Prefer <content:encoded> if exists, else <description>
                val contentEncoded = findTagText(itemXml, "content:encoded")
                val descriptionRaw = if (contentEncoded.isNotBlank()) contentEncoded else findTagText(itemXml, "description")
                val description = decodeEntities(cleanHtml(descriptionRaw)).trim()

                val duration = decodeEntities(findTagText(itemXml, "itunes:duration")).trim()

                val audioUrl = firstAudioUrlFromXml(itemXml).orEmpty().trim()

                EpisodeUi(
                    id = "${title.hashCode()}_$index",
                    title = title,
                    pubDateText = pubDate,
                    audioUrl = audioUrl,
                    description = if (duration.isNotBlank())
                        "Duration: $duration\n$description".trim()
                    else description
                )
            }
        }
    }

    // -------------------- helpers --------------------

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

    /**
     * Finds FIRST audio url in given xml block:
     * - <enclosure url="...">
     * - <media:content url="...">
     * - <media:enclosure url="...">
     * - <link rel="enclosure" href="..."> (Atom)
     */
    private fun firstAudioUrlFromXml(xml: String): String? {
        // 1) enclosure
        findFirstUrl(xml, Regex("""<enclosure[^>]*url="([^"]+)"""", RegexOption.IGNORE_CASE))?.let { return it }

        // 2) media:content
        findFirstUrl(xml, Regex("""<media:content[^>]*url="([^"]+)"""", RegexOption.IGNORE_CASE))?.let { return it }

        // 3) media:enclosure (some feeds)
        findFirstUrl(xml, Regex("""<media:enclosure[^>]*url="([^"]+)"""", RegexOption.IGNORE_CASE))?.let { return it }

        // 4) atom link rel=enclosure
        findFirstUrl(xml, Regex("""<link[^>]*rel="enclosure"[^>]*href="([^"]+)"""", RegexOption.IGNORE_CASE))?.let { return it }

        return null
    }

    private fun findFirstUrl(xml: String, pattern: Regex): String? {
        val m = pattern.find(xml) ?: return null
        val url = m.groupValues.getOrNull(1)?.trim().orEmpty()
        if (url.isBlank()) return null

        // Keep even if no extension (some are redirects)
        val lower = url.lowercase()
        val looksAudio = lower.contains(".mp3") || lower.contains(".m4a") || lower.contains("audio") || lower.contains("redirect")
        return if (looksAudio) url else url
    }

    private fun findTagText(xml: String, tag: String): String {
        // CDATA first
        val cdata = Regex(
            """<${Regex.escape(tag)}[^>]*>\s*<!\[CDATA\[(.*?)]]>\s*</${Regex.escape(tag)}>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        ).find(xml)?.groupValues?.getOrNull(1)

        if (!cdata.isNullOrBlank()) return cdata

        // normal tag
        val normal = Regex(
            """<${Regex.escape(tag)}[^>]*>(.*?)</${Regex.escape(tag)}>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        ).find(xml)?.groupValues?.getOrNull(1)

        return normal?.trim().orEmpty()
    }

    private fun cleanHtml(text: String): String {
        return text
            .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("<p[^>]*>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("</p>", RegexOption.IGNORE_CASE), "\n")
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
            .replace("&nbsp;", " ")
    }
}
