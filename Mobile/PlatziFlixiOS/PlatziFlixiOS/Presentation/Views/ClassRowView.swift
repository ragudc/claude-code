import SwiftUI

/// Row component that displays a single class within a course
struct ClassRowView: View {
    let classItem: CourseDetailClass
    let index: Int

    var body: some View {
        HStack(alignment: .top, spacing: Spacing.spacing4) {
            // Class number badge
            ZStack {
                Circle()
                    .fill(Color.primaryBlue)
                    .frame(width: 36, height: 36)

                Text("\(index)")
                    .font(.buttonSmall)
                    .foregroundColor(.white)
            }
            .accessibilityHidden(true)

            // Class info
            VStack(alignment: .leading, spacing: Spacing.spacing2) {
                Text(classItem.name)
                    .font(.bodyEmphasized)
                    .foregroundColor(.primary)
                    .lineLimit(2)
                    .multilineTextAlignment(.leading)

                Text(classItem.description)
                    .font(.bodyRegular)
                    .foregroundColor(.secondary)
                    .lineLimit(3)
                    .multilineTextAlignment(.leading)
            }

            Spacer(minLength: 0)
        }
        .padding(Spacing.spacing4)
        .cardStyle()
        .accessibilityElement(children: .combine)
        .accessibilityLabel("Clase \(index): \(classItem.name)")
    }
}

// MARK: - Previews
#Preview {
    VStack(spacing: Spacing.spacing4) {
        ClassRowView(
            classItem: CourseDetail.mock.classes[0],
            index: 1
        )
        ClassRowView(
            classItem: CourseDetail.mock.classes[1],
            index: 2
        )
    }
    .padding()
}
