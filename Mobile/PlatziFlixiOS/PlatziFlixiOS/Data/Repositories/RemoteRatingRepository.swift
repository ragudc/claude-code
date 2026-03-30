import Foundation

final class RemoteRatingRepository: RatingRepository {

    private let networkService: NetworkService

    init(networkService: NetworkService = NetworkManager.shared) {
        self.networkService = networkService
    }

    func submitRating(courseId: Int, userId: Int, rating: Int) async throws -> CourseRating {
        let body = try JSONEncoder().encode(RatingRequestDTO(userId: userId, rating: rating))
        let endpoint = BodyEndpoint(base: RatingAPIEndpoints.submitRating(courseId: courseId), bodyData: body)
        let dto = try await networkService.request(endpoint, responseType: RatingResponseDTO.self)
        return RatingMapper.toDomain(dto)
    }

    func getUserRating(courseId: Int, userId: Int) async throws -> CourseRating? {
        let endpoint = RatingAPIEndpoints.getUserRating(courseId: courseId, userId: userId)
        do {
            let dto = try await networkService.request(endpoint, responseType: RatingResponseDTO.self)
            return RatingMapper.toDomain(dto)
        } catch NetworkError.requestFailed(let code) where code == 404 {
            return nil
        }
    }

    func updateRating(courseId: Int, userId: Int, rating: Int) async throws -> CourseRating {
        let body = try JSONEncoder().encode(RatingRequestDTO(userId: userId, rating: rating))
        let endpoint = BodyEndpoint(base: RatingAPIEndpoints.updateRating(courseId: courseId, userId: userId), bodyData: body)
        let dto = try await networkService.request(endpoint, responseType: RatingResponseDTO.self)
        return RatingMapper.toDomain(dto)
    }

    func deleteRating(courseId: Int, userId: Int) async throws {
        let endpoint = RatingAPIEndpoints.deleteRating(courseId: courseId, userId: userId)
        _ = try await networkService.request(endpoint)
    }
}

/// Wraps any APIEndpoint to inject a body payload
private struct BodyEndpoint: APIEndpoint {
    let base: APIEndpoint
    let bodyData: Data

    var baseURL: String { base.baseURL }
    var path: String { base.path }
    var method: HTTPMethod { base.method }
    var headers: [String: String]? { base.headers }
    var parameters: [String: Any]? { base.parameters }
    var body: Data? { bodyData }
}
