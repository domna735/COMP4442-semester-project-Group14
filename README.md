# COMP4442 Semester Project Group 14

Spring Boot cloud-hosted microservice project for COMP4442.

## Current Progress
- Phase 1 baseline setup completed
- Phase 2 completed: Task Management CRUD APIs implemented with validation and OpenAPI docs
- Phase 3 started: database integration baseline prepared (dev H2 + prod env-based datasource)
- Current focus: Task 2 AWS RDS + EC2 deployment execution

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
- systemd unit template: `deploy/systemd/cloud-compute.service`

Make run script executable on EC2:

```bash
chmod +x deploy/ec2/run-prod.sh
```

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

## Test
```bash
mvn test
```
