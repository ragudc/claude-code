package com.espaciotiago.platziflixandroid.data.network

import com.espaciotiago.platziflixandroid.data.entities.CourseDTO
import com.espaciotiago.platziflixandroid.data.entities.CourseDetailDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * API service interface for course-related endpoints
 */
interface ApiService {

    /**
     * Fetches all courses from the API
     * @return Response containing list of courses
     */
    @GET("courses")
    suspend fun getAllCourses(): Response<List<CourseDTO>>

    /**
     * Fetches a single course detail by its slug
     * @param slug The unique slug identifying the course
     * @return Response containing the course detail
     */
    @GET("courses/{slug}")
    suspend fun getCourseBySlug(@Path("slug") slug: String): Response<CourseDetailDTO>
} 