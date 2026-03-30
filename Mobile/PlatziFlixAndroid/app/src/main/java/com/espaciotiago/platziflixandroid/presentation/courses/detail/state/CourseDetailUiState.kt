package com.espaciotiago.platziflixandroid.presentation.courses.detail.state

import com.espaciotiago.platziflixandroid.domain.models.CourseDetail

data class CourseDetailUiState(
    val isLoading: Boolean = false,
    val courseDetail: CourseDetail? = null,
    val error: String? = null,
    val userRating: Int? = null,
    val isSubmittingRating: Boolean = false,
    val ratingError: String? = null
)

sealed class CourseDetailUiEvent {
    object LoadDetail : CourseDetailUiEvent()
    object Retry : CourseDetailUiEvent()
    data class SubmitRating(val rating: Int) : CourseDetailUiEvent()
    object DeleteRating : CourseDetailUiEvent()
}
