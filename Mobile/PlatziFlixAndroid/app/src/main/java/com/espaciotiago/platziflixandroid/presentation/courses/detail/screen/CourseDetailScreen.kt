package com.espaciotiago.platziflixandroid.presentation.courses.detail.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.shape.RoundedCornerShape
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.espaciotiago.platziflixandroid.domain.models.ClassItem
import com.espaciotiago.platziflixandroid.domain.models.CourseDetail
import com.espaciotiago.platziflixandroid.presentation.courses.components.ErrorMessage
import com.espaciotiago.platziflixandroid.presentation.courses.components.LoadingIndicator
import com.espaciotiago.platziflixandroid.presentation.courses.detail.components.ClassListItem
import com.espaciotiago.platziflixandroid.presentation.courses.detail.state.CourseDetailUiEvent
import com.espaciotiago.platziflixandroid.presentation.courses.detail.state.CourseDetailUiState
import com.espaciotiago.platziflixandroid.presentation.courses.detail.viewmodel.CourseDetailViewModel
import com.espaciotiago.platziflixandroid.ui.theme.CornerRadius
import com.espaciotiago.platziflixandroid.ui.theme.PlatziFlixAndroidTheme
import com.espaciotiago.platziflixandroid.ui.theme.Spacing

/**
 * Screen that displays the full detail of a course
 *
 * @param viewModel ViewModel that manages the screen state
 * @param onBack Callback to navigate back to the course list
 * @param modifier Modifier for styling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(
    viewModel: CourseDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.courseDetail?.name ?: "Detalle del curso",
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        CourseDetailContent(
            uiState = uiState,
            onRetry = { viewModel.handleEvent(CourseDetailUiEvent.Retry) },
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun CourseDetailContent(
    uiState: CourseDetailUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> {
            LoadingIndicator(modifier = modifier.fillMaxSize())
        }

        uiState.error != null && uiState.courseDetail == null -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(Spacing.medium),
                contentAlignment = Alignment.Center
            ) {
                ErrorMessage(
                    message = uiState.error,
                    onRetry = onRetry
                )
            }
        }

        uiState.courseDetail != null -> {
            CourseDetailBody(
                courseDetail = uiState.courseDetail,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun CourseDetailBody(
    courseDetail: CourseDetail,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = Spacing.large),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        // Thumbnail
        item {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(courseDetail.thumbnail)
                    .crossfade(true)
                    .build(),
                contentDescription = "Thumbnail de ${courseDetail.name}",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(bottomStart = CornerRadius.large, bottomEnd = CornerRadius.large)),
                contentScale = ContentScale.Crop
            )
        }

        // Course name and description
        item {
            Column(
                modifier = Modifier.padding(horizontal = Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                Text(
                    text = courseDetail.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = courseDetail.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Classes section header
        if (courseDetail.classes.isNotEmpty()) {
            item {
                Text(
                    text = "Clases (${courseDetail.classes.size})",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = Spacing.medium)
                )
            }

            itemsIndexed(
                items = courseDetail.classes,
                key = { _, classItem -> classItem.id }
            ) { index, classItem ->
                ClassListItem(
                    classItem = classItem,
                    index = index + 1,
                    modifier = Modifier.padding(horizontal = Spacing.medium)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CourseDetailBodyPreview() {
    PlatziFlixAndroidTheme {
        CourseDetailBody(
            courseDetail = CourseDetail(
                id = 1,
                name = "Curso de Kotlin",
                description = "Aprende Kotlin desde cero hasta convertirte en un desarrollador experto. Cubre desde conceptos básicos hasta programación avanzada con Kotlin.",
                thumbnail = "",
                slug = "curso-de-kotlin",
                classes = listOf(
                    ClassItem(1, "Introducción a Kotlin", "Conoce los fundamentos del lenguaje.", "introduccion"),
                    ClassItem(2, "Variables y Tipos", "Aprende sobre val, var y los tipos de datos.", "variables-tipos"),
                    ClassItem(3, "Funciones en Kotlin", "Crea funciones simples y de orden superior.", "funciones")
                )
            )
        )
    }
}
