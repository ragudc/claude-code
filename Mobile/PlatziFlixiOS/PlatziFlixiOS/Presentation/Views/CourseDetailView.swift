import SwiftUI

/// View that displays the full detail of a course including its classes
struct CourseDetailView: View {
    let slug: String

    @StateObject private var viewModel: CourseDetailViewModel

    init(slug: String) {
        self.slug = slug
        _viewModel = StateObject(wrappedValue: CourseDetailViewModel(slug: slug))
    }

    var body: some View {
        ZStack {
            Color.groupedBackground
                .ignoresSafeArea()

            if viewModel.isLoading {
                loadingView
            } else if let errorMessage = viewModel.errorMessage, viewModel.courseDetail == nil {
                errorView(message: errorMessage)
            } else if let courseDetail = viewModel.courseDetail {
                courseDetailContent(courseDetail)
            }
        }
        .navigationTitle(viewModel.courseDetail?.name ?? "Detalle")
        .navigationBarTitleDisplayMode(.inline)
    }

    // MARK: - View Components

    private var loadingView: some View {
        VStack(spacing: Spacing.spacing6) {
            ProgressView()
                .scaleEffect(1.5)
                .progressViewStyle(CircularProgressViewStyle(tint: .primaryBlue))

            Text("Cargando curso...")
                .font(.bodyEmphasized)
                .foregroundColor(.secondary)
        }
        .accessibilityElement(children: .combine)
        .accessibilityLabel("Cargando curso")
    }

    private func errorView(message: String) -> some View {
        VStack(spacing: Spacing.spacing6) {
            Image(systemName: "exclamationmark.triangle")
                .font(.system(size: 48))
                .foregroundColor(.warningOrange)

            VStack(spacing: Spacing.spacing3) {
                Text("No se pudo cargar el curso")
                    .font(.title2)
                    .fontWeight(.semibold)
                    .foregroundColor(.primary)

                Text(message)
                    .font(.bodyRegular)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
            }

            Button("Reintentar") {
                viewModel.loadDetail()
            }
            .font(.buttonMedium)
            .foregroundColor(.white)
            .padding(.horizontal, Spacing.spacing6)
            .padding(.vertical, Spacing.spacing3)
            .background(Color.primaryBlue)
            .cornerRadius(Radius.radiusMedium)
        }
        .padding(Spacing.spacing6)
    }

    private func courseDetailContent(_ courseDetail: CourseDetail) -> some View {
        ScrollView {
            LazyVStack(alignment: .leading, spacing: Spacing.spacing6) {
                // Thumbnail
                AsyncImage(url: URL(string: courseDetail.thumbnail)) { image in
                    image
                        .resizable()
                        .aspectRatio(16 / 9, contentMode: .fill)
                } placeholder: {
                    RoundedRectangle(cornerRadius: 0)
                        .fill(Color(.systemGray5))
                        .aspectRatio(16 / 9, contentMode: .fit)
                        .overlay(
                            Image(systemName: "photo")
                                .font(.title)
                                .foregroundColor(.secondary)
                        )
                }
                .frame(maxWidth: .infinity)
                .clipped()
                .accessibilityLabel("Imagen del curso \(courseDetail.name)")

                // Course name and description
                VStack(alignment: .leading, spacing: Spacing.spacing3) {
                    Text(courseDetail.name)
                        .font(.title1)
                        .foregroundColor(.primary)
                        .multilineTextAlignment(.leading)

                    Text(courseDetail.description)
                        .font(.bodyRegular)
                        .foregroundColor(.secondary)
                        .multilineTextAlignment(.leading)
                }
                .padding(.horizontal, Spacing.spacing4)

                // Ratings section
                if let averageRating = courseDetail.averageRating,
                   let totalRatings = courseDetail.totalRatings,
                   totalRatings > 0 {
                    ratingSectionView(averageRating: averageRating, totalRatings: totalRatings)
                        .padding(.horizontal, Spacing.spacing4)
                } else {
                    ratingSectionView(averageRating: nil, totalRatings: nil)
                        .padding(.horizontal, Spacing.spacing4)
                }

                // Classes section
                if !courseDetail.classes.isEmpty {
                    VStack(alignment: .leading, spacing: Spacing.spacing4) {
                        Text("Clases (\(courseDetail.classes.count))")
                            .font(.title2)
                            .foregroundColor(.primary)
                            .padding(.horizontal, Spacing.spacing4)

                        LazyVStack(spacing: Spacing.spacing3) {
                            ForEach(Array(courseDetail.classes.enumerated()), id: \.element.id) { index, classItem in
                                ClassRowView(classItem: classItem, index: index + 1)
                                    .padding(.horizontal, Spacing.spacing4)
                            }
                        }
                    }
                }
            }
            .padding(.bottom, Spacing.spacing8)
        }
        .accessibilityLabel("Detalle del curso \(courseDetail.name)")
    }
}

    private func ratingSectionView(averageRating: Double?, totalRatings: Int?) -> some View {
        VStack(alignment: .leading, spacing: Spacing.spacing3) {
            Text("Calificaciones")
                .font(.title2)
                .foregroundColor(.primary)

            if let avg = averageRating, let total = totalRatings {
                StarRatingDisplayView(rating: avg, totalRatings: total)
            }

            Text(viewModel.userRating != nil ? "Tu calificación:" : "Califica este curso:")
                .font(.bodyEmphasized)
                .foregroundColor(.secondary)

            if viewModel.isSubmittingRating {
                ProgressView()
            } else {
                StarRatingInputView(
                    currentRating: viewModel.userRating,
                    onRatingSelected: { rating in viewModel.submitRating(rating) }
                )
                if viewModel.userRating != nil {
                    Button("Eliminar calificación") {
                        viewModel.deleteRating()
                    }
                    .font(.buttonSmall)
                    .foregroundColor(.errorRed)
                }
            }

            if let ratingError = viewModel.ratingError {
                Text(ratingError)
                    .font(.captionRegular)
                    .foregroundColor(.errorRed)
            }
        }
    }

// MARK: - Previews
#Preview("Normal State") {
    NavigationView {
        CourseDetailView(slug: "curso-de-react")
    }
}

#Preview("Dark Mode") {
    NavigationView {
        CourseDetailView(slug: "curso-de-react")
    }
    .preferredColorScheme(.dark)
}
