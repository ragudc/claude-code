package com.espaciotiago.platziflixandroid.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val StarYellow = Color(0xFFFFB800)
private val StarGray = Color(0xFFCCCCCC)

/**
 * Read-only star display supporting fractional (half) stars for showing average ratings.
 */
@Composable
fun RatingDisplay(
    rating: Float,
    modifier: Modifier = Modifier,
    starSize: Dp = 20.dp
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (i in 1..5) {
            val fill = (rating - (i - 1)).coerceIn(0f, 1f)
            StarWithFill(fill = fill, size = starSize)
        }
    }
}

/**
 * Interactive star rating — tap a star to select a rating from 1 to 5.
 */
@Composable
fun RatingInput(
    currentRating: Int?,
    onRatingSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    starSize: Dp = 32.dp
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 1..5) {
            Icon(
                imageVector = if ((currentRating ?: 0) >= i) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = "Calificar con $i estrella${if (i > 1) "s" else ""}",
                tint = if ((currentRating ?: 0) >= i) StarYellow else StarGray,
                modifier = Modifier
                    .size(starSize)
                    .clickable { onRatingSelected(i) }
            )
        }
    }
}

@Composable
private fun StarWithFill(fill: Float, size: Dp) {
    Box(modifier = Modifier.size(size)) {
        Icon(
            imageVector = Icons.Outlined.StarOutline,
            contentDescription = null,
            tint = StarGray,
            modifier = Modifier.size(size)
        )
        if (fill > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fill)
                    .clip(RectangleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = StarYellow,
                    modifier = Modifier.size(size)
                )
            }
        }
    }
}
