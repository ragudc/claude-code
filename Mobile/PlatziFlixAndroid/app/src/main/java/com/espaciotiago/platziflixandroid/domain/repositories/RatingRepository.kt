package com.espaciotiago.platziflixandroid.domain.repositories

import com.espaciotiago.platziflixandroid.domain.models.CourseRating

interface RatingRepository {
    suspend fun submitRating(courseId: Int, userId: Int, rating: Int): Result<CourseRating>
    suspend fun getUserRating(courseId: Int, userId: Int): Result<CourseRating?>
    suspend fun updateRating(courseId: Int, userId: Int, rating: Int): Result<CourseRating>
    suspend fun deleteRating(courseId: Int, userId: Int): Result<Unit>
}
