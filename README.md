# COMP4442 Semester Project Group 14

Cloud Compute Service is a Spring Boot cloud computing application with:

- JWT authentication (access token + refresh token)
- user-scoped task CRUD
- secure file upload/list/download APIs
- compute utility APIs
- local SQLite default runtime and EC2 production deployment scripts

## 1. Technology Stack

- Java 17
- Spring Boot 3.3.5
- Spring Security
- Spring Data JPA
- SQLite (default local)
- MySQL/PostgreSQL (production via environment variables)
- OpenAPI/Swagger UI
- Bash deployment and verification scripts

## 2. Repository Structure

- src/main/java: application source code
- src/main/resources/static: frontend pages
- src/test/java: integration tests
- scripts: local one-command setup and smoke test
- deploy/ec2: production env template, DB precheck, run script, deploy verifier
- deploy/systemd: systemd service template
- deploy/keys: JWT ECDSA key generation script

## 3. Local Quick Start

Prerequisites:

- Java 17+
- Maven 3.8+
- curl

Run one-command setup and test:

```bash
chmod +x scripts/one-click-dev.sh scripts/smoke-test.sh
./scripts/one-click-dev.sh
```

Optional auto-stop mode:

```bash
./scripts/one-click-dev.sh --stop-after-test
```

If server is already running, run smoke test only:

```bash
./scripts/smoke-test.sh
```

## 4. JWT Key Setup (Required)

Generate keys:

```bash
chmod +x deploy/keys/key.sh
./deploy/keys/key.sh
```

Place keys for local runtime:

- src/main/resources/cert/ECDSA_384_private.pem
- src/main/resources/cert/ECDSA_384_public.pem

For EC2 production, use:

- /opt/cloud-compute/cert/ECDSA_384_private.pem
- /opt/cloud-compute/cert/ECDSA_384_public.pem

## 5. API Coverage

Authentication:

- POST /api/v1/auth/register
- POST /api/v1/auth/login
- POST /api/v1/auth/refresh
- GET /api/v1/auth/me
- POST /api/v1/auth/logout

Tasks (protected):

- POST /api/v1/tasks
- GET /api/v1/tasks
- GET /api/v1/tasks/{id}
- PUT /api/v1/tasks/{id}
- DELETE /api/v1/tasks/{id}

Files (protected):

- POST /api/v1/files/upload
- GET /api/v1/files/list
- GET /api/v1/files/download/{filename}

Compute:

- GET /api/v1/compute/ping
- POST /api/v1/compute/calculate

Swagger:

- /swagger-ui/index.html
- /v3/api-docs

## 6. EC2 Deployment (Production)

### 6.1 Build

```bash
mvn clean package -DskipTests
```

### 6.2 Prepare env file

```bash
cp deploy/ec2/.env.prod.example deploy/ec2/.env.prod
```

Set values in deploy/ec2/.env.prod:

- SPRING_PROFILES_ACTIVE=prod
- DB_URL
- DB_USERNAME
- DB_PASSWORD
- DB_DRIVER_CLASS_NAME
- JWT_PRIVATE_KEY_PATH
- JWT_PUBLIC_KEY_PATH

### 6.3 Database precheck

```bash
chmod +x deploy/ec2/setup-db.sh
./deploy/ec2/setup-db.sh deploy/ec2/.env.prod
```

### 6.4 Run service

```bash
chmod +x deploy/ec2/run-prod.sh
./deploy/ec2/run-prod.sh
```

### 6.5 Verify deployment

```bash
chmod +x deploy/ec2/verify-deploy.sh
./deploy/ec2/verify-deploy.sh http://<EC2_PUBLIC_IP>:8080
```

### 6.6 Archive live EC2 verification evidence (recommended)

```bash
chmod +x deploy/ec2/live-verify-archive.sh
./deploy/ec2/live-verify-archive.sh http://<EC2_PUBLIC_IP>:8080
```

Artifacts are saved under `evidence/ec2/<timestamp>_<host>/` and include:
- verifier output log (`verify-deploy.log`)
- metadata (`metadata.txt`)
- optional screenshots (if a headless Chromium browser is available)

### 6.7 Operational notes (2026-04-18)

1. For startup/readiness checks on `/api/v1/compute/ping`, validate HTTP status code (`200`) instead of matching legacy body text such as `pong`.
2. A polling command that only greps response body may report false negatives even when app is healthy.
3. Protected file download endpoint requires `Authorization: Bearer <accessToken>`.
4. Directly opening a protected download URL in browser address bar may return `401` because Authorization header is not attached automatically.
5. Preferred demo flow: login first, then download from in-page UI action that performs token-authenticated fetch.

## 7. systemd Service

Template file:

- deploy/systemd/cloud-compute.service

After adapting paths on EC2:

```bash
sudo cp deploy/systemd/cloud-compute.service /etc/systemd/system/cloud-compute.service
sudo systemctl daemon-reload
sudo systemctl enable cloud-compute.service
sudo systemctl start cloud-compute.service
sudo systemctl status cloud-compute.service
```

## 8. Current Verification Status

Latest validated locally:

- mvn clean test passed
- one-click smoke flow passed
- deploy verification script passed against local endpoint
- protected route behavior verified (task/file APIs reject unauthenticated requests)

## 9. Assignment Compliance Checklist

### 9.1 Technological Merit

Current status: Strong

Evidence:

- Spring Boot layered architecture
- JWT access + refresh implementation
- secure file handling and protected APIs
- SQL persistence with profile-based local/prod separation
- EC2 deployment scripts and verifier automation

### 9.2 Development Trace Completeness (GitHub)

Current status: Satisfied (trace exists and is substantial)

Evidence:

- repository contains many commits across feature, security, and deployment phases
- multi-author contribution history exists in git log
- clear milestone progression captured in process log

Required action before grading:

- share the GitHub repository with teacher account: wchshapp_business@icloud.com
- if any member objects to future teaching/research use, send email to csqwang@polyu.edu.hk before final exam weeks with title:
  Comp4442 [Your student ID] [Your @connect.polyu.hk preferred name] Objection to share the COMP4442 semester project Git repository for future teaching/research

### 9.3 Presentation and Demo Clarity

Current status: In progress, near ready

Evidence:

- ppt.md and ppt_script.md prepared
- realtime_demo_playbook.md and full real time demo playbook.md prepared
- 12-minute demo flow exists with API verification steps

Pending:

- final rehearsal with strict 12-minute accumulated clock discipline
- final member job-division slide/script check

### 9.4 Document Quality

Current status: In progress

Evidence:

- report.md template and full_technical_report.md updated
- key playbooks and process log synchronized with current JWT + SQLite + EC2 flow

Pending:

- final Word report export with strict format check:
  - max 10 pages excluding cover
  - Times New Roman 12
  - A4
  - 1 inch margins
  - single column
  - line spacing not less than single
- final PowerPoint export with max 10 content pages excluding cover
- ensure slides/report content matches live presentation and demo behavior

## 10. Related Docs

- one_command_playbook.md
- playbook.md
- realtime_demo_playbook.md
- full real time demo playbook.md
- test_execution_guide.md
- full_technical_report.md
- report.md
- ppt.md
- ppt_script.md
- process log.md
