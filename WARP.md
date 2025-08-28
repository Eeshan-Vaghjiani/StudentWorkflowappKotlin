# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

This is a Kotlin multiplatform web application for academic workflow coordination. The project has been migrated from Laravel/React to a modern Kotlin stack with Ktor backend and Kotlin/JS frontend.

## Development Commands

### Building the Project
```bash
# Build entire project
./gradlew build

# Build specific modules
./gradlew :backend:build
./gradlew :frontend:build
./gradlew :shared:build
```

### Running the Application
```bash
# Start backend server (runs on localhost:8080)
cd backend && ./gradlew run

# Start frontend development server
cd frontend && npm install && ./gradlew jsBrowserDevelopmentRun

# Alternative quick start (uses build.sh)
./build.sh
```

### Testing
```bash
# Run backend tests
cd backend && ./gradlew test

# Run frontend tests  
cd frontend && ./gradlew jsTest

# Run all tests from root
./gradlew test

# Run specific test
./gradlew :backend:test --tests "com.studentworkflow.ApplicationTest"
```

### Development Workflow
```bash
# Clean and rebuild
./gradlew clean build

# Run with hot reload (backend)
cd backend && ./gradlew run --continuous

# Check for dependency updates
./gradlew dependencyUpdates
```

## Architecture Overview

### Module Structure
- **backend/**: Ktor-based REST API with service layer pattern
- **frontend/**: Kotlin/JS React application with Tailwind CSS
- **shared/**: Common data models and serialization logic
- **mobile-app-mockups/**: Design assets and mockups

### Backend Architecture
The backend follows clean architecture principles:

- **Application.kt**: Main entry point, dependency injection setup
- **plugins/**: Ktor plugins for routing, security, serialization
- **routes/**: API route handlers (AuthRoutes, AIRoutes, etc.)
- **services/**: Business logic layer (UserService, AIService, etc.)
- **db/**: Database configuration and table definitions using Exposed ORM
- **utils/**: Utility classes and helper functions

### Key Services
- **UserService**: User management and authentication logic
- **AIService**: OpenRouter API integration for AI assistance
- **JwtService**: JWT token generation and validation
- **GoogleCalendarService**: Google Calendar API integration
- **EmailService**: Email notifications
- **PricingService**: Subscription and payment handling

### Frontend Architecture
- **App.kt**: Main React application with routing setup
- **Main.kt**: Application entry point
- **pages/**: Page components (Dashboard, Login, Admin, etc.)
- Uses React Router v6 for navigation
- Tailwind CSS for styling with custom design system
- State management via React hooks and custom state

### Shared Module
- **models/Models.kt**: All data models with kotlinx.serialization
- **models/User.kt**: User-specific models
- Provides type safety across frontend/backend boundary

## Database Schema

The application uses Exposed ORM with the following main entities:
- **Users**: User accounts with AI usage tracking and 2FA support
- **StudyGroups**: Collaborative study groups
- **Tasks**: Academic tasks with priority and assignment
- **StudySessions**: Study time tracking
- **PomodoroSessions**: Pomodoro technique implementation
- **Messages**: Group and direct messaging
- **Subscriptions**: Payment and subscription management
- **GoogleCalendars**: Calendar integration data

## Technology Stack

### Backend (JVM)
- Kotlin 1.9.24
- Ktor 2.3.8 with Netty engine
- Exposed ORM 0.45.0 for database operations
- JWT authentication with bcrypt password hashing
- H2 database for development, PostgreSQL for production
- JUnit 5 and TestContainers for testing

### Frontend (Kotlin/JS)
- Kotlin/JS 1.9.24 targeting browser
- React 18.2.0 with React Router v6
- Tailwind CSS 3.4.0 for styling
- Zustand 4.4.7 for state management
- Axios for HTTP client, date-fns for date handling

### Key Integration Points
- **OpenRouter API**: AI assistance integration
- **Google Calendar API**: Calendar synchronization
- **Email Service**: User notifications and password reset

## Environment Setup

### Prerequisites
- JDK 17+
- Node.js 18.0+
- Gradle 8.0+ (wrapper included)

### Environment Variables (backend/.env)
```env
DATABASE_URL=jdbc:h2:./student_workflow_db
DATABASE_USER=sa
DATABASE_PASSWORD=

JWT_SECRET=your-secret-key-here
JWT_EXPIRATION=86400

OPENROUTER_API_KEY=your-api-key-here

GOOGLE_CLIENT_ID=your-client-id
GOOGLE_CLIENT_SECRET=your-client-secret
```

## Development Patterns

### Backend Patterns
- Dependency injection through constructor parameters in Application.kt
- Service layer pattern for business logic separation
- Plugin-based configuration for Ktor features
- Exposed ORM with explicit table definitions
- JWT-based authentication with role-based access

### Frontend Patterns
- React functional components with Kotlin/JS
- Page-based routing structure
- Custom hooks for state management (useAuthState)
- Tailwind utility classes for styling
- Type-safe API communication using shared models

### Code Organization
- Package structure mirrors business domains (auth, ai, calendar, etc.)
- Separation of concerns: routes handle HTTP, services handle business logic
- Shared models ensure type safety between frontend and backend
- Configuration externalized through environment variables

## Deployment

### Production Build
```bash
# Backend JAR
./gradlew :backend:build
java -jar backend/build/libs/backend-1.0.0.jar

# Frontend production bundle
./gradlew :frontend:jsBrowserProductionRun
# Deploy frontend/build/dist/ to web server
```

### Database Migration
The application uses Exposed's auto-migration features. For production:
1. Configure PostgreSQL connection in environment variables
2. Tables are created automatically on first run
3. No manual migration scripts required

## Common Development Tasks

### Adding New API Endpoints
1. Define route handler in appropriate routes file (e.g., AuthRoutes.kt)
2. Implement business logic in corresponding service
3. Add request/response models to shared/models/Models.kt
4. Update Routing.kt if needed for route configuration

### Adding New Frontend Pages
1. Create new page component in frontend/src/main/kotlin/com/studentworkflow/pages/
2. Add route definition in App.kt Routes block
3. Update navigation in Header component if needed
4. Import shared models for API communication

### Database Schema Changes
1. Update table definitions in backend/src/main/kotlin/com/studentworkflow/db/Tables.kt
2. Exposed will auto-migrate on next application start
3. Update corresponding models in shared module if needed

## Common Issues and Solutions

### Gradle Plugin Version Conflicts
If you encounter "plugin already on classpath" errors:
1. Ensure all plugin versions are declared in root `build.gradle.kts` with `apply false`
2. Module build files should reference plugins without version numbers
3. Use `./gradlew wrapper --gradle-version X.X.X` to update wrapper

### JVM Target Compatibility
For "Inconsistent JVM-target compatibility" errors:
1. Add `kotlin { jvmToolchain(21) }` to backend and shared modules
2. Kotlin 1.9.24 supports JVM target 21 (not 24)
3. Ensure all Kotlin/JVM modules use same toolchain version

### Duplicate Data Class Errors
If you see "Redeclaration" errors:
1. Check shared/src/commonMain/kotlin/com/studentworkflow/models/ for duplicates
2. Ensure each data class is defined only once across all files
3. User class should only exist in User.kt, not Models.kt

### Missing Dependencies
Common missing dependencies have been added:
- `io.ktor:ktor-client-cio-jvm` for HTTP client
- `org.jetbrains.exposed:exposed-java-time` for timestamp support
- ZXing libraries for QR code generation
- TOTP library for 2FA authentication
