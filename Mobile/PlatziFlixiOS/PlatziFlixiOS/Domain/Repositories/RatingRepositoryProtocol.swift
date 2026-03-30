import Foundation

protocol RatingRepository {
    func submitRating(courseId: Int, userId: Int, rating: Int) async throws -> CourseRating
    func getUserRating(courseId: Int, userId: Int) async throws -> CourseRating?
    func updateRating(courseId: Int, userId: Int, rating: Int) async throws -> CourseRating
    func deleteRating(courseId: Int, userId: Int) async throws
}
