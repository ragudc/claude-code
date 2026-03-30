package com.espaciotiago.platziflixandroid.data.mappers

import com.espaciotiago.platziflixandroid.data.entities.ClassDetailDTO
import com.espaciotiago.platziflixandroid.domain.models.ClassDetail

/**
 * Maps ClassDetailDTO to ClassDetail domain model
 */
object ClassDetailMapper {
    fun fromDTO(dto: ClassDetailDTO): ClassDetail {
        return ClassDetail(
            id = dto.id,
            name = dto.title,
            description = dto.description,
            slug = dto.slug,
            videoUrl = dto.video,
            duration = dto.duration
        )
    }
}
