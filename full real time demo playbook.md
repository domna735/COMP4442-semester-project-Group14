# Full Real-Time Demo Playbook

This playbook is the live-operation guide for demonstrating the full cloud service end-to-end:
- local verification,
- EC2 deployment setup,
- database connectivity checks,
- JWT auth + task + file API usage,
- and a timed demo sequence.

---

## 1) Demo Objectives

By the end of the demo, you should prove:
1. Service is reachable and healthy on cloud endpoint.
2. Database connection is correct and persistent (RDS or SQLite fallback).
3. JWT login works (access + refresh token).
4. Task APIs are protected and functional.
5. File upload/list/download is protected and functional.
6. Unauthenticated access to protected APIs returns `401`.

---

## 1.1) Critical Live Notes (2026-04-18)

1. **Do not** use `grep 'pong'` as readiness criteria for `/api/v1/compute/ping`.
	- Use HTTP status code `200` as the primary startup/health signal.
2. Ping endpoint currently returns JSON message payload; body-text may evolve, status code is the stable contract for readiness.
3. For protected file downloads, direct URL open in browser address bar may show `401` because Bearer token is not attached automatically.
4. Use in-page download button/flow that sends `Authorization: Bearer <accessToken>`.
5. Keep one fallback terminal proof ready:
	- no token => `401`
	- with token => `200`

---

## 2) Prerequisites

On EC2 host:
1. Java 17+
2. Maven 3.8+
3. curl
4. Git
5. (Optional) `mysql` or `psql` client for direct DB credential test in `setup-db.sh`

Install quick commands (Ubuntu):

```bash
sudo apt-get update
sudo apt-get install -y openjdk-17-jdk maven curl git netcat-openbsd
```

Optional DB clients:

```bash
sudo apt-get install -y mysql-client
sudo apt-get install -y postgresql-client
```

---

## 3) Repository + Build on EC2

```bash
git clone https://github.com/domna735/COMP4442-semester-project-Group14.git
cd COMP4442-semester-project-Group14

mvn clean package -DskipTests
```

Expected artifact:
- `target/cloud-compute-service-0.0.1-SNAPSHOT.jar`

---

## 4) JWT Key Preparation (Required)

If keys are not present:

```bash
chmod +x deploy/keys/key.sh
./deploy/keys/key.sh
```

Move keys to target path used by production env:

```bash
sudo mkdir -p /opt/cloud-compute/cert
sudo cp deploy/keys/ECDSA_384_private.pem /opt/cloud-compute/cert/
sudo cp deploy/keys/ECDSA_384_public.pem /opt/cloud-compute/cert/
sudo chown -R "$USER":"$USER" /opt/cloud-compute/cert
```

---

## 5) Production Environment File

Create env file:

```bash
cp deploy/ec2/.env.prod.example deploy/ec2/.env.prod
```

Edit `deploy/ec2/.env.prod` and fill values.

### 5.1 MySQL RDS Example

```bash
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:mysql://<RDS_ENDPOINT>:3306/<DB_NAME>?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=<DB_USER>
DB_PASSWORD=<DB_PASSWORD>
DB_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver
JWT_PRIVATE_KEY_PATH=file:/opt/cloud-compute/cert/ECDSA_384_private.pem
JWT_PUBLIC_KEY_PATH=file:/opt/cloud-compute/cert/ECDSA_384_public.pem
```

### 5.2 PostgreSQL RDS Example

```bash
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:postgresql://<RDS_ENDPOINT>:5432/<DB_NAME>
DB_USERNAME=<DB_USER>
DB_PASSWORD=<DB_PASSWORD>
DB_DRIVER_CLASS_NAME=org.postgresql.Driver
JWT_PRIVATE_KEY_PATH=file:/opt/cloud-compute/cert/ECDSA_384_private.pem
JWT_PUBLIC_KEY_PATH=file:/opt/cloud-compute/cert/ECDSA_384_public.pem
```

### 5.3 SQLite Fallback (Single EC2 Demo)

```bash
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:sqlite:/opt/cloud-compute/comp4442.db
DB_USERNAME=sa
DB_PASSWORD=
DB_DRIVER_CLASS_NAME=org.sqlite.JDBC
JWT_PRIVATE_KEY_PATH=file:/opt/cloud-compute/cert/ECDSA_384_private.pem
JWT_PUBLIC_KEY_PATH=file:/opt/cloud-compute/cert/ECDSA_384_public.pem
```

---

## 6) Database Connectivity Precheck (Must Run)

```bash
chmod +x deploy/ec2/setup-db.sh
./deploy/ec2/setup-db.sh deploy/ec2/.env.prod
```

If it fails:
1. Check RDS security group inbound rule (3306 MySQL / 5432 PostgreSQL).
2. Ensure inbound source allows EC2 security group.
3. Ensure outbound from EC2 allows DB destination.
4. Verify endpoint/port/db name/user/password.

---

## 7) Start Service (Production Profile)

```bash
chmod +x deploy/ec2/run-prod.sh
./deploy/ec2/run-prod.sh
```

In another terminal, verify health:

```bash
curl -s -o /dev/null -w "%{http_code}\n" http://<EC2_PUBLIC_IP>:8080/api/v1/compute/ping
```

Expect `200`.

---

## 8) Full Deployment Verification Script

```bash
chmod +x deploy/ec2/verify-deploy.sh
./deploy/ec2/verify-deploy.sh http://<EC2_PUBLIC_IP>:8080
```

This verifies:
1. Ping health
2. Swagger/OpenAPI
3. Register/Login/Me
4. Task create/list
5. File upload/list/download
6. Refresh token
7. Logout
8. Unauthenticated protected API checks

Keep this output as demo evidence.

---

## 9) Manual API Demo (Optional but Recommended)

Base URL:

```bash
BASE_URL=http://<EC2_PUBLIC_IP>:8080
```

### 9.1 Register

```bash
curl -s -X POST "$BASE_URL/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"username":"demo_api_user","email":"demo_api_user@example.com","password":"DemoPass123!"}'
```

### 9.2 Login and capture tokens

```bash
LOGIN_JSON=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"demo_api_user","password":"DemoPass123!"}')

echo "$LOGIN_JSON"
ACCESS_TOKEN=$(echo "$LOGIN_JSON" | sed -n 's/.*"accessToken"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
REFRESH_TOKEN=$(echo "$LOGIN_JSON" | sed -n 's/.*"refreshToken"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
```

### 9.3 Me endpoint

```bash
curl -s "$BASE_URL/api/v1/auth/me" -H "Authorization: Bearer $ACCESS_TOKEN"
```

### 9.4 Create task

```bash
curl -s -X POST "$BASE_URL/api/v1/tasks" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{"title":"Demo Task","description":"Created in live demo","status":"TODO"}'
```

### 9.5 List tasks

```bash
curl -s "$BASE_URL/api/v1/tasks" -H "Authorization: Bearer $ACCESS_TOKEN"
```

### 9.6 Refresh access token

```bash
curl -s -X POST "$BASE_URL/api/v1/auth/refresh" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}"
```

### 9.7 Upload file

```bash
echo "demo upload file" > /tmp/demo-upload.txt
curl -s -X POST "$BASE_URL/api/v1/files/upload" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -F "file=@/tmp/demo-upload.txt;filename=demo-upload.txt"
```

### 9.8 List files

```bash
curl -s "$BASE_URL/api/v1/files/list" -H "Authorization: Bearer $ACCESS_TOKEN"
```

### 9.9 Logout

```bash
curl -s -X POST "$BASE_URL/api/v1/auth/logout" -H "Authorization: Bearer $ACCESS_TOKEN"
```

### 9.10 Unauthorized check

```bash
curl -s -o /dev/null -w "%{http_code}\n" "$BASE_URL/api/v1/tasks"
```

Expect `401`.

---

## 10) Frontend Demo URLs

1. Home: `http://<EC2_PUBLIC_IP>:8080/`
2. Login: `http://<EC2_PUBLIC_IP>:8080/login.html`
3. Register: `http://<EC2_PUBLIC_IP>:8080/register.html`
4. Task board (protected): `http://<EC2_PUBLIC_IP>:8080/task.html`
5. Swagger: `http://<EC2_PUBLIC_IP>:8080/swagger-ui/index.html`

Expected protection behavior:
1. `task.html` and `edit.html` redirect to login when unauthenticated.
2. `/api/v1/tasks/**` and `/api/v1/files/**` return `401` without token.

---

## 11) Timed Live Demo Script (12 Minutes)

1. Minute 0-1: open Home + Swagger, show service is up.
2. Minute 1-3: register + login user, show JWT token flow.
3. Minute 3-6: task create/list/update/delete behavior.
4. Minute 6-8: file upload/list/download behavior.
5. Minute 8-10: refresh token + logout + unauthorized `401` behavior.
6. Minute 10-12: show EC2 setup summary (`.env.prod`, `setup-db.sh`, `run-prod.sh`, `verify-deploy.sh`) and final pass output.

---

## 12) Troubleshooting Quick Guide

1. App not starting:
	- Check env required vars in `deploy/ec2/.env.prod`
	- Verify jar exists: `target/cloud-compute-service-0.0.1-SNAPSHOT.jar`

2. DB connection failure:
	- Run `./deploy/ec2/setup-db.sh deploy/ec2/.env.prod`
	- Fix SG rules, endpoint, credentials

3. JWT key load failure:
	- Verify key files exist at `/opt/cloud-compute/cert/`
	- Verify env paths in `.env.prod` are correct

4. 401 on protected API after login:
	- Ensure Bearer token is sent in Authorization header
	- If expired, call refresh endpoint and retry

5. File upload blocked:
	- Ensure extension is in allowlist (`txt`, `pdf`, `png`, `jpg`, `jpeg`, `gif`, `csv`, `json`, `md`, `zip`)

---

## 13) Evidence to Keep After Demo

1. Output of `./deploy/ec2/setup-db.sh deploy/ec2/.env.prod`
2. Output of `./deploy/ec2/verify-deploy.sh http://<EC2_PUBLIC_IP>:8080`
3. Screenshots:
	- login success
	- task operations
	- file operations
	- swagger endpoint list
4. Optional: terminal logs from app startup and API calls

