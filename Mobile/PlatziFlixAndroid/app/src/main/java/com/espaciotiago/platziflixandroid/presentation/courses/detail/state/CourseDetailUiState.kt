package com.espaciotiago.platziflixandroid.presentation.courses.detail.state

import com.espaciotiago.platziflixandroid.domain.models.CourseDetail

/**
 * UI State for the Course Detail screen
 */
data class CourseDetailUiState(
    val isLoading: Boolean = false,
    val courseDetail: CourseDetail? = null,
    val error: String? = null
)

/**
 * UI Events for the Course Detail screen
 */
sealed class CourseDetailUiEvent {
    object LoadDetail : CourseDetailUiEvent()
    object Retry : CourseDetailUiEvent()
}
