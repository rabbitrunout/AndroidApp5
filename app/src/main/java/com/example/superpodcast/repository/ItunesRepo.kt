package com.example.superpodcast.repository

import com.example.superpodcast.service.ItunesService
import com.example.superpodcast.service.PodcastResponse

class ItunesRepo(private val service: ItunesService) {
    suspend fun searchByTerm(term: String): PodcastResponse =
        service.searchPodcasts(term)

    suspend fun lookupById(id: Long): PodcastResponse =
        service.lookupPodcast(id)
}
