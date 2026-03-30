package com.espaciotiago.platziflixandroid.data.entities

import com.google.gson.annotations.SerializedName

/**
 * DTO for the class detail response from GET /classes/{class_id}
 * Note: the backend returns the video field as "video", not "video_url"
 */
data class ClassDetailDTO(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("slug") val slug: String,
    @SerializedName("video") val video: String?,
    @SerializedName("duration") val duration: Int
)
