package com.espaciotiago.platziflixandroid.data.repositories

import com.espaciotiago.platziflixandroid.data.mappers.ClassDetailMapper
import com.espaciotiago.platziflixandroid.data.mappers.CourseDetailMapper
import com.espaciotiago.platziflixandroid.data.mappers.CourseMapper
import com.espaciotiago.platziflixandroid.data.network.ApiService
import com.espaciotiago.platziflixandroid.domain.models.ClassDetail
import com.espaciotiago.platziflixandroid.domain.models.Course
import com.espaciotiago.platziflixandroid.domain.models.CourseDetail
import com.espaciotiago.platziflixandroid.domain.repositories.CourseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementation of CourseRepository that fetches data from remote API
 */
class RemoteCourseRepository(
    private val apiService: ApiService
) : CourseRepository {

    /**
     * Retrieves all courses from the remote API
     * @return Result containing list of courses or error
     */
    override suspend fun getAllCourses(): Result<List<Course>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllCourses()
                if (response.isSuccessful) {
                    val courseDTOList = response.body() ?: emptyList()
                    Result.success(CourseMapper.fromDTOList(courseDTOList))
                } else {
                    Result.failure(
                        Exception("Failed to fetch courses: ${response.code()} ${response.message()}")
                    )
                }
            } catch (exception: Exception) {
                Result.failure(exception)
            }
        }
    }

    /**
     * Retrieves a single course detail by its slug from the remote API
     * @return Result containing the course detail or error
     */
    override suspend fun getCourseBySlug(slug: String): Result<CourseDetail> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCourseBySlug(slug)
                if (response.isSuccessful) {
                    val dto = response.body()
                        ?: return@withContext Result.failure(Exception("Empty response body"))
                    Result.success(CourseDetailMapper.fromDTO(dto))
                } else {
                    Result.failure(
                        Exception("Failed to fetch course: ${response.code()} ${response.message()}")
                    )
                }
            } catch (exception: Exception) {
                Result.failure(exception)
            }
        }
    }

    /**
     * Retrieves a single class detail by its ID from the remote API
     * @return Result containing the class detail or error
     */
    override suspend fun getClassById(classId: Int): Result<ClassDetail> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getClassById(classId)
                if (response.isSuccessful) {
                    val dto = response.body()
                        ?: return@withContext Result.failure(Exception("Empty response body"))
                    Result.success(ClassDetailMapper.fromDTO(dto))
                } else {
                    Result.failure(
                        Exception("Failed to fetch class: ${response.code()} ${response.message()}")
                    )
                }
            } catch (exception: Exception) {
                Result.failure(exception)
            }
        }
    }
} 