package com.example.superpodcast.ui

import androidx.lifecycle.ViewModel
import com.example.superpodcast.model.PodcastSummaryViewData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class AppStateViewModel : ViewModel() {

    // храним подкасты, которые уже были получены из поиска
    private val _podcasts = MutableStateFlow<Map<Long, PodcastSummaryViewData>>(emptyMap())
    val podcasts: StateFlow<Map<Long, PodcastSummaryViewData>> = _podcasts

    fun putPodcast(p: PodcastSummaryViewData) {
        _podcasts.update { it + (p.id to p) }
    }

    fun getPodcastById(id: Long): PodcastSummaryViewData? = _podcasts.value[id]
}
