package com.espaciotiago.platziflixandroid.domain.repositories

import com.espaciotiago.platziflixandroid.domain.models.ClassDetail
import com.espaciotiago.platziflixandroid.domain.models.Course
import com.espaciotiago.platziflixandroid.domain.models.CourseDetail

/**
 * Repository interface for Course operations
 */
interface CourseRepository {

    /**
     * Retrieves all courses from the data source
     * @return Result containing list of courses or error
     */
    suspend fun getAllCourses(): Result<List<Course>>

    /**
     * Retrieves a single course detail by its slug
     * @param slug The unique slug identifying the course
     * @return Result containing the course detail or error
     */
    suspend fun getCourseBySlug(slug: String): Result<CourseDetail>

    /**
     * Retrieves a single class detail by its ID
     * @param classId The numeric ID of the class
     * @return Result containing the class detail or error
     */
    suspend fun getClassById(classId: Int): Result<ClassDetail>
} 