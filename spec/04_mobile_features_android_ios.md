# Plan de Implementación Técnico: Features Mobile Pendientes — Android e iOS

**Versión**: 1.0
**Fecha**: 2026-03-29
**Alcance**: Android (Kotlin/Compose + MVI) + iOS (Swift/SwiftUI + MVVM)
**Features**: Búsqueda de cursos · Navegación a clase + Reproductor · Sistema de ratings

---

## Análisis de Estado Inicial

### Hallazgos Estructurales Pre-implementación

| Componente | Estado detectado |
|---|---|
| Android `Course` / `CourseDetail` domain model | Sin `averageRating` / `totalRatings` — backend ya los devuelve, se pierden en el mapper |
| Android `ClassItem` / `CourseDetailDTO` | Sin `videoUrl` — backend devuelve `"video"` (no `"video_url"`) en `GET /classes/{id}` |
| Android `CourseListUiState` | Sin `searchQuery` ni filtrado — iOS ya lo tiene implementado |
| iOS `Course` / `CourseDetail` | Sin `averageRating` / `totalRatings` |
| iOS `CourseDetailClass` | Sin `videoUrl` para navegación a clase |
| iOS Búsqueda | **Completamente implementada** en `CourseListViewModel` con debounce Combine |
| iOS `ClassDetailDTO.swift` | **Ya existía** con todos los campos incluyendo `video_url` |
| Backend ratings endpoints | **Completamente implementados** (`POST/GET/PUT/DELETE /courses/{id}/ratings`) |
| Backend `GET /classes/{class_id}` | **Implementado**, devuelve campo `"video"` (no `"video_url"`) |

### Decisión de User ID para Ratings
Sin sistema de autenticación real, se usa `ANONYMOUS_USER_ID = 1` como constante en ambas plataformas. Preparado para ser reemplazado cuando se implemente auth.

---

## Orden de Implementación

```
Feature A — Búsqueda Android (solo Presentation Layer)
  └── Sin dependencias. Riesgo mínimo. Filtrado client-side igual que iOS.

Feature C — Reproductor de Video / Navegación a Clase (Android + iOS)
  └── Extensión aditiva de DTOs y modelos. No rompe contratos existentes.
      Estrategia: lazy load vía GET /classes/{id} al navegar, sin cambiar CourseDetailDTO.
      Android: Intent.ACTION_VIEW para abrir video en navegador externo (sin ExoPlayer).
      iOS: VideoPlayer (AVKit) disponible desde iOS 14 — pendiente de implementar UI.

Feature B — Sistema de Ratings (Android + iOS)
  └── El más complejo. Modifica modelos de dominio existentes.
      Va último porque no bloquea las otras features.
```

---

## Feature A: Búsqueda de Cursos — Android

### Archivos modificados (3)

#### `presentation/courses/state/CourseListUiState.kt`
```kotlin
data class CourseListUiState(
    val isLoading: Boolean = false,
    val courses: List<Course> = emptyList(),
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val searchQuery: String = ""          // NUEVO
)

sealed class CourseListUiEvent {
    object LoadCourses : CourseListUiEvent()
    object RefreshCourses : CourseListUiEvent()
    object ClearError : CourseListUiEvent()
    data class UpdateSearchQuery(val query: String) : CourseListUiEvent()  // NUEVO
}
```

#### `presentation/courses/viewmodel/CourseListViewModel.kt`
- `private var allCourses: List<Course>` — guarda catálogo completo sin filtrar
- `private val searchQueryFlow = MutableStateFlow("")` con `debounce(300ms)`
- `loadCourses()` y `refreshCourses()` guardan en `allCourses` y aplican filtro activo
- `filterCourses()` — filtra por `name.contains(query, ignoreCase = true)`

#### `presentation/courses/screen/CourseListScreen.kt`
- `OutlinedTextField` con ícono `Search` debajo del `LargeTopAppBar`
- Mensaje contextual: `"Sin resultados para \"${query}\""` vs `"No hay cursos disponibles"`

### Arquitectura: solo Presentation Layer, sin cambios en Domain ni Data

---

## Feature C: Navegación a Clase + Reproductor de Video

### Estrategia de acceso al video
**Lazy load** — Al hacer clic en una clase, navegar a una pantalla nueva que llama a `GET /classes/{class_id}`. No se modifica `CourseDetailDTO` ni `ClassItem`. La pantalla de clase carga su propio detalle.

**Atención**: El backend devuelve `"video"` (no `"video_url"`). Los DTOs deben usar este nombre.

### Android — Archivos creados (6)

| Archivo | Contenido |
|---|---|
| `domain/models/ClassDetail.kt` | `id, name, description, slug, videoUrl: String?, duration: Int` |
| `data/entities/ClassDetailDTO.kt` | `@SerializedName("video") val video: String?` |
| `data/mappers/ClassDetailMapper.kt` | Objeto singleton. Mapea `dto.title → name`, `dto.video → videoUrl` |
| `presentation/classes/state/ClassDetailUiState.kt` | `isLoading, classDetail, error` + eventos `LoadDetail`, `Retry` |
| `presentation/classes/viewmodel/ClassDetailViewModel.kt` | MVI. `classId: Int` como parámetro constructor |
| `presentation/classes/screen/ClassDetailScreen.kt` | Muestra nombre, descripción. Si `videoUrl != null`, botón "Ver video" con `Intent.ACTION_VIEW` |

### Android — Archivos modificados (8)

| Archivo | Cambio |
|---|---|
| `domain/repositories/CourseRepository.kt` | + `suspend fun getClassById(classId: Int): Result<ClassDetail>` |
| `data/network/ApiService.kt` | + `@GET("classes/{class_id}") suspend fun getClassById(...)` |
| `data/repositories/RemoteCourseRepository.kt` | Implementa `getClassById` |
| `data/repositories/MockCourseRepository.kt` | Implementa `getClassById` con mock |
| `di/AppModule.kt` | + `fun provideClassDetailViewModel(classId: Int)` |
| `navigation/AppNavigation.kt` | + ruta `"classes/{classId}"` con `NavType.IntType` |
| `presentation/courses/detail/components/ClassListItem.kt` | + `onClick: (ClassItem) -> Unit`, + ícono `PlayArrow` |
| `presentation/courses/detail/screen/CourseDetailScreen.kt` | Propaga `onClassClick: (ClassItem) -> Unit` hasta `ClassListItem` |

### iOS — Archivos creados (4)

| Archivo | Contenido |
|---|---|
| `Domain/Models/ClassDetail.swift` | `id, name, description, slug, videoUrl: String?, duration: Int` |
| `Data/Mapper/ClassDetailMapper.swift` | `struct` estático. `dto.title → name`, `dto.videoUrl → videoUrl` |
| `Presentation/ViewModels/ClassDetailViewModel.swift` | `@MainActor ObservableObject`. Carga con `getClassDetail(classId)` |
| `Presentation/Views/ClassDetailView.swift` | Muestra nombre, descripción. Si `videoUrl != null`, `VideoPlayer` (AVKit) |

### iOS — Archivos modificados (5)

| Archivo | Cambio |
|---|---|
| `Domain/Repositories/CourseRepositoryProtocol.swift` | + `func getClassDetail(_ classId: Int) async throws -> ClassDetail` |
| `Data/Repositories/CourseAPIEndpoints.swift` | + `case getClassById(Int)` con path `/classes/{id}` |
| `Data/Repositories/RemoteCourseRepository.swift` | Implementa `getClassDetail` |
| `Presentation/Views/ClassRowView.swift` | Hace la fila clickeable con `onTapGesture` |
| `Presentation/Views/CourseDetailView.swift` | Envuelve `ClassRowView` en `NavigationLink` → `ClassDetailView` |

---

## Feature B: Sistema de Ratings

### Modelo de datos del backend (ya implementado)
```
CourseRating: id, course_id, user_id, rating (1-5), created_at, updated_at, deleted_at
- Soft delete con deleted_at
- UNIQUE constraint activo por (course_id, user_id)
- average_rating y total_ratings calculados en backend, incluidos en GET /courses/{slug}
```

### Endpoints (ya implementados en backend)
```
POST   /courses/{course_id}/ratings         body: { "user_id": int, "rating": int }
GET    /courses/{course_id}/ratings/user/{user_id}
PUT    /courses/{course_id}/ratings/{user_id}
DELETE /courses/{course_id}/ratings/{user_id}
```

### Android — Archivos creados (7)

| Archivo | Contenido |
|---|---|
| `domain/models/CourseRating.kt` | `id, courseId, userId, rating: Int` |
| `data/entities/RatingDTO.kt` | `RatingRequestDTO` + `RatingResponseDTO` |
| `data/mappers/RatingMapper.kt` | Objeto singleton |
| `domain/repositories/RatingRepository.kt` | Interface con 4 métodos |
| `data/repositories/RemoteRatingRepository.kt` | Implementación Retrofit |
| `presentation/components/StarRatingComponent.kt` | `RatingDisplay` (medias estrellas via Box+clip) + `RatingInput` (interactivo 1-5) |

### Android — Archivos modificados (10)

| Archivo | Cambio |
|---|---|
| `domain/models/Course.kt` | + `averageRating: Float? = null`, `totalRatings: Int? = null` |
| `domain/models/CourseDetail.kt` | + `averageRating: Float? = null`, `totalRatings: Int? = null` |
| `data/entities/CourseDTO.kt` | + `@SerializedName("average_rating")`, `@SerializedName("total_ratings")` |
| `data/entities/CourseDetailDTO.kt` | + mismos campos |
| `data/mappers/CourseMapper.kt` | Mapea los nuevos campos |
| `data/mappers/CourseDetailMapper.kt` | Mapea los nuevos campos |
| `data/network/ApiService.kt` | + 4 endpoints de ratings |
| `presentation/courses/detail/state/CourseDetailUiState.kt` | + `userRating`, `isSubmittingRating`, `ratingError`, eventos `SubmitRating`/`DeleteRating` |
| `presentation/courses/detail/viewmodel/CourseDetailViewModel.kt` | + `ratingRepository: RatingRepository`, lógica submit/delete/loadUserRating, `ANONYMOUS_USER_ID = 1` |
| `presentation/courses/detail/screen/CourseDetailScreen.kt` | + `RatingSection` composable con display + input + botón eliminar |
| `di/AppModule.kt` | + `ratingRepository: RatingRepository by lazy { RemoteRatingRepository(apiService) }` |

### iOS — Archivos creados (7)

| Archivo | Contenido |
|---|---|
| `Domain/Models/CourseRating.swift` | `struct CourseRating: Identifiable` |
| `Data/Entities/RatingDTO.swift` | `RatingRequestDTO: Encodable` + `RatingResponseDTO: Decodable` |
| `Data/Mapper/RatingMapper.swift` | `struct` estático |
| `Domain/Repositories/RatingRepositoryProtocol.swift` | `protocol RatingRepository` con 4 métodos |
| `Data/Repositories/RatingAPIEndpoints.swift` | `enum` con 4 casos. POST/GET/PUT/DELETE |
| `Data/Repositories/RemoteRatingRepository.swift` | Implementación con `BodyEndpoint` helper para inyectar body |
| `Presentation/Views/StarRatingView.swift` | `StarRatingDisplayView` (medias estrellas con `GeometryReader+clipped`) + `StarRatingInputView` |

### iOS — Archivos modificados (7)

| Archivo | Cambio |
|---|---|
| `Domain/Models/Course.swift` | + `averageRating: Double?`, `totalRatings: Int?` (+ actualizar mocks) |
| `Domain/Models/CourseDetail.swift` | + `averageRating: Double?`, `totalRatings: Int?` (+ actualizar mock) |
| `Data/Entities/CourseDTO.swift` | + campos en `CourseDetailDTO` con `CodingKeys` |
| `Data/Mapper/CourseDetailMapper.swift` | Mapea los nuevos campos |
| `Data/Mapper/CourseMapper.swift` | Pasa `nil` en los nuevos campos |
| `Presentation/ViewModels/CourseDetailViewModel.swift` | + `ratingRepository`, `userRating`, `isSubmittingRating`, `ratingError`, métodos submit/delete |
| `Presentation/Views/CourseDetailView.swift` | + `ratingSectionView()` con display + input interactivo + botón eliminar |

---

## Estado Final del Proyecto

| Feature | Backend | Web | Android | iOS |
|---|---|---|---|---|
| Catálogo de cursos | ✅ | ✅ | ✅ | ✅ |
| Detalle de curso + clases | ✅ | ✅ | ✅ | ✅ |
| Navegación entre pantallas | — | ✅ | ✅ | ✅ |
| Reproductor de video | ✅ | ✅ | ✅ (browser) | ⬜ |
| Sistema de ratings | ✅ | ✅ | ✅ | ✅ |
| Búsqueda de cursos | ⬜ | ⬜ | ✅ | ✅ |
| Navegación a clase (video) | — | ✅ | ✅ | ⬜ |

### Pendientes iOS
- `ClassDetailView.swift` — pantalla de clase con reproductor de video (AVKit `VideoPlayer`)
- `ClassDetailViewModel.swift` — carga `GET /classes/{id}`
- Modificar `ClassRowView.swift` + `CourseDetailView.swift` para la navegación

---

## Consideraciones Arquitecturales

### Separación de repositorios (Ratings)
`RatingRepository` separado de `CourseRepository`. Los ratings son entidad de dominio diferente con CRUD completo. Agregar 4 métodos a `CourseRepository` violaría SRP.

### Campo `"video"` vs `"video_url"`
El backend devuelve `"video"` en `GET /classes/{id}`. Los DTOs deben usar `@SerializedName("video")` (Android) y `case videoUrl = "video"` en `CodingKeys` (iOS).

### Medias estrellas en Compose
Sin `StarHalf` en Material Icons básicos. Solución: `Box` con dos `Icon` solapados, el relleno clippeado con `RectangleShape` al ancho proporcional al `fill` (0.0–1.0).

### Medias estrellas en SwiftUI
`GeometryReader` sobre `Image(systemName: "star.fill")` con `.frame(width: geo.size.width * fill).clipped()`.

### Body en endpoints iOS (Ratings)
`NetworkService.request` no acepta body directamente. Solución: `BodyEndpoint` — wrapper privado que implementa `APIEndpoint` inyectando `Data` como `body`.
