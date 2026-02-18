import Foundation
import Combine

/// ViewModel responsible for managing the course detail state and business logic
@MainActor
class CourseDetailViewModel: ObservableObject {

    // MARK: - Published Properties
    @Published var courseDetail: CourseDetail? = nil
    @Published var isLoading: Bool = false
    @Published var errorMessage: String? = nil

    // MARK: - Private Properties
    private let courseRepository: CourseRepository
    private let slug: String

    // MARK: - Initialization
    init(slug: String, courseRepository: CourseRepository = RemoteCourseRepository()) {
        self.slug = slug
        self.courseRepository = courseRepository
        loadDetail()
    }

    // MARK: - Public Methods

    /// Loads the course detail from the repository
    func loadDetail() {
        Task {
            await performLoadDetail()
        }
    }

    /// Clears the error message
    func clearError() {
        errorMessage = nil
    }

    // MARK: - Private Methods

    private func performLoadDetail() async {
        isLoading = true
        errorMessage = nil

        do {
            courseDetail = try await courseRepository.getCourseDetail(slug)
        } catch {
            errorMessage = handleError(error)
        }

        isLoading = false
    }

    /// Handles and formats error messages for user display
    private func handleError(_ error: Error) -> String {
        switch error {
        case NetworkError.networkUnavailable:
            return "No hay conexión a internet. Verifica tu conexión e inténtalo de nuevo."
        case NetworkError.timeout:
            return "La solicitud tardó demasiado. Inténtalo de nuevo."
        case NetworkError.requestFailed(let statusCode):
            switch statusCode {
            case 404:
                return "No se encontró el curso."
            case 500...599:
                return "Error del servidor. Inténtalo más tarde."
            default:
                return "Error al cargar el curso (Código: \(statusCode))."
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
