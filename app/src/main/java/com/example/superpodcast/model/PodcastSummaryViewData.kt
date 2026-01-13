package com.example.superpodcast.model

data class PodcastSummaryViewData(
    val id: Long,
    val title: String,
    val author: String,
    val imageUrl: String,
    val feedUrl: String,

    // ✅ Доп. инфо для Details
    val releaseDate: String? = null,        // ISO строка от iTunes
    val genre: String? = null,
    val country: String? = null,
    val trackCount: Int? = null,
    val collectionUrl: String? = null
)
