package com.espaciotiago.platziflixandroid.presentation.classes.state

import com.espaciotiago.platziflixandroid.domain.models.ClassDetail

data class ClassDetailUiState(
    val isLoading: Boolean = false,
    val classDetail: ClassDetail? = null,
    val error: String? = null
)

sealed class ClassDetailUiEvent {
    object LoadDetail : ClassDetailUiEvent()
    object Retry : ClassDetailUiEvent()
}
