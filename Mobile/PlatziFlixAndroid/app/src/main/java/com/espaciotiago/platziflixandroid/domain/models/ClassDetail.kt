package com.espaciotiago.platziflixandroid.domain.models

/**
 * Domain model representing the full detail of a class, including its video URL
 */
data class ClassDetail(
    val id: Int,
    val name: String,
    val description: String,
    val slug: String,
    val videoUrl: String?,
    val duration: Int
)
