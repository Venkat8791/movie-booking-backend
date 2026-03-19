# 🎬 MovieBooking — BookMyShow Clone

A full-stack movie ticket booking system built with **React**, **Spring Boot**, and **PostgreSQL**, following a modular monolith architecture designed for easy microservice extraction.

---

## 🏗️ Architecture

### Modular Monolith
The backend is structured as a multi-module Maven project where each domain is self-contained — controllers, services, repositories, entities and DTOs all live within their own module. Cross-module references use UUIDs instead of JPA relationships to keep modules decoupled.

```
moviebooking/
├── auth/          → User registration, login, JWT authentication
├── movie/         → Movie catalogue and search
├── theatre/       → Theatres, screens and seat management
├── show/          → Shows and seat availability
└── booking/       → Bookings and payment status
```

---

## 🛠️ Tech Stack

| Layer      | Technology                          |
|------------|-------------------------------------|
| Frontend   | React JS                            |
| Backend    | Spring Boot 3.4.x (Java 17)         |
| Database   | PostgreSQL                          |
| Auth       | Spring Security + JWT (JJWT 0.12.6) |
| Migrations | Liquibase                           |
| ORM        | Spring Data JPA / Hibernate         |
| Build      | Maven (multi-module)                |

---

## 🗄️ Database Schema

7 tables designed for a production-grade booking system:

```
users           → registered users with roles (USER, ADMIN)
theatres        → theatre master data
screens         → screens within a theatre (2D, 3D, IMAX)
seats           → seat map per screen with types and price multipliers
movies          → movie catalogue
shows           → a movie on a screen at a specific time
bookings        → a user's booking for a show
show_seats      → lazy seat inventory (only booked/pending rows exist)
```

### Key Design Decisions

- **Lazy `show_seats`** — rows are only inserted when a seat is claimed, not prepopulated. Absence of a row means the seat is available. Solves the double-booking race condition via `UNIQUE(show_id, seat_id)` + `INSERT ON CONFLICT DO NOTHING`.
- **UUID primary keys** — all tables use UUID PKs for security (no enumerable IDs) and future distributed system compatibility.
- **Modular entity isolation** — cross-module references store only UUIDs, not JPA `@ManyToOne` relationships, keeping modules truly independent.
- **Price calculation** — `final_price = show.base_price × seat.price_multiplier`, allowing per-seat-type pricing (REGULAR, PREMIUM, RECLINER) on top of a base show price.

---

## 🔐 Authentication

JWT-based stateless authentication:

- Passwords hashed with **BCrypt**
- Tokens signed with **HMAC-SHA256**
- Token expiry configurable via `jwt.expiration` property
- Role-based access control — `ROLE_USER` for customers, `ROLE_ADMIN` for management endpoints

### Auth Endpoints

```
POST /api/auth/register    → create account
POST /api/auth/login       → returns JWT token
```

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+

### 1. Clone the repository

```bash
git clone https://github.com/yourusername/moviebooking.git
cd moviebooking
```

### 2. Create the database

```sql
CREATE DATABASE bookmyshow;
```

### 3. Configure environment

Update `auth/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/bookmyshow
spring.datasource.username=your_db_username
spring.datasource.password=your_db_password

jwt.secret=your_super_secret_key_minimum_32_characters
jwt.expiration=86400000
```

### 4. Run the application

```bash
# From root directory
mvn clean install

# Run the auth module (main entry point)
cd auth
mvn spring-boot:run
```

Liquibase will automatically create all tables on first run.

### 5. Verify

```bash
# Register a user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "phone": "9876543210",
    "password": "password123",
    "confirmPassword": "password123"
  }'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

---

## 📁 Project Structure

```
moviebooking/
├── pom.xml                          ← parent pom with dependencyManagement
├── auth/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/mxmovies/auth/
│       │   ├── config/              ← SecurityConfig
│       │   ├── controller/          ← AuthController
│       │   ├── dto/
│       │   │   ├── request/         ← RegisterRequest, LoginRequest
│       │   │   └── response/        ← AuthResponse, UserResponse
│       │   ├── entity/              ← User, Role
│       │   ├── exception/           ← GlobalExceptionHandler
│       │   ├── repository/          ← UserRepository
│       │   ├── security/            ← JwtUtil, JwtAuthFilter, CustomUserDetailsService
│       │   └── service/             ← AuthService
│       └── resources/
│           ├── application.properties
│           └── db/changelog/
│               ├── db.changelog-master.yaml
│               └── changes/ddl/
│                   ├── 001-create-theatres.sql
│                   ├── 002-create-screens.sql
│                   ├── 003-create-users.sql
│                   ├── 004-create-movies.sql
│                   ├── 005-create-seats.sql
│                   ├── 006-create-shows.sql
│                   ├── 007-create-bookings.sql
│                   ├── 008-create-show-seats.sql
│                   └── 009-alter-users-add-role.sql
├── movie/
├── theatre/
├── show/
└── booking/
```

---

## 📋 API Overview

### Auth
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/auth/register` | Public | Register new user |
| POST | `/api/auth/login` | Public | Login, returns JWT |

### Theatre *(coming soon)*
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/theatres` | Admin | Create theatre |
| GET | `/api/theatres` | Public | List all theatres |
| GET | `/api/theatres/city/{city}` | Public | Theatres by city |
| POST | `/api/theatres/{id}/screens` | Admin | Add screen |

### Movies *(coming soon)*
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/movies` | Admin | Add movie |
| GET | `/api/movies` | Public | List all movies |
| GET | `/api/movies/search?title=` | Public | Search by title |

### Shows *(coming soon)*
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/shows` | Admin | Create show |
| GET | `/api/shows?movieId=` | Public | Shows by movie |
| GET | `/api/shows/{id}/seats` | Public | Seat availability |

### Bookings *(coming soon)*
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/bookings` | User | Create booking |
| GET | `/api/bookings/my` | User | My bookings |
| PUT | `/api/bookings/{id}/cancel` | User | Cancel booking |

---

## 🔮 Planned Features

- [ ] Theatre module (theatres, screens, seats)
- [ ] Movie module (catalogue, search)
- [ ] Show module (scheduling, seat availability)
- [ ] Booking module (seat selection, concurrency handling)
- [ ] MongoDB integration for seat layout rendering
- [ ] Social login (Google OAuth2)
- [ ] Payment gateway integration
- [ ] React frontend

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/theatre-module`)
3. Commit your changes (`git commit -m 'Add theatre module'`)
4. Push to the branch (`git push origin feature/theatre-module`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License.
