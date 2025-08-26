
# Student Workflow App (Kotlin)

This is a Kotlin-based full-stack web application for a web-based academic workflow coordination system for university students. This project is a migration from a Laravel/React stack.

## Project Structure

The project is a Kotlin multiplatform project with the following structure:

- `backend`: A Ktor-based backend application.
- `frontend`: A React-based frontend application written in Kotlin/JS.
- `shared`: A module for code shared between the backend and frontend (e.g., data models).

## Prerequisites

- JDK 17 or later
- Node.js and npm
- Gradle

## Getting Started

### Backend

To run the backend server, execute the following Gradle task:

```bash
./gradlew :backend:run
```

The server will start on `http://localhost:8080`.

### Frontend

To run the frontend application, execute the following Gradle task:

```bash
./gradlew :frontend:jsBrowserDevelopmentRun
```

This will start a development server and open the application in your default browser.

## Building the Application

To build the entire project, run:

```bash
./gradlew build
```
