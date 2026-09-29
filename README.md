# OpsFlow

OpsFlow is a full-stack operations management application built with Spring Boot, Vue.js, and MySQL.

The application provides a simple interface for creating, managing, searching, and monitoring operational records, with support for filtering, sorting, pagination, reporting, validation, and automated backend testing.

## Features

- Create operation records
- Update existing operations
- Delete operations
- View operation details
- Filter operations by date range
- Filter operations by time range
- Combine date and time filters
- Sort records by:
    - ID
    - Title
    - Date
    - Time
    - Status
- Ascending and descending sorting
- Server-side pagination
- Dashboard overview
- Recent operations display
- Operation status summaries
- Records view
- Reports view
- Request validation
- API error handling
- Configurable frontend API URL
- Automated backend tests

## Tech Stack

### Backend

- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- Jakarta Validation
- MySQL
- Maven
- JUnit 5

### Frontend

- Vue 3
- Vue Router
- JavaScript
- Vite
- HTML5
- CSS3

## Project Structure

```text
opsflow-backend/
├── frontend/
│   ├── public/
│   ├── src/
│   │   ├── config/
│   │   │   └── api.js
│   │   ├── router/
│   │   │   └── index.js
│   │   ├── views/
│   │   │   ├── DashboardView.vue
│   │   │   ├── OperationsView.vue
│   │   │   ├── RecordsView.vue
│   │   │   └── ReportsView.vue
│   │   ├── App.vue
│   │   └── main.js
│   ├── .env.example
│   ├── package.json
│   └── vite.config.js
│
├── src/
│   ├── main/
│   │   ├── java/com/opsflow/backend/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── exception/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   └── OpsflowBackendApplication.java
│   │   └── resources/
│   │
│   └── test/
│       └── java/com/opsflow/backend/
│
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

## Getting Started

### Prerequisites

Make sure the following are installed:

- Java 17 or later
- Maven
- MySQL
- Node.js
- npm

## Backend Setup

Clone the repository and open the project directory.

Configure the MySQL database connection in the Spring Boot configuration under:

```text
src/main/resources/
```

Then start the backend application.

Using Maven Wrapper on Windows:

```bash
mvnw.cmd spring-boot:run
```

Or run:

```text
OpsflowBackendApplication.java
```

directly from your IDE.

By default, the backend runs on:

```text
http://localhost:8080
```

## Frontend Setup

Navigate to the frontend directory:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Create a local `.env` file based on `.env.example`:

```env
VITE_API_BASE_URL=http