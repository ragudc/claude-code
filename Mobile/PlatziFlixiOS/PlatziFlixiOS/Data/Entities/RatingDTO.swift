import Foundation

struct RatingRequestDTO: Encodable {
    let userId: Int
    let rating: Int

    enum CodingKeys: String, CodingKey {
        case userId = "user_id"
        case rating
    }
}

struct RatingResponseDTO: Decodable {
    let id: Int
    let courseId: Int
    let userId: Int
    let rating: Int

    enum CodingKeys: String, CodingKey {
        case id
        case courseId = "course_id"
        case userId = "user_id"
        case rating
    }
}
