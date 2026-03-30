import Foundation
import Combine

/// ViewModel responsible for managing the course detail state and business logic
@MainActor
class CourseDetailViewModel: ObservableObject {

    // MARK: - Published Properties
    @Published var courseDetail: CourseDetail? = nil
    @Published var isLoading: Bool = false
    @Published var errorMessage: String? = nil
    @Published var userRating: Int? = nil
    @Published var isSubmittingRating: Bool = false
    @Published var ratingError: String? = nil

    // MARK: - Private Properties
    private let courseRepository: CourseRepository
    private let ratingRepository: RatingRepository
    private let slug: String
    private let anonymousUserId = 1

    // MARK: - Initialization
    init(
        slug: String,
        courseRepository: CourseRepository = RemoteCourseRepository(),
        ratingRepository: RatingRepository = RemoteRatingRepository()
    ) {
        self.slug = slug
        self.courseRepository = courseRepository
        self.ratingRepository = ratingRepository
        loadDetail()
    }

    // MARK: - Public Methods

    func loadDetail() {
        Task { await performLoadDetail() }
    }

    func clearError() {
        errorMessage = nil
    }

    func submitRating(_ rating: Int) {
        Task { await performSubmitRating(rating) }
    }

    func deleteRating() {
        Task { await performDeleteRating() }
    }

    // MARK: - Private Methods

    private func performLoadDetail() async {
        isLoading = true
        errorMessage = nil
        do {
            courseDetail = try await courseRepository.getCourseDetail(slug)
            if let courseId = courseDetail?.id {
                await loadUserRating(courseId: courseId)
            }
        } catch {
            errorMessage = handleError(error)
        }
        isLoading = false
    }

    private func loadUserRating(courseId: Int) async {
        do {
            let rating = try await ratingRepository.getUserRating(courseId: courseId, userId: anonymousUserId)
            userRating = rating?.rating
        } catch {
            // Silently ignore — not having a rating is not an error state
        }
    }

    private func performSubmitRating(_ rating: Int) async {
        guard let courseId = courseDetail?.id else { return }
        isSubmittingRating = true
        ratingError = nil
        do {
            if userRating != nil {
                _ = try await ratingRepository.updateRating(courseId: courseId, userId: anonymousUserId, rating: rating)
            } else {
                _ = try await ratingRepository.submitRating(courseId: courseId, userId: anonymousUserId, rating: rating)
            }
            userRating = rating
            await performLoadDetail()
        } catch {
            ratingError = "Error al enviar calificación. Inténtalo de nuevo."
        }
        isSubmittingRating = false
    }

    private func performDeleteRating() async {
        guard let courseId = courseDetail?.id else { return }
        isSubmittingRating = true
        ratingError = nil
        do {
            try await ratingRepository.deleteRating(courseId: courseId, userId: anonymousUserId)
            userRating = nil
            await performLoadDetail()
        } catch {
            ratingError = "Error al eliminar calificación. Inténtalo de nuevo."
        }
        isSubmittingRating = false
    }

    private func handleError(_ error: Error) -> String {
        switch error {
        case NetworkError.networkUnavailable:
            return "No hay conexión a internet. Verifica tu conexión e inténtalo de nuevo."
        case NetworkError.timeout:
            return "La solicitud tardó demasiado. Inténtalo de nuevo."
        case NetworkError.requestFailed(let statusCode):
            switch statusCode {
            case 404: return "No se encontró el curso."
            case 500...599: return "Error del servidor. Inténtalo más tarde."
            default: return "Error al cargar el curso (Código: \(statusCode))."
            }
        case NetworkError.decodingError:
            return "Error al procesar los datos del servidor."
        case NetworkError.invalidURL:
            return "Error de configuración de la aplicación."
        default:
            return "Error inesperado. Inténtalo de nuevo."
        }
    }
}
