# Cloud Compute Service - Real-Time Demo Playbook

**Purpose:** Step-by-step guide for executing a live 12-minute demonstration of the Cloud Compute Service during presentation.

**Preparation Time:** 5 minutes (before demo starts)  
**Demo Duration:** 12 minutes (including Q&A buffer)  
**Walkthrough Order:** Auth Flow → Task Management → User Isolation → Data Persistence

**Related Documentation:**
- **[test_execution_guide.md](test_execution_guide.md)** — Comprehensive manual testing procedures (34 test cases, critical path validation)
- **[playbook.md](playbook.md)** — Operational setup and configuration guide
- **[README.md](README.md)** — Quick-start reference

---

## Pre-Demo Checklist (DO THIS BEFORE PRESENTATION)

- [ ] **Start Application**
  ```bash
  cd /home/domna/COMP4442-semester-project-Group14
  mvn spring-boot:run
  # Or: java -jar target/cloud-compute-service-0.0.1-SNAPSHOT.jar
  ```
  Wait for message: "Started CloudComputeServiceApplication in X.XXX seconds"

- [ ] **Verify Application is Running**
  - Open browser: http://localhost:8080
  - Expected: Home page with "Cloud Compute Service" and navigation links
  - Open Swagger API docs: http://localhost:8080/swagger-ui.html
  - Expected: All endpoints visible

- [ ] **Clear Browser Cookies/Session (Important!)**
  - Press `Ctrl+Shift+Delete` (or Cmd+Shift+Delete on Mac)
  - Delete all cookies for localhost
  - This ensures demo starts with unauthenticated state

- [ ] **Have Browser Windows Ready**
  - **Tab 1:** http://localhost:8080 (home page)
  - **Tab 2:** http://localhost:8080/swagger-ui.html (API reference)
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
> "After registration, users log in. Spring Security creates a session that persists across all subsequent requests. The session is stored in the browser (HttpSession) and validated on each API call."

**Action Steps:**

1. **Login as Alice** (0:00 - 1:00)
   - Fill login form with:
     - Username: `alice`
     - Password: `AlicePass123!`
   - Click "Login"
   - Page redirects to http://localhost:8080/task.html
   - Show "Signed in as alice" message at top of page
   - **Speaking:** "Upon login, AuthService calls Spring's AuthenticationManager to verify credentials against the database. If valid, a SecurityContext is created and stored in the HTTP session."

2. **Show Session Cookie** (1:00 - 1:30)
   - Open Browser Dev Tools (F12)
   - Go to Storage → Cookies → localhost:8080
   - Show `JSESSIONID` cookie
   - **Speaking:** "This cookie maintains the authenticated session. All subsequent API requests send this cookie, and Spring Security automatically validates it."

3. **Inspect Browser Network Traffic** (1:30 - 2:00)
   - Open Network tab in Dev Tools
   - Refresh task.html
   - Show GET request with Cookie header containing JSESSIONID
   - Show response: GET /api/v1/auth/me returns current user info
   - **Speaking:** "Behind the scenes, task.html calls GET /api/v1/auth/me to verify the session is still valid. If no session, it automatically redirects to login.html."

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
   - Open Swagger UI: http://localhost:8080/swagger-ui.html
   - Expand "Authentication Endpoints" section
   - Show all endpoints: /auth/register, /auth/login, /auth/me, /auth/logout
   - Expand "Task Endpoints"
   - Show: POST /tasks, GET /tasks, GET /tasks/{id}, PUT /tasks/{id}, DELETE /tasks/{id}
   - **Speaking:** "All endpoints are documented in Swagger. The 401 responses show that authentication is required for protected endpoints."

2. **Optional: Run Postman** (0:45 - 1:00)
   - (Only if time permits) Show sample curl command for login:
     ```bash
     curl -X POST http://localhost:8080/api/v1/auth/login \
       -H "Content-Type: application/json" \
       -c cookies.txt \
       -d '{"username": "bob", "password": "BobPass456!"}'
     ```
   - Show JSON response with user info
   - **Speaking:** "REST APIs return structured JSON. The session is managed via HTTP cookies."

---

### **SEGMENT 7: Summary & Architecture Diagram (0.5 Minutes)**

**Speaking Points:**

> "Let me summarize the architecture:

1. **Frontend:** Multi-page HTML UI (login, register, task list, edit task)
2. **Backend:** Spring Boot with Spring Security for authentication
3. **Business Logic:** Service layer with user-scoped CRUD operations
4. **Data Access:** Spring Data JPA repositories with user filtering
5. **Persistence:** SQL database (H2 for dev, MySQL/PostgreSQL for prod)
6. **Security:** BCrypt password hashing, session-based auth, user-scoped queries

All components work together to provide secure, isolated multi-user task management."

---

## Troubleshooting During Demo

### Issue: Application is slow to start
**Solution:** Start application **30 seconds before demo begins**. Show Swagger docs while loading.

### Issue: Accidentally logged out
**Solution:** Press back button or type URL directly to login page. Re-login quickly.

### Issue: Forgot task ID or data got mixed up
**Solution:** Open H2 console (http://localhost:8080/h2-console) and manually view database tables in real-time:
```sql
SELECT * FROM app_user; -- View all registered users
SELECT * FROM task;      -- View all tasks
```

### Issue: Browser shows "Connection refused"
**Solution:** Open terminal and check if app is still running. If needed, restart:
```bash
mvn spring-boot:run
```

### Issue: Cannot login (invalid credentials)
**Solution:** Double-check username/password. If unsure, register a new test account on the spot.

---

## Quick Reference - Demo URLs

| Component | URL |
|-----------|-----|
| Home Page | http://localhost:8080 |
| Register Page | http://localhost:8080/register.html |
| Login Page | http://localhost:8080/login.html |
| Task Page | http://localhost:8080/task.html |
| Swagger Docs | http://localhost:8080/swagger-ui.html |
| H2 Console | http://localhost:8080/h2-console |

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

---

## Time Allocation

```
Total: 12 minutes

Segment 1 (Auth & Registration):  3 min → 3 min elapsed
Segment 2 (Login & Session):      2 min → 5 min elapsed
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
> - **Secure Authentication:** BCrypt password hashing, session management
> - **User Isolation:** User-scoped database queries prevent cross-user data access
> - **REST API:** Well-documented endpoints with proper HTTP status codes
> - **Clean UI:** Responsive HTML pages with real-time feedback
>
> The application is production-ready and can be deployed on AWS EC2 with RDS for scalable, reliable task management across many users."

---

**Demo Version:** 1.0  
**Last Updated:** 2026-03-28  
**Estimated Demo Time:** 12 minutes + Q&A
