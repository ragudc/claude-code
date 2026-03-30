import Foundation

/// Mapper to convert CourseDetailDTO to CourseDetail domain model
struct CourseDetailMapper {

    /// Converts a CourseDetailDTO to a CourseDetail domain model
    static func toDomain(_ dto: CourseDetailDTO) -> CourseDetail {
        return CourseDetail(
            id: dto.id,
            name: dto.name,
            description: dto.description,
            thumbnail: dto.thumbnail,
            slug: dto.slug,
            teacherIds: dto.teacherId ?? [],
            classes: dto.classes?.map { classDTO in
                CourseDetailClass(
                    id: classDTO.id,
                    name: classDTO.name,
                    description: classDTO.description,
                    slug: classDTO.slug
                )
            } ?? [],
            averageRating: dto.averageRating,
            totalRatings: dto.totalRatings
        )
    }
}
