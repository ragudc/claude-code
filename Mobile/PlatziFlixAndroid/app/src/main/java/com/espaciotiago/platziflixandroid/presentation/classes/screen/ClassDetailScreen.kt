package com.espaciotiago.platziflixandroid.presentation.classes.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import com.espaciotiago.platziflixandroid.domain.models.ClassDetail
import com.espaciotiago.platziflixandroid.presentation.classes.state.ClassDetailUiEvent
import com.espaciotiago.platziflixandroid.presentation.classes.viewmodel.ClassDetailViewModel
import com.espaciotiago.platziflixandroid.presentation.courses.components.ErrorMessage
import com.espaciotiago.platziflixandroid.presentation.courses.components.LoadingIndicator
import com.espaciotiago.platziflixandroid.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassDetailScreen(
    viewModel: ClassDetailViewModel,
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
                        text = uiState.classDetail?.name ?: "Clase",
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
        when {
            uiState.isLoading -> {
                LoadingIndicator(modifier = Modifier.padding(innerPadding).fillMaxSize())
            }

            uiState.error != null && uiState.classDetail == null -> {
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .padding(Spacing.medium),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorMessage(
                        message = uiState.error!!,
                        onRetry = { viewModel.handleEvent(ClassDetailUiEvent.Retry) }
                    )
                }
            }

            uiState.classDetail != null -> {
                ClassDetailBody(
                    classDetail = uiState.classDetail!!,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun ClassDetailBody(
    classDetail: ClassDetail,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        Text(
            text = classDetail.name,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = classDetail.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!classDetail.videoUrl.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(Spacing.medium))
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(classDetail.videoUrl))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Text(
                    text = "  Ver video",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
