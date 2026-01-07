package com.example.superpodcast.util

/**
 * Advanced / unusual search criteria:
 * 1) Regex filter on podcast title
 * 2) Minimum word count in the title
 */
object AdvancedFilters {

    fun matchesRegex(title: String, pattern: String): Boolean {
        return try {
            Regex(pattern, RegexOption.IGNORE_CASE).containsMatchIn(title)
        } catch (e: Exception) {
            // If regex is invalid, do not filter out anything
            true
        }
    }

    fun hasMinWordCount(title: String, minWords: Int): Boolean {
        val words = title.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        return words.size >= minWords
    }

    fun excludesWords(title: String, banned: List<String>): Boolean {
        val lower = title.lowercase()
        return banned.none { lower.contains(it.lowercase()) }
    }

}
