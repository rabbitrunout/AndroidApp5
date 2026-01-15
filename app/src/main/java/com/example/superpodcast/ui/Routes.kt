package com.example.superpodcast.ui

import android.net.Uri

object Routes {
    const val DISCOVER = "discover"

    const val ARG_TERM = "term"
    const val ARG_ID = "id"

    // Query routes (term optional)
    const val SEARCH_ROUTE = "search?term={term}"
    const val DETAILS_ROUTE = "details/{id}?term={term}"
    const val EPISODES_ROUTE = "episodes/{id}"

    fun search(term: String = ""): String =
        "search?term=${Uri.encode(term)}"

    fun details(id: Long, returnTerm: String = ""): String =
        "details/$id?term=${Uri.encode(returnTerm)}"

    fun episodes(id: Long): String =
        "episodes/$id"
}

