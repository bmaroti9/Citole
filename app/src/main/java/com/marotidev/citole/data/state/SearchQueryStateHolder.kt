package com.marotidev.citole.data.state

import kotlinx.coroutines.flow.MutableStateFlow

class SearchQueryStateHolder {
    val query : MutableStateFlow<String> = MutableStateFlow("")

    fun updateQuery(to: String) {
        query.value = to
    }
}