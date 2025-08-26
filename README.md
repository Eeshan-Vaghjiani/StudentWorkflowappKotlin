
# Student Workflow App (Kotlin)

This is a Kotlin-based full-stack web application for a web-based academic workflow coordination system for university students. This project is a complete migration from a Laravel/React stack to a modern Kotlin multiplatform architecture.

## 🚀 Migration Status

### ✅ **Phase 1: Project Setup (COMPLETED)**
- Gradle multiplatform project structure
- Module organization (backend, frontend, shared)
- Build configuration and dependencies

### ✅ **Phase 2: Backend Migration (COMPLETED)**
- Ktor application with full API structure
- Exposed database framework integration
- AI service with OpenRouter API integration
- Google Calendar integration
- JWT authentication system
- Comprehensive service layer

### ✅ **Phase 3: Frontend Migration (COMPLETED)**
- React components migrated to Kotlin/JS
- Modern UI with Tailwind CSS
- Responsive design and mobile-first approach
- Complete routing system
- State management implementation

### ✅ **Phase 4: Admin Dashboard & Testing (COMPLETED)**
- Comprehensive admin dashboard
- User management interface
- System monitoring and analytics
- Testing framework setup
- Complete documentation

## 🏗️ Architecture

The application follows a modern, scalable architecture:

```
StudentWorkflowApp/
├── backend/           # Ktor-based REST API
├── frontend/          # Kotlin/JS React application
├── shared/            # Common data models and utilities
└── mobile-app-mockups/ # Design mockups and assets
```

### Backend (Ktor)
- **Framework**: Ktor 2.3.6 with Netty engine
- **Database**: Exposed ORM with H2 (dev) / PostgreSQL (prod)
- **Authentication**: JWT-based with bcrypt password hashing
- **Integrations**: OpenRouter AI API, Google Calendar API
- **Architecture**: Clean architecture with service layer pattern

### Frontend (Kotlin/JS)
- **Framework**: React 18 with Kotlin/JS
- **Styling**: Tailwind CSS with custom design system
- **Routing**: React Router v6
- **State Management**: React hooks with custom state management
- **Build**: Gradle with Kotlin Multiplatform plugin

### Shared Module
- **Data Models**: Common entities used by both frontend and backend
- **Serialization**: Kotlinx Serialization for JSON handling
- **Validation**: Shared validation logic

## 🛠️ Technology Stack

### Backend
- **Language**: Kotlin 1.9.20
- **Framework**: Ktor 2.3.6
- **Database**: Exposed ORM 0.45.0
- **Authentication**: JWT + bcrypt
- **Testing**: JUnit 5 + TestContainers

### Frontend
- **Language**: Kotlin/JS 1.9.20
- **Framework**: React 18.2.0
- **Styling**: Tailwind CSS 3.4.0
- **Build**: Gradle + Kotlin Multiplatform
- **Dependencies**: React Router, Axios, date-fns

### Development Tools
- **Build System**: Gradle 8.0+
- **Package Manager**: npm (frontend dependencies)
- **Version Control**: Git
- **IDE Support**: IntelliJ IDEA, VS Code

## 📋 Prerequisites

- **JDK**: 17 or later
- **Node.js**: 18.0 or later
- **Gradle**: 8.0 or later
- **Database**: H2 (development) or PostgreSQL (production)

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone <repository-url>
cd StudentWorkflowApp
```

### 2. Backend Setup
```bash
# Navigate to backend directory
cd backend

# Run the application
./gradlew run

# The server will start on http://localhost:8080
```

### 3. Frontend Setup
```bash
# Navigate to frontend directory
cd frontend

# Install npm dependencies
npm install

# Run the development server
./gradlew jsBrowserDevelopmentRun

# The application will open in your default browser
```

### 4. Build the Entire Project
```bash
# From the root directory
./gradlew build
```

## 🧪 Testing

### Backend Testing
```bash
cd backend
./gradlew test
```

### Frontend Testing
```bash
cd frontend
./gradlew jsTest
```

### Integration Testing
```bash
# Run all tests
./gradlew test
```

## 📱 Features

### Core Functionality
- **User Authentication**: Secure login/registration with JWT
- **Task Management**: Create, organize, and track academic tasks
- **Calendar Integration**: Google Calendar sync and event management
- **Workflow Tracking**: Visual progress tracking and status management
- **AI Assistance**: OpenRouter API integration for intelligent insights

### Admin Features
- **User Management**: Comprehensive user administration
- **System Monitoring**: Real-time system health and performance metrics
- **Analytics Dashboard**: User growth and usage statistics
- **System Configuration**: Environment and service configuration

### User Experience
- **Responsive Design**: Mobile-first approach with Tailwind CSS
- **Modern UI**: Clean, intuitive interface following Material Design principles
- **Real-time Updates**: Live data synchronization
- **Accessibility**: WCAG 2.1 AA compliance

## 🔧 Configuration

### Environment Variables
Create a `.env` file in the backend directory:

```env
# Database
DATABASE_URL=jdbc:h2:./student_workflow_db
DATABASE_USER=sa
DATABASE_PASSWORD=

# JWT
JWT_SECRET=your-secret-key-here
JWT_EXPIRATION=86400

# OpenRouter API
OPENROUTER_API_KEY=your-api-key-here

# Google Calendar
GOOGLE_CLIENT_ID=your-client-id
GOOGLE_CLIENT_SECRET=your-client-secret
```

### Database Configuration
The application supports both H2 (development) and PostgreSQL (production):

```kotlin
// H2 for development
implementation("com.h2database:h2:2.2.224")

// PostgreSQL for production
implementation("org.postgresql:postgresql:42.7.1")
```

## 📊 API Documentation

### Authentication Endpoints
- `POST /auth/register` - User registration
- `POST /auth/login` - User authentication
- `POST /auth/refresh` - Token refresh
- `POST /auth/logout` - User logout

### Task Management
- `GET /api/tasks` - List all tasks
- `POST /api/tasks` - Create new task
- `PUT /api/tasks/{id}` - Update task
- `DELETE /api/tasks/{id}` - Delete task

### Calendar Integration
- `GET /api/calendar/events` - List calendar events
- `POST /api/calendar/events` - Create calendar event
- `PUT /api/calendar/events/{id}` - Update event
- `DELETE /api/calendar/events/{id}` - Delete event

### AI Services
- `POST /api/ai/assist` - Get AI assistance
- `POST /api/ai/analyze` - Analyze workflow data

## 🚀 Deployment

### Backend Deployment
```bash
# Build the JAR
./gradlew :backend:build

# Run the application
java -jar backend/build/libs/backend-1.0.0.jar
```

### Frontend Deployment
```bash
# Build the production bundle
./gradlew :frontend:jsBrowserProductionRun

# Deploy the dist/ directory to your web server
```

### Docker Deployment
```bash
# Build and run with Docker
docker build -t student-workflow-app .
docker run -p 8080:8080 student-workflow-app
```

## 🔒 Security Features

- **JWT Authentication**: Secure token-based authentication
- **Password Hashing**: bcrypt with salt for password security
- **CORS Configuration**: Proper cross-origin resource sharing
- **Input Validation**: Comprehensive input sanitization
- **Rate Limiting**: API rate limiting to prevent abuse
- **HTTPS Support**: SSL/TLS encryption for production

## 📈 Performance Optimization

- **Database Indexing**: Optimized database queries
- **Caching**: Redis integration for session management
- **CDN Support**: Static asset optimization
- **Lazy Loading**: Component-level code splitting
- **Image Optimization**: WebP format support

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:
- Create an issue in the GitHub repository
- Contact the development team
- Check the documentation wiki

## 🔄 Migration Notes

### From Laravel to Ktor
- **Models**: Eloquent models → Exposed table definitions
- **Controllers**: Laravel controllers → Ktor route handlers
- **Middleware**: Laravel middleware → Ktor plugins
- **Database**: MySQL/PostgreSQL with Eloquent → Exposed ORM
- **Authentication**: Laravel Sanctum → JWT with Ktor Auth

### From React (JavaScript) to Kotlin/JS
- **Components**: JSX → Kotlin React components
- **State Management**: Redux/Zustand → React hooks + custom state
- **Build System**: Webpack → Gradle + Kotlin Multiplatform
- **Type Safety**: PropTypes → Kotlin type system
- **Testing**: Jest → JUnit + Kotlin test frameworks

## 🎯 Roadmap

### Short Term (Next 3 months)
- [ ] Mobile app development
- [ ] Advanced analytics dashboard
- [ ] Real-time collaboration features
- [ ] Enhanced AI integration

### Medium Term (3-6 months)
- [ ] Multi-tenant architecture
- [ ] Advanced reporting system
- [ ] Integration with LMS platforms
- [ ] Performance optimization

### Long Term (6+ months)
- [ ] Machine learning insights
- [ ] Predictive analytics
- [ ] Advanced workflow automation
- [ ] Enterprise features

---

**Built with ❤️ using Kotlin, Ktor, and React**
