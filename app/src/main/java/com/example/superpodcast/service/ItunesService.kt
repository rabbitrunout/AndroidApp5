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

    // ✅ "по-взрослому": получаем подкаст по id
    @GET("lookup")
    suspend fun lookupPodcast(
        @Query("id") id: Long,
        @Query("entity") entity: String = "podcast"
    ): PodcastResponse
}
