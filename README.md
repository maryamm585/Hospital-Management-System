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
## Setup & Run

### Clone the Repository
git clone https://github.com/maryamm585/Hospital-Management-System.git
cd Hospital-Managment-System

### Configure Database

spring.datasource.url=jdbc:mysql://localhost:3306/hospital_db
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

### Build & Run

mvn clean install
mvn spring-boot:run

### Access API
Server runs on: http://localhost:8080

