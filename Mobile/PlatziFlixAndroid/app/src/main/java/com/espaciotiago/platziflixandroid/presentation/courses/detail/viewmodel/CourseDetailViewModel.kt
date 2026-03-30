package com.espaciotiago.platziflixandroid.presentation.courses.detail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.espaciotiago.platziflixandroid.domain.repositories.CourseRepository
import com.espaciotiago.platziflixandroid.domain.repositories.RatingRepository
import com.espaciotiago.platziflixandroid.presentation.courses.detail.state.CourseDetailUiEvent
import com.espaciotiago.platziflixandroid.presentation.courses.detail.state.CourseDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CourseDetailViewModel(
    private val slug: String,
    private val courseRepository: CourseRepository,
    private val ratingRepository: RatingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseDetailUiState())
    val uiState: StateFlow<CourseDetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    fun handleEvent(event: CourseDetailUiEvent) {
        when (event) {
            is CourseDetailUiEvent.LoadDetail -> loadDetail()
            is CourseDetailUiEvent.Retry -> loadDetail()
            is CourseDetailUiEvent.SubmitRating -> submitRating(event.rating)
            is CourseDetailUiEvent.DeleteRating -> deleteRating()
        }
    }

    private fun loadDetail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            courseRepository.getCourseBySlug(slug)
                .onSuccess { courseDetail ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        courseDetail = courseDetail,
                        error = null
                    )
                    loadUserRating(courseDetail.id)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error desconocido"
                    )
                }
        }
    }

    private fun loadUserRating(courseId: Int) {
        viewModelScope.launch {
            ratingRepository.getUserRating(courseId, ANONYMOUS_USER_ID)
                .onSuccess { rating ->
                    _uiState.value = _uiState.value.copy(userRating = rating?.rating)
                }
        }
    }

    private fun submitRating(rating: Int) {
        val courseId = _uiState.value.courseDetail?.id ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmittingRating = true, ratingError = null)
            val result = if (_uiState.value.userRating != null) {
                ratingRepository.updateRating(courseId, ANONYMOUS_USER_ID, rating)
            } else {
                ratingRepository.submitRating(courseId, ANONYMOUS_USER_ID, rating)
            }
            result
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isSubmittingRating = false, userRating = rating)
                    loadDetail()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isSubmittingRating = false,
                        ratingError = e.message ?: "Error al enviar la calificación"
                    )
                }
        }
    }

    private fun deleteRating() {
        val courseId = _uiState.value.courseDetail?.id ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmittingRating = true, ratingError = null)
            ratingRepository.deleteRating(courseId, ANONYMOUS_USER_ID)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isSubmittingRating = false, userRating = null)
                    loadDetail()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isSubmittingRating = false,
                        ratingError = e.message ?: "Error al eliminar la calificación"
                    )
                }
        }
    }

    companion object {
        const val ANONYMOUS_USER_ID = 1
    }
}
