package com.espaciotiago.platziflixandroid.data.network

import com.espaciotiago.platziflixandroid.data.entities.ClassDetailDTO
import com.espaciotiago.platziflixandroid.data.entities.CourseDTO
import com.espaciotiago.platziflixandroid.data.entities.CourseDetailDTO
import com.espaciotiago.platziflixandroid.data.entities.RatingRequestDTO
import com.espaciotiago.platziflixandroid.data.entities.RatingResponseDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
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

    /**
     * Fetches a single class detail by its ID
     * @param classId The numeric ID of the class
     * @return Response containing the class detail
     */
    @GET("classes/{class_id}")
    suspend fun getClassById(@Path("class_id") classId: Int): Response<ClassDetailDTO>

    @POST("courses/{courseId}/ratings")
    suspend fun submitRating(
        @Path("courseId") courseId: Int,
        @Body request: RatingRequestDTO
    ): Response<RatingResponseDTO>

    @GET("courses/{courseId}/ratings/user/{userId}")
    suspend fun getUserRating(
        @Path("courseId") courseId: Int,
        @Path("userId") userId: Int
    ): Response<RatingResponseDTO>

    @PUT("courses/{courseId}/ratings/{userId}")
    suspend fun updateRating(
        @Path("courseId") courseId: Int,
        @Path("userId") userId: Int,
        @Body request: RatingRequestDTO
    ): Response<RatingResponseDTO>

    @DELETE("courses/{courseId}/ratings/{userId}")
    suspend fun deleteRating(
        @Path("courseId") courseId: Int,
        @Path("userId") userId: Int
    ): Response<Unit>
} 