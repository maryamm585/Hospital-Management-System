# Hospital-Management-System

A Spring Boot–based Hospital Management System that provides APIs for managing users, doctors, appointments, orders, and medical operations.  
This project follows a layered architecture with **Entities, DTOs, Mappers, Repositories, Services, and Controllers**.

---
## Tech Stack
- Java 17+
- Spring Boot 3.x
- Spring Data JPA (Hibernate)
- Spring Security + JWT
- MySQL
- Docker & Docker Compose
- Lombok
- Jakarta Validation
---
## 📂 Project Structure
Hospital.system/
│── src/main/java/Hospital/system
│   ├── Entity/        # JPA Entities
│   ├── DTO/           # Data Transfer Objects
│   ├── Mapper/        # MapStruct mappers
│   ├── Repository/    # Spring Data JPA repositories
│   ├── Service/       # Business logic
│   ├── Controller/    # REST Controllers
│   └── Security/      # JWT + Spring Security
│
│── src/main/resources
│   ├── application.yml
│   └── data.sql
│
│── Dockerfile
│── docker-compose.yml
│── pom.xml
│── README.md

---

## 🚀 Features
- User authentication & role management (Admin, Doctor, Patient).
- Appointment scheduling & management.
- Order management with items & total price.
- Medical operations tracking.
- DTOs for clean API communication.
- Validation & exception handling.

---

## Entities
- User – Represents system users (patients, doctors, etc..).
- Role (enum) – Defines user roles within the system (e.g., DOCTOR, PATIENT).
- Appointment – Represents medical appointments between patients and doctors.
- AppointmentStatus (enum) – Defines appointment lifecycle states (e.g., PENDING, CONFIRMED, CANCELLED, COMPLETED).
- PatientRecord – Stores medical history and health details of patients.
- Prescription – Represents medications prescribed by doctors.
- Medicine – Stores information about available medicines.
- Order – Represents medicine orders placed by patients.
- OrderItem – Represents individual medicines in an order.
- OrderStatus (enum) – Defines different states of an order (e.g., PLACED, SHIPPED, DELIVERED, CANCELLED).
- Message – Represents messages exchanged between doctor and patient.

  
---
## Setup & Run

### Clone the Repository : git clone https://github.com/maryamm585/Hospital-Management-System.git
cd Hospital-Managment-System
### Configure Database
### Build & Run
### Access API
Server runs on: http://localhost:8080

  ---

## Security Configuration

Added system security files for the Hospital Management System with 4 roles: **ADMIN**, **DOCTOR**, **PATIENT**, and **PHARMACY**.

### Files:

#### 1. Security Layer

- **TokenBlacklistService.java**
- **JwtUtil.java**
- **JwtAuthFilter.java**
- **CustomUserDetailsService.java**
- **SecurityConfig.java**

#### 2. Controller Layer

- **AuthController.java**

#### 3. Service Layer

- **AuthService.java**

### Role-Based Access Control

The SecurityConfig now includes endpoint protection for all 4 roles:

#### ADMIN

- `/api/admin/**` - Full admin access
- `/api/users/**` - User management

#### DOCTOR

- `/api/doctors/**` - Doctor-specific endpoints
- `/api/appointments/doctor/**` - Doctor appointment management
- `/api/prescriptions/doctor/**` - Prescription management
- `/api/patient-records/doctor/**` - Patient record access
- `/api/messages/doctor/**` - Doctor-patient messaging

#### PATIENT

- `/api/patients/**` - Patient-specific endpoints
- `/api/appointments/patient/**` - Patient appointment booking
- `/api/orders/patient/**` - Medicine ordering
- `/api/messages/patient/**` - Patient-doctor messaging

#### PHARMACY

- `/api/pharmacy/**` - Pharmacy-specific endpoints
- `/api/medicines/**` - Medicine CRUD operations
- `/api/orders/pharmacy/**` - Order management

#### Shared Endpoints

- `/api/appointments` - Accessible by DOCTOR and PATIENT
- `/api/messages` - Accessible by DOCTOR and PATIENT
- `/api/medicines/list` - Viewable by PATIENT, DOCTOR, and PHARMACY

### API Endpoints

#### Authentication Endpoints (Public)

```
POST /api/auth/register - Register new user
POST /api/auth/login - User login
POST /api/auth/logout - User logout
```

### Usage Example

#### Registration Request:

```json
{
  "name": "Dr. John Smith",
  "email": "john.smith@hospital.com",
  "password": "password123",
  "role": "DOCTOR"
}
```

#### Login Request:

```json
{
  "email": "john.smith@hospital.com",
  "password": "password123"
}
```

#### Response Format:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "john.smith@hospital.com",
  "role": "DOCTOR",
  "message": "Login successful"
}
```


  
