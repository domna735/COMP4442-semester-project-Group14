# Cloud Compute Service - Real-Time Demo Playbook

**Purpose:** Step-by-step guide for executing a live 12-minute demonstration of the Cloud Compute Service during presentation.

**Default Demo Mode:** Cloud EC2 first. Use localhost only when classroom network/public ingress is unavailable.

**Important Update (2026-04-17):**
- Authentication is now JWT access + refresh token, not server session cookie.
- Protected API calls use `Authorization: Bearer <accessToken>`.
- Local default DB is SQLite (`comp4442.db`), and production uses env-driven MySQL/PostgreSQL.
- File upload/download endpoints are now protected and included in verification flow.

**Operational Clarification (2026-04-18):**
- For `/api/v1/compute/ping`, check HTTP `200` as readiness; do not rely on searching response body for legacy `pong` text.
- Protected file download URL opened directly in browser may return `401` if Bearer token is not attached.
- Demo should use the UI download action (token-authenticated fetch flow) to prove authorized file access.

**Preparation Time:** 5 minutes (before demo starts)  
**Demo Duration:** 12 minutes (including Q&A buffer)  
**Walkthrough Order:** Auth Flow → Task Management → User Isolation → Data Persistence

**Related Documentation:**
- **[test_execution_guide.md](test_execution_guide.md)** — Comprehensive manual testing procedures (34 test cases, critical path validation)
- **[playbook.md](playbook.md)** — Operational setup and configuration guide
- **[README.md](README.md)** — Quick-start reference

---

## EC2 Live Demo Mode (For Real Cloud Presentation)

Use this section when presenting on your real AWS instance instead of localhost.

### A. Current EC2 target
- Current example (2026-04-25): Elastic IP 54.253.135.61

- Instance Name: COMP4442 Semester Project Group 14
- Instance ID: i-0f2d54704d5c42a6a
- Public DNS/IP can change after Stop -> Start (unless Elastic IP is attached)
- SSH Key: COMP4442 Semester Project Group 14.pem

Check current endpoint quickly:

```bash
aws ec2 describe-instances \
   --instance-ids i-0f2d54704d5c42a6a \
   --region ap-southeast-2 \
   --query 'Reservations[0].Instances[0].[PublicIpAddress,PublicDnsName,State.Name]' \
   --output table
```

### B. Required AWS Security Group rule (critical)

Your app is confirmed healthy inside EC2 (localhost:8080 returns 200). To allow audience/public browser access, make sure inbound rules include:

- TCP 22 from your IP (or demo environment range)
- TCP 8080 from your demo source

Recommended for classroom demo:

- TCP 8080 from 0.0.0.0/0 (temporary)

After demo, remove or narrow this rule.

### C. One-minute pre-demo verification

Run from your local machine:

```bash
ssh -i "/home/domna/COMP4442 Semester Project Group 14.pem" ubuntu@54.253.135.61 "curl -s -o /dev/null -w 'LOCAL_PING=%{http_code}' http://localhost:8080/api/v1/compute/ping"
curl -s -o /dev/null -w "PUBLIC_PING=%{http_code}\n" http://54.253.135.61:8080/api/v1/compute/ping
```

Expected before presentation starts:

- LOCAL_PING=200
- PUBLIC_PING=200

If PUBLIC_PING is 000/connection refused, fix Security Group inbound TCP 8080 first.

### D. Demo URLs (EC2 mode)

- Home: http://54.253.135.61:8080
- Register: http://54.253.135.61:8080/register.html
- Login: http://54.253.135.61:8080/login.html
- Task: http://54.253.135.61:8080/task.html
- Swagger: http://54.253.135.61:8080/swagger-ui/index.html

### D1. English 30-second stage script (ready to read)

"Good morning/afternoon. Our Spring Boot service is running on a real AWS EC2 public endpoint.
I will first show login and task creation with User A, then log out and sign in as User B.
User B cannot see User A's tasks, which proves strict user-level isolation.
Finally, I will open Swagger and show protected APIs and successful responses.
This demonstrates end-to-end functionality: authentication, authorization, task CRUD, and secure API access in a real cloud environment."

### D2. English click order for live demo (fast path)

1. Open `http://54.253.135.61:8080`
2. Click `Login` and sign in with prepared account A.
3. Open `task.html`, create one task, and show it in list.
4. Click `Logout`.
5. Login with account B.
6. Show account B task list does not contain account A data.
7. Open `http://54.253.135.61:8080/swagger-ui/index.html` and expand one protected endpoint.
8. Conclude with: "Auth + isolation + CRUD + API docs are all working on EC2."

### E. Optional: start/restart app on EC2 (if needed)

```bash
ssh -i "/home/domna/COMP4442 Semester Project Group 14.pem" ubuntu@54.253.135.61
cd ~/COMP4442-semester-project-Group14
pkill -f cloud-compute-service-0.0.1-SNAPSHOT.jar || true
nohup ./deploy/ec2/run-prod.sh > ~/cloud-compute-prod.log 2>&1 &
tail -n 60 ~/cloud-compute-prod.log
```

### F. Fallback demo route (if public 8080 is blocked)

If PUBLIC_PING is still 000 but LOCAL_PING is 200, use SSH local port forwarding:

```bash
ssh -i "/home/domna/COMP4442 Semester Project Group 14.pem" -L 8080:localhost:8080 ubuntu@54.253.135.61
```

Keep this terminal open, then demo with local browser URLs:

- http://localhost:8080
- http://localhost:8080/login.html
- http://localhost:8080/task.html
- http://localhost:8080/swagger-ui/index.html

This still runs the backend on EC2, but traffic is tunneled through SSH.

---

## EC2 Pre-Demo Command Pack (Copy to Terminal)

Use this section when you shut down EC2 to save cost and need a fast warm-up before demo.

### Fastest one-click mode (recommended for time limit)

```bash
chmod +x ./scripts/ec2-pre-demo-one-click.sh
./scripts/ec2-pre-demo-one-click.sh --resolve-from-aws --run-verify
```

If EC2 was stopped and you already configured AWS CLI locally, use:

```bash
./scripts/ec2-pre-demo-one-click.sh --auto-start --resolve-from-aws --run-verify
```

If public network fails and you need localhost fallback tunnel:

```bash
./scripts/ec2-pre-demo-one-click.sh --resolve-from-aws --open-tunnel
```

If you prefer SSH via public DNS instead of public IP:

```bash
./scripts/ec2-pre-demo-one-click.sh --resolve-from-aws --prefer-dns --run-verify
```

One-click script location:
- `scripts/ec2-pre-demo-one-click.sh`

Detailed manual command pack is kept below for transparency and troubleshooting.

### Stable endpoint option (recommended long-term)

To avoid IP changes completely, allocate and associate an Elastic IP (EIP) to this instance.
After EIP association, update `BASE_URL` once in this playbook and your demo commands remain stable across restarts.

### 1) Set local variables once

```bash
KEY="/home/domna/COMP4442 Semester Project Group 14.pem"
HOST="ubuntu@54.253.135.61"
BASE_URL="http://54.253.135.61:8080"
```

### 2) If EC2 was stopped, start it first

Preferred: start from AWS Console (EC2 -> Instance -> Start).

Optional (only if AWS CLI is configured on your machine):

```bash
aws ec2 start-instances --instance-ids i-0f2d54704d5c42a6a --region ap-southeast-2
```

### 3) Wait until SSH is reachable

```bash
for i in {1..30}; do
   if ssh -i "$KEY" -o ConnectTimeout=5 -o StrictHostKeyChecking=no "$HOST" "echo SSH_OK" >/dev/null 2>&1; then
      echo "SSH reachable"
      break
   fi
   echo "Waiting for EC2 SSH..."
   sleep 10
done
```

### 4) Build and start app on EC2 (cloud mode)

```bash
ssh -i "$KEY" -o StrictHostKeyChecking=no "$HOST" "cd ~/COMP4442-semester-project-Group14 && mvn -q -DskipTests clean package"
ssh -i "$KEY" -o StrictHostKeyChecking=no "$HOST" "pkill -f cloud-compute-service-0.0.1-SNAPSHOT.jar || true; cd ~/COMP4442-semester-project-Group14 && nohup ./deploy/ec2/run-prod.sh > ~/cloud-compute-prod.log 2>&1 &"
```

### 5) Health checks (must pass before demo)

```bash
ssh -i "$KEY" -o StrictHostKeyChecking=no "$HOST" "curl -s -o /dev/null -w 'LOCAL_PING=%{http_code}\n' http://localhost:8080/api/v1/compute/ping"
curl -s -o /dev/null -w "PUBLIC_PING=%{http_code}\n" "$BASE_URL/api/v1/compute/ping"
```

Expected:
- LOCAL_PING=200
- PUBLIC_PING=200

### 6) Optional end-to-end verify (recommended)

```bash
./deploy/ec2/verify-deploy.sh "$BASE_URL"
```

### 7) If public network path fails, fallback to localhost tunnel

```bash
ssh -i "$KEY" -L 8080:localhost:8080 "$HOST"
```

Then demo via:
- http://localhost:8080
- http://localhost:8080/login.html
- http://localhost:8080/task.html
- http://localhost:8080/swagger-ui/index.html

---

## Pre-Demo Checklist (DO THIS BEFORE PRESENTATION)

- [ ] **Use Cloud EC2 as primary demo target**

- [ ] **Start/Resume EC2 instance if it was stopped**
   - AWS Console -> EC2 -> Start instance `i-0f2d54704d5c42a6a`
   - Wait until instance state is `running` and status checks are `2/2 passed`

- [ ] **Run EC2 Pre-Demo Command Pack**
   - Quick path: `./scripts/ec2-pre-demo-one-click.sh --run-verify`
   - Confirm `LOCAL_PING=200` and `PUBLIC_PING=200`

- [ ] **Confirm public demo pages are reachable**
   - Home: `http://54.253.135.61:8080`
   - Login: `http://54.253.135.61:8080/login.html`
   - Task: `http://54.253.135.61:8080/task.html`
   - Swagger: `http://54.253.135.61:8080/swagger-ui/index.html`

- [ ] **Fallback only if network/public ingress fails**
   - Use SSH tunnel (`-L 8080:localhost:8080`) and demo via localhost

- [ ] **(Fallback local-only mode, last resort)**
  ```bash
   cd /home/domna/COMP4442-semester-project-Group14-v2
  mvn spring-boot:run
  # Or: java -jar target/cloud-compute-service-0.0.1-SNAPSHOT.jar
  ```
  Wait for message: "Started CloudComputeServiceApplication in X.XXX seconds"

- [ ] **Verify Application is Running (Cloud-first)**
   - Open browser: `http://54.253.135.61:8080`
   - Expected: Home page with "Cloud Compute Service" and navigation links
   - Open Swagger API docs: `http://54.253.135.61:8080/swagger-ui/index.html`
   - Expected: All endpoints visible

- [ ] **Clear Browser Local Storage + Cookies (Important!)**
   - Press `Ctrl+Shift+Delete` (or Cmd+Shift+Delete on Mac)
   - Delete site data for `54.253.135.61:8080` (cookies + local storage)
   - If using tunnel fallback, also clear site data for `localhost:8080`
   - This ensures demo starts with unauthenticated state

- [ ] **Have Browser Windows Ready (Cloud mode)**
   - **Tab 1:** http://54.253.135.61:8080 (home page)
   - **Tab 2:** http://54.253.135.61:8080/swagger-ui/index.html (API reference)
  - **Tab 3:** Postman or curl for API testing (optional backup)

- [ ] **Have Terminal Ready**
  - Keep terminal visible showing application logs
  - This shows real-time session/database activity

---

## Demo Flow (12 Minutes Total)

### **SEGMENT 1: Authentication & User Registration (3 Minutes)**

**Speaking Points:**
> "We have implemented SQL-backed user authentication with Spring Security. Users register with username, email, and password. Passwords are hashed using BCrypt (never stored in plain text)."

**Action Steps:**

1. **Navigate to Registration Page** (0:00 - 0:30)
   - Click "Register" link on home page
   - Show registration form with fields: username, email, password
   - Point out form validation: "Username must be 3-80 characters, email format required"

2. **Register First User - "Alice"** (0:30 - 1:30)
   - Fill form:
     - Username: `alice`
     - Email: `alice@example.com`
     - Password: `AlicePass123!`
   - Click "Register"
   - Point out: redirects to login page automatically
   - **Speaking:** "The registration API validates duplicate usernames and emails directly in the database, then redirects the user to login."

3. **Test Duplicate Username** (1:30 - 2:00)
   - Go back to Register page
   - Try registering with same username `alice`
   - Show error message: "Username 'alice' already exists"
   - **Speaking:** "The UserRepository checks for existing usernames before creating a new account. This prevents duplicate accounts."

4. **Check Terminal Logs** (2:00 - 2:30)
   - Point to terminal showing SQL CRUD logs
   - Show INSERT into AppUser table with hashed password
   - **Speaking:** "In the background, Spring Boot's JPA layer generated an INSERT statement. The password is stored as a BCrypt hash, not plaintext."

---

### **SEGMENT 2: Login & Session Establishment (2 Minutes)**

**Speaking Points:**
> "After registration, users log in and receive an access token plus a refresh token. The frontend stores them client-side and sends the access token as Bearer authorization for protected APIs."

**Action Steps:**

1. **Login as Alice** (0:00 - 1:00)
   - Fill login form with:
     - Username: `alice`
     - Password: `AlicePass123!`
   - Click "Login"
   - Page redirects to `http://54.253.135.61:8080/task.html` (or localhost in fallback mode)
   - Show "Signed in as alice" message at top of page
   - **Speaking:** "Upon login, AuthService calls Spring's AuthenticationManager to verify credentials against the database. If valid, a SecurityContext is created and stored in the HTTP session."

2. **Show Token Storage** (1:00 - 1:30)
   - Open Browser Dev Tools (F12)
   - Go to Application → Local Storage → `54.253.135.61:8080` (or `localhost:8080` in fallback mode)
   - Show `accessToken` and `refreshToken`
   - **Speaking:** "The access token secures API calls. If it expires, refresh endpoint issues a new one without full re-login."

3. **Inspect Browser Network Traffic** (1:30 - 2:00)
   - Open Network tab in Dev Tools
   - Refresh task.html
   - Show GET request with `Authorization: Bearer ...`
   - Show response: GET /api/v1/auth/me returns current user info
   - **Speaking:** "Behind the scenes, task.html calls GET /api/v1/auth/me using bearer token. If token is missing/invalid, the page redirects to login."

---

### **SEGMENT 3: Task Management - Create and List (2.5 Minutes)**

**Speaking Points:**
> "Each authenticated user can create, read, update, and delete tasks. All tasks are permanently stored in the database and are scoped to the logged-in user—users cannot see other users' tasks."

**Action Steps:**

1. **View Task List** (0:00 - 0:30)
   - Show task.html task list (initially empty for new user)
   - **Speaking:** "Alice's task list is initially empty. Notice the page also displays the username in the header—this proves we know who is logged in."

2. **Create First Task** (0:30 - 1:30)
   - Fill "Create Task" form:
     - Title: `Implement authentication module`
     - Description: `Build Spring Security with BCrypt password hashing`
     - Status: `TODO`
   - Click "Create"
   - Task appears in the list immediately
   - Show new task row with: title, description, status, ownerUsername (alice), createdAt, updatedAt
   - **Speaking:** "The task is created in the database with user_id pointing to Alice's record. The API response includes the ownerUsername field, confirming ownership."

3. **Create Second Task** (1:30 - 2:00)
   - Fill form:
     - Title: `Write database integration tests`
     - Description: `Verify task CRUD endpoints work with user scoping`
     - Status: `TODO`
   - Click "Create"
   - Show both tasks in list
   - **Speaking:** "Alice now has two tasks. Each task is permanently stored in the database with a foreign key to the AppUser table."

4. **Check Terminal Logs** (2:00 - 2:30)
   - Show INSERT and SELECT statements in logs
   - Point out: `SELECT ... WHERE user_id = ?` showing user-scoped query
   - **Speaking:** "The database layer filters tasks by user_id, ensuring Alice only sees her own tasks."

---

### **SEGMENT 4: User Isolation - Second User "Bob"** (2.5 Minutes)**

**Speaking Points:**
> "This is the critical security feature: user isolation. Two different users cannot see or access each other's tasks, even if they know the task ID."

**Action Steps:**

1. **Logout Alice** (0:00 - 0:30)
   - Click "Logout" button on task.html
   - Application redirects to login.html
   - Show page is now back to unauthenticated state
   - Check browser cookies: JSESSIONID is cleared
   - **Speaking:** "SessionContextLogoutHandler() clears the SecurityContext, removing the session cookie. Alice is now logged out."

2. **Register Second User - "Bob"** (0:30 - 1:30)
   - Go to Register page
   - Fill form:
     - Username: `bob`
     - Email: `bob@example.com`
     - Password: `BobPass456!`
   - Click "Register" then login
   - Show "Signed in as bob" message
   - Show Bob's task list is **completely empty**
   - **Speaking:** "Bob is a separate user. Notice his task list is empty—he has no tasks. He cannot see Alice's tasks because they are filtered by user_id in the database query."

3. **Create Task as Bob** (1:30 - 2:00)
   - Fill form:
     - Title: `Review code changes`
     - Description: `Check pull requests from team members`
     - Status: `TODO`
   - Click "Create"
   - Show Bob has only 1 task
   - **Speaking:** "Bob creates his own task. In the database, this task has a user_id pointing to Bob's record, not Alice's."

4. **Try to Access Alice's Task (Security Demonstration)** (2:00 - 2:30)
   - Open Browser Dev Tools → Network tab
   - Manually construct URL to edit Alice's task (e.g., /edit.html?id=1)
   - Show 404 error or redirect to task.html
   - **Speaking:** "Even if Bob manually tries to access Alice's task URL or API, the TaskController method includes @AuthenticationPrincipal,  and TaskService verifies ownership via findByIdAndUserId(id, userId). Unauthorized access is blocked."

---

### **SEGMENT 5: Task Update & Soft Delete (1.5 Minutes)**

**Speaking Points:**
> "The full CRUD lifecycle: Create, Read, Update, Delete. All operations respect user ownership and enforce data isolation."

**Action Steps:**

1. **Update Bob's Task** (0:00 - 0:45)
   - Find Bob's task in list
   - Click "Edit" button
   - Navigate to edit.html?id=3 (or corresponding task ID)
   - Modify task:
     - Title: `Review code changes - URGENT`
     - Status: `IN_PROGRESS`
   - Click "Save"
   - Redirects to task.html
   - Show updated task in list with new title and status
   - **Speaking:** "The edit page fetches the task by ID and user_id. After update, the updatedAt timestamp changes, showing the modification time."

2. **Delete Task** (0:45 - 1:30)
   - Find updated task in Bob's list
   - Click "Delete" button
   - Task disappears from list
   - Show notification/confirmation (or just absence of task)
   - **Speaking:** "The task is permanently deleted from the database. Note there's no 'undelete' feature—once deleted, it's gone. This demonstrates proper data lifecycle management."

---

### **SEGMENT 6: API Testing with Postman/curl (1 Minute - Optional)**

**Speaking Points:**
> "Behind the UI, all operations use REST APIs. Let me show you the raw API responses."

**Action Steps:**

1. **Show Swagger Documentation** (0:00 - 0:45)
   - Open Swagger UI: `http://54.253.135.61:8080/swagger-ui/index.html`
   - Expand "Authentication Endpoints" section
   - Show all endpoints: /auth/register, /auth/login, /auth/me, /auth/logout
   - Expand "Task Endpoints"
   - Show: POST /tasks, GET /tasks, GET /tasks/{id}, PUT /tasks/{id}, DELETE /tasks/{id}
   - **Speaking:** "All endpoints are documented in Swagger. The 401 responses show that authentication is required for protected endpoints."

2. **Optional: Run Postman** (0:45 - 1:00)
   - (Only if time permits) Show sample curl command for login:
     ```bash
       curl -X POST http://54.253.135.61:8080/api/v1/auth/login \
       -H "Content-Type: application/json" \
       -d '{"username": "bob", "password": "BobPass456!"}'
     ```
   - Show JSON response with user info
    - **Speaking:** "REST APIs return structured JSON. Subsequent protected calls include Bearer access token in Authorization header."

---

### **SEGMENT 7: Summary & Architecture Diagram (0.5 Minutes)**

**Speaking Points:**

> "Let me summarize the architecture:

1. **Frontend:** Multi-page HTML UI (login, register, task list, edit task)
2. **Backend:** Spring Boot with Spring Security for authentication
3. **Business Logic:** Service layer with user-scoped CRUD operations
4. **Data Access:** Spring Data JPA repositories with user filtering
5. **Persistence:** SQL database (SQLite for local dev, MySQL/PostgreSQL for prod)
6. **Security:** BCrypt password hashing, JWT access+refresh, user-scoped queries

All components work together to provide secure, isolated multi-user task management."

---

## Troubleshooting During Demo

### Issue: Real online version cannot login
**Quick diagnosis status (verified on 2026-04-17):**
- Public backend API is healthy and login endpoint works on EC2.
- Full deploy verification against `http://54.253.135.61:8080` passed (register/login/tasks/files/refresh/logout).

**Likely causes are browser-side state, not backend service.**

**Fix steps (in order):**
1. Open `http://54.253.135.61:8080/login.html` directly (avoid stale tabs).
2. Clear site storage for `54.253.135.61:8080` (Local Storage + cookies).
3. Hard refresh (`Ctrl+Shift+R`).
4. Retry login with a known valid account.
5. If still failing, open browser DevTools > Network and check `POST /api/v1/auth/login`:
    - `200`: backend is fine, check frontend token handling and page redirect.
    - `401`: wrong username/password.
    - `4xx/5xx`: capture response body and check EC2 app log.

**Server-side confirmation commands:**
```bash
curl -s -o /dev/null -w "PUBLIC_PING=%{http_code}\n" http://54.253.135.61:8080/api/v1/compute/ping
curl -s -X POST http://54.253.135.61:8080/api/v1/auth/login \
   -H "Content-Type: application/json" \
   -d '{"username":"<your_user>","password":"<your_password>"}'
```

If first command returns `200` and login API returns token JSON, backend is working correctly.

### Issue: Ping polling shows repeated timeout but service is actually healthy
**Cause:** polling script checks response body for `pong` instead of checking HTTP status.

**Fix:**
1. Use status-based probe:
   ```bash
   curl -s -o /dev/null -w "%{http_code}\n" http://54.253.135.61:8080/api/v1/compute/ping
   ```
2. Treat `200` as healthy.

### Issue: Download shows Unauthorized even after login
**Cause:** direct browser navigation to protected download URL may not attach `Authorization: Bearer` header.

**Fix:**
1. Download from in-page UI action (token-authenticated fetch).
2. If needed, verify behavior explicitly:
   - without token: `401`
   - with token: `200`

### Issue: Application is slow to start
**Solution:** Start EC2 app 2-5 minutes before demo begins (build + boot). Keep `tail -n 60 ~/cloud-compute-prod.log` ready on SSH tab.

### Issue: Accidentally logged out
**Solution:** Press back button or type URL directly to login page. Re-login quickly.

### Issue: Forgot task ID or data got mixed up
**Solution:** Use API calls in Swagger (`GET /api/v1/tasks`) after login to inspect current user task list.

### Issue: Browser shows "Connection refused"
**Solution:** Open terminal and check EC2 process and logs. If needed, restart on EC2:
```bash
ssh -i "/home/domna/COMP4442 Semester Project Group 14.pem" ubuntu@54.253.135.61 "pkill -f cloud-compute-service-0.0.1-SNAPSHOT.jar || true; cd ~/COMP4442-semester-project-Group14 && nohup ./deploy/ec2/run-prod.sh > ~/cloud-compute-prod.log 2>&1 &"
```

### Issue: Cannot login (invalid credentials)
**Solution:** Double-check username/password. If unsure, register a new test account on the spot.

---

## Quick Reference - Demo URLs

| Component | URL |
|-----------|-----|
| Home Page (Cloud Primary) | http://54.253.135.61:8080 |
| Register Page (Cloud Primary) | http://54.253.135.61:8080/register.html |
| Login Page (Cloud Primary) | http://54.253.135.61:8080/login.html |
| Task Page (Cloud Primary) | http://54.253.135.61:8080/task.html |
| Swagger Docs (Cloud Primary) | http://54.253.135.61:8080/swagger-ui/index.html |
| Localhost Fallback (Tunnel) | http://localhost:8080 |

---

## Quick Reference - Demo Test Accounts

**Pre-Registered Users (if created beforehand):**
- User 1: `alice` / `AlicePass123!`
- User 2: `bob` / `BobPass456!`

**Or create during demo** (recommended for freshness)

---

## Post-Demo Checklist

- [ ] Collect screenshots of key screens (registration, login, task list, edit page)
- [ ] Document any errors or unexpected behavior
- [ ] Save terminal output showing API logs
- [ ] Note any improvements or feature requests from audience
- [ ] Keep application running for extended Q&A if needed

### Cost-control shutdown (Free Tier friendly)

After finishing demo and evidence capture:

1. EC2 Console → Instance Actions → Stop instance
2. Confirm status becomes Stopped
3. Keep notes of:
   - Public IP/DNS may change on next start (unless Elastic IP is attached)
   - If IP changes, update URLs in this playbook before next demo

---

## Time Allocation

```
Total: 12 minutes

Segment 1 (Auth & Registration):  3 min → 3 min elapsed
Segment 2 (Login & Token):        2 min → 5 min elapsed
Segment 3 (Task CRUD):            2.5 min → 7.5 min elapsed
Segment 4 (User Isolation):       2.5 min → 10 min elapsed
Segment 5 (Update & Delete):      1.5 min → 11.5 min elapsed
Segment 6 (API Docs):             0.5 min → 12 min elapsed

Buffer: Handle Q&A or technical issues
```

---

## Presenter Notes

- **Speak slowly and clearly** — audience needs to follow database/network concepts
- **Point at screen** — use cursor to highlight key UI elements and data fields
- **Pause for impact** — after showing error messages or important transitions
- **Engage audience** — ask "Does everyone see the updated task?" or "Notice how Bob's list is empty?"
- **Emphasize security** — highlight user isolation feature multiple times
- **Connect to code** — when showing API docs, mention the @AuthenticationPrincipal annotation

---

## Conclusion Statement

> "In summary, we've demonstrated a fully functional multi-user task management system built with Spring Boot and Spring Security. The architecture implements:
>
> - **Secure Authentication:** BCrypt password hashing, JWT access+refresh token flow
> - **User Isolation:** User-scoped database queries prevent cross-user data access
> - **REST API:** Well-documented endpoints with proper HTTP status codes
> - **Clean UI:** Responsive HTML pages with real-time feedback
>
> The application is production-ready and can be deployed on AWS EC2 with RDS for scalable, reliable task management across many users."

---

**Demo Version:** 1.0  
**Last Updated:** 2026-04-18  
**Estimated Demo Time:** 12 minutes + Q&A
