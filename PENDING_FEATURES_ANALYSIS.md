# Análisis Técnico: Features Pendientes — Platziflix Mobile

**Fecha**: 2026-03-29
**Alcance**: Android (Kotlin/Compose + MVI) + iOS (Swift/SwiftUI + MVVM)
**Features analizadas**: Búsqueda de cursos · Sistema de ratings · Reproductor de video / Navegación a clase

---

## Resumen Ejecutivo

El análisis del código existente revela tres hallazgos estructurales que impactan el orden de implementación:

1. **La búsqueda ya existe en iOS** (`CourseListViewModel.searchText` + `filteredCourses`). Solo falta portarla a Android y al Backend.
2. **Los modelos de dominio en Android carecen de campos de rating** (`averageRating`, `totalRatings`) tanto en `Course` como en `CourseDetail`. iOS también carece de estos campos.
3. **El `video_url` ya está mapeado en iOS** (`Class.swift` tiene `videoUrl: String?`) pero el `ClassItem` de Android no tiene este campo, y el `ClassDTO` anidado en `CourseDetailDTO` tampoco lo incluye. El endpoint `GET /classes/{class_id}` ya existe en el backend.

Estos hallazgos definen el orden de implementación.

---

## Estado Real del Código (post-análisis)

| Componente | Estado real |
|---|---|
| Android `Course` domain model | No tiene `averageRating` / `totalRatings` |
| Android `CourseDetail` domain model | No tiene `averageRating` / `totalRatings` |
| Android `CourseDetailDTO` / `ClassItemDTO` | No tiene `video_url` |
| Android `ClassItem` domain model | No tiene `videoUrl` |
| Android `CourseListUiState` | No tiene `searchQuery` / `filteredCourses` |
| iOS `Course` domain model | No tiene `averageRating` / `totalRatings` |
| iOS `CourseDetail` domain model | No tiene `averageRating` / `totalRatings` |
| iOS `CourseDetailClass` domain model | No tiene `videoUrl` |
| iOS `ClassDTO` en `CourseDTO.swift` | No tiene `video_url` |
| iOS búsqueda | Completamente implementada en `CourseListViewModel` |
| Backend endpoint `GET /classes/{id}` | Implementado, devuelve `video_url` |
| Backend endpoints de ratings | Completamente implementados |

---

## Dependencias entre Features

```
Feature A: Búsqueda (Android)
  └── Solo afecta Presentation Layer. Sin dependencias hacia otras features.
      Puede implementarse de forma completamente independiente.

Feature B: Ratings (Android + iOS)
  ├── Requiere extender modelos de dominio: Course + CourseDetail
  ├── Requiere nuevos DTOs de request/response para ratings
  ├── En Android: afecta CourseDTO, Course, CourseDetailDTO, CourseDetail
  └── En iOS: afecta CourseDetailDTO, CourseDetail, CourseDetailClass

Feature C: Reproductor de Video / Navegación a Clase (Android + iOS)
  ├── Requiere extender ClassItem (Android) y CourseDetailClass (iOS) con videoUrl
  ├── Requiere nuevo endpoint en la capa de datos: GET /classes/{id}
  ├── Android: nuevo ClassDetailDTO, nueva ruta en AppNavigation, nueva screen
  └── iOS: nuevo ClassDetailDTO a usar o reutilizar ClassDetailDTO ya existente
      NOTA: iOS ya tiene ClassDetailDTO.swift con video_url — solo falta la UI
```

**Orden recomendado**: A (Búsqueda Android) → C (Reproductor) → B (Ratings)

**Justificación**:
- A es el cambio más pequeño y aislado: solo toca Presentation. No rompe nada existente.
- C requiere extender DTOs y modelos, pero es una extensión aditiva (agregar campos). Habilita la navegación a clase que el usuario espera antes de ver ratings.
- B es el más complejo: requiere nuevos endpoints de API, nuevos DTOs, nuevo repositorio, nuevo ViewModel, nuevo UI component reutilizable (StarRating). Va al final porque no bloquea las otras dos features.

---

## Feature A: Búsqueda de Cursos (Android)

### Estado actual
iOS tiene búsqueda completa con debounce (Combine). Android no tiene ningún mecanismo de búsqueda: `CourseListUiState` solo tiene `courses`, `isLoading`, `error`, `isRefreshing`. El backend no tiene endpoint de búsqueda; la búsqueda debe ser client-side (filtrado local), igual que iOS.

### Impacto arquitectural

**Capa Presentation — Android (únicos cambios necesarios)**

No se toca Domain, Data ni Backend. La búsqueda es client-side sobre la lista ya cargada.

### Archivos a modificar

**`CourseListUiState.kt`**
- Agregar campo `searchQuery: String = ""`
- Agregar evento `UpdateSearchQuery(query: String)` en el sealed class `CourseListUiEvent`
- Agregar campo calculado no es posible en data class con StateFlow; la lógica de filtrado va en el ViewModel

**`CourseListViewModel.kt`**
- Manejar el nuevo evento `UpdateSearchQuery` en `handleEvent()`
- Agregar propiedad derivada `filteredCourses` como StateFlow o calcularla en el estado
- Estrategia recomendada: guardar `allCourses` privadamente, exponer `filteredCourses` en `uiState.courses`
- Agregar debounce con `kotlinx.coroutines.flow.debounce` sobre el query (300ms, igual que iOS)

**`CourseListScreen.kt`**
- Agregar `SearchBar` o `TextField` de Material 3 (`DockedSearchBar` es el componente recomendado en M3)
- Conectar el campo de texto al evento `UpdateSearchQuery`
- Agregar estado local `var searchBarActive by remember { mutableStateOf(false) }`

### Archivos a crear: ninguno

### Riesgos
- El `DockedSearchBar` de Material 3 es experimental (`@ExperimentalMaterial3Api`). Alternativa: usar `TextField` simple en el `TopAppBar`.
- La lógica de filtrado debe aplicarse sobre `allCourses` (la lista completa), no sobre `uiState.courses` filtrada. Si se guarda solo la lista filtrada en el estado y el usuario borra el query, se necesita re-fetch. Solución: mantener dos listas en el ViewModel o en el estado.

### Rutas exactas

```
.../presentation/courses/state/CourseListUiState.kt          MODIFICAR
.../presentation/courses/viewmodel/CourseListViewModel.kt    MODIFICAR
.../presentation/courses/screen/CourseListScreen.kt          MODIFICAR
```

---

## Feature C: Reproductor de Video / Navegación a Clase

### Estado actual

**Backend**: El endpoint `GET /classes/{class_id}` ya existe en `main.py`. Devuelve `{ id, title, description, slug, video, duration }`. Nótese que el campo es `"video"`, no `"video_url"`.

**Android**: `ClassItemDTO` (anidado en `CourseDetailDTO`) solo tiene `id`, `name`, `description`, `slug`. El modelo de dominio `ClassItem` tampoco tiene `videoUrl`. No hay pantalla de clase ni ruta de navegación.

**iOS**: `CourseDetailClass` (en `CourseDetail.swift`) solo tiene `id`, `name`, `description`, `slug` — sin `videoUrl`. Sin embargo, `ClassDetailDTO.swift` ya existe con todos los campos incluyendo `video_url`. El modelo de dominio `Class.swift` también tiene `videoUrl: String?`. El problema es que `CourseDetailClass` (usado en la lista de la pantalla de detalle) no tiene `videoUrl`, y no hay pantalla de clase en la UI.

### Estrategia de implementación

Hay dos enfoques para acceder al video:

**Opción 1 — Lazy load**: Al hacer clic en una clase, navegar a una pantalla de clase que llama a `GET /classes/{class_id}` para obtener el `video_url` en ese momento.

**Opción 2 — Eager load**: Incluir `video_url` en el `ClassDTO` del detalle del curso, evitando un HTTP call extra.

**Recomendación**: Opción 1 (Lazy load). El endpoint ya existe y es la forma más limpia: la pantalla de lista de clases solo necesita el slug para navegar, y la pantalla de clase carga su propio detalle. Esto mantiene el payload del detalle del curso pequeño y es consistente con cómo funciona la navegación en el Frontend web (`/classes/[class_id]`).

### Impacto arquitectural — Android

**Domain Layer**
- `ClassItem.kt`: sin cambios (no necesita `videoUrl` para navegar)
- Crear nuevo modelo `ClassDetail.kt` con campos: `id`, `courseId`, `name`, `description`, `slug`, `videoUrl`, `duration`

**Data Layer**
- Crear `ClassDetailDTO.kt` en `data/entities/` con campos: `id`, `course_id`, `name`, `description`, `slug`, `video`, `duration` (el backend devuelve `"video"`, no `"video_url"`)
- Crear `ClassDetailMapper.kt` en `data/mappers/` (objeto singleton, patrón establecido)
- Modificar `ApiService.kt`: agregar `@GET("classes/{class_id}") suspend fun getClassById(@Path("class_id") classId: Int): Response<ClassDetailDTO>`
- Modificar `CourseRepository.kt` (interface): agregar `suspend fun getClassById(classId: Int): Result<ClassDetail>`
- Modificar `RemoteCourseRepository.kt`: implementar `getClassById()`
- Modificar `MockCourseRepository.kt`: implementar `getClassById()` con datos mock

**Presentation Layer**
- Crear `ClassDetailUiState.kt` en `presentation/classes/state/`
- Crear `ClassDetailViewModel.kt` en `presentation/classes/viewmodel/`
- Crear `ClassDetailScreen.kt` en `presentation/classes/screen/`
- Modificar `AppNavigation.kt`: agregar ruta `"classes/{classId}"` con argumento `Int`
- Modificar `ClassListItem.kt`: hacer el item clickeable, pasar `onClassClick: (ClassItem) -> Unit`
- Modificar `CourseDetailScreen.kt`: recibir `onClassClick: (ClassItem) -> Unit` y pasarlo hacia `ClassListItem`
- Modificar `AppModule.kt`: agregar `fun provideClassDetailViewModel(classId: Int): ClassDetailViewModel`

**Nota sobre el reproductor de video**: Android no tiene `ExoPlayer` en el `build.gradle` actual. Para reproducir video se necesita agregar la dependencia `androidx.media3:media3-exoplayer` o redirigir al navegador. Estrategia recomendada: abrir el video en el navegador externo con `Intent(Intent.ACTION_VIEW)` en la primera iteración (sin dependencias nuevas), y en una segunda iteración integrar ExoPlayer.

### Impacto arquitectural — iOS

**Domain Layer**
- `CourseDetailClass.swift` (en `CourseDetail.swift`): sin cambios para la navegación
- El modelo `Class.swift` ya tiene `videoUrl`. Se puede usar directamente o crear un `ClassDetail` separado.
- Crear `ClassDetail.swift` en `Domain/Models/` (o reutilizar `Class.swift` ya existente)

**Data Layer**
- `ClassDetailDTO.swift` ya existe en `Data/Entities/` con todos los campos necesarios
- Crear `ClassDetailMapper.swift` en `Data/Mapper/` (patrón struct estático establecido)
- `CourseAPIEndpoints.swift`: agregar `case getClassById(Int)` con path `/classes/{id}`
- `CourseRepositoryProtocol.swift`: agregar `func getClassDetail(_ classId: Int) async throws -> ClassDetail`
- `RemoteCourseRepository.swift`: implementar `getClassDetail()`

**Presentation Layer**
- Crear `ClassDetailViewModel.swift` en `Presentation/ViewModels/`
- Crear `ClassDetailView.swift` en `Presentation/Views/`
- Modificar `ClassRowView.swift`: hacer la fila clickeable con `onTapGesture`
- Modificar `CourseDetailView.swift`: envolver `ClassRowView` en `NavigationLink` hacia `ClassDetailView`
  - Patrón ya establecido en `CourseListView`: usar `NavigationLink` programático con `@State var selectedClassId`

### Archivos a crear — Android

```
.../domain/models/ClassDetail.kt                                   CREAR
.../data/entities/ClassDetailDTO.kt                                CREAR
.../data/mappers/ClassDetailMapper.kt                              CREAR
.../presentation/classes/state/ClassDetailUiState.kt               CREAR
.../presentation/classes/viewmodel/ClassDetailViewModel.kt         CREAR
.../presentation/classes/screen/ClassDetailScreen.kt               CREAR
```

### Archivos a modificar — Android

```
.../domain/repositories/CourseRepository.kt                        MODIFICAR
.../data/network/ApiService.kt                                     MODIFICAR
.../data/repositories/RemoteCourseRepository.kt                    MODIFICAR
.../data/repositories/MockCourseRepository.kt                      MODIFICAR
.../di/AppModule.kt                                                MODIFICAR
.../navigation/AppNavigation.kt                                    MODIFICAR
.../presentation/courses/detail/components/ClassListItem.kt        MODIFICAR
.../presentation/courses/detail/screen/CourseDetailScreen.kt       MODIFICAR
```

### Archivos a crear — iOS

```
PlatziFlixiOS/Domain/Models/ClassDetail.swift                      CREAR (o reutilizar Class.swift)
PlatziFlixiOS/Data/Mapper/ClassDetailMapper.swift                  CREAR
PlatziFlixiOS/Presentation/ViewModels/ClassDetailViewModel.swift   CREAR
PlatziFlixiOS/Presentation/Views/ClassDetailView.swift             CREAR
```

### Archivos a modificar — iOS

```
PlatziFlixiOS/Domain/Repositories/CourseRepositoryProtocol.swift   MODIFICAR
PlatziFlixiOS/Data/Repositories/CourseAPIEndpoints.swift           MODIFICAR
PlatziFlixiOS/Data/Repositories/RemoteCourseRepository.swift       MODIFICAR
PlatziFlixiOS/Presentation/Views/ClassRowView.swift                MODIFICAR
PlatziFlixiOS/Presentation/Views/CourseDetailView.swift            MODIFICAR
```

### Contrato del nuevo endpoint (ya implementado en backend)

```
GET /classes/{class_id}
Response 200:
{
  "id": 1,
  "title": "Introducción a React",
  "description": "...",
  "slug": "introduccion-a-react",
  "video": "https://...",
  "duration": 0
}
Response 404: { "detail": "Class not found" }
```

**Atención**: El campo es `"video"` en el backend (no `"video_url"`). Los DTOs deben usar este nombre.

---

## Feature B: Sistema de Ratings (Android + iOS)

### Estado actual

Los endpoints del backend están completos:
- `POST /courses/{course_id}/ratings` — crear/upsert rating
- `GET /courses/{course_id}/ratings/stats` — estadísticas agregadas
- `GET /courses/{course_id}/ratings/user/{user_id}` — rating del usuario
- `PUT /courses/{course_id}/ratings/{user_id}` — actualizar rating
- `DELETE /courses/{course_id}/ratings/{user_id}` — eliminar rating (soft delete)

Los campos `average_rating` y `total_ratings` ya están incluidos en el response de `GET /courses/{slug}` (visible en `CourseDetailDTO` del Frontend: `Course` interface tiene `average_rating?: number`).

**Android**: `CourseDetailDTO` no tiene `average_rating` ni `total_ratings`. `CourseDetail` domain model tampoco. `Course` domain model tampoco.

**iOS**: `CourseDetailDTO` en `CourseDTO.swift` no tiene `average_rating` ni `total_ratings`. `CourseDetail.swift` tampoco. `Course.swift` tampoco.

### Decisión arquitectural crítica

El sistema de ratings requiere una decisión sobre gestión de identidad del usuario. El backend requiere un `user_id` en cada request de rating. En la implementación web actual se usa un `user_id` hardcodeado o simulado. Para mobile, la estrategia recomendada para esta primera iteración es usar un `user_id` fijo simulado (ej. `1`) almacenado como constante, sin sistema de autenticación completo. Esto permite implementar la feature completa y sustituir el `user_id` cuando se agregue autenticación real.

### Impacto arquitectural — Android

**Domain Layer**

Modificar `Course.kt`:
```
averageRating: Float? = null
totalRatings: Int? = null
```

Modificar `CourseDetail.kt`:
```
averageRating: Float? = null
totalRatings: Int? = null
```

Crear `CourseRating.kt` (nuevo modelo de dominio para operaciones de rating):
```
id: Int, courseId: Int, userId: Int, rating: Int, createdAt: String, updatedAt: String
```

Crear `RatingStats.kt`:
```
averageRating: Float, totalRatings: Int, ratingDistribution: Map<Int, Int>
```

**Data Layer**

Modificar `CourseDetailDTO.kt`: agregar campos `average_rating: Float?` y `total_ratings: Int?`

Modificar `CourseDTO.kt`: agregar `average_rating: Float?` y `total_ratings: Int?`

Modificar `CourseDetailMapper.kt`: mapear los nuevos campos

Modificar `CourseMapper.kt`: mapear los nuevos campos

Crear `RatingDTO.kt` en `data/entities/`:
- `RatingRequestDTO` (body del POST/PUT): `user_id: Int`, `rating: Int`
- `RatingResponseDTO`: `id`, `course_id`, `user_id`, `rating`, `created_at`, `updated_at`
- `RatingStatsDTO`: `average_rating`, `total_ratings`, `rating_distribution`

Crear `RatingMapper.kt` en `data/mappers/`

Modificar `ApiService.kt`: agregar los endpoints de ratings:
```kotlin
@POST("courses/{courseId}/ratings")
suspend fun addRating(@Path("courseId") courseId: Int, @Body request: RatingRequestDTO): Response<RatingResponseDTO>

@GET("courses/{courseId}/ratings/stats")
suspend fun getRatingStats(@Path("courseId") courseId: Int): Response<RatingStatsDTO>

@GET("courses/{courseId}/ratings/user/{userId}")
suspend fun getUserRating(@Path("courseId") courseId: Int, @Path("userId") userId: Int): Response<RatingResponseDTO>

@PUT("courses/{courseId}/ratings/{userId}")
suspend fun updateRating(@Path("courseId") courseId: Int, @Path("userId") userId: Int, @Body request: RatingRequestDTO): Response<RatingResponseDTO>

@DELETE("courses/{courseId}/ratings/{userId}")
suspend fun deleteRating(@Path("courseId") courseId: Int, @Path("userId") userId: Int): Response<Unit>
```

Modificar `CourseRepository.kt` interface: agregar métodos de rating

Crear `RatingRepository.kt` interface en `domain/repositories/` con:
```kotlin
suspend fun addRating(courseId: Int, userId: Int, rating: Int): Result<CourseRating>
suspend fun getRatingStats(courseId: Int): Result<RatingStats>
suspend fun getUserRating(courseId: Int, userId: Int): Result<CourseRating?>
suspend fun updateRating(courseId: Int, userId: Int, rating: Int): Result<CourseRating>
suspend fun deleteRating(courseId: Int, userId: Int): Result<Unit>
```

Crear `RemoteRatingRepository.kt` en `data/repositories/`

**Presentation Layer**

Crear `StarRatingComponent.kt` en `presentation/components/` — componente reutilizable (similar al `StarRating` del Frontend). Debe soportar:
- Solo lectura (display del rating promedio)
- Interactivo (selección de rating 1-5 con tap)
- Medias estrellas para el display del promedio

Modificar `CourseDetailUiState.kt`:
- Agregar `ratingStats: RatingStats? = null`
- Agregar `userRating: Int? = null`
- Agregar `isSubmittingRating: Boolean = false`
- Agregar `ratingError: String? = null`
- Agregar eventos: `SubmitRating(rating: Int)`, `DeleteRating`, `LoadRatings`

Modificar `CourseDetailViewModel.kt`: manejar los nuevos eventos de rating

Modificar `CourseDetailScreen.kt`: agregar sección de rating con el `StarRatingComponent`

Modificar `AppModule.kt`: agregar `ratingRepository` como dependencia lazy

### Impacto arquitectural — iOS

**Domain Layer**

Modificar `Course.swift`:
```swift
let averageRating: Double?
let totalRatings: Int?
```

Modificar `CourseDetail.swift`:
```swift
let averageRating: Double?
let totalRatings: Int?
```

Crear `CourseRating.swift` en `Domain/Models/`:
```swift
struct CourseRating: Identifiable { id, courseId, userId, rating, createdAt, updatedAt }
```

Crear `RatingStats.swift` en `Domain/Models/`:
```swift
struct RatingStats { averageRating: Double, totalRatings: Int, ratingDistribution: [Int: Int] }
```

**Data Layer**

Modificar `CourseDTO.swift`: agregar `averageRating` y `totalRatings` en `CourseDetailDTO`

Modificar `CourseDetailMapper.swift`: mapear los nuevos campos

Crear `RatingDTO.swift` en `Data/Entities/`:
- `RatingRequestDTO: Encodable` (para POST/PUT body)
- `RatingResponseDTO: Codable`
- `RatingStatsDTO: Codable`

Crear `RatingMapper.swift` en `Data/Mapper/`

Crear `RatingAPIEndpoints.swift` en `Data/Repositories/` (o agregar casos a `CourseAPIEndpoints`):
- `.addRating(courseId: Int)` — POST
- `.getRatingStats(courseId: Int)` — GET
- `.getUserRating(courseId: Int, userId: Int)` — GET
- `.updateRating(courseId: Int, userId: Int)` — PUT
- `.deleteRating(courseId: Int, userId: Int)` — DELETE

Crear `RatingRepositoryProtocol.swift` en `Domain/Repositories/`

Crear `RemoteRatingRepository.swift` en `Data/Repositories/`

**Presentation Layer**

Crear `StarRatingView.swift` en `Presentation/Views/` — componente reutilizable:
- Soportar display read-only con medias estrellas (usando `Image(systemName: "star.leadinghalf.filled")`)
- Soportar modo interactivo con tap/drag para seleccionar rating

Modificar `CourseDetailViewModel.swift`:
- Agregar `@Published var ratingStats: RatingStats? = nil`
- Agregar `@Published var userRating: Int? = nil`
- Agregar `@Published var isSubmittingRating: Bool = false`
- Agregar `func submitRating(_ rating: Int)` y `func deleteRating()`

Modificar `CourseDetailView.swift`: agregar sección de rating con `StarRatingView`

### Archivos a crear — Android

```
.../domain/models/CourseRating.kt                                  CREAR
.../domain/models/RatingStats.kt                                   CREAR
.../domain/repositories/RatingRepository.kt                        CREAR
.../data/entities/RatingDTO.kt                                     CREAR
.../data/mappers/RatingMapper.kt                                   CREAR
.../data/repositories/RemoteRatingRepository.kt                    CREAR
.../presentation/components/StarRatingComponent.kt                 CREAR
```

### Archivos a modificar — Android

```
.../domain/models/Course.kt                                        MODIFICAR
.../domain/models/CourseDetail.kt                                  MODIFICAR
.../data/entities/CourseDTO.kt                                     MODIFICAR
.../data/entities/CourseDetailDTO.kt                               MODIFICAR
.../data/mappers/CourseMapper.kt                                   MODIFICAR
.../data/mappers/CourseDetailMapper.kt                             MODIFICAR
.../data/network/ApiService.kt                                     MODIFICAR
.../data/repositories/RemoteCourseRepository.kt                    MODIFICAR (opcional: mover ratings a RemoteRatingRepository)
.../di/AppModule.kt                                                MODIFICAR
.../presentation/courses/detail/state/CourseDetailUiState.kt       MODIFICAR
.../presentation/courses/detail/viewmodel/CourseDetailViewModel.kt MODIFICAR
.../presentation/courses/detail/screen/CourseDetailScreen.kt       MODIFICAR
```

### Archivos a crear — iOS

```
PlatziFlixiOS/Domain/Models/CourseRating.swift                     CREAR
PlatziFlixiOS/Domain/Models/RatingStats.swift                      CREAR
PlatziFlixiOS/Domain/Repositories/RatingRepositoryProtocol.swift   CREAR
PlatziFlixiOS/Data/Entities/RatingDTO.swift                        CREAR
PlatziFlixiOS/Data/Mapper/RatingMapper.swift                       CREAR
PlatziFlixiOS/Data/Repositories/RatingAPIEndpoints.swift           CREAR
PlatziFlixiOS/Data/Repositories/RemoteRatingRepository.swift       CREAR
PlatziFlixiOS/Presentation/Views/StarRatingView.swift              CREAR
```

### Archivos a modificar — iOS

```
PlatziFlixiOS/Domain/Models/Course.swift                           MODIFICAR
PlatziFlixiOS/Domain/Models/CourseDetail.swift                     MODIFICAR
PlatziFlixiOS/Data/Entities/CourseDTO.swift                        MODIFICAR
PlatziFlixiOS/Data/Mapper/CourseDetailMapper.swift                 MODIFICAR
PlatziFlixiOS/Presentation/ViewModels/CourseDetailViewModel.swift  MODIFICAR
PlatziFlixiOS/Presentation/Views/CourseDetailView.swift            MODIFICAR
```

---

## Plan de Implementación por Sprints

### Sprint 1: Búsqueda Android (estimado: 1 día)

Pasos secuenciales:

1. Modificar `CourseListUiState.kt`:
   - Agregar `searchQuery: String = ""`
   - Agregar evento `data class UpdateSearchQuery(val query: String) : CourseListUiEvent()`

2. Modificar `CourseListViewModel.kt`:
   - Agregar `private val allCourses = MutableStateFlow<List<Course>>(emptyList())`
   - En `loadCourses()` y `refreshCourses()`, guardar en `allCourses` además de en `uiState`
   - Manejar `UpdateSearchQuery`: filtrar `allCourses` y actualizar `uiState.courses`
   - Agregar debounce con `viewModelScope.launch { delay(300); applyFilter() }`

3. Modificar `CourseListScreen.kt`:
   - Agregar `SearchBar` de Material 3 en el `TopAppBar` o debajo de él
   - Conectar al evento `UpdateSearchQuery`

4. Verificar que el test existente `CourseListViewModelTest.kt` sigue pasando y agregar test para `UpdateSearchQuery`.

### Sprint 2: Reproductor de Video — Android (estimado: 2-3 días)

Pasos secuenciales:

1. Crear `ClassDetail.kt` en `domain/models/`

2. Agregar `getClassById` a `CourseRepository.kt` interface

3. Crear `ClassDetailDTO.kt` en `data/entities/` — mapear el campo `"video"` del backend

4. Crear `ClassDetailMapper.kt` en `data/mappers/`

5. Agregar `getClassById` a `ApiService.kt`

6. Implementar `getClassById` en `RemoteCourseRepository.kt`

7. Implementar `getClassById` con mock data en `MockCourseRepository.kt`

8. Crear `ClassDetailUiState.kt`, `ClassDetailViewModel.kt`, `ClassDetailScreen.kt`

9. Hacer `ClassListItem.kt` clickeable — agregar `onClick: (ClassItem) -> Unit`

10. Modificar `CourseDetailScreen.kt` para recibir y propagar `onClassClick`

11. Agregar la nueva ruta en `AppNavigation.kt` y el factory en `AppModule.kt`

### Sprint 2 paralelo: Reproductor de Video — iOS (estimado: 1-2 días)

Pasos secuenciales:

1. Crear `ClassDetail.swift` en `Domain/Models/` (o usar `Class.swift` existente)

2. Crear `ClassDetailMapper.swift` en `Data/Mapper/`

3. Agregar `getClassById` a `CourseAPIEndpoints.swift`

4. Agregar `getClassDetail(_ classId: Int)` a `CourseRepositoryProtocol.swift`

5. Implementar en `RemoteCourseRepository.swift`

6. Crear `ClassDetailViewModel.swift` en `Presentation/ViewModels/`

7. Crear `ClassDetailView.swift` en `Presentation/Views/` — usar `AVPlayer` vía `VideoPlayer` de SwiftUI (disponible en iOS 14+)

8. Modificar `ClassRowView.swift` para aceptar `onTap: () -> Void`

9. Modificar `CourseDetailView.swift` para agregar `NavigationLink` a `ClassDetailView`

### Sprint 3: Sistema de Ratings — Android (estimado: 3-4 días)

Pasos secuenciales:

1. Extender `Course.kt` y `CourseDetail.kt` con campos de rating (nullable para compatibilidad con datos existentes)

2. Actualizar `CourseDTO.kt`, `CourseDetailDTO.kt` con los nuevos campos

3. Actualizar `CourseMapper.kt` y `CourseDetailMapper.kt`

4. Crear DTOs de rating: `RatingDTO.kt`

5. Crear `RatingRepository.kt` interface y `RemoteRatingRepository.kt`

6. Agregar endpoints a `ApiService.kt`

7. Crear `StarRatingComponent.kt` — prioridad alta, es el componente visual central

8. Extender `CourseDetailUiState.kt` con campos de rating

9. Extender `CourseDetailViewModel.kt` con lógica de rating

10. Integrar `StarRatingComponent` en `CourseDetailScreen.kt`

11. Actualizar `AppModule.kt` con `ratingRepository`

### Sprint 3 paralelo: Sistema de Ratings — iOS (estimado: 2-3 días)

Pasos secuenciales:

1. Extender `Course.swift` y `CourseDetail.swift` con campos de rating

2. Actualizar `CourseDTO.swift` y `CourseDetailMapper.swift`

3. Crear `CourseRating.swift`, `RatingStats.swift` en `Domain/Models/`

4. Crear `RatingDTO.swift` en `Data/Entities/`

5. Crear `RatingMapper.swift` en `Data/Mapper/`

6. Crear `RatingAPIEndpoints.swift` y `RemoteRatingRepository.swift`

7. Crear `StarRatingView.swift` — componente reutilizable con medias estrellas

8. Extender `CourseDetailViewModel.swift` con lógica de rating

9. Integrar `StarRatingView` en `CourseDetailView.swift`

---

## Consideraciones Arquitecturales

### Separación de repositorios

Para el sistema de ratings existe la decisión de agregar los métodos de rating a `CourseRepository` o crear un `RatingRepository` separado.

**Recomendación**: `RatingRepository` separado. Los ratings son una entidad de dominio diferente, con su propio ciclo de vida y operaciones CRUD completas. Agregar 5 métodos a `CourseRepository` viola el Single Responsibility Principle. `CourseRepository` solo debe gestionar lecturas de cursos.

### Gestión del userId en mobile

El sistema de ratings requiere un `user_id`. Sin autenticación, la estrategia es:
- Definir `const val ANONYMOUS_USER_ID = 1` en una clase `UserSession` o en `AppModule`
- Usar este ID en todos los requests de rating
- Documentar con un TODO para reemplazar cuando se implemente auth

### Observación sobre ClassDTO vs CourseDetailClass

Existe una inconsistencia entre iOS: `CourseDetailClass` (en el detalle de curso, sin `videoUrl`) y `Class` (modelo completo con `videoUrl`). Para la navegación a clase, no es necesario cambiar `CourseDetailClass` porque la pantalla de clase carga su propio detalle via `GET /classes/{id}`. Esta separación es intencional y correcta.

### Manejo de la media estrella en Android

El componente `StarRating` del frontend usa CSS para medias estrellas. En Compose, la media estrella se puede implementar con un `Canvas` que dibuja una estrella partida o usando un `Box` con dos `Icon` solapados con `clipToBounds`. La librería de Material 3 no provee medias estrellas out-of-the-box.

### Compatibilidad del reproductor de video

- **iOS**: `VideoPlayer` de SwiftUI (`import AVKit`) funciona desde iOS 14. Soporta URLs de video directo (MP4, HLS). Para YouTube habría que usar `WKWebView`.
- **Android**: Para URLs directas, ExoPlayer (`media3-exoplayer`) es la opción estándar. Para YouTube, usar `WebView` o la API de YouTube. Dado que la primera iteración usa `Intent.ACTION_VIEW`, el tipo de URL no es un bloqueador inmediato.

---

## Resumen de Archivos por Feature

### Feature A: Búsqueda Android
| Archivo | Acción |
|---|---|
| `CourseListUiState.kt` | Modificar |
| `CourseListViewModel.kt` | Modificar |
| `CourseListScreen.kt` | Modificar |

### Feature C: Reproductor (Android)
| Archivo | Acción |
|---|---|
| `ClassDetail.kt` | Crear |
| `ClassDetailDTO.kt` | Crear |
| `ClassDetailMapper.kt` | Crear |
| `ClassDetailUiState.kt` | Crear |
| `ClassDetailViewModel.kt` | Crear |
| `ClassDetailScreen.kt` | Crear |
| `CourseRepository.kt` | Modificar |
| `ApiService.kt` | Modificar |
| `RemoteCourseRepository.kt` | Modificar |
| `MockCourseRepository.kt` | Modificar |
| `AppModule.kt` | Modificar |
| `AppNavigation.kt` | Modificar |
| `ClassListItem.kt` | Modificar |
| `CourseDetailScreen.kt` | Modificar |

### Feature C: Reproductor (iOS)
| Archivo | Acción |
|---|---|
| `ClassDetail.swift` | Crear |
| `ClassDetailMapper.swift` | Crear |
| `ClassDetailViewModel.swift` | Crear |
| `ClassDetailView.swift` | Crear |
| `CourseRepositoryProtocol.swift` | Modificar |
| `CourseAPIEndpoints.swift` | Modificar |
| `RemoteCourseRepository.swift` | Modificar |
| `ClassRowView.swift` | Modificar |
| `CourseDetailView.swift` | Modificar |

### Feature B: Ratings (Android)
| Archivo | Acción |
|---|---|
| `CourseRating.kt` | Crear |
| `RatingStats.kt` | Crear |
| `RatingRepository.kt` | Crear |
| `RatingDTO.kt` | Crear |
| `RatingMapper.kt` | Crear |
| `RemoteRatingRepository.kt` | Crear |
| `StarRatingComponent.kt` | Crear |
| `Course.kt` | Modificar |
| `CourseDetail.kt` | Modificar |
| `CourseDTO.kt` | Modificar |
| `CourseDetailDTO.kt` | Modificar |
| `CourseMapper.kt` | Modificar |
| `CourseDetailMapper.kt` | Modificar |
| `ApiService.kt` | Modificar |
| `AppModule.kt` | Modificar |
| `CourseDetailUiState.kt` | Modificar |
| `CourseDetailViewModel.kt` | Modificar |
| `CourseDetailScreen.kt` | Modificar |

### Feature B: Ratings (iOS)
| Archivo | Acción |
|---|---|
| `CourseRating.swift` | Crear |
| `RatingStats.swift` | Crear |
| `RatingRepositoryProtocol.swift` | Crear |
| `RatingDTO.swift` | Crear |
| `RatingMapper.swift` | Crear |
| `RatingAPIEndpoints.swift` | Crear |
| `RemoteRatingRepository.swift` | Crear |
| `StarRatingView.swift` | Crear |
| `Course.swift` | Modificar |
| `CourseDetail.swift` | Modificar |
| `CourseDTO.swift` | Modificar |
| `CourseDetailMapper.swift` | Modificar |
| `CourseDetailViewModel.swift` | Modificar |
| `CourseDetailView.swift` | Modificar |
