# E-Commerce Security Project

## Project Purpose
This is an academic demonstration platform focused on e-commerce security. It aims to implement standard e-commerce functionality (catalog, cart, checkout) while prioritizing robust security controls such as secure authentication, RBAC, input validation, and protection against common web vulnerabilities (OWASP Top 10).

## Architecture & Technology Stack
- **Backend:** Java 17, Spring Boot, Spring Web, Spring Data JPA
- **Frontend:** React, TypeScript, Vite
- **Database:** PostgreSQL
- **Infrastructure:** Docker Compose

## Getting Started

### 1. Environment Setup
Copy the example environment file and configure it (do NOT commit the actual `.env` file):
```bash
cp .env.example .env
```

### 2. Start PostgreSQL Database
```bash
docker-compose up -d
```

### 3. Start Backend
Navigate to the `backend` directory and run the Spring Boot application:
```bash
cd backend
mvnw spring-boot:run
```
The backend health endpoint will be available at: http://localhost:8081/api/health

### 4. Start Frontend
Navigate to the `frontend` directory, install dependencies, and start the development server:
```bash
cd frontend
npm install
npm run dev
```
The frontend will be available at: http://localhost:5173
