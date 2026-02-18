# Platziflix — Proyecto Multi-plataforma

Plataforma de cursos online estilo Netflix. Una API REST central alimenta una web y dos apps móviles nativas.

---

## Arquitectura del Sistema

```
PostgreSQL 15 (puerto 5432)
        │
FastAPI Backend (puerto 8000)
  GET /courses
  GET /courses/{slug}
  GET /health
        │
┌───────┴──────────────────────────┐
│                                  │
Next.js 15 (puerto 3000)    Apps Móviles Nativas
                            Android (Kotlin) + iOS (Swift)
```

---

## Stack Tecnológico

### Backend
- FastAPI + PostgreSQL 15 + SQLAlchemy 2.0 + Alembic + Docker Compose + UV
- Puerto: **8000** | DB puerto: **5432**
- DB: usuario `platziflix_user`, password `platziflix_password`, database `platziflix_db`

### Frontend
- Next.js 15 (App Router) + React 19 + TypeScript strict + SCSS + CSS Modules
- Testing: Vitest + React Testing Library | Fonts: Geist Sans & Geist Mono
- Puerto: **3000**

### Android
- Kotlin + Jetpack Compose + Material 3 + Retrofit 2.9 + OkHttp 4.12 + Coil 2.5
- Navigation Compose 2.7.6 | DI manual (AppModule object, lazy, USE_MOCK_DATA flag)
- Arquitectura: Clean Architecture + MVVM + MVI (StateFlow + sealed events)
- Base URL: `http://10.0.2.2:8000/` (IP especial del emulador Android)
- Min SDK: 24 | Target SDK: 35 | Compose BOM: 2024.09.00

### iOS
- Swift + SwiftUI + URLSession + async/await + Combine (search debounce)
- Arquitectura: Clean Architecture + MVVM (@MainActor + @Published)
- Base URL: `http://localhost:8000`
- Protocol-based DI: `CourseRepository` protocol + `RemoteCourseRepository`

---

## Estructura del Proyecto

```
claude-code/
├── Backend/
│   ├── app/
│   │   ├── alembic/versions/     # Migraciones DB
│   │   ├── models/               # SQLAlchemy models
│   │   ├── routers/              # FastAPI routers
│   │   └── services/             # Service Layer (business logic)
│   ├── Makefile
│   └── docker-compose.yml
│
├── Frontend/
│   ├── app/
│   │   ├── courses/[slug]/       # Detalle de curso
│   │   └── page.tsx              # Catálogo (home)
│   └── components/
│       ├── StarRating/           # Partial stars, ratings
│       └── VideoPlayer/          # Reproductor integrado
│
└── Mobile/
    ├── PlatziFlixAndroid/
    │   └── app/src/main/java/com/espaciotiago/platziflixandroid/
    │       ├── MainActivity.kt                    # Entry → AppNavigation
    │       ├── data/
    │       │   ├── entities/                      # CourseDTO, CourseDetailDTO
    │       │   ├── mappers/                       # CourseMapper, CourseDetailMapper
    │       │   ├── network/                       # ApiService (Retrofit), NetworkModule
    │       │   └── repositories/                  # RemoteCourseRepository, MockCourseRepository
    │       ├── di/AppModule.kt                    # DI manual singleton
    │       ├── domain/
    │       │   ├── models/                        # Course, CourseDetail, ClassItem
    │       │   └── repositories/CourseRepository.kt  # Interface
    │       ├── navigation/AppNavigation.kt        # NavHost: 2 rutas
    │       ├── presentation/courses/
    │       │   ├── components/                    # CourseCard, ErrorMessage, LoadingIndicator
    │       │   ├── screen/CourseListScreen.kt
    │       │   ├── state/CourseListUiState.kt
    │       │   ├── viewmodel/CourseListViewModel.kt
    │       │   └── detail/
    │       │       ├── components/ClassListItem.kt
    │       │       ├── screen/CourseDetailScreen.kt
    │       │       ├── state/CourseDetailUiState.kt
    │       │       └── viewmodel/CourseDetailViewModel.kt
    │       └── ui/theme/                          # Color, Spacing, CornerRadius, Type, Theme
    │
    └── PlatziFlixiOS/PlatziFlixiOS/
        ├── PlatziFlixiOSApp.swift                 # Entry → ContentView → CourseListView
        ├── Domain/
        │   ├── Models/                            # Course, Teacher, Class, CourseDetail, CourseDetailClass
        │   └── Repositories/CourseRepositoryProtocol.swift
        ├── Data/
        │   ├── Entities/                          # CourseDTO, CourseDetailDTO, ClassDTO, TeacherDTO
        │   ├── Mapper/                            # CourseMapper, CourseDetailMapper, TeacherMapper, ClassMapper
        │   └── Repositories/                      # RemoteCourseRepository, CourseAPIEndpoints
        ├── Presentation/
        │   ├── ViewModels/                        # CourseListViewModel, CourseDetailViewModel
        │   └── Views/                             # CourseListView, CourseCardView, CourseDetailView,
        │                                          # ClassRowView, DesignSystem
        └── Services/                              # NetworkService, NetworkManager, APIEndpoint,
                                                   # HTTPMethod, NetworkError
```

---

## Modelo de Datos

```
Course (1) ────── (M) Lesson (1) ────── (M) Class
   │
   └── (M:M via course_teachers) ── Teacher

CourseRating
  - user_id, course_id (único activo por par, soft delete con deleted_at)
  - rating: 1–5
  - average_rating, total_ratings calculados en el backend
```

### Campos relevantes

| Entidad | Campos clave |
|---|---|
| Course | id, name, description, thumbnail, slug, teacher_id[] |
| Lesson | id, course_id, name, description, slug |
| Class | id, course_id, name, description, slug, video_url |
| Teacher | id, name, email |
| CourseRating | id, course_id, user_id, rating, deleted_at |

---

## API Endpoints

| Método | Ruta | Respuesta |
|---|---|---|
| GET | `/` | mensaje bienvenida |
| GET | `/health` | status API + DB |
| GET | `/courses` | `CourseDTO[]` |
| GET | `/courses/{slug}` | `CourseDetailDTO` (incluye classes[], teacher_id[], average_rating, total_ratings) |

### Estructura `CourseDetailDTO`
```json
{
  "id": 1,
  "name": "Curso de React.js",
  "description": "...",
  "thumbnail": "https://...",
  "slug": "curso-de-react",
  "teacher_id": [1, 2],
  "average_rating": 4.5,
  "total_ratings": 120,
  "classes": [
    { "id": 1, "name": "Intro", "description": "...", "slug": "intro" }
  ]
}
```

---

## Patrones de Arquitectura

### Flujo de datos (Android e iOS)

```
UI (Screen/View)
  │ observa StateFlow / @Published
ViewModel
  │ llama suspend / async throws
Repository (interface/protocol)
  │ implementado por RemoteCourseRepository
  │ HTTP: Retrofit / NetworkManager (URLSession)
  ▼
GET /courses/{slug}  →  CourseDetailDTO (JSON)
  │
Mapper (CourseDetailMapper)
  │
Domain Model (CourseDetail)
  │
UiState actualizado → UI re-renders
```

### MVI Android
```kotlin
// Evento entra por handleEvent()
sealed class CourseDetailUiEvent {
    object LoadDetail : CourseDetailUiEvent()
    object Retry : CourseDetailUiEvent()
}
// Estado inmutable con copy()
_uiState.value = _uiState.value.copy(isLoading = true, error = null)
// Repository devuelve Result<T>
repository.getCourseBySlug(slug)
    .onSuccess { _uiState.value = _uiState.value.copy(courseDetail = it) }
    .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
```

### MVVM iOS
```swift
@MainActor
class CourseDetailViewModel: ObservableObject {
    @Published var courseDetail: CourseDetail?
    @Published var isLoading: Bool = false
    @Published var errorMessage: String? = nil

    func loadDetail() { Task { await performLoadDetail() } }
}
```

### Mapper Android (patrón objeto singleton)
```kotlin
object CourseDetailMapper {
    fun fromDTO(dto: CourseDetailDTO): CourseDetail { ... }
}
```

### Mapper iOS (patrón struct estático)
```swift
struct CourseDetailMapper {
    static func toDomain(_ dto: CourseDetailDTO) -> CourseDetail { ... }
}
```

---

## Navegación

### Android — NavHost (AppNavigation.kt)
```
"courses"         → CourseListScreen
"courses/{slug}"  → CourseDetailScreen
```
- `viewModel<CourseDetailViewModel>(key = slug)` — instancia separada por curso
- `AppModule.provideCourseDetailViewModel(slug)` crea el ViewModel

### iOS — NavigationView + NavigationLink programático
```swift
// En CourseListView (courseListContent)
@State private var selectedCourseSlug: String? = nil

ZStack {
    NavigationLink(
        destination: CourseDetailView(slug: course.slug),
        tag: course.slug,
        selection: $selectedCourseSlug
    ) { EmptyView() }.opacity(0)

    CourseCardView(course: course) { selectedCourseSlug = course.slug }
}
```

---

## Design System

### Android (ui/theme/)
```kotlin
// Spacing.kt
Spacing.extraSmall = 4.dp | Spacing.small  = 8.dp
Spacing.medium     = 16.dp | Spacing.large = 24.dp

// CornerRadius.kt
CornerRadius.small = 4.dp | CornerRadius.large     = 12.dp
CornerRadius.medium = 8.dp | CornerRadius.extraLarge = 16.dp
```

### iOS (DesignSystem.swift)
```swift
Spacing.spacing2 = 8pt  | Spacing.spacing4 = 16pt | Spacing.spacing6 = 24pt
Radius.radiusMedium = 8pt | Radius.radiusLarge = 12pt
Color.primaryBlue = #007AFF | Color.cardBackground = .secondarySystemBackground
// Modificador reutilizable:
.cardStyle()   // background + cornerRadius + shadow
```

---

## DI Android — AppModule.kt

```kotlin
object AppModule {
    private const val USE_MOCK_DATA = false  // cambiar a true para desarrollo sin backend

    private val courseRepository: CourseRepository by lazy {
        if (USE_MOCK_DATA) MockCourseRepository()
        else RemoteCourseRepository(apiService)
    }

    fun provideCourseListViewModel()              = CourseListViewModel(courseRepository)
    fun provideCourseDetailViewModel(slug: String) = CourseDetailViewModel(slug, courseRepository)
}
```

---

## Estado de Implementación

| Feature | Backend | Web | Android | iOS |
|---|---|---|---|---|
| Catálogo de cursos | ✅ | ✅ | ✅ | ✅ |
| Detalle de curso + clases | ✅ | ✅ | ✅ | ✅ |
| Navegación entre pantallas | — | ✅ | ✅ | ✅ |
| Reproductor de video | ✅ | ✅ | ⬜ | ⬜ |
| Sistema de ratings | ✅ | ✅ | ⬜ | ⬜ |
| Búsqueda de cursos | ⬜ | ⬜ | ⬜ | ✅ |
| Navegación a clase (video) | — | ✅ | ⬜ | ⬜ |

---

## Comandos de Desarrollo

### Backend (siempre via Docker — verificar que el contenedor esté corriendo)
```bash
cd Backend
make start            # Inicia Docker Compose (API + DB)
make stop             # Detiene containers
make migrate          # Aplica migraciones Alembic
make create-migration # Crea nueva migración
make seed             # Pobla datos de prueba
make seed-fresh       # Reset completo de datos + seed
make logs             # Ver logs de todos los servicios
```

### Frontend
```bash
cd Frontend
yarn dev              # Servidor desarrollo (puerto 3000)
yarn build            # Build de producción
yarn test             # Tests (Vitest)
yarn lint             # Linter
```

---

## Convenciones de Naming

| Plataforma | Convención |
|---|---|
| Python (Backend) | snake_case |
| TypeScript (Frontend) | camelCase (variables), PascalCase (componentes) |
| Kotlin (Android) | camelCase (variables/funciones), PascalCase (clases) |
| Swift (iOS) | camelCase (variables/funciones), PascalCase (tipos) |
| API JSON | snake_case (`teacher_id`, `created_at`) |
| Rutas de navegación | kebab-case (`curso-de-react`) |

---

## Reglas Importantes

1. **Docker obligatorio** para el backend — antes de ejecutar comandos, verificar que el contenedor API esté corriendo y revisar el Makefile.
2. **TypeScript strict** en Frontend — no usar `any`.
3. **API REST es la única fuente de datos** para Frontend y Mobile — nunca datos hardcodeados en producción.
4. **Migraciones Alembic** para todo cambio de esquema DB — nunca modificar la DB directamente.
5. **Mapper obligatorio** entre DTO y Domain Model en mobile — la UI nunca consume DTOs directamente.
6. **MockCourseRepository** disponible en Android para desarrollo sin backend (`USE_MOCK_DATA = true`).
7. **Red Android emulador**: usar `10.0.2.2` (no `localhost`) para conectar al backend.
8. **CourseDetailDTO** en iOS ya está definido en `CourseDTO.swift` junto con `ClassDTO`.
9. Al agregar nuevos endpoints: actualizar `ApiService.kt` (Android) y `CourseAPIEndpoints.swift` (iOS).
10. Al agregar nuevas entidades de dominio: crear DTO → Mapper → Domain Model → Repository interface → implementación.
