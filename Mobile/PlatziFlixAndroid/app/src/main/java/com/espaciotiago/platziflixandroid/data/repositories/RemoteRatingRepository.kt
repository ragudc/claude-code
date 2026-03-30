package com.espaciotiago.platziflixandroid.data.repositories

import com.espaciotiago.platziflixandroid.data.entities.RatingRequestDTO
import com.espaciotiago.platziflixandroid.data.mappers.RatingMapper
import com.espaciotiago.platziflixandroid.data.network.ApiService
import com.espaciotiago.platziflixandroid.domain.models.CourseRating
import com.espaciotiago.platziflixandroid.domain.repositories.RatingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoteRatingRepository(
    private val apiService: ApiService
) : RatingRepository {

    override suspend fun submitRating(courseId: Int, userId: Int, rating: Int): Result<CourseRating> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.submitRating(courseId, RatingRequestDTO(userId, rating))
                if (response.isSuccessful) {
                    val dto = response.body() ?: return@withContext Result.failure(Exception("Empty response"))
                    Result.success(RatingMapper.fromDTO(dto))
                } else {
                    Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getUserRating(courseId: Int, userId: Int): Result<CourseRating?> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUserRating(courseId, userId)
                when {
                    response.isSuccessful -> Result.success(response.body()?.let { RatingMapper.fromDTO(it) })
                    response.code() == 404 -> Result.success(null)
                    else -> Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateRating(courseId: Int, userId: Int, rating: Int): Result<CourseRating> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateRating(courseId, userId, RatingRequestDTO(userId, rating))
                if (response.isSuccessful) {
                    val dto = response.body() ?: return@withContext Result.failure(Exception("Empty response"))
                    Result.success(RatingMapper.fromDTO(dto))
                } else {
                    Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun deleteRating(courseId: Int, userId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteRating(courseId, userId)
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
