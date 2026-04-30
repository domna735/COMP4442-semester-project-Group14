# One-Command Setup and Test Playbook

This playbook is the fastest path to run the Cloud Compute Service locally and verify core APIs.

## Goal
- Start local server with SQLite default database
- Validate API flow automatically
- Keep usage easy for all team members

## Prerequisites
- Java 17+
- Maven 3.8+
- curl

Quick checks:

```bash
java -version
mvn -version
curl --version
```

## One-Command Setup + Test

From project root:

```bash
chmod +x scripts/one-click-dev.sh scripts/smoke-test.sh
./scripts/one-click-dev.sh
```

What it does:
- Starts Spring Boot app on `http://localhost:8080`
- Waits until `GET /api/v1/compute/ping` is healthy
- Runs automatic smoke tests (JWT + file flow):
  - register user
  - login
  - get current user
  - create task
  - list tasks
  - refresh access token
  - upload file
  - list files
  - download file
  - logout
  - verify unauthenticated task/file access returns `401`

## One-Command Setup + Test + Auto Stop

```bash
./scripts/one-click-dev.sh --stop-after-test
```

Use this when you only want validation and do not need the server running afterward.

## One-Command Test Only (Server Already Running)

```bash
./scripts/smoke-test.sh
```

Optional custom base URL:

```bash
BASE_URL=http://localhost:8080 ./scripts/smoke-test.sh
```

## Expected Pass Output

You should see pass lines for all checks:
- Ping endpoint: HTTP 200
- Register user: HTTP 201
- Login user: HTTP 200
- Get current user: HTTP 200
- Create task: HTTP 201
- List tasks: HTTP 200
- Refresh access token: HTTP 200
- Upload file: HTTP 200
- List files: HTTP 200
- Download file: HTTP 200
- Logout user: HTTP 200
- Reject unauthenticated tasks access: HTTP 401
- Reject unauthenticated file list access: HTTP 401

Final line should indicate all smoke tests passed.

## Troubleshooting

### curl missing
Install curl first.

Ubuntu/Debian:

```bash
sudo apt update && sudo apt install -y curl
```

macOS (Homebrew):

```bash
brew install curl
```

### Java or Maven missing
Ubuntu/Debian:

```bash
sudo apt update && sudo apt install -y openjdk-17-jdk maven
```

### Port 8080 already in use
Find and stop the existing process, then rerun:

```bash
lsof -i :8080
```

### App failed to start
Check log produced by script:

```bash
cat .one-click-dev.log
```

## Recommended Team Workflow
1. Run one-command setup+test before demo rehearsal
2. Run manual UI flow from `test_execution_guide.md` for screenshots
3. Run `mvn test` before final push

## Related Docs
- `README.md`
- `playbook.md`
- `test_execution_guide.md`
- `realtime_demo_playbook.md`
