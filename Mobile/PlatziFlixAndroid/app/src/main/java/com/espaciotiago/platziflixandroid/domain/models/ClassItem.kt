package com.espaciotiago.platziflixandroid.domain.models

/**
 * Domain model representing a single class within a course
 */
data class ClassItem(
    val id: Int,
    val name: String,
    val description: String,
    val slug: String
)
