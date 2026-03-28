# Cloud Compute Service - Operational Playbook

This document provides a comprehensive step-by-step guide to set up, configure, and run the Cloud Compute Service application from scratch.

**Related Documentation:**
- **[test_execution_guide.md](test_execution_guide.md)** — Complete manual testing procedures with 7 phases, 34 test cases, and critical user isolation verification (START HERE FOR TESTING)
- **[realtime_demo_playbook.md](realtime_demo_playbook.md)** — 12-minute timed demonstration script for live presentation
- **[README.md](README.md)** — Quick-start guide and API overview

---

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Project Structure](#project-structure)
3. [Environment Setup](#environment-setup)
4. [Database Configuration](#database-configuration)
5. [Building the Application](#building-the-application)
6. [Running the Application](#running-the-application)
7. [API Endpoints Reference](#api-endpoints-reference)
8. [User Authentication Flow](#user-authentication-flow)
9. [Task Management Flow](#task-management-flow)
10. [Testing & Verification](#testing--verification)
11. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software
- **Java Development Kit (JDK)** — Version 17 or later
  - Download: https://adoptopenjdk.net/
  - Verify: `java -version` (output should show version 17+)

- **Apache Maven** — Version 3.8.0 or later
  - Download: https://maven.apache.org/download.cgi
  - Verify: `mvn -version` (output should show Maven 3.8.0+)

- **Git** — For cloning the repository
  - Download: https://git-scm.com/
  - Verify: `git --version`

- **MySQL** or **PostgreSQL** (for production only; H2 in-memory used for development)
  - MySQL 8.0+: https://dev.mysql.com/downloads/mysql/
  - PostgreSQL 12+: https://www.postgresql.org/download/

### System Requirements
- **Disk Space:** Minimum 1 GB free space
- **RAM:** Minimum 2 GB RAM available
- **Network:** Internet connection for Maven dependency download

### OS Support
- Linux (Ubuntu 20.04+, CentOS 7+)
- macOS 10.14+
- Windows 10+ (with Git Bash or PowerShell)

---

## Project Structure

```
COMP4442-semester-project-Group14/
├── src/
│   ├── main/
│   │   ├── java/hk/polyu/comp4442/cloudcompute/
│   │   │   ├── CloudComputeServiceApplication.java       (Main entry point)
│   │   │   ├── config/SecurityConfig.java                (Spring Security config)
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java                   (Auth endpoints)
│   │   │   │   ├── TaskController.java                   (Task CRUD endpoints)
│   │   │   │   └── ComputeController.java                (Compute endpoints)
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java                      (Auth business logic)
│   │   │   │   ├── TaskService.java                      (Task business logic)
│   │   │   │   └── ComputeService.java                   (Compute logic)
│   │   │   ├── repository/
│   │   │   │   ├── AppUserRepository.java                (User JPA repository)
│   │   │   │   └── TaskRepository.java                   (Task JPA repository)
│   │   │   ├── entity/
│   │   │   │   ├── AppUser.java                          (User entity)
│   │   │   │   ├── Task.java                             (Task entity)
│   │   │   │   └── TaskStatus.java                       (Task status enum)
│   │   │   ├── security/
│   │   │   │   ├── CustomUserDetails.java                (User details impl)
│   │   │   │   └── AppUserDetailsService.java            (User details service)
│   │   │   ├── dto/
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── AuthUserResponse.java
│   │   │   │   ├── AuthResponse.java
│   │   │   │   ├── CreateTaskRequest.java
│   │   │   │   ├── TaskResponse.java
│   │   │   │   ├── CalculateRequest.java
│   │   │   │   └── CalculateResponse.java
│   │   │   └── exception/
│   │   │       ├── GlobalExceptionHandler.java           (Exception handler)
│   │   │       ├── ApiError.java                         (Error response)
│   │   │       └── TaskNotFoundException.java            (Custom exception)
│   │   └── resources/
│   │       ├── application.properties                    (Common config)
│   │       ├── application-dev.properties                (Dev H2 config)
│   │       ├── application-prod.properties               (Prod MySQL/PostgreSQL config)
│   │       └── static/
│   │           ├── index.html                            (Home page)
│   │           ├── login.html                            (Login page)
│   │           ├── register.html                         (Registration page)
│   │           ├── task.html                             (Task management page)
│   │           └── edit.html                             (Task edit page)
│   └── test/
│       └── java/hk/polyu/comp4442/cloudcompute/
│           ├── CloudComputeServiceApplicationTests.java
│           └── TaskUiAndApiIntegrationTests.java         (Integration tests)
├── target/                                                (Build output directory)
├── pom.xml                                                (Maven configuration)
├── README.md                                              (Project overview)
├── playbook.md                                            (This file)
├── plan.md                                                (Project plan)
├── process log.md                                         (Activity log)
└── plan for project.md                                    (Task split plan)
```

---

## Environment Setup

### Step 1: Clone the Repository
```bash
git clone https://github.com/domna735/COMP4442-semester-project-Group14.git
cd COMP4442-semester-project-Group14
```

### Step 2: Verify Java Installation
```bash
java -version
# Expected output: openjdk version "17" or later
```

### Step 3: Verify Maven Installation
```bash
mvn -version
# Expected output: Apache Maven 3.8.0 or later
```

### Step 4: Set JAVA_HOME Environment Variable (if not already set)

**On Linux/macOS:**
```bash
export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
echo $JAVA_HOME  # Verify it's set
```

**On Windows:**
```cmd
setx JAVA_HOME "C:\Program Files\Java\jdk-17"
```
Then restart your terminal.

### Step 5: Create Project Directory Structure (if needed)
```bash
# The repository already contains the structure, but verify:
ls -la src/main/java/hk/polyu/comp4442/cloudcompute/
ls -la src/main/resources/
```

---

## Database Configuration

### Development Environment (H2 In-Memory)

**Default Configuration** — No setup required. H2 is embedded in the Spring Boot dependency.

**Profile:** `dev` (set as default in `application.properties`)

**Properties File:** `src/main/resources/application-dev.properties`
```properties
spring.datasource.url=jdbc:h2:mem:cloudcompute;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

**H2 Console Access:**
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:cloudcompute`
- Username: `sa`
- Password: (leave blank)

---

### Production Environment (MySQL or PostgreSQL)

#### Option A: MySQL 8.0+

**Step 1: Create MySQL Database**
```bash
mysql -u root -p
```

**Step 2: In MySQL Console:**
```sql
CREATE DATABASE cloudcompute;
CREATE USER 'cc_user'@'localhost' IDENTIFIED BY 'cc_password_123';
GRANT ALL PRIVILEGES ON cloudcompute.* TO 'cc_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

**Step 3: Configure Environment Variables**
```bash
export DB_URL="jdbc:mysql://localhost:3306/cloudcompute"
export DB_USERNAME="cc_user"
export DB_PASSWORD="cc_password_123"
export DB_DRIVER_CLASS_NAME="com.mysql.cj.jdbc.Driver"
export SPRING_PROFILES_ACTIVE="prod"
```

**Step 4: Verify Connection**
```bash
mysql -u cc_user -p -h localhost -e "use cloudcompute; show tables;"
```

---

#### Option B: PostgreSQL 12+

**Step 1: Create PostgreSQL Database**
```bash
sudo -u postgres psql
```

**Step 2: In PostgreSQL Console:**
```sql
CREATE DATABASE cloudcompute;
CREATE USER cc_user WITH PASSWORD 'cc_password_123';
ALTER ROLE cc_user SET client_encoding TO 'utf8';
ALTER ROLE cc_user SET default_transaction_isolation TO 'read committed';
ALTER ROLE cc_user SET default_transaction_deferrable TO on;
ALTER ROLE cc_user SET timezone TO 'UTC';
GRANT ALL PRIVILEGES ON DATABASE cloudcompute TO cc_user;
\c cloudcompute
GRANT ALL ON SCHEMA public TO cc_user;
\q
```

**Step 3: Configure Environment Variables**
```bash
export DB_URL="jdbc:postgresql://localhost:5432/cloudcompute"
export DB_USERNAME="cc_user"
export DB_PASSWORD="cc_password_123"
export DB_DRIVER_CLASS_NAME="org.postgresql.Driver"
export SPRING_PROFILES_ACTIVE="prod"
```

**Step 4: Verify Connection**
```bash
psql -U cc_user -h localhost -d cloudcompute -c "SELECT version();"
```

---

## Building the Application

### Step 1: Clean Previous Build
```bash
mvn clean
```

### Step 2: Download Dependencies
```bash
mvn dependency:resolve
```

### Step 3: Compile Java Code
```bash
mvn compile
```

### Step 4: Run Unit and Integration Tests
```bash
mvn test
# Expected output: BUILD SUCCESS (6 tests passed, 0 failed)
```

### Step 5: Build Application Package
```bash
mvn package -DskipTests
# Or with tests:
mvn package
```

**Output:**
- JAR file: `target/cloud-compute-service-0.0.1-SNAPSHOT.jar`
- Size: Approximately 50-60 MB

### Full Build Command (One-Line)
```bash
mvn clean dependency:resolve compile test package
```

---

## Running the Application

### Option 1: Run from IDE (Spring Boot)

#### Using Spring Tool Suite (STS) or IntelliJ IDEA:
1. Open project in IDE
2. Right-click on `CloudComputeServiceApplication.java`
3. Select "Run As" > "Spring Boot App"
4. Application starts on http://localhost:8080

---

### Option 2: Run from Command Line (Development)

**Command:**
```bash
mvn spring-boot:run
```

**Expected Output:**
```
[INFO] --- spring-boot-maven-plugin:3.3.5:run (default-cli) @ cloud-compute-service ---
[INFO] Attaching agents: []
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::               (v3.3.5)

2026-03-28 10:30:17.123  INFO 12345 --- [main] h.p.c.c.CloudComputeServiceApplication   : Starting CloudComputeServiceApplication using Java 17.0.1 on hostname with PID 12345
...
2026-03-28 10:30:20.456  INFO 12345 --- [main] h.p.c.c.CloudComputeServiceApplication   : Started CloudComputeServiceApplication in 3.234 seconds (JVM running for 3.567)
```

**Application Ready:**
- Base URL: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console (dev profile only)

---

### Option 3: Run JAR File (Production)

**Command:**
```bash
java -jar target/cloud-compute-service-0.0.1-SNAPSHOT.jar
```

**With Custom Profiles and Logging:**
```bash
java -Dspring.profiles.active=prod \
     -Dserver.port=8080 \
     -DLOG_LEVEL=INFO \
     -jar target/cloud-compute-service-0.0.1-SNAPSHOT.jar
```

---

### Option 4: Run with System Service (systemd on Linux)

**Step 1: Create Service File**
```bash
sudo nano /etc/systemd/system/cloud-compute.service
```

**Step 2: Add Configuration**
```ini
[Unit]
Description=Cloud Compute Service
After=network.target

[Service]
Type=simple
User=deployuser
WorkingDirectory=/opt/cloud-compute
ExecStart=/usr/bin/java -jar /opt/cloud-compute/cloud-compute-service-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

**Step 3: Enable and Start**
```bash
sudo systemctl enable cloud-compute
sudo systemctl start cloud-compute
sudo systemctl status cloud-compute
```

**Check Logs:**
```bash
sudo journalctl -u cloud-compute -f
```

---

## API Endpoints Reference

### Authentication Endpoints

#### 1. Register New User
```
POST /api/v1/auth/register
Content-Type: application/json

Request Body:
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123!"
}

Response (201 Created):
{
  "message": "User registered successfully",
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "role": "USER"
  },
  "timestamp": "2026-03-28T10:30:45.123Z"
}

Error (400 Bad Request):
{
  "code": "VALIDATION_ERROR",
  "message": "Username 'john_doe' already exists",
  "timestamp": "2026-03-28T10:30:45.123Z"
}
```

#### 2. Login User
```
POST /api/v1/auth/login
Content-Type: application/json

Request Body:
{
  "username": "john_doe",
  "password": "SecurePass123!"
}

Response (200 OK):
{
  "message": "Login successful",
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "role": "USER"
  },
  "timestamp": "2026-03-28T10:30:45.123Z"
}

Error (401 Unauthorized):
{
  "code": "AUTHENTICATION_FAILED",
  "message": "Invalid credentials",
  "timestamp": "2026-03-28T10:30:45.123Z"
}
```

#### 3. Get Current User Info
```
GET /api/v1/auth/me
Authorization: (Session cookie)

Response (200 OK):
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "role": "USER"
}

Error (401 Unauthorized):
{
  "code": "UNAUTHENTICATED",
  "message": "No active session",
  "timestamp": "2026-03-28T10:30:45.123Z"
}
```

#### 4. Logout User
```
POST /api/v1/auth/logout
Authorization: (Session cookie)

Response (200 OK):
{
  "message": "Logged out successfully",
  "timestamp": "2026-03-28T10:30:45.123Z"
}
```

---

### Task Management Endpoints

#### 1. Create Task
```
POST /api/v1/tasks
Content-Type: application/json
Authorization: (Session cookie - Required)

Request Body:
{
  "title": "Implement feature X",
  "description": "Build the authentication module",
  "status": "TODO"
}

Response (201 Created):
{
  "id": 101,
  "ownerUsername": "john_doe",
  "title": "Implement feature X",
  "description": "Build the authentication module",
  "status": "TODO",
  "createdAt": "2026-03-28T10:30:45.123Z",
  "updatedAt": "2026-03-28T10:30:45.123Z"
}

Error (401 Unauthorized):
{
  "code": "UNAUTHENTICATED",
  "message": "No active session",
  "timestamp": "2026-03-28T10:30:45.123Z"
}
```

#### 2. Get All Tasks (Current User)
```
GET /api/v1/tasks
Authorization: (Session cookie - Required)

Response (200 OK):
[
  {
    "id": 101,
    "ownerUsername": "john_doe",
    "title": "Implement feature X",
    "description": "Build the authentication module",
    "status": "IN_PROGRESS",
    "createdAt": "2026-03-28T09:00:00.000Z",
    "updatedAt": "2026-03-28T10:30:45.123Z"
  },
  {
    "id": 102,
    "ownerUsername": "john_doe",
    "title": "Write documentation",
    "description": "Document the API endpoints",
    "status": "TODO",
    "createdAt": "2026-03-28T10:00:00.000Z",
    "updatedAt": "2026-03-28T10:00:00.000Z"
  }
]
```

#### 3. Get Task by ID
```
GET /api/v1/tasks/{id}
Authorization: (Session cookie - Required)

Response (200 OK):
{
  "id": 101,
  "ownerUsername": "john_doe",
  "title": "Implement feature X",
  "description": "Build the authentication module",
  "status": "IN_PROGRESS",
  "createdAt": "2026-03-28T09:00:00.000Z",
  "updatedAt": "2026-03-28T10:30:45.123Z"
}

Error (404 Not Found):
{
  "code": "TASK_NOT_FOUND",
  "message": "Task not found with id: 101",
  "timestamp": "2026-03-28T10:30:45.123Z"
}
```

#### 4. Update Task
```
PUT /api/v1/tasks/{id}
Content-Type: application/json
Authorization: (Session cookie - Required)

Request Body:
{
  "title": "Implement feature X - Updated",
  "description": "Build the authentication module with JWT",
  "status": "IN_PROGRESS"
}

Response (200 OK):
{
  "id": 101,
  "ownerUsername": "john_doe",
  "title": "Implement feature X - Updated",
  "description": "Build the authentication module with JWT",
  "status": "IN_PROGRESS",
  "createdAt": "2026-03-28T09:00:00.000Z",
  "updatedAt": "2026-03-28T10:45:00.000Z"
}
```

#### 5. Delete Task
```
DELETE /api/v1/tasks/{id}
Authorization: (Session cookie - Required)

Response (200 OK):
{
  "message": "Task deleted successfully"
}
```

---

### Compute Service Endpoints

#### Calculate Operation
```
POST /api/v1/compute/calculate
Content-Type: application/json

Request Body:
{
  "operandA": 10,
  "operandB": 5,
  "operation": "ADD"
}

Supported Operations: ADD, SUBTRACT, MULTIPLY, DIVIDE

Response (200 OK):
{
  "operandA": 10,
  "operandB": 5,
  "operation": "ADD",
  "result": 15.0,
  "timestamp": "2026-03-28T10:30:45.123Z"
}
```

---

## User Authentication Flow

### Step 1: Register New Account
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "email": "alice@example.com",
    "password": "AlicePass123!"
  }'
```

### Step 2: Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -c cookies.txt \
  -d '{
    "username": "alice",
    "password": "AlicePass123!"
  }'
```

### Step 3: Check Session
```bash
curl -X GET http://localhost:8080/api/v1/auth/me \
  -b cookies.txt
```

### Step 4: Logout
```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -b cookies.txt
```

---

## Task Management Flow

### Step 1: Create a Task (as authenticated user)
```bash
curl -X POST http://localhost:8080/api/v1/tasks \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "title": "Write unit tests",
    "description": "Test all service methods",
    "status": "TODO"
  }'
```

### Step 2: Get All Tasks
```bash
curl -X GET http://localhost:8080/api/v1/tasks \
  -b cookies.txt
```

### Step 3: Get Single Task
```bash
curl -X GET http://localhost:8080/api/v1/tasks/101 \
  -b cookies.txt
```

### Step 4: Update Task
```bash
curl -X PUT http://localhost:8080/api/v1/tasks/101 \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "title": "Write unit tests - Updated",
    "description": "Test all service methods and edge cases",
    "status": "IN_PROGRESS"
  }'
```

### Step 5: Delete Task
```bash
curl -X DELETE http://localhost:8080/api/v1/tasks/101 \
  -b cookies.txt
```

---

## Troubleshooting

### Issue: "Port 8080 already in use"

**Solution 1:** Kill the process using port 8080
```bash
# On Linux/macOS:
lsof -i :8080
kill -9 <PID>

# On Windows:
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

**Solution 2:** Use a different port
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

---

### Issue: "Maven command not found"

**Solution:**
```bash
# Check if MAVEN_HOME is set
echo $MAVEN_HOME

# If not set, add to PATH:
export PATH=$PATH:/path/to/apache-maven-x.x.x/bin

# Or permanently add to ~/.bashrc or ~/.zshrc:
echo 'export PATH=$PATH:/path/to/apache-maven-x.x.x/bin' >> ~/.bashrc
source ~/.bashrc
```

---

### Issue: "No databases selected" (H2 Console)

**Solution:** Use full JDBC URL in H2 console:
```
JDBC URL: jdbc:h2:mem:cloudcompute;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
Username: sa
Password: (leave blank)
```

---

### Issue: "Cannot login after registration"

**Possible Causes:**
1. Database is not persisting (in-memory H2 may have reset)
2. Username/password mismatch
3. Session is not being stored

**Debug:**
```bash
# Check H2 database
curl http://localhost:8080/h2-console

# Check logs for auth errors
# Look for error messages in terminal output
```

---

### Issue: "Test failures after build"

**Solution:**
```bash
# Run tests in verbose mode
mvn test -X

# Run specific test class
mvn test -Dtest=TaskUiAndApiIntegrationTests

# Skip tests (if time-sensitive)
mvn package -DskipTests
```

---

### Issue: "Database connection refused (MySQL/PostgreSQL)"

**MySQL:**
```bash
# Check MySQL is running
mysql -u root -p -e "SELECT 1"

# Check user credentials
mysql -u cc_user -p cc_password_123 -h localhost -e "SELECT 1"

# Verify database exists
mysql -u cc_user -p -e "SHOW DATABASES;"
```

**PostgreSQL:**
```bash
# Check PostgreSQL is running
psql -U postgre s -c "SELECT 1"

# Verify database and user exist
psql -U cc_user -h localhost -d cloudcompute -c "SELECT 1"
```

---

### Issue: "Application exits after startup"

**Solution:** Check logs for errors
```bash
# Run application and capture all output
mvn spring-boot:run 2>&1 | tee app.log

# Check for common errors:
# - DataSource initialization error (check DB config)
# - Class not found (missing dependency)
# - Port already in use
```

---

## Quick Reference Commands

| Task | Command |
|------|---------|
| Clean build | `mvn clean` |
| Build with tests | `mvn package` |
| Build without tests | `mvn package -DskipTests` |
| Run locally | `mvn spring-boot:run` |
| Run JAR | `java -jar target/cloud-compute-service-0.0.1-SNAPSHOT.jar` |
| Run tests | `mvn test` |
| Check code style | `mvn validate` |
| Generate Javadoc | `mvn javadoc:javadoc` |
| View dependency tree | `mvn dependency:tree` |

---

## Support & Documentation

- **Swagger API Docs:** http://localhost:8080/swagger-ui.html
- **H2 Console (Dev):** http://localhost:8080/h2-console
- **GitHub Repository:** https://github.com/domna735/COMP4442-semester-project-Group14
- **Spring Boot Documentation:** https://spring.io/projects/spring-boot
- **Spring Security Guide:** https://spring.io/projects/spring-security

---

**Last Updated:** 2026-03-28  
**Document Version:** 1.0  
**Maintainer:** Group 14 COMP4442
