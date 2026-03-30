import SwiftUI

/// Read-only star display with fractional fill support for showing average ratings
struct StarRatingDisplayView: View {
    let rating: Double
    let totalRatings: Int
    var starSize: CGFloat = 18

    var body: some View {
        HStack(spacing: 2) {
            ForEach(1...5, id: \.self) { index in
                let fill = min(max(rating - Double(index - 1), 0.0), 1.0)
                StarFillView(fill: fill, size: starSize)
            }
            Text(String(format: "%.1f", rating))
                .font(.captionRegular)
                .foregroundColor(.secondary)
            Text("(\(totalRatings))")
                .font(.captionRegular)
                .foregroundColor(.secondary)
        }
    }
}

/// Interactive star input — tap to select rating 1-5
struct StarRatingInputView: View {
    let currentRating: Int?
    let onRatingSelected: (Int) -> Void
    var starSize: CGFloat = 28

    var body: some View {
        HStack(spacing: 6) {
            ForEach(1...5, id: \.self) { index in
                Image(systemName: (currentRating ?? 0) >= index ? "star.fill" : "star")
                    .resizable()
                    .frame(width: starSize, height: starSize)
                    .foregroundColor((currentRating ?? 0) >= index ? .yellow : Color(.systemGray4))
                    .onTapGesture { onRatingSelected(index) }
            }
        }
    }
}

/// Single star with fractional horizontal fill
private struct StarFillView: View {
    let fill: Double
    let size: CGFloat

    var body: some View {
        ZStack(alignment: .leading) {
            Image(systemName: "star")
                .resizable()
                .frame(width: size, height: size)
                .foregroundColor(Color(.systemGray4))

            if fill > 0 {
                GeometryReader { geo in
                    Image(systemName: "star.fill")
                        .resizable()
                        .frame(width: size, height: size)
                        .foregroundColor(.yellow)
                        .frame(width: geo.size.width * CGFloat(fill), alignment: .leading)
                        .clipped()
                }
                .frame(width: size, height: size)
            }
        }
        .frame(width: size, height: size)
    }
}
