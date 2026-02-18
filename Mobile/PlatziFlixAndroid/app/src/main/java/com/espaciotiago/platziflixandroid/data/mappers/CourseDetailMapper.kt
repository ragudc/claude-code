package com.espaciotiago.platziflixandroid.data.mappers

import com.espaciotiago.platziflixandroid.data.entities.CourseDetailDTO
import com.espaciotiago.platziflixandroid.domain.models.ClassItem
import com.espaciotiago.platziflixandroid.domain.models.CourseDetail

/**
 * Mapper to convert CourseDetailDTO to CourseDetail domain model
 */
object CourseDetailMapper {

    fun fromDTO(dto: CourseDetailDTO): CourseDetail {
        return CourseDetail(
            id = dto.id,
            name = dto.name,
            description = dto.description,
            thumbnail = dto.thumbnail,
            slug = dto.slug,
            classes = dto.classes?.map { classDTO ->
                ClassItem(
                    id = classDTO.id,
                    name = classDTO.name,
                    description = classDTO.description,
                    slug = classDTO.slug
                )
            } ?: emptyList()
        )
    }
}
