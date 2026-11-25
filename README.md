
#SpaceSync Backend — Concurrent Space & Resource Booking API

The **SpaceSync Backend** is a Java and Spring Boot REST API powered by PostgreSQL. It provides a secure and reliable platform for managing reservations of shared spaces and equipment.

The application supports different environments, including campuses, libraries, offices, coworking spaces, restaurants, meeting rooms, event gardens, laboratories, and other reservable facilities.

Its main technical objective is to **prevent overlapping confirmed bookings, even when multiple users attempt to reserve the same resource simultaneously**.

To guarantee this, ReserveFlow uses PostgreSQL range types, exclusion constraints, and database transactions rather than relying only on application-level availability checks.

The backend is responsible for:

- Managing users, roles, and authentication.
- Creating and managing reservable resources.
- Providing resource discovery and availability information.
- Creating, retrieving, and canceling reservations.
- Preventing double bookings under concurrent requests.
- Managing first-come, first-served waiting lists.
- Automatically promoting eligible users from waiting lists.
- Generating and storing in-app notifications.
- Enforcing authorization, validation, and secure database operations.

---

## Table of Contents

- [Backend Core Responsibilities](#backend-core-responsibilities)
- [Backend Project Structure](#backend-project-structure)
- [Project Architecture and File Responsibilities](#project-architecture-and-file-responsibilities)
- [Tech Stack](#tech-stack)
- [Data Model and Concurrency Design](#data-model-and-concurrency-design)
- [REST API Reference](#rest-api-reference)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation and Setup](#installation-and-setup)
  - [PostgreSQL Setup](#postgresql-setup)
  - [Environment Variables](#environment-variables)
  - [Running the Application](#running-the-application)
- [Available Maven Commands](#available-maven-commands)
- [Contributing](#contributing-fork-and-pull-request)
- [Development Guidelines](#development-guidelines)

---

## Backend Core Responsibilities

### 1. Resource Discovery & Admin Management

- **Resource Catalog:** Provide REST endpoints for listing and retrieving available resources.

- **Resource Filtering:** Allow users to search resources by category, location, capacity, and availability.

- **Resource Details:** Store and return resource descriptions, amenities, images, operating hours, and capacity.

- **Administrative Management:** Allow authorized administrators to create, update, deactivate, or delete resources.

- **Role-Based Access Control:** Restrict resource administration operations to authorized users.

### 2. Secure Booking & Concurrency Control

- **Reservation Management:** Allow authenticated users to create, view, and cancel reservations.

- **Time Interval Validation:** Ensure booking start times are earlier than end times and fall within permitted operating hours.

- **Overlap Prevention:** Reject overlapping confirmed reservations for the same resource.

- **Database-Level Concurrency Protection:** Use PostgreSQL exclusion constraints with `btree_gist` to prevent double bookings.

- **Back-to-Back Reservations:** Support consecutive time intervals such as 10:00–12:00 followed by 12:00–14:00.

- **HTTP Conflict Handling:** Return `409 Conflict` when a requested interval overlaps an existing confirmed reservation.

- **Reservation Ownership:** Ensure users can only manage their own reservations unless authorized as administrators.

### 3. Automatic Waitlist & Notification Engine

- **Waitlist Enrollment:** Allow users to join a waiting list when their requested resource is unavailable.

- **FIFO Queue Management:** Process eligible waiting list entries in first-come, first-served order.

- **Duplicate Prevention:** Prevent users from creating duplicate waiting list entries for the same resource and time interval.

- **Automatic Promotion:** When a confirmed reservation is canceled, automatically evaluate eligible waiting list entries for promotion.

- **Transactional Promotion:** Ensure promotion operations cannot create overlapping confirmed reservations or duplicate promotions.

- **In-App Notifications:** Notify users when their reservation or waiting list status changes.

---

## Backend Project Structure

The application follows a layered Spring Boot architecture.

Each folder and file has a specific responsibility. Contributors should create and maintain files within the appropriate package.

```text id="rfproject"
reserveflow-backend/
│
├── src/
│   ├── main/
│   │   ├── java/com/reserveflow/
│   │   │   │
│   │   │   ├── config/
│   │   │   │   # Spring Security, CORS, database transaction,
│   │   │   │   # and shared application configurations.
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   # REST controllers that receive HTTP requests
│   │   │   │   # and return responses to frontend clients.
│   │   │   │   │
│   │   │   │   ├── AuthController.java
│   │   │   │   │   # Handles user registration and login.
│   │   │   │   │
│   │   │   │   ├── ResourceController.java
│   │   │   │   │   # Handles resource listing, filtering,
│   │   │   │   │   # creation, updates, and deletion.
│   │   │   │   │
│   │   │   │   ├── ReservationController.java
│   │   │   │   │   # Handles reservation creation, retrieval,
│   │   │   │   │   # and cancellation requests.
│   │   │   │   │
│   │   │   │   ├── WaitlistController.java
│   │   │   │   │   # Handles joining, viewing, and leaving
│   │   │   │   │   # resource waiting lists.
│   │   │   │   │
│   │   │   │   └── NotificationController.java
│   │   │   │       # Returns booking and waitlist notifications
│   │   │   │       # belonging to the authenticated user.
│   │   │   │
│   │   │   ├── dto/
│   │   │   │   # Defines API request and response data.
│   │   │   │   │
│   │   │   │   ├── ResourceDto.java
│   │   │   │   │   # Transfers resource details such as
│   │   │   │   │   # name, location, capacity, and amenities.
│   │   │   │   │
│   │   │   │   ├── AvailabilityResponse.java
│   │   │   │   │   # Returns available and occupied intervals
│   │   │   │   │   # for a selected resource and date.
│   │   │   │   │
│   │   │   │   ├── ReservationRequest.java
│   │   │   │   │   # Defines required booking information:
│   │   │   │   │   # resource, start time, and end time.
│   │   │   │   │
│   │   │   │   ├── ReservationResponse.java
│   │   │   │   │   # Returns reservation details and status.
│   │   │   │   │
│   │   │   │   ├── WaitlistRequest.java
│   │   │   │   │   # Defines the resource and requested
│   │   │   │   │   # interval for waitlist enrollment.
│   │   │   │   │
│   │   │   │   ├── WaitlistResponse.java
│   │   │   │   │   # Returns waitlist entry information,
│   │   │   │   │   # status, and queue details.
│   │   │   │   │
│   │   │   │   └── NotificationDto.java
│   │   │   │       # Transfers notification messages,
│   │   │   │       # timestamps, and read status.
│   │   │   │
│   │   │   ├── exception/
│   │   │   │   # Handles application exceptions and converts
│   │   │   │   # failures into consistent HTTP responses.
│   │   │   │   │
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   │       # Handles validation errors, missing
│   │   │   │       # resources, forbidden requests, and
│   │   │   │       # reservation conflicts (HTTP 409).
│   │   │   │
│   │   │   ├── model/
│   │   │   │   # JPA entities representing database tables
│   │   │   │   # and their relationships.
│   │   │   │   │
│   │   │   │   ├── User.java
│   │   │   │   │   # Represents users, credentials, and roles.
│   │   │   │   │
│   │   │   │   ├── Resource.java
│   │   │   │   │   # Represents rooms, venues, or equipment
│   │   │   │   │   # that can be reserved.
│   │   │   │   │
│   │   │   │   ├── Reservation.java
│   │   │   │   │   # Stores confirmed or canceled bookings,
│   │   │   │   │   # time intervals, and associated users.
│   │   │   │   │
│   │   │   │   ├── WaitlistEntry.java
│   │   │   │   │   # Stores users waiting for occupied slots
│   │   │   │   │   # and their queue timestamps.
│   │   │   │   │
│   │   │   │   └── Notification.java
│   │   │   │       # Stores reservation status notifications
│   │   │   │       # and read/unread information.
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   # Spring Data JPA interfaces used to
│   │   │   │   # access and persist database records.
│   │   │   │   │
│   │   │   │   ├── UserRepository.java
│   │   │   │   │   # Finds users by ID or email.
│   │   │   │   │
│   │   │   │   ├── ResourceRepository.java
│   │   │   │   │   # Retrieves resources and applies
│   │   │   │   │   # category, capacity, and location filters.
│   │   │   │   │
│   │   │   │   ├── ReservationRepository.java
│   │   │   │   │   # Retrieves bookings by resource, user,
│   │   │   │   │   # status, and requested interval.
│   │   │   │   │
│   │   │   │   ├── WaitlistEntryRepository.java
│   │   │   │   │   # Retrieves eligible waiting list entries
│   │   │   │   │   # ordered by queue timestamp.
│   │   │   │   │
│   │   │   │   └── NotificationRepository.java
│   │   │   │       # Stores and retrieves user notifications.
│   │   │   │
│   │   │   ├── security/
│   │   │   │   # Implements authentication, role checks,
│   │   │   │   # and resource ownership authorization.
│   │   │   │   │
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   │   # Validates JWT tokens and establishes
│   │   │   │   │   # the authenticated user.
│   │   │   │   │
│   │   │   │   └── AccessControlService.java
│   │   │   │       # Verifies User/Admin permissions and
│   │   │   │       # reservation ownership before operations.
│   │   │   │
│   │   │   ├── service/
│   │   │   │   # Contains core business logic and
│   │   │   │   # transactional reservation workflows.
│   │   │   │   │
│   │   │   │   ├── ResourceService.java
│   │   │   │   │   # Manages resource CRUD operations and
│   │   │   │   │   # validates administrative changes.
│   │   │   │   │
│   │   │   │   ├── AvailabilityService.java
│   │   │   │   │   # Calculates available and occupied
│   │   │   │   │   # intervals based on operating hours
│   │   │   │   │   # and confirmed reservations.
│   │   │   │   │
│   │   │   │   ├── ReservationService.java
│   │   │   │   │   # Creates and cancels bookings, validates
│   │   │   │   │   # intervals, and handles conflicts
│   │   │   │   │   # using database transactions.
│   │   │   │   │
│   │   │   │   ├── WaitlistPromotionService.java
│   │   │   │   │   # Selects the earliest eligible waitlist
│   │   │   │   │   # entry and safely promotes it when
│   │   │   │   │   # a confirmed reservation is canceled.
│   │   │   │   │
│   │   │   │   └── NotificationService.java
│   │   │   │       # Creates and retrieves notifications
│   │   │   │       # for booking and waitlist changes.
│   │   │   │
│   │   │   └── ReserveFlowApplication.java
│   │   │       # Main class that starts Spring Boot.
│   │   │
│   │   └── resources/
│   │       ├── db/migration/
│   │       │   # Flyway SQL migrations for database setup
│   │       │   # and schema changes.
│   │       │   │
│   │       │   └── V1__init_schema_and_exclusion.sql
│   │       │       # Creates initial database tables,
│   │       │       # enables btree_gist, and defines the
│   │       │       # booking overlap exclusion constraint.
│   │       │
│   │       ├── application.properties
│   │       │   # Default Spring Boot configuration using
│   │       │   # environment variable placeholders.
│   │       │
│   │       └── application-dev.properties
│   │           # Development-specific Spring settings.
│   │
│   └── test/
│       └── java/com/reserveflow/
│           # Unit and integration tests for booking,
│           # concurrency, security, availability,
│           # cancellation, and waitlist promotion.
│
├── .env.example
│   # Template listing required environment variables
│   # without containing real credentials.
│
├── .env
│   # Local configuration with actual values.
│   # Created by each contributor; never committed.
│
├── .gitignore
│   # Excludes local secrets, build artifacts,
│   # and IDE-generated files.
│
├── mvnw
│   # Maven wrapper executable for Linux and macOS.
│
├── mvnw.cmd
│   # Maven wrapper executable for Windows.
│
├── pom.xml
│   # Maven dependencies, plugins, and build configuration.
│
└── README.md
    # Project setup, API, and contribution documentation.
```

> **Important:** This is the intended project structure. Every contributor must create a local `.env` file using `.env.example`. The `.env` file must never be committed to GitHub.

---

## Project Architecture and File Responsibilities

### 1. Controller Layer

**Package:** `com.reserveflow.controller`

Controllers receive HTTP requests and delegate processing to the appropriate services.

| File | Responsibility |
|------|----------------|
| `AuthController.java` | Handles authentication and registration |
| `ResourceController.java` | Provides resource discovery and admin CRUD endpoints |
| `ReservationController.java` | Handles reservation creation, retrieval, and cancellation |
| `WaitlistController.java` | Allows users to join, view, or leave waiting lists |
| `NotificationController.java` | Returns notifications belonging to authenticated users |

**Implementation Guidelines:**

- Controllers should only handle HTTP requests and responses.
- Business logic must remain in service classes.
- Use DTOs for request and response data.
- Apply request validation.
- Return appropriate HTTP status codes.

### 2. Service Layer

**Package:** `com.reserveflow.service`

Services implement the application's business rules.

| File | Responsibility |
|------|----------------|
| `ResourceService.java` | Manages resource creation, updates, filtering, and deletion |
| `AvailabilityService.java` | Calculates resource availability using operating hours and confirmed bookings |
| `ReservationService.java` | Creates, validates, retrieves, and cancels reservations |
| `WaitlistPromotionService.java` | Promotes eligible waiting list entries safely after cancellations |
| `NotificationService.java` | Creates and retrieves booking-related notifications |

**Implementation Guidelines:**

- Place business rules inside service classes.
- Use constructor dependency injection.
- Use `@Transactional` for operations requiring atomic database changes.
- Never rely only on a Java availability check to prevent double bookings.
- Ensure waitlist promotion is transactionally safe.
- Avoid direct database queries inside controllers.

### 3. Repository Layer

**Package:** `com.reserveflow.repository`

Repositories provide database access using Spring Data JPA.

| File | Responsibility |
|------|----------------|
| `UserRepository.java` | Retrieves users by ID or email |
| `ResourceRepository.java` | Retrieves resources and supports filtering |
| `ReservationRepository.java` | Manages reservation records and interval queries |
| `WaitlistEntryRepository.java` | Retrieves waiting list entries by status and queue order |
| `NotificationRepository.java` | Stores and retrieves notification records |

Repositories should be injected into service classes rather than directly into controllers.

### 4. Model Layer

**Package:** `com.reserveflow.model`

Models represent the database entities.

| File | Responsibility |
|------|----------------|
| `User.java` | Represents users, credentials, and roles |
| `Resource.java` | Represents reservable spaces or equipment |
| `Reservation.java` | Represents booking intervals and reservation status |
| `WaitlistEntry.java` | Represents users waiting for unavailable slots |
| `Notification.java` | Represents booking notifications and read status |

Use JPA annotations to define relationships between entities.

### 5. DTO Layer

**Package:** `com.reserveflow.dto`

DTOs define the information exchanged between API clients and the backend.

| File | Responsibility |
|------|----------------|
| `ResourceDto.java` | Transfers resource information |
| `AvailabilityResponse.java` | Returns available and occupied time intervals |
| `ReservationRequest.java` | Defines booking request fields |
| `ReservationResponse.java` | Returns reservation information and status |
| `WaitlistRequest.java` | Defines requested waiting list intervals |
| `WaitlistResponse.java` | Returns waitlist status and queue information |
| `NotificationDto.java` | Returns user notification details |

Use Jakarta Bean Validation annotations where necessary.

### 6. Security Layer

**Package:** `com.reserveflow.security`

- `JwtAuthenticationFilter.java` validates incoming JWT tokens and identifies authenticated users.
- `AccessControlService.java` verifies whether users have permission to perform protected operations.

Administrators may manage resources, while regular users should only manage their own reservations and waiting list entries.

### 7. Configuration Layer

**Package:** `com.reserveflow.config`

This package contains configurations for:

- Spring Security.
- CORS.
- Authentication.
- Transaction management.
- Other shared application settings.

Configuration classes should be created according to the project's needs.

### 8. Exception Handling

**Package:** `com.reserveflow.exception`

`GlobalExceptionHandler.java` should provide consistent responses for:

- `400 Bad Request` — Invalid request data.
- `401 Unauthorized` — Missing or invalid authentication.
- `403 Forbidden` — Insufficient permissions.
- `404 Not Found` — Requested resource does not exist.
- `409 Conflict` — Overlapping reservation or other business conflict.
- `500 Internal Server Error` — Unexpected application failures.

### 9. Database Migrations

**Folder:** `src/main/resources/db/migration/`

SQL migration files define the database schema and constraints.

`V1__init_schema_and_exclusion.sql` is responsible for:

- Creating initial database tables.
- Enabling the PostgreSQL `btree_gist` extension.
- Defining the exclusion constraint for confirmed reservations.
- Creating required relationships and database constraints.

New database changes should be implemented using additional versioned migrations.

### 10. Testing

**Package:** `src/test/java/com/reserveflow/`

Tests should cover:

- Resource CRUD operations.
- Booking validation.
- Overlapping reservation prevention.
- Simultaneous reservation attempts.
- Back-to-back booking support.
- Reservation ownership checks.
- Cancellation behavior.
- Automatic waitlist promotion.
- Notification generation.
- Authentication and authorization.

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Java 17+ |
| Framework | Spring Boot 3.x |
| REST API | Spring Web |
| Persistence | Spring Data JPA |
| Database | PostgreSQL 14+ |
| Concurrency Protection | PostgreSQL `btree_gist` and exclusion constraints |
| Transactions | Spring Transaction Management |
| Authentication | JWT |
| Authorization | Spring Security |
| Validation | Jakarta Bean Validation |
| Database Migrations | Flyway |
| Build Tool | Maven |
| Testing | JUnit / Spring Boot Test |

---

## Data Model and Concurrency Design

### 1. User

| Field | Description |
|-------|-------------|
| `id` | Unique user identifier |
| `email` | User email |
| `passwordHash` | Securely hashed password |
| `role` | USER or ADMIN |
| `createdAt` | Account creation timestamp |

### 2. Resource

| Field | Description |
|-------|-------------|
| `id` | Unique resource identifier |
| `name` | Resource name |
| `category / type` | Room, office, equipment, garden, etc. |
| `location` | Resource location |
| `capacity` | Maximum number of users |
| `amenities` | Available facilities |
| `description` | Resource description |
| `imageUrl` | Resource image URL |
| `openingTime` | Opening time |
| `closingTime` | Closing time |
| `active` | Whether the resource is available for use |

### 3. Reservation

| Field | Description |
|-------|-------------|
| `id` | Reservation identifier |
| `resourceId` | Reserved resource |
| `userId` | User making the reservation |
| `startTime` | Reservation start timestamp |
| `endTime` | Reservation end timestamp |
| `status` | CONFIRMED, CANCELED, WAITLISTED |
| `createdAt` | Reservation creation timestamp |
| `updatedAt` | Last modification timestamp |

### 4. WaitlistEntry

| Field | Description |
|-------|-------------|
| `id` | Waiting list entry identifier |
| `resourceId` | Requested resource |
| `userId` | User waiting for the resource |
| `startTime` | Requested start timestamp |
| `endTime` | Requested end timestamp |
| `status` | WAITING, PROMOTED, CANCELED |
| `queuedAt` | Timestamp used for FIFO ordering |

### 5. Notification

| Field | Description |
|-------|-------------|
| `id` | Notification identifier |
| `userId` | Recipient |
| `reservationId` | Related reservation |
| `message` | Notification content |
| `readStatus` | Read/unread state |
| `createdAt` | Notification creation timestamp |

---

## PostgreSQL Overlap Prevention

The main technical requirement is preventing overlapping confirmed reservations under concurrent requests.


### 1. Handling Concurrent Requests

When two users submit overlapping reservations simultaneously:

1. Both requests reach the backend.
2. The backend attempts to create confirmed reservations.
3. PostgreSQL enforces the exclusion constraint.
4. At most one overlapping reservation can be committed.
5. The conflicting request is rejected.
6. The API returns HTTP `409 Conflict`.
7. The frontend can offer the user an opportunity to join the waitlist.

> **Important:** The exclusion constraint is the final guarantee against double bookings. Application-level availability checks improve the user experience but cannot replace database-level protection.

---

## REST API Reference

### 1. Resources & Availability

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/resources` | List and filter resources |
| GET | `/api/resources/:id` | Retrieve resource details |
| GET | `/api/resources/:id/availability` | Retrieve availability for a date or interval |
| POST | `/api/resources` | Create a resource (Admin) |
| PUT | `/api/resources/:id` | Update resource details (Admin) |
| DELETE | `/api/resources/:id` | Delete or deactivate a resource (Admin) |

### 2. Reservations

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/reservations` | Create a reservation |
| GET | `/api/reservations/my` | Retrieve the current user's reservations |
| GET | `/api/reservations/:id` | Retrieve reservation details |
| PATCH | `/api/reservations/:id/cancel` | Cancel a reservation and trigger waitlist evaluation |

### 3. Waitlist & Notifications

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/waitlist` | Join a waiting list |
| GET | `/api/waitlist/my` | Retrieve the current user's waiting list entries |
| DELETE | `/api/waitlist/:id` | Leave a waiting list |
| GET | `/api/notifications` | Retrieve in-app notifications |

---

## Getting Started

Follow these steps to configure and run the ReserveFlow Backend locally.

### Prerequisites

Ensure you have:

- Java Development Kit (JDK 17 or higher).
- Maven 3.8+ or the included Maven wrapper.
- PostgreSQL 14 or higher.
- Git installed.
- A local PostgreSQL database user with appropriate permissions.

Verify Java:

```bash id="rfjava"
java -version
```

Verify Git:

```bash id="rfgit"
git --version
```

### Installation and Setup

#### 1. Fork the Repository

Fork the original repository to your GitHub account.

#### 2. Clone Your Fork

```bash id="rfclone"
git clone https://github.com/<your-username>/<backend-repo-name>.git
cd <backend-repo-name>
```

#### 3. Install Dependencies

**Linux / macOS:**

```bash id="rfbuild"
./mvnw clean install
```

**Windows PowerShell:**

```powershell id="rfbuildwin"
.\mvnw.cmd clean install
```

### PostgreSQL Setup

#### 1. Create the Database

```bash id="rfcreatedb"
psql -U postgres -c "CREATE DATABASE reserveflow;"
```

#### 2. Enable the Required PostgreSQL Extension

```bash id="rfcreatedbext"
psql -U postgres -d reserveflow -c "CREATE EXTENSION IF NOT EXISTS btree_gist;"
```

#### 3. Apply Database Migrations

The project uses Flyway migration files stored in:

```text id="rfmigrationpath"
src/main/resources/db/migration/
```

The initial migration is:

```text id="rfmigrationname"
V1__init_schema_and_exclusion.sql
```

When Flyway is correctly configured, migrations run automatically during application startup.

> **Note:** Ensure the database user has the required permissions to execute the migration scripts.

---

## Environment Variables

### Important: Required for All Contributors

Every contributor must create a local `.env` file using the `.env.example` template provided in the repository.

The `.env.example` file defines the configuration variables required to run the application.

The `.env` file must contain the actual configuration values for the contributor's local environment.

### 1. Create the .env File

After cloning the repository, locate:

```text id="rfenvtree"
reserveflow-backend/
├── .env.example
└── ...
```

Copy `.env.example` into a new file named `.env`.

**Linux / macOS:**

```bash id="rfenvcopy"
cp .env.example .env
```

**Windows PowerShell:**

```powershell id="rfenvcopywin"
Copy-Item .env.example .env
```

### 2. Configure Your Environment Variables

Open `.env` and replace all placeholder values with your actual configuration.

**Example `.env.example`:**

```dotenv id="rfenvexample"
DB_URL=jdbc:postgresql://localhost:5432/reserveflow
DB_USERNAME=your_database_username
DB_PASSWORD=your_database_password

JWT_SECRET=your_256_bit_secret_signing_key

CORS_ALLOWED_ORIGINS=http://localhost:3000
```

**Example local `.env`:**

```dotenv id="rfenvlocal"
DB_URL=jdbc:postgresql://localhost:5432/reserveflow
DB_USERNAME=postgres
DB_PASSWORD=replace_with_your_actual_password

JWT_SECRET=replace_with_your_secure_random_secret

CORS_ALLOWED_ORIGINS=http://localhost:3000
```

> **Important:** Replace the example passwords and secrets with your actual values. The `.env` file must never contain unused placeholders when starting the application.

### 3. Configure Spring Boot to Use Environment Variables

Spring Boot can access environment variables using placeholders in `application.properties`.

**File:**

`src/main/resources/application.properties`

```properties id="rfprops"
spring.application.name=reserveflow-backend

spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

jwt.secret=${JWT_SECRET}

app.cors.allowed-origins=${CORS_ALLOWED_ORIGINS}

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

spring.flyway.enabled=true

server.port=8080
```

The custom JWT and CORS property names must match those used by the project's configuration classes.

> **Note:** Spring Boot does not automatically load a `.env` file by default. Contributors must either export the variables or use a configured `.env` loading mechanism.

**Linux / macOS:**

For a standard shell-compatible `.env` file:

```bash id="rfenvexport"
set -a
source .env
set +a

./mvnw spring-boot:run
```

**Windows:**

Configure the variables through your IDE's run configuration or use a project-supported environment-loading mechanism.

### 4. Never Commit .env

The `.env` file contains sensitive configuration information and must never be pushed to GitHub.

Ensure `.gitignore` includes:

```gitignore id="rfignore"
.env
.env.*
!.env.example

target/
.idea/
*.iml
```

### 5. Contributor Environment Rules

Every contributor must follow these requirements:

1. Always create `.env` from `.env.example`.
2. Replace all placeholders with actual local values.
3. Ensure PostgreSQL is running and accessible.
4. Use a secure JWT signing secret.
5. Never commit `.env` to GitHub.
6. Never include passwords or secrets in Pull Requests.
7. If a new environment variable is introduced, update `.env.example` with a safe placeholder.
8. Document any newly required environment variables.
9. Keep local secrets private.

---

## Running the Application

### 1. Start PostgreSQL

Ensure the PostgreSQL server is running.

### 2. Verify Environment Configuration

Confirm that the environment variables are configured and available to Spring Boot.

### 3. Start the Backend

**Linux / macOS:**

```bash id="rfrun"
./mvnw spring-boot:run
```

**Windows PowerShell:**

```powershell id="rfrunwin"
.\mvnw.cmd spring-boot:run
```

The backend should start at:

`http://localhost:8080`

The REST API endpoints will be accessible under:

`http://localhost:8080/api`

---

## Available Maven Commands

| Command | Description |
|---------|-------------|
| `./mvnw spring-boot:run` | Starts the Spring Boot application |
| `./mvnw test` | Runs unit and integration tests |
| `./mvnw clean package` | Builds an executable JAR |
| `./mvnw clean install` | Builds, tests, and installs the application locally |
| `./mvnw clean verify` | Runs the full Maven verification lifecycle |

For Windows, use `.\mvnw.cmd` instead of `./mvnw`.

---

## Contributing (Fork & Pull Request)

Contributions to the ReserveFlow Backend are welcome.

All contributors must follow the project structure, environment setup, and Git workflow.

### 1. Fork the Repository

1. Open the original repository on GitHub.
2. Click **Fork**.
3. Create a copy under your GitHub account.

### 2. Clone Your Fork

```bash id="rffork"
git clone https://github.com/<your-username>/<backend-repo-name>.git
cd <backend-repo-name>
```

### 3. Add the Upstream Remote

```bash id="rfupstream"
git remote add upstream https://github.com/<original-owner>/<backend-repo-name>.git
```

Verify remotes:

```bash id="rfremote"
git remote -v
```

### 4. Configure Your Local Environment

Before implementing features, every contributor must:

1. Copy `.env.example` to `.env`.
2. Replace placeholders with actual local values.
3. Create the PostgreSQL database.
4. Ensure the `btree_gist` extension is available.
5. Configure environment variables for Spring Boot.
6. Apply the required database migrations.
7. Start the application successfully.

**Linux / macOS:**

```bash id="rfcontribenv"
cp .env.example .env
```

**Windows PowerShell:**

```powershell id="rfcontribenvwin"
Copy-Item .env.example .env
```

> **Important:** The `.env` file must stay on your local machine. Never include it in commits or Pull Requests.

### 5. Create a Feature Branch

Do not commit directly to `main`.

```bash id="rfbranch"
git checkout -b feature/your-feature-name
```

Examples:

- `feature/resource-management`
- `feature/reservation-service`
- `feature/availability-check`
- `feature/waitlist-promotion`
- `feature/notification-service`
- `test/concurrent-booking`
- `fix/booking-conflict`

### 6. Implement Your Changes

Follow the layered architecture:

```text id="rflayers"
HTTP Request
     |
     v
Controller
     |
     v
Service
     |
     v
Repository
     |
     v
PostgreSQL Database
```

For example, when implementing reservation creation:

1. `ReservationController.java` receives the booking request.
2. `ReservationRequest.java` validates the input.
3. `ReservationService.java` applies booking rules inside a transaction.
4. `AccessControlService.java` verifies permissions where required.
5. `ReservationRepository.java` persists the reservation.
6. PostgreSQL enforces the exclusion constraint.
7. A successful booking returns confirmation.
8. An overlapping booking returns HTTP `409 Conflict`.

### 7. Test Your Changes

Before committing:

- Verify that your endpoints work correctly.
- Ensure request validation is implemented.
- Test authentication and authorization.
- Verify reservation ownership.
- Test simultaneous overlapping booking attempts.
- Confirm that back-to-back bookings are accepted.
- Test cancellation and waitlist promotion.
- Ensure database migrations work correctly.
- Check that existing functionality remains unaffected.

Run:

```bash id="rftest"
./mvnw test
./mvnw clean verify
```

### 8. Commit Your Changes

```bash id="rfcommit"
git add .
git commit -m "feat: add concurrency-safe reservation handling"
```

Recommended commit prefixes:

| Prefix | Purpose |
|--------|---------|
| `feat:` | New functionality |
| `fix:` | Bug fixes |
| `refactor:` | Code restructuring |
| `test:` | Adding or updating tests |
| `docs:` | Documentation changes |
| `chore:` | Maintenance tasks |

### 9. Push Your Feature Branch

```bash id="rfpush"
git push origin feature/your-feature-name
```

### 10. Open a Pull Request

1. Navigate to your forked GitHub repository.
2. Click **Compare & pull request**.
3. Select the original repository's corresponding  branch as the target.
4. Provide a clear description of your changes.
5. Include any database migration changes.
6. Document newly introduced environment variables.
7. Provide sample API requests and responses when relevant.
8. Mention the tests you performed.
9. Ensure your Pull Request does not contain secrets.

---

## Development Guidelines

### 1. Follow the Layered Architecture

Keep controllers, services, repositories, entities, and DTOs separated according to their responsibilities.

### 2. Use Constructor Dependency Injection

Prefer constructor injection for connecting services and repositories.

### 3. Validate All Requests

Use Jakarta Bean Validation for request data and enforce additional business validation in the service layer.

### 4. Use Database Transactions

Reservation creation, cancellation, and waitlist promotion must use appropriate database transactions.

### 5. Enforce Database-Level Concurrency Protection

Do not rely solely on application-level availability checks.

Use the PostgreSQL exclusion constraint to guarantee non-overlapping confirmed reservations.

### 6. Apply Role-Based Access Control

Ensure resource administration is limited to administrators and personal reservation operations are restricted to authorized users.

### 7. Handle Errors Consistently

Use `GlobalExceptionHandler.java` to produce clear error responses.

### 8. Protect Sensitive Information

Never expose:

- Database passwords.
- JWT signing secrets.
- User credentials.
- Environment configuration containing secrets.

### 9. Maintain Database Migrations

All schema changes must be handled through versioned migration files.

Avoid manually modifying shared database schemas without a corresponding migration.

### 10. Update Documentation

When introducing new functionality:

- Update the API documentation.
- Update `.env.example` when new variables are needed.
- Document new database migrations.
- Add relevant tests.
- Keep the project structure documentation accurate.

---

## Project Notes

SpaceSync is designed to manage reservations across different industries while maintaining reliable booking behavior.

The main technical guarantee is that **two overlapping reservations for the same resource cannot both be confirmed**, even when requests arrive simultaneously.

The backend must enforce this guarantee using PostgreSQL database constraints and transaction-safe workflows.

The waiting list system should promote eligible users in FIFO order while ensuring no duplicate confirmations or overlapping bookings are created.
