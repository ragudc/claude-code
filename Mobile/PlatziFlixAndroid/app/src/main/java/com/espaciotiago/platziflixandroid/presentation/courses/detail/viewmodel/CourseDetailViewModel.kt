package com.espaciotiago.platziflixandroid.presentation.courses.detail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.espaciotiago.platziflixandroid.domain.repositories.CourseRepository
import com.espaciotiago.platziflixandroid.presentation.courses.detail.state.CourseDetailUiEvent
import com.espaciotiago.platziflixandroid.presentation.courses.detail.state.CourseDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Course Detail screen following MVI pattern
 */
class CourseDetailViewModel(
    private val slug: String,
    private val courseRepository: CourseRepository
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
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error desconocido"
                    )
                }
        }
    }
}
