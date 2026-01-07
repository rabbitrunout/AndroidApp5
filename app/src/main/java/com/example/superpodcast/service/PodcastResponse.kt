package com.example.superpodcast.service

data class PodcastResponse(
    val resultCount: Int,
    val results: List<PodcastDto>
)

data class PodcastDto(
    val collectionId: Long,
    val collectionName: String?,
    val artistName: String?,
    val artworkUrl100: String?,
    val feedUrl: String?
)
