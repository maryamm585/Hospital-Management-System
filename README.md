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

  
