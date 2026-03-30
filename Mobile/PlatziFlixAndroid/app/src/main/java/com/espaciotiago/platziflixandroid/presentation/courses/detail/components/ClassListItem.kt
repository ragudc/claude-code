package com.espaciotiago.platziflixandroid.presentation.courses.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.espaciotiago.platziflixandroid.domain.models.ClassItem
import com.espaciotiago.platziflixandroid.ui.theme.CornerRadius
import com.espaciotiago.platziflixandroid.ui.theme.PlatziFlixAndroidTheme
import com.espaciotiago.platziflixandroid.ui.theme.Spacing

/**
 * List item component that displays a single class within a course
 *
 * @param classItem The class data to display
 * @param index The 1-based position of this class in the list
 * @param onClick Callback when the item is clicked
 * @param modifier Modifier for styling
 */
@Composable
fun ClassListItem(
    classItem: ClassItem,
    index: Int,
    onClick: (ClassItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onClick(classItem) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.large),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = Spacing.extraSmall
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
            verticalAlignment = Alignment.Top
        ) {
            // Class number badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = index.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Class info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
            ) {
                Text(
                    text = classItem.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = classItem.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Ver clase",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ClassListItemPreview() {
    PlatziFlixAndroidTheme {
        ClassListItem(
            classItem = ClassItem(
                id = 1,
                name = "Introducción al curso",
                description = "En esta clase aprenderás los conceptos fundamentales que necesitas para comenzar.",
                slug = "introduccion"
            ),
            index = 1,
            onClick = {}
        )
    }
}
