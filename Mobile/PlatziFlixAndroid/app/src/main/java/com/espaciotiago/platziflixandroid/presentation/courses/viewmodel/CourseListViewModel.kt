package com.espaciotiago.platziflixandroid.presentation.courses.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.espaciotiago.platziflixandroid.domain.models.Course
import com.espaciotiago.platziflixandroid.domain.repositories.CourseRepository
import com.espaciotiago.platziflixandroid.presentation.courses.state.CourseListUiEvent
import com.espaciotiago.platziflixandroid.presentation.courses.state.CourseListUiState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

/**
 * ViewModel for Course List screen following MVI pattern
 */
@OptIn(FlowPreview::class)
class CourseListViewModel(
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseListUiState())
    val uiState: StateFlow<CourseListUiState> = _uiState.asStateFlow()

    private var allCourses: List<Course> = emptyList()
    private val searchQueryFlow = MutableStateFlow("")

    init {
        loadCourses()
        viewModelScope.launch {
            searchQueryFlow
                .debounce(300)
                .collect { query -> applyFilter(query) }
        }
    }

    /**
     * Handles UI events from the View
     */
    fun handleEvent(event: CourseListUiEvent) {
        when (event) {
            is CourseListUiEvent.LoadCourses -> loadCourses()
            is CourseListUiEvent.RefreshCourses -> refreshCourses()
            is CourseListUiEvent.ClearError -> clearError()
            is CourseListUiEvent.UpdateSearchQuery -> updateSearchQuery(event.query)
        }
    }

    /**
     * Loads courses from repository
     */
    private fun loadCourses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            courseRepository.getAllCourses()
                .onSuccess { courses ->
                    allCourses = courses
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        courses = filterCourses(courses, _uiState.value.searchQuery),
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Unknown error occurred"
                    )
                }
        }
    }

    /**
     * Refreshes courses list
     */
    private fun refreshCourses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRefreshing = true,
                error = null
            )

            courseRepository.getAllCourses()
                .onSuccess { courses ->
                    allCourses = courses
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        courses = filterCourses(courses, _uiState.value.searchQuery),
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        error = exception.message ?: "Unknown error occurred"
                    )
                }
        }
    }

    private fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchQueryFlow.value = query
    }

    private fun applyFilter(query: String) {
        _uiState.value = _uiState.value.copy(
            courses = filterCourses(allCourses, query)
        )
    }

    private fun filterCourses(courses: List<Course>, query: String): List<Course> {
        if (query.isBlank()) return courses
        return courses.filter { it.name.contains(query, ignoreCase = true) }
    }

    /**
     * Clears error state
     */
    private fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
} 