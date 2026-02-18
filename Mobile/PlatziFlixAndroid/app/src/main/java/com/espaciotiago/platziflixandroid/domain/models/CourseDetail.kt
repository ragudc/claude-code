package com.espaciotiago.platziflixandroid.domain.models

/**
 * Domain model representing the full detail of a course, including its classes
 */
data class CourseDetail(
    val id: Int,
    val name: String,
    val description: String,
    val thumbnail: String,
    val slug: String,
    val classes: List<ClassItem>
)
