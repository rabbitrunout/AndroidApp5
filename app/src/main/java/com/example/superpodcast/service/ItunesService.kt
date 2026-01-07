package com.example.superpodcast.service

import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesService {

    @GET("search")
    suspend fun searchPodcasts(
        @Query("term") term: String,
        @Query("media") media: String = "podcast",
        @Query("limit") limit: Int = 50,
        @Query("country") country: String = "CA"
    ): PodcastResponse
}
