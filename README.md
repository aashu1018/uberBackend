# Uber-Like Ride-Hailing Application

A Spring Boot backend application that mimics core features of a ride-hailing platform: user auth, rider/driver roles, ride lifecycle, fare calculation, driver matching, payments (wallet & cash), and ratings.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Architecture Overview](#architecture-overview)
- [Domain Model](#domain-model)
- [Features](#features)
- [API Overview](#api-overview)
- [Design Patterns](#design-patterns)
- [Security](#security)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Notes & Conventions](#notes--conventions)

---

## Tech Stack

| Category        | Technology |
|----------------|------------|
| **Runtime**    | Java 21    |
| **Framework**  | Spring Boot 3.4.2 |
| **Data**      | Spring Data JPA, PostgreSQL |
| **Security**  | Spring Security, JWT (JJWT 0.12.6) |
| **Spatial**   | Hibernate Spatial 6.6.4, JTS (Geometry) |
| **Mapping**   | ModelMapper 3.2.1 |
| **API Docs**  | SpringDoc OpenAPI 2.7.0 |
| **Mail**      | Spring Boot Mail |
| **Testing**   | JUnit 5, Testcontainers (PostgreSQL) |
| **Monitoring**| Spring Boot Actuator |

---

## Architecture Overview

- **Layered architecture**: Controllers → Services → Repositories → Entities.
- **REST APIs** with a global response wrapper (`ApiResponse<T>`) and centralized exception handling.
- **Role-based access**: `RIDER`, `DRIVER`, `ADMIN` with method-level security (`@Secured`).
- **Stateless auth**: JWT in `Authorization: Bearer <token>`; refresh token via HTTP-only cookie.
- **Strategy pattern** for fare calculation and driver matching; **Strategy pattern** for payment processing (Wallet vs Cash).

---

## Domain Model

### Core Entities

| Entity | Description |
|--------|-------------|
| **User** | Base identity: name, email, password, roles. Implements `UserDetails` for Spring Security. |
| **Rider** | One-to-one with User; holds aggregate rider rating. |
| **Driver** | One-to-one with User; availability, vehicleId, rating, `currentLocation` (Point). |
| **RideRequest** | Rider’s request: pickup/drop points (Geometry), payment method, fare, status (PENDING/CONFIRMED/CANCELLED). |
| **Ride** | Created when a driver accepts a request: pickup/drop, rider, driver, OTP, fare, status (CONFIRMED/ONGOING/ENDED/CANCELLED), timestamps. |
| **Payment** | Tied to a Ride; payment method, amount, status (PENDING/CONFIRMED/REFUNDED). |
| **Wallet** | One per User; balance; has many `WalletTransaction`. |
| **WalletTransaction** | Credit/Debit entries with amount, transaction method (RIDE/BANKING), optional ride and transactionId. |
| **Rating** | Per ride: driver rating (by rider) and rider rating (by driver). |

### Enums

- **Role**: `ADMIN`, `RIDER`, `DRIVER`
- **RideStatus**: `CANCELLED`, `CONFIRMED`, `ONGOING`, `ENDED`
- **RideRequestStatus**: `PENDING`, `CANCELLED`, `CONFIRMED`
- **PaymentMethod**: `CASH`, `WALLET`
- **PaymentStatus**: `PENDING`, `CONFIRMED`, `REFUNDED`
- **TransactionType**: `CREDIT`, `DEBIT`
- **TransactionMethod**: `BANKING`, `RIDE`

### Spatial Data

- Pickup/drop and driver location use **JTS `Point`** with **SRID 4326** (WGS84).
- PostgreSQL stores them as `Geometry(Point, 4326)`.
- Driver matching uses PostGIS functions: `ST_Distance`, `ST_DWithin`.

---

## Features

### Authentication & Onboarding

- **Signup**: New user gets `RIDER` role; a `Rider` profile and a `Wallet` are created.
- **Login**: Returns access token (body) and sets refresh token in HTTP-only cookie.
- **Refresh**: Uses cookie `refreshToken` to issue a new access token.
- **Driver onboarding**: Existing user (by `userId`) can be onboarded as driver (vehicleId); gains `DRIVER` role and a `Driver` record.

### Ride Lifecycle

1. **Request** (Rider): Creates `RideRequest` with pickup/drop (Geometry), payment method. Fare is computed by **fare strategy** (default or surge). **Driver matching strategy** returns a list of matching drivers (nearest or top-rated by rider rating). *(Notification to drivers is marked TODO.)*
2. **Accept** (Driver): Validates request is PENDING and driver is available; creates `Ride` with OTP, sets request to CONFIRMED, marks driver unavailable.
3. **Start** (Driver): Validates OTP and ride status CONFIRMED; sets ride to ONGOING, creates `Payment` and `Rating` records.
4. **End** (Driver): Sets ride to ENDED, marks driver available, **processes payment** via the chosen payment strategy.
5. **Cancel**: Rider or driver can cancel in appropriate status; driver availability is restored when applicable.

### Fare Calculation (Strategy)

- **Default**: `distance × RIDE_FARE_MULTIPLIER` (distance from OSRM).
- **Surge**: Same formula × `SURGE_FACTOR` (2) when current time is in surge window (e.g. 18:00–21:00).
- Distance is computed by **DistanceService** (OSRM implementation: driving route between two points).

### Driver Matching (Strategy)

- **Nearest**: Up to 10 available drivers within 10 km, ordered by distance (`findTenNearestDrivers`).
- **Top-rated**: Up to 10 available drivers within 15 km, ordered by rating (`findTenNearByTopRatedDrivers`).
- Choice: rider rating ≥ 4.8 → top-rated strategy; otherwise → nearest.

### Payments (Strategy)

- **Wallet**: Rider’s wallet debited by fare; driver’s wallet credited with `fare × (1 - PLATFORM_COMMISSION)` (commission 30%). Payment status set to CONFIRMED.
- **Cash**: Driver receives cash; platform commission (30%) is debited from driver’s wallet. Payment status set to CONFIRMED.

### Ratings

- After ride ends, rider can rate driver and driver can rate rider.
- Ratings are stored on `Rating`; aggregate rating for driver/rider is recomputed as average of all ratings.

### Wallet

- Created on signup. Used for ride payments (wallet method), platform commission (cash rides), and credits (e.g. driver share).
- Every credit/debit is recorded as a `WalletTransaction`.

---

## API Overview

- **Base**: REST; JSON.
- **Auth**: `/auth/**` is public; all other endpoints require JWT in `Authorization: Bearer <token>`.
- **Roles**: `/rider/**` → `ROLE_RIDER`; `/drivers/**` → `ROLE_DRIVER`.

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST   | `/auth/signup` | Public | Register new rider |
| POST   | `/auth/onboardNewDriver/{userId}` | Public | Onboard driver |
| POST   | `/auth/login` | Public | Login; returns access token, sets refresh cookie |
| POST   | `/auth/refresh` | Public (cookie) | New access token from refresh cookie |
| POST   | `/rider/requestRide` | Rider | Create ride request |
| POST   | `/rider/cancelRide/{rideId}` | Rider | Cancel ride |
| POST   | `/rider/rateDriver` | Rider | Rate driver for a ride |
| GET    | `/rider/getMyProfile` | Rider | Current rider profile |
| GET    | `/rider/getMyRides` | Rider | Paginated ride history |
| POST   | `/drivers/acceptRide/{rideRequestId}` | Driver | Accept request → create ride |
| POST   | `/drivers/startRide/{rideRequestId}` | Driver | Start ride (OTP in body); *path param is effectively ride id* |
| POST   | `/drivers/endRide/{rideId}` | Driver | End ride and process payment |
| POST   | `/drivers/cancelRide/{rideId}` | Driver | Cancel ride |
| POST   | `/drivers/rateRider` | Driver | Rate rider for a ride |
| GET    | `/drivers/getMyProfile` | Driver | Current driver profile |
| GET    | `/drivers/getMyRides` | Driver | Paginated ride history |

- **API docs**: SpringDoc OpenAPI (e.g. `/v3/api-docs`, Swagger UI) and **Actuator** endpoints are excluded from the global `ApiResponse` wrapper.

---

## Design Patterns

- **Strategy**: `RideFareCalculationStrategy` (default vs surge), `DriverMatchingStrategy` (nearest vs top-rated), `PaymentStrategy` (wallet vs cash). Managers: `StrategyManager`, `PaymentStrategyManager`.
- **Repository**: JPA repositories for all entities; custom spatial queries in `DriverRepository`.
- **DTO**: Request/response DTOs (e.g. `SignupDTO`, `LoginRequestDTO`, `RideRequestDTO`, `RideDTO`, `RatingDTO`) with ModelMapper for entity ↔ DTO mapping.
- **Global exception handling**: `@RestControllerAdvice` with `GlobalExceptionHandler` for `ResourceNotFoundException`, `RuntimeConflictException`, validation errors, JWT and auth exceptions; responses use `ApiResponse` with `ApiError`.
- **Response wrapping**: `GlobalResponseHandler` wraps successful responses in `ApiResponse<T>` except for docs/actuator.

---

## Security

- **WebSecurityConfig**: Stateless session; CSRF disabled; `/auth/**` permitted; all other requests authenticated; `JWTAuthFilter` before `UsernamePasswordAuthenticationFilter`.
- **JWTAuthFilter**: Reads `Authorization: Bearer <token>`, validates JWT, loads `User` and sets `SecurityContext`.
- **JWT**: Access token (e.g. 10 min), refresh token (e.g. 6 months); subject = user id; secret from `jwt.secretKey`.
- **Password**: BCrypt via `PasswordEncoder` bean.
- **Method security**: `@Secured("ROLE_RIDER")` / `@Secured("ROLE_DRIVER")` on rider and driver controllers.

---

## Configuration

- **Profiles**: `application.properties` (base), `application-dev.properties`, `application-prod.properties`.
- **Required**: DB URL, username, password; `jwt.secretKey` (e.g. in dev profile); optional SMTP for mail.
- **JPA**: Dialect `PostgreSQLDialect`; `ddl-auto` (e.g. `create-drop` in dev); dev can use `data.sql` for initial data.
- **Actuator**: Included; adjust exposure in production.

---

## Running the Application

1. **Java 21** and **Maven** installed.
2. **PostgreSQL** with PostGIS (for spatial columns and queries).
3. Clone and configure:
   - Set `spring.datasource.url`, `username`, `password` in `application.properties` or profile.
   - Set `jwt.secretKey` (min length per JJWT; e.g. in `application-dev.properties`).
4. Run:
   ```bash
   mvn spring-boot:run
   ```
   Or activate profile:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```
5. Use login/signup to get JWT; call rider/driver APIs with `Authorization: Bearer <access_token>`.

---

## Testing

- **Testcontainers**: PostgreSQL container for integration tests.
- **Tests**: e.g. `AuthControllerTest`, `AuthServiceImplTest`, `UberApplicationTests`, `TestContainerConfiguration`.
- Run: `mvn test`.

---

## Project Structure

```
src/main/java/com/project/uber/uberApplication/
├── UberApplication.java
├── advices/           # ApiResponse, ApiError, GlobalExceptionHandler, GlobalResponseHandler
├── configs/           # WebSecurityConfig, SecurityConfig
├── controllers/       # AuthController, RiderController, DriverController
├── dto/               # Request/response DTOs
├── entities/          # JPA entities + enums
├── exceptions/        # ResourceNotFoundException, RuntimeConflictException
├── repositories/      # JPA repositories (Driver has spatial queries)
├── security/          # JWTService, JWTAuthFilter
├── services/          # Interfaces and impl (Auth, Rider, Driver, Ride, RideRequest, Payment, Wallet, Rating, Distance, EmailSender, etc.)
└── strategies/        # Fare, driver matching, payment strategies + managers
```

---

## Notes & Conventions

- **Current user in dev**: `RiderServiceImpl.getCurrentRider()` and `DriverServiceImpl.getCurrentDriver()` may be hardcoded (e.g. rider id 1, driver id 2) for development; production should use `SecurityContextHolder` to get the authenticated user and resolve rider/driver from it.
- **Start ride API**: `POST /drivers/startRide/{rideRequestId}` takes the **ride id** (returned from accept ride) in the path; the parameter name is misleading and could be renamed to `rideId` for clarity.
- **DriverRepository**: Native queries use `current_location` (snake_case); ensure entity/database column naming is consistent (e.g. `currentLocation` mapped to `current_location`).
- **OSRM**: Distance service uses public OSRM API; consider rate limits and fallbacks for production.
- **Fare/surge window**: Surge times (e.g. 18:00–21:00) are in `StrategyManager`; consider making them configurable.

---

## License

Demo/educational project; no license specified in repo.
