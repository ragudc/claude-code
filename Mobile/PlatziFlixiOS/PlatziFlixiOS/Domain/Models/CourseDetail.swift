import Foundation

/// Domain model representing the full detail of a course, including its classes
struct CourseDetail: Identifiable {
    let id: Int
    let name: String
    let description: String
    let thumbnail: String
    let slug: String
    let teacherIds: [Int]
    let classes: [CourseDetailClass]
}

/// Domain model representing a single class within a course detail
struct CourseDetailClass: Identifiable {
    let id: Int
    let name: String
    let description: String
    let slug: String
}

// MARK: - Mock Data for Preview
extension CourseDetail {
    static let mock = CourseDetail(
        id: 4,
        name: "Curso de React.js",
        description: "Aprende React.js desde cero hasta avanzado. Domina componentes, hooks, estado, contexto y las mejores prácticas para crear aplicaciones web modernas y escalables.",
        thumbnail: "https://thumbs.cdn.mdstrm.com/thumbs/512e13acaca1ebcd2f000279/thumb_6733882e4711f40de0f1325f_6733882e4711f40de0f13270_13s.jpg?w=640&q=50",
        slug: "curso-de-react",
        teacherIds: [1, 2],
        classes: [
            CourseDetailClass(id: 1, name: "Introducción a React", description: "Conoce el ecosistema de React y su filosofía de componentes.", slug: "introduccion-react"),
            CourseDetailClass(id: 2, name: "JSX y Componentes", description: "Aprende a escribir JSX y crear componentes funcionales.", slug: "jsx-componentes"),
            CourseDetailClass(id: 3, name: "Estado con useState", description: "Gestiona el estado local de tus componentes con el hook useState.", slug: "estado-usestate"),
            CourseDetailClass(id: 4, name: "Efectos con useEffect", description: "Sincroniza tu UI con efectos secundarios usando useEffect.", slug: "efectos-useeffect"),
            CourseDetailClass(id: 5, name: "Contexto Global", description: "Comparte estado entre componentes usando Context API.", slug: "contexto-global")
        ]
    )
}
