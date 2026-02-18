package com.espaciotiago.platziflixandroid.data.entities

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object for the course detail endpoint response
 */
data class CourseDetailDTO(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("thumbnail")
    val thumbnail: String,

    @SerializedName("slug")
    val slug: String,

    @SerializedName("teacher_id")
    val teacherIds: List<Int>? = null,

    @SerializedName("classes")
    val classes: List<ClassItemDTO>? = null
) {
    /**
     * Nested DTO for class items within a course
     */
    data class ClassItemDTO(
        @SerializedName("id")
        val id: Int,

        @SerializedName("name")
        val name: String,

        @SerializedName("description")
        val description: String,

        @SerializedName("slug")
        val slug: String
    )
}
