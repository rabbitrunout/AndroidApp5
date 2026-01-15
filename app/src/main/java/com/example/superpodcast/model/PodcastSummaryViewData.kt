package com.example.superpodcast.model

data class PodcastSummaryViewData(
    val id: Long,
    val title: String,
    val author: String,
    val imageUrl: String,

    // ✅ важно: может быть null / пустой / не RSS
    val feedUrl: String?,

    // Details
    val releaseDate: String? = null,
    val genre: String? = null,
    val country: String? = null,
    val trackCount: Int? = null,
    val collectionUrl: String? = null
)
