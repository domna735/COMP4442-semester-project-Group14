# COMP4442 Semester Project Group 14

Spring Boot cloud-hosted microservice project for COMP4442.

## Easy Setup (One Command)

For local development, you can run everything with one script (H2 in-memory DB, no external DB setup required).

Prerequisites:
- Java 17+
- Maven 3.8+
- curl

```bash
chmod +x scripts/one-click-dev.sh scripts/smoke-test.sh
./scripts/one-click-dev.sh
```

This script will:
- Start the Spring Boot app on `http://localhost:8080`
- Wait for health endpoint readiness
- Run API smoke tests automatically (register, login, me, create task, list tasks, logout)

Detailed quick guide: [one_command_playbook.md](one_command_playbook.md)

Optional:

```bash
./scripts/one-click-dev.sh --stop-after-test
```

If server is already running, test only:

```bash
./scripts/smoke-test.sh
```

## Current Progress
- Phase 1 baseline setup completed
- Phase 2 completed: Task Management CRUD APIs implemented with validation and OpenAPI docs
- Phase 3 started: database integration baseline prepared (dev H2 + prod env-based datasource)
- Current focus: Task 3 UI multi-page flow with SQL-backed user authentication and protected task management

## Step 1 Scope
- Create baseline Spring Boot service
- Provide a compute API endpoint
- Prepare project for cloud deployment on AWS EC2

## Step 2 Scope (In Progress)
- Build Task Management APIs (create, retrieve, update, delete)
- Add persistence layer and database integration path for AWS RDS
- Extend API validation, error handling, and OpenAPI docs for Task module

## API (Task Management)
- `POST /api/v1/tasks`
- `GET /api/v1/tasks`
- `GET /api/v1/tasks/{id}`
- `PUT /api/v1/tasks/{id}`
- `DELETE /api/v1/tasks/{id}`

## API (Authentication and User Management)
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/me`

Notes:
- User accounts are stored in SQL table `users`.
- Passwords are stored as BCrypt hashes (`password_hash`).
- Task APIs are user-scoped: each authenticated user sees and edits only their own tasks.

## UI Pages
- Home page: `/index.html`
- Login page: `/login.html`
- Register page: `/register.html`
- Task page (protected): `/task.html`
- Edit page (protected): `/edit.html?id=<taskId>`

## Task 2 RDS and EC2 Checklist (Teammate Runbook)
Execute the following in order.

1. Create an AWS RDS instance.
  - Engine: MySQL 8.x or PostgreSQL 15+
  - Public access: enabled (for project testing)
  - Note endpoint, port, DB name, username, and password

2. Configure AWS security groups.
  - RDS inbound: allow DB port from EC2 security group
  - EC2 inbound: allow `22` (SSH) and `8080` (API demo)

3. Launch EC2 and install runtime.
```bash
sudo apt update
sudo apt install -y openjdk-17-jdk maven
java -version
mvn -version
```

4. Build application JAR locally or on EC2.
```bash
mvn clean package -DskipTests
```

5. Set production datasource variables on EC2.
  - For MySQL example:
```bash
export DB_URL='jdbc:mysql://<RDS_ENDPOINT>:3306/<DB_NAME>?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export DB_USERNAME='<RDS_USERNAME>'
export DB_PASSWORD='<RDS_PASSWORD>'
export DB_DRIVER_CLASS_NAME='com.mysql.cj.jdbc.Driver'
```
  - For PostgreSQL example:
```bash
export DB_URL='jdbc:postgresql://<RDS_ENDPOINT>:5432/<DB_NAME>'
export DB_USERNAME='<RDS_USERNAME>'
export DB_PASSWORD='<RDS_PASSWORD>'
export DB_DRIVER_CLASS_NAME='org.postgresql.Driver'
```

6. Run application with production profile.
```bash
java -jar target/cloud-compute-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

7. Verify endpoint from browser or Postman.
```bash
curl http://<EC2_PUBLIC_IP>:8080/api/v1/compute/ping
curl http://<EC2_PUBLIC_IP>:8080/api/v1/tasks
```

8. Verify Swagger and Task CRUD flow.
  - Open `http://<EC2_PUBLIC_IP>:8080/swagger-ui/index.html`
  - Test create, read, update, and delete task against RDS

## systemd Service Template (EC2)
Create service file `/etc/systemd/system/cloud-compute.service`.

```ini
[Unit]
Description=Cloud Compute Service
After=network.target

[Service]
User=ubuntu
WorkingDirectory=/home/ubuntu/COMP4442-semester-project-Group14
Environment=SPRING_PROFILES_ACTIVE=prod
Environment=DB_URL=jdbc:mysql://<RDS_ENDPOINT>:3306/<DB_NAME>?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
Environment=DB_USERNAME=<RDS_USERNAME>
Environment=DB_PASSWORD=<RDS_PASSWORD>
Environment=DB_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver
ExecStart=/usr/bin/java -jar /home/ubuntu/COMP4442-semester-project-Group14/target/cloud-compute-service-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

Enable and start service.

```bash
sudo systemctl daemon-reload
sudo systemctl enable cloud-compute.service
sudo systemctl start cloud-compute.service
sudo systemctl status cloud-compute.service
```

## Deployment Files In Repository
- Environment template: `deploy/ec2/.env.prod.example`
- Production run script: `deploy/ec2/run-prod.sh`
- Deployment verification script: `deploy/ec2/verify-deploy.sh`
- systemd unit template: `deploy/systemd/cloud-compute.service`

Make run script executable on EC2:

```bash
chmod +x deploy/ec2/run-prod.sh
chmod +x deploy/ec2/verify-deploy.sh
```

Run deployment verification from EC2 or your local machine:

```bash
./deploy/ec2/verify-deploy.sh http://<EC2_PUBLIC_IP>:8080
```

## Troubleshooting RDS Connection (Quick)
1. Security group issue
  - Symptom: timeout or connection refused
  - Check: RDS inbound rule allows DB port (3306/5432) from EC2 security group

2. Driver class mismatch
  - Symptom: `Cannot load driver class` error
  - Check: `DB_DRIVER_CLASS_NAME` matches database engine
  - MySQL: `com.mysql.cj.jdbc.Driver`
  - PostgreSQL: `org.postgresql.Driver`

3. JDBC URL format error
  - Symptom: invalid URL or handshake error
  - Check: endpoint, port, database name, and URL prefix are correct
  - MySQL format: `jdbc:mysql://<endpoint>:3306/<db>?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
  - PostgreSQL format: `jdbc:postgresql://<endpoint>:5432/<db>`

4. Wrong credentials
  - Symptom: access denied / authentication failed
  - Check: `DB_USERNAME` and `DB_PASSWORD` values on EC2 environment or `EnvironmentFile`

## API (Initial)
- `GET /api/v1/compute/ping`
- `POST /api/v1/compute/calculate`

### Example Request
```json
{
  "operandA": 5,
  "operandB": 3,
  "operator": "ADD"
}
```

### Example Response
```json
{
  "expression": "5.0 + 3.0",
  "result": 8.0,
  "timestamp": "2026-03-20T00:00:00"
}
```

## Local Run (after Java and Maven are installed)
```bash
mvn spring-boot:run
```

Then open `http://localhost:8080/api/v1/compute/ping`.

Open Swagger UI at `http://localhost:8080/swagger-ui/index.html`.

## Easy Test Flow (Recommended)

### 1) Fast API verification (auto)

```bash
./scripts/one-click-dev.sh --stop-after-test
```

Expected pass checks:
- `GET /api/v1/compute/ping` → 200
- `POST /api/v1/auth/register` → 200
- `POST /api/v1/auth/login` → 200
- `GET /api/v1/auth/me` → 200
- `POST /api/v1/tasks` → 201
- `GET /api/v1/tasks` → 200
- `POST /api/v1/auth/logout` → 200

### 2) UI/manual verification (for screenshots and demo)
- Start app: `mvn spring-boot:run`
- Open `http://localhost:8080`
- Follow `test_execution_guide.md` phase-by-phase
- Capture evidence screenshots required in this README and `plan for project.md`

## Test
```bash
mvn test
```

## UI-Based Functional Test (Started)
Automated UI-backed function tests are implemented in:
- `src/test/java/hk/polyu/comp4442/cloudcompute/TaskUiAndApiIntegrationTests.java`

What is covered:
- UI homepage availability (`/` and `/index.html`)
- Task API CRUD flow used by the UI (create, list, update, delete)
- Validation error behavior for invalid task payloads

Run only UI/API integration tests:

```bash
mvn -Dtest=TaskUiAndApiIntegrationTests test
```

## Task 3 UI Test Checklist (Report and Demo Evidence)
Use this checklist to collect evidence for report/presentation artifacts.

- [ ] Open UI homepage and capture screenshot of initial state
- [ ] Create one task from UI and capture success message + updated list
- [ ] Edit task title/description/status and capture updated row state
- [ ] Delete task and capture list refresh after deletion
- [ ] Apply each status filter (TODO, IN_PROGRESS, DONE) and capture filtered results
- [ ] Trigger one validation error (empty title) and capture error message
- [ ] Run `mvn -Dtest=TaskUiAndApiIntegrationTests test` and save passing output screenshot
- [ ] Record date/time, tester name, environment (local or EC2), and app URL in report notes
