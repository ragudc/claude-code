import Foundation

struct CourseRating: Identifiable {
    let id: Int
    let courseId: Int
    let userId: Int
    let rating: Int
}
