# 🚖 Uber-Like Ride Booking Backend (Spring Boot - Monolithic Architecture)

A fully functional **ride booking backend system** modeled after Uber, built using **Spring Boot** in a **monolithic architecture**. This project simulates the backend of an online cab booking service, handling authentication, ride management, payment processing, and more.

---

## 🗂️ System Design

### 🔧 Architecture Overview

This project follows a monolithic architecture with clean separations between services like:

- **UserService** – Manages authentication and user operations
- **RiderService / DriverService** – Handle ride lifecycle events
- **RideRequestService** – Core ride matching engine using strategy pattern
- **Wallet, Rating, Payment** – Supporting components
- **EmailService** – Sends notifications to drivers
- **Strategy Pattern** – Used for ride matching and fare calculation logic

📌 **Design Flow:**

![Design Flow](./8351816e-b751-461b-b989-1b180be8678a.png)

📌 **UML Overview:**

![UML Diagram](./510ff026-ad1e-4c9c-a581-5cbf26452210.png)

---

## 💡 Features

### 👤 **User Module**
- `signUp`, `logIn`

### 🛠 **Admin Module**
- `onboardDriver`
- `getAllRides`

### 🚘 **Rider Module**
- `requestRide`, `cancelRide`, `rateDriver`, `addFunds`
- `getAllRides`

### 🚖 **Driver Module**
- `acceptRide`, `startRide`, `cancelRide`, `endRide`
- `rateRider`, `debitFunds`
- `getAllRides`

### 🔁 **System Tasks**
- Update location
- Send ride request email to drivers

---

## ⚙️ Design Patterns Implemented

| Pattern            | Used For                                     |
|--------------------|-----------------------------------------------|
| **Strategy Pattern**  | Driver Matching (`Nearest`, `RatingBased`), Fare Calculation (`Surge`, `Default`) |
| **Builder Pattern**   | Creating immutable objects like RideRequest |
| **Factory Pattern**   | Instantiating strategies dynamically        |
| **Singleton Pattern** | Shared services (e.g., Email Service)       |

---

## 🧪 Tech Stack

- **Java 17**
- **Spring Boot**
- **Spring Data JPA**
- **Swagger UI**
- **Maven**
- **H2 / MySQL / PostgreSQL**

---

## 📂 API Endpoints Summary

| Role   | Endpoints                                                   |
|--------|-------------------------------------------------------------|
| **User**   | `/signup`, `/login`, `/logout`                             |
| **Admin**  | `/onboardDriver`, `/getAllRides`                           |
| **Rider**  | `/requestRide`, `/cancelRide`, `/rateDriver`, `/addFunds` |
| **Driver** | `/acceptRide`, `/startRide`, `/endRide`, `/rateRider`, `/debitFunds` |
| **System** | `/updateLocation`, Email notification on ride request     |

---

# Swagger UI
http://localhost:8080/swagger-ui/
