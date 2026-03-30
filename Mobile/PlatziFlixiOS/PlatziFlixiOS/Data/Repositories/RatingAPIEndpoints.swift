import Foundation

enum RatingAPIEndpoints {
    case submitRating(courseId: Int)
    case getUserRating(courseId: Int, userId: Int)
    case updateRating(courseId: Int, userId: Int)
    case deleteRating(courseId: Int, userId: Int)
}

extension RatingAPIEndpoints: APIEndpoint {
    var baseURL: String { "http://localhost:8000" }

    var path: String {
        switch self {
        case .submitRating(let courseId):
            return "/courses/\(courseId)/ratings"
        case .getUserRating(let courseId, let userId):
            return "/courses/\(courseId)/ratings/user/\(userId)"
        case .updateRating(let courseId, let userId):
            return "/courses/\(courseId)/ratings/\(userId)"
        case .deleteRating(let courseId, let userId):
            return "/courses/\(courseId)/ratings/\(userId)"
        }
    }

    var method: HTTPMethod {
        switch self {
        case .submitRating: return .POST
        case .getUserRating: return .GET
        case .updateRating: return .PUT
        case .deleteRating: return .DELETE
        }
    }

    var headers: [String: String]? { nil }
    var parameters: [String: Any]? { nil }
    var body: Data? { nil }
}
