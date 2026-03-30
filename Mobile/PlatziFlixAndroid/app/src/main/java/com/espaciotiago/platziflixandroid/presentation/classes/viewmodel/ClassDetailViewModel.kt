package com.espaciotiago.platziflixandroid.presentation.classes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.espaciotiago.platziflixandroid.domain.repositories.CourseRepository
import com.espaciotiago.platziflixandroid.presentation.classes.state.ClassDetailUiEvent
import com.espaciotiago.platziflixandroid.presentation.classes.state.ClassDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClassDetailViewModel(
    private val classId: Int,
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassDetailUiState())
    val uiState: StateFlow<ClassDetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    fun handleEvent(event: ClassDetailUiEvent) {
        when (event) {
            is ClassDetailUiEvent.LoadDetail -> loadDetail()
            is ClassDetailUiEvent.Retry -> loadDetail()
        }
    }

    private fun loadDetail() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            courseRepository.getClassById(classId)
                .onSuccess { detail ->
                    _uiState.value = _uiState.value.copy(isLoading = false, classDetail = detail)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al cargar la clase"
                    )
                }
        }
    }
}
