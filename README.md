# FitJourney

App Android nativa para planificar entrenamientos, crear rutinas a medida y seguir tu progreso, con su propia API REST en Node.js.

Proyecto final del CFGS de Desarrollo de Aplicaciones Multiplataforma (IES Ágora, 2026).

![Java](https://img.shields.io/badge/Java-11-orange?logo=openjdk)
![Android](https://img.shields.io/badge/Android-minSdk%2024-3DDC84?logo=android&logoColor=white)
![Node.js](https://img.shields.io/badge/Node.js-Express%205-339933?logo=nodedotjs&logoColor=white)
![Prisma](https://img.shields.io/badge/Prisma-ORM-2D3748?logo=prisma)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Supabase-4169E1?logo=postgresql&logoColor=white)

<p>
  <img src="https://github.com/user-attachments/assets/f449d811-0c0a-4e6a-8c60-d9935bfcfe44" width="220" />
  <img src="https://github.com/user-attachments/assets/a5829c80-8d72-4ac8-85cf-d672641d577d" width="220" />
  <img src="https://github.com/user-attachments/assets/f03442e1-d32f-4cbd-9174-9b6deaa4c1a2" width="220" />
  <img src="https://github.com/user-attachments/assets/e2f08222-d9fa-42eb-99bd-29d19ed64d8c" width="220" />
</p>

## Funcionalidades

- **Cuentas de usuario**: registro, login con JWT, edición de perfil con foto, desactivación y reactivación de cuenta.
- **Rutinas**: crear, editar y borrar rutinas propias eligiendo ejercicios con series, repeticiones y peso objetivo.
- **Catálogo de ejercicios**: biblioteca con imagen, vídeo, grupo muscular, material y nivel de dificultad. Los administradores pueden añadir, editar y borrar ejercicios.
- **Sesión de entrenamiento**: ejecutas una rutina y vas registrando cada serie (repeticiones, peso, tiempo o velocidad en cardio) con cronómetro, y al terminar se guarda el entrenamiento.
- **Historial**: listado de entrenamientos pasados con el detalle de cada uno.
- **Métricas**: IMC, porcentaje de grasa estimado, masa magra y calorías quemadas por sesión (cálculo por MET).
- **Recordatorios**: notificación periódica para entrenar con WorkManager.
- **Panel de administración**: gestión de usuarios (roles, edición y borrado) para cuentas admin.
- **Modo claro / oscuro** y layouts adaptados a horizontal.

## Arquitectura

```mermaid
flowchart LR
    subgraph App["App Android · Java"]
        V[Fragments / Activities] --> VM[ViewModels]
        VM --> R[Repositories]
        R --> RF[Retrofit + AuthInterceptor]
    end
    RF -- "REST · JWT Bearer" --> API
    subgraph API["API REST · Node.js"]
        RT[Routes + Swagger] --> MW[Middleware auth / isAdmin]
        MW --> C[Controllers]
        C --> P[Prisma ORM]
    end
    P --> DB[(PostgreSQL · Supabase)]
```

La app sigue **MVVM**: las vistas solo pintan y capturan eventos, los ViewModels exponen el estado con LiveData y sobreviven a los cambios de configuración, y los repositorios son la única puerta de acceso a datos (API remota y preferencias locales). Las respuestas de red se envuelven en un `Resource` (loading / success / error) para que la UI reaccione a cada estado.

La API está organizada en rutas → middleware → controladores. La autenticación usa JWT con contraseñas hasheadas con bcrypt, y las rutas de administración pasan por un middleware `isAdmin`. Toda la API está documentada con Swagger.

## Stack

| Capa | Tecnologías |
|---|---|
| App | Java 11, Android SDK 36, Navigation Component, ViewBinding, LiveData + ViewModel, Retrofit + Gson, OkHttp, Glide, WorkManager, Lombok |
| API | Node.js, Express 5, Prisma, express-validator, jsonwebtoken, bcryptjs, Swagger (swagger-jsdoc + swagger-ui-express) |
| Datos | PostgreSQL alojado en Supabase |
| Despliegue | API en Render |

## Endpoints

| Recurso | Endpoints |
|---|---|
| `/api/auth` | `POST /register`, `POST /login`, `POST /reactivate`, `POST /delete` |
| `/api/users` | `GET /profile`, `PUT /profile`, `POST /deactivate` · admin: `GET /`, `PUT /:id`, `DELETE /:id` |
| `/api/exercises` | `GET /`, `GET /:id` · admin: `POST /`, `PUT /:id`, `DELETE /:id` |
| `/api/routines` | `GET /`, `POST /`, `GET /:id`, `PUT /:id`, `DELETE /:id` |
| `/api/workouts` | `GET /`, `POST /`, `GET /:id`, `DELETE /:id` |

Documentación interactiva en `/api-docs` con la API arrancada.

## Estructura del repositorio

```
FitJourneyProject/
├── FitJourney/          # App Android (Android Studio)
│   └── app/src/main/java/es/iesagora/fitjourney/
│       ├── ui/          # Activities y Fragments por pantalla
│       ├── viewmodel/   # ViewModels
│       ├── data/        # Modelos, repositorios y cliente Retrofit
│       └── utils/       # Cálculos de fitness, imágenes, notificaciones
└── FitJourneyAPI/       # API REST (Node.js + Express + Prisma)
    ├── routes/          # Rutas + documentación Swagger
    ├── controllers/     # Lógica de cada recurso
    ├── middlewares/     # Autenticación JWT y rol admin
    └── prisma/          # Esquema de la base de datos
```

## Cómo ejecutarlo

### API

```bash
cd FitJourneyAPI
npm install
```

Crea un `.env` en `FitJourneyAPI/`:

```env
DATABASE_URL=postgresql://usuario:password@host:6543/postgres?pgbouncer=true
DIRECT_URL=postgresql://usuario:password@host:5432/postgres
JWT_SECRET=una_clave_larga_y_aleatoria
PORT=3000
```

```bash
npx prisma generate
npx prisma db push   # crea las tablas en tu base de datos
node api.js
```

La documentación queda en `http://localhost:3000/api-docs`.

### App

1. Abre la carpeta `FitJourney/` con Android Studio (AGP 8.13, JDK 17 para Gradle).
2. Si usas tu propia API, cambia `BASE_URL` en `data/remote/RetrofitClient.java`.
3. Ejecuta en un emulador o dispositivo con Android 7.0 (API 24) o superior.

## Descarga

[Descarga el APK](FitJourney/app/release/app-release.apk) e instálalo en un móvil con Android 7.0 o superior (hay que permitir la instalación desde orígenes desconocidos). La app usa la API desplegada en Render, que se duerme tras un rato sin uso, así que la primera petición puede tardar unos segundos.

## Autor

**Daniel Martín López** · [LinkedIn](https://www.linkedin.com/in/daniel-martin-lopez-380996337)
