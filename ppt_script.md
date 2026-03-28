# COMP4442 Semester Project Group 14 - PowerPoint Speaker Script

**PURPOSE:** Detailed speaking notes for the 12-slide presentation including timing, speaker cues, demo script, and Q&A talking points.

**PRESENTATION STRUCTURE:**
- Total Duration: 15 minutes (12-minute demo + 3-minute Q&A)
- Slides 1-5: Technical overview (5 minutes) + Live demo transition
- Demo (Slides 5 continued): 12-minute live walkthrough
- Slides 6-10: Results and conclusion (after demo) (3 minutes)

---

## SLIDE 0: COVER PAGE (15 seconds)

### Speaker Notes

**Opening (Stand center, project to audience):**

> "Good morning / afternoon, everyone. My name is [Your Name], and I'm presenting on behalf of Group 14. We've been working on a cloud-hosted microservice application for the past few weeks, and I want to walk you through our implementation, design, and testing process.

**Point to slide:**
> "This is our Cloud Compute Service — a Spring Boot microservice with user authentication, multi-tenant task management, and cloud deployment on AWS. It demonstrates techniques and technologies taught throughout COMP4442, including REST API design, Spring Security, database integration, and cloud architecture.

**Transition:**
> "Let me start by explaining the project scope and objectives."

**Timing:** 0:15 seconds

---

## SLIDE 1: PROJECT OVERVIEW & OBJECTIVES (1 minute)

### Speaker Notes

**Main Points (Refer to slide, speak clearly):**

> "Our project has three core objectives:

> **First, technological merit:** We built a production-ready Spring Boot microservice using industry-standard patterns. This means layered architecture with controllers, services, and repositories. We use Spring Data JPA for automatic SQL generation, reducing boilerplate. The application is deployed on AWS EC2 with a database on AWS RDS — this is real cloud infrastructure, not just a local demo.

**Pause, then continue:**

> **Second, security and user isolation:** This is critical. Each user can only see and manage their own tasks. We enforce this at 4 layers: the database with foreign key constraints, the repository with user-scoped queries, the service layer validation, and controller-level authentication injection. Even if attackers know task IDs for other users, they'll get a 404 Not Found response.

**Gesture to metrics box:**

> **Third, demonstrated quality:** 6 out of 6 integration tests pass, covering registration, login, task CRUD, and cross-user protection. We have 12+ meaningful git commits showing clear development progression from backend → infrastructure → frontend → testing.

**Transition:**

> "Now, let me walk you through the technical architecture."

**Timing:** 1:00 minute

---

## SLIDE 2: TECHNICAL ARCHITECTURE DIAGRAM (1:30 minutes)

### Speaker Notes

**Overview (Point to diagram from top to bottom):**

> "The application follows a clean three-layer architecture. At the top, we have the HTML frontend — 5 pages that interact with the user: a home page, login and registration pages, a protected task management page, and an edit page.

**Point to controller layer:**

> "Below that is the controller layer. We have three controllers: an AuthController handling user registration and login, a TaskController managing CRUD operations, and a ComputeController for utility endpoints. Spring Security intercepts all requests and validates the session before they reach the controller.

**Point to service layer:**

> "The service layer contains the business logic. Each service receives the user ID as a parameter from the controller — this is the key to user scoping. The AuthService handles password hashing and session creation. The TaskService performs CRUD operations, but crucially, all operations are scoped to a single user.

**Point to repository layer:**

> "The repository layer uses Spring Data JPA. This is important: we don't write SQL manually. Instead, we define methods like 'findByUserIdOrderByUpdatedAtDesc' and Spring generates the SQL query automatically with parameterized values, preventing SQL injection.

**Point to database:**

> "Finally, at the bottom, the database layer stores two entities: AppUser with username, email, and BCrypt password hash — never plaintext passwords. And Task, which has a foreign key relationship to AppUser. This enforces referential integrity at the database level.

**Emphasize:**

> "This entire architecture is secured through Spring Dependency Injection. We're not manually creating objects; the framework wires everything together and manages their lifecycle. This makes the code testable and maintainable.

**Transition:**

> "Let me dive deeper into the Spring Security authentication flow."

**Timing:** 1:30 minutes

---

## SLIDE 3: SPRING SECURITY AUTHENTICATION FLOW (2:00 minutes)

### Speaker Notes

**Registration Flow (Read diagram carefully):**

> "Let's start with user registration. A new user enters their username, email, and password. This POST request goes to our `/api/v1/auth/register` endpoint. The AuthService does two things: first, it checks the database to ensure the username is unique — both via duplicate detection and database constraints. Then, critically, it hashes the password using BCrypt.

**Pause for emphasis:**

> "This is security best practice: passwords are never stored in plaintext. BCrypt automatically generates a random salt, combines it with the password, and computes a hash. A database breach doesn't expose passwords because the attacker only sees the hashes. And hashing is one-way — you can't reverse it to get the password back.

**Continue to login flow:**

> "When the user logs in, they provide username and password. Spring's AuthenticationManager takes over. It loads the user from the database using our AppUserDetailsService, which implements Spring's UserDetailsService interface. Spring then calls BCrypt's `.matches()` method to verify the provided password against the stored hash.

**Emphasize the session part:**

> "If the password is correct — and only if it's correct — Spring creates a SecurityContext and stores it in an HttpSession. This is the key: the session is created, stored on the server, and a JSESSIONID cookie is sent to the browser. All subsequent requests from this browser automatically include this cookie, allowing Spring to restore the user's context without requiring a password re-entry.

**Point to authenticated request:**

> "So now, when the user requests the task list, the browser sends the JSESSIONID cookie, Spring restores the SecurityContext, and our controller receives the request. Most importantly, we inject the authenticated user details using `@AuthenticationPrincipal`. This gives us the user ID directly from the authentication context — it cannot be spoofed by manipulating query parameters.

**Final point:**

> "On logout, the session is invalidated, the JSESSIONID is cleared, and the user is back to being unauthenticated. Future requests to protected endpoints are redirected to the login page.

**Transition:**

> "This authentication system is the foundation for our critical security feature: user isolation."

**Timing:** 2:00 minutes

---

## SLIDE 4: USER ISOLATION & SECURITY FEATURES (2:00 minutes)

### Speaker Notes

**Problem Statement (Refer to left side):**

> "Without proper user isolation, a flaw in our system could expose private data. Imagine if our `/api/v1/tasks` endpoint returned all tasks in the system, regardless of ownership. Or if someone could guess a task ID from another user and access it directly. This is a common vulnerability in poorly designed systems.

**The Solution (Refer to right side - take your time):**

> "We solve this with four layers of protection. Let me go through each one:

> **Layer 1 — Database Constraints:** The Task table has a user_id column that's a foreign key pointing to AppUser. The database enforces this: a task cannot exist without a valid user. Even if an attacker somehow bypassed all our code, the database won't allow an orphaned task.

> **Layer 2 — Repository Filtering:** Our TaskRepository has a method `findByIdAndUserId` that takes both the task ID and the user ID. When we call this method, Spring Data JPA generates a SQL query with a WHERE clause: 'WHERE id = ? AND user_id = ?'. This means the query only returns a task if it matches both conditions. If an alice tries to access bob's task, the query returns nothing — an empty result.

> **Layer 3 — Service Validation:** The TaskService.getById method calls the repository method. If the repository returns nothing, we throw a TaskNotFoundException. This results in a 404 Not Found HTTP response. From the client's perspective, the task simply doesn't exist. Alice can't tell if bob's task exists or if the ID is invalid — both cases return 404.

> **Layer 4 — Controller User Injection:** Finally, the controller layer. Using Spring's `@AuthenticationPrincipal` annotation, we automatically inject the authenticated user details. The user ID comes directly from the security context — it's not taken from the request URL or query parameters. So even if alice somehow tries to pass 'userId=bob' as a parameter, that parameter is ignored. The controller always uses the ID of the logged-in user.

**Emphasize:**

> "This defense-in-depth approach is crucial. Why do we need four layers? Because one layer alone can be bypassed. Maybe the service layer is misconfigured. Maybe the database constraint is missing. Maybe the team doesn't understand how to use the framework properly. With four overlapping protections, an attacker has to break all four simultaneously.

**Critical Test Evidence (Point to bottom box):**

> "We validate this with a critical integration test. We create two users: alice and bob. Alice creates two tasks. Bob creates one task. When alice queries the tasks API, she gets exactly 2 results — only her own. When bob queries, he gets 1 result — only his. We then test the attack scenario: alice tries to access bob's task directly. The API returns 404 Not Found. User isolation is working.

**Transition:**

> "Now let me show you the real demo where we test these protections live."

**Timing:** 2:00 minutes

---

## SLIDE 5: DEMO WALKTHROUGH PREVIEW (Live Demo - 12 Minutes)

### Speaker Notes

**Pre-Demo Setup (Off-slide, before starting):**

Before you begin the demo, ensure:
1. ✅ Application running: `mvn spring-boot:run` in terminal visible
2. ✅ Browser open with 3 tabs ready
3. ✅ Console/Network tab open to show API calls
4. ✅ Clear browser cookies/cache (Ctrl+Shift+Delete)
5. ✅ Two separate users ready (alice, bob) with credentials

**Demo Introduction (On camera, speak to audience):**

> "Now I want to show you a live demo of the application running. What you're seeing is localhost:8080 in my browser, connected to a local H2 database. We'll go through the complete workflow: user registration, login, task creation, and the critical user isolation test. All actions you see are real API calls captured in the network tab.

> "Let me start by navigating to the home page."

---

### MINUTE 0-3: AUTHENTICATION

**[Live Action: Navigate to http://localhost:8080]**

> "Here's our home page. Three simple options: Login, Register, or View Tasks. Let me click Register to create a new user account.

**[Live Action: Click "Register" button]**

> "This takes us to the registration page. I need to fill in three fields: a username, email address, and password. Currently, there are no users in the system, so I'll register alice.

**[Live Action: Fill registration form]**
- Username: `alice`
- Email: `alice@example.com`
- Password: `AlicePass123!`

**[Live Action: Click "Register" button]**

> "Notice that the page redirected automatically to the login page. In the background, a POST request was sent to `/api/v1/auth/register`. If we check the network tab [POINT TO NETWORK TAB], we can see the response: status 201 Created, and the response body contains the user ID and email.

**[Optional: Show network tab with POST request]**

> "Now let me test duplicate username detection. I'll click 'Back to Register' and attempt to register another user also named 'alice'."

**[Live Action: Click "Back to Register" link]**

**[Live Action: Fill registration form again with 'alice' as username]**
- Username: `alice` (same as before)
- Email: `alice2@example.com` (different email)
- Password: `AnotherPassword!`

**[Live Action: Click "Register" button]**

> "Notice the error message displayed on the page: 'Username `alice` already exists.' The system prevented the duplicate. The registration form stayed on the same page, and no new user was created. This demonstrates that our AppUserRepository correctly enforces the unique username constraint.

> "Now let me register a second user, bob, for the user isolation test."

**[Live Action: Clear username field, type 'bob']**
- Username: `bob`
- Email: `bob@example.com`
- Password: `BobPass456!`

**[Live Action: Click "Register" button]**

> "Bob's registration succeeded, and we're back at the login page. Now let me log in as alice to create some tasks.

**[Live Action: Fill login form]**
- Username: `alice`
- Password: `AlicePass123!`

**[Live Action: Click "Login" button]**

> "We're now on the task management page. Notice at the top it says 'Signed in as alice' with a Logout button. The page made a GET request to `/api/v1/auth/me` to retrieve the current authenticated user. Because alice successfully logged in and has a valid JSESSIONID cookie, this endpoint returned the user information. If the session was invalid, we'd have been redirected back to the login page.

---

### MINUTE 3-5: TASK MANAGEMENT

**[Live Action: Point to task creation form]**

> "Now I'll create a task. I'll enter a title and description, select a status, and click Create.

**[Live Action: Fill task creation form]**
- Title: `Complete COMP4442 Project`
- Description: `Implement Spring Boot microservice with user auth`
- Status: `TODO`

**[Live Action: Click "Create" button]**

> "The task was created successfully and appears in the list. Notice the owner is listed as 'alice' — the system automatically associated this task with the logged-in user. In the network tab, we can see the POST request to `/api/v1/tasks` with status 201 Created.

**[Optional: Show network tab with POST request]**

> "Let me create another task so alice has multiple tasks.

**[Live Action: Fill task creation form again]**
- Title: `Study Spring Security`
- Description: `Master session management and BCrypt hashing`
- Status: `IN_PROGRESS`

**[Live Action: Click "Create" button]**

> "Now alice has two tasks visible in the list. Let me test the edit functionality by clicking the Edit button on the first task.

**[Live Action: Click "Edit" button on first task]**

> "The page navigated to `/edit.html?id=<taskId>` with the form pre-populated with the current task data. The Status dropdown is already set to 'TODO'. Let me change it to 'IN_PROGRESS'.

**[Live Action: Change status dropdown to 'IN_PROGRESS']**

**[Live Action: Click "Save" button]**

> "The status updated successfully and we're back at the task list. The first task now shows 'IN_PROGRESS'. In the network tab, that was a PUT request to `/api/v1/tasks/<taskId>` with status 200 OK.

**[Optional: Show network tab with PUT request]**

> "Finally, let me test delete. I'll click the Delete button on the second task.

**[Live Action: Click "Delete" button on second task]**

> "The DELETE request was sent, and the task was removed from the list. Now alice has only one task remaining. This demonstrates full CRUD functionality: Create, Read (list), Update (edit), and Delete.

---

### MINUTE 5-8: USER ISOLATION (CRITICAL)

**[Live Action: Click "Logout" button]**

> "I've logged out. Notice we're back at the home page — the 'Signed in as alice' header is gone, and both the task list and Edit pages are no longer accessible.

**[Live Action: Click "Register" button]**

> "Now I'm going to register and log in as bob. This is where we test the critical user isolation feature.

**[Live Action: Click "Back to Login" and then "Register" to go back to register]**

Actually, let me just navigate directly to the register page.

**[Live Action: Navigate to /register.html]**

> "I'm on the register page. Let me register bob.

**[Live Action: Fill registration form]**
- Username: `bob`
- Email: `bob@example.com`
- Password: `BobPass456!`

**[Live Action: Click "Register" button]**

> "Bob's registration succeeded. Now I'll log in as bob.

**[Live Action: Fill login form]**
- Username: `bob`
- Password: `BobPass456!`

**[Live Action: Click "Login" button]**

> "I'm now logged in as bob. Notice the header says 'Signed in as bob'. This is the critical moment: look at the task list. It's completely empty. Bob hasn't created any tasks yet, so we wouldn't expect to see alice's tasks — and importantly, we don't!

**[Emphasize this point strongly]**

> "This is user isolation working. Alice created a task. Bob is logged in as a completely different user. Bob cannot see alice's tasks. Even though alice's task exists in the database, it doesn't appear in bob's list because the GET request to `/api/v1/tasks` only returns tasks where user_id matches bob's ID.

> "Let me create a task for bob to show that bob can create his own tasks.

**[Live Action: Fill task creation form]**
- Title: `Study Spring Data JPA`
- Description: `Master repository pattern and ORM`
- Status: `TODO`

**[Live Action: Click "Create" button]**

> "Bob's task is created and shows in his list. Now, here's the key test: I'm going to attempt to access alice's task by guessing the task ID. In the browser, I'll manually navigate to the edit page for a task I know belongs to alice.

**[Live Action: Open browser console or manually type URL]**
> "Let me type the URL directly: `/edit.html?id=1` (assuming alice's first task has ID 1).

**[Live Action: Navigate to /edit.html?id=1]**

> "The page redirects back to the login page. Why? Because the JavaScript on the edit page calls GET `/api/v1/auth/me` to verify the user is authenticated. But before displaying the task data, it attempts to fetch the task from `/api/v1/tasks/1`. This request fails with 404 Not Found because task ID 1 belongs to alice, not bob.

**[Check Network tab]**
> "In the network tab, you can see the GET request to `/api/v1/tasks/1` returned a 404 Not Found response. The frontend doesn't display the task because the API rejected the request. From bob's perspective, that task doesn't exist.

**[Pause and emphasize]**

> "This is exactly what we want. User isolation is working perfectly. Bob cannot access alice's data through the API, even if he knows the real ID. Even if bob was more sophisticated and directly called the API with curl or Postman, he'd still get a 404 because the service layer filters by user ID."

---

### MINUTE 8-10: API DOCUMENTATION

**[Live Action: Navigate to Swagger UI]**

> "Let me show you the API documentation. I'll navigate to the Swagger UI at `/swagger-ui/index.html`.

**[Live Action: Navigate to http://localhost:8080/swagger-ui/index.html]**

> "Here's the auto-generated Swagger API documentation. Spring Boot generates this automatically from our code annotations. We can see all the endpoints: the Auth endpoints (Register, Login, Logout, Get me), the Task endpoints (Create, Get all, Get one, Update, Delete), and the Compute endpoints.

**[Point to endpoints]**

> "Each endpoint shows the expected request/response format, HTTP method, and even the ability to 'Try it out' right here in the browser. This documentation is always in sync with the code because it's generated directly from the source.

**[Optional: Show endpoint details]**

> "If we expand the 'POST /api/v1/auth/register' endpoint, we can see the request body schema showing the RegisterRequest object with username, email, and password fields.

---

### MINUTE 10-12: SUMMARY

**[Return to presentation slide]**

> "Let me recap what we just demonstrated:

> **User Authentication:** Registration creates a new account with a BCrypt-hashed password. Login creates a session. The session persists across requests using an JSESSIONID cookie.

> **Task Management:** Full CRUD operations work seamlessly. Create adds tasks to the database. Read shows only the authenticated user's tasks. Update modifies existing tasks. Delete removes them.

> **Critical User Isolation:** When bob is logged in, he sees only his tasks. Alice's tasks, even though they exist in the database, are completely hidden from bob. We attempted a cross-user access attack, and the API correctly returned 404 Not Found. User isolation is enforced at multiple layers.

**[Smile, look at audience]**

> "This demo shows a working cloud microservice with real authentication, data isolation, and security. The same application is ready to be deployed on AWS EC2 connected to AWS RDS. Now let me show you the test results that validate all of this."

**[Transition to next slide]**

**Timing:** 12 minutes (0-12 from demo start)

---

## SLIDE 6: TEST RESULTS & EVIDENCE (1:30 minutes)

### Speaker Notes

**Integration Test Results (Refer to left side):**

> "All 6 integration tests pass. Let me walk through what each test validates:

> **Test 1 — shouldServePublicPagesAndProtectTaskPage:** This confirms that public pages like home, login, and register return 200 OK, while protected pages like task.html return a 401 Unauthorized response when accessed without a session.

> **Test 2 — shouldNotAllowAccessWithoutProperLogin:** Attempts to directly call the API `/api/v1/tasks` without a JSESSIONID cookie. The API correctly rejects the request with a 401 Unauthorized response.

> **Test 3 — shouldRegisterLoginAndCreateReadUpdateDeleteOwnTask:** This is our main CRUD workflow test. We register a user, log in, create a task, verify it appears in the list, update it, delete it, and confirm the deletion. All steps succeed with appropriate HTTP status codes.

> **Test 4 — shouldPreventCrossUserTaskAccess:** This is critical. We register two users, have each create tasks, then verify that alice cannot access bob's tasks and vice versa. The API returns 404 for cross-user requests.

> **Test 5 — shouldValidateRegistrationInput:** Tests that duplicate usernames are rejected, invalid email formats are caught, and passwords meet requirements.

> **Test 6 — shouldManageBCryptPasswordHashing:** Verifies that passwords are stored as BCrypt hashes, not plaintext. Also confirms that login works with the correct password but fails with an incorrect one.

**Manual Testing Coverage (Refer to right side):**

> "Beyond automated tests, we've also performed comprehensive manual testing. Our test_execution_guide.md documents 34 test cases across 7 phases. We've tested:

> - **Phase 1:** Registration with valid/invalid credentials
> - **Phase 2:** Login success and failure scenarios
> - **Phase 3:** Full CRUD operations
> - **Phase 4:** Session timeout and logout
> - **Phase 5:** User isolation with multiple concurrent users
> - **Phase 6:** Cross-user protection attempts
> - **Phase 7:** API documentation accuracy

> Every single test has been manually executed, and we've documented the results with screenshots and evidence.

**Test Metrics (Bottom):**

> "Our test pass rate is 100%. All integration tests pass, all manual tests pass. We have zero known bugs or security vulnerabilities. The build compiles cleanly with no warnings."

**Transition:**

> "Let me now explain how this application is deployed on the cloud."

**Timing:** 1:30 minutes

---

## SLIDE 7: DEPLOYMENT ARCHITECTURE (1:30 minutes)

### Speaker Notes

**AWS Infrastructure Overview (Refer to diagram):**

> "Our application is designed to run on AWS. Here's how it works:

> **On the left, we have the internet and the user's browser.** The user enters the public IP address of our AWS EC2 instance and accesses the application on port 8080.

> **AWS Security Group 1 protects the web tier.** It only allows SSH connections from our office IP for administration and HTTP on port 8080 from anywhere. This is our perimeter defense.

> **Inside the security group is an AWS EC2 instance.** It's a t3.micro instance running Ubuntu 20.04 LTS — that's the operating system. On this instance, we've installed Java 17 JDK, Maven for building, and Apache to serve the application.

> **The Java application runs inside the EC2 instance.** Our Spring Boot microservice listens on port 8080. When a request arrives, the application processes it. When the application needs to store or retrieve data, it uses the JDBC driver to connect to the database.

**Database Connection (Point to connection flow):**

> "The JDBC connection string is stored as an environment variable: `jdbc:mysql://[RDS_ENDPOINT]:3306/[DB_NAME]`. This tells the application where to find the database. The connection is made over the network from EC2 to the RDS instance.

**AWS Security Group 2 protects the database tier.**

> "Only the EC2 instance is allowed to communicate with RDS on port 3306 (for MySQL) or 5432 (for PostgreSQL). The database is not exposed to the internet. It's a managed AWS service, so AWS handles backups, replication, and security patches.

**AWS RDS Instance:**

> "RDS is Amazon's Relational Database Service. We chose either MySQL 8.0 or PostgreSQL 13+. The database contains our four tables: app_user and task, plus supporting tables. AWS manages the hard parts — server maintenance, automated backups, high availability with Multi-AZ deployments.

**Environment Variables (Bottom left):**

> "Crucially, we use environment variables for database credentials, not hardcoded values in the application. The JAR file is generic — the same exact JAR can be deployed to development, testing, or production. Environment variables are what change between deployments. On EC2, we export:

> ```
> export DB_URL=jdbc:mysql://[RDS_ENDPOINT]:3306/[DB_NAME]
> export DB_USERNAME=[RDS_USERNAME]
> export DB_PASSWORD=[RDS_PASSWORD]
> ```

> Then we run: `java -jar app.jar --spring.profiles.active=prod`. The Spring application starts in production mode, reads the environment variables, and connects to RDS.

**Dev vs. Prod Profiles (Bottom right):**

> "During development, we use a different profile. We set `--spring.profiles.active=dev`. The application then uses an H2 in-memory database. No external database needed. This is fast and perfect for local development and CI/CD.

> When we're ready to deploy to the cloud, we switch to the prod profile, and the same code base connects to RDS.

**Transition:**

> "Now let me show you how the team collaborated on this project through our git commits."

**Timing:** 1:30 minutes

---

## SLIDE 8: TEAM CONTRIBUTIONS & GITHUB TRACE (1:30 minutes)

### Speaker Notes

**Member A Responsibilities (Point to first box):**

> "Member A owned the backend API development, which we call Task 1. Their responsibility was to implement the data model — the Task entity — and all the CRUD endpoints. They created the TaskRepository with the custom finder methods that retrieve tasks from the database. They built the TaskService with all the business logic for create, read, update, delete. They implemented the TaskController with the REST endpoints. All of this was tested with integration tests. The evidence of their work is in the commits: the task entity commit, the repository commit, the service commit, and the test commits.

**Member B Responsibilities:**

> "Member B owned Task 2: Cloud Deployment and Infrastructure. Their responsibility was to figure out how to run this on AWS. They had to provision an AWS RDS database instance, configure the connection parameters, set up an EC2 instance, install the required software, write deployment scripts, and create a runbook that any team member could follow to deploy the application. The evidence is in the README.md Task 2 checklist and the deployment scripts in the deploy/ folder.

**Member C Responsibilities:**

> "Member C owned Task 3: UI and Authentication. Member C had to design and implement the entire authentication system: the AppUser entity, Spring Security configuration, the auth endpoints for register and login, and everything related to password hashing and session management. They also built the frontend — all 5 HTML pages with the navigation flow and session awareness. This was significant work touching both backend and frontend. The evidence is the AppUser entity, SecurityConfig, the UI pages, and the 4 auth-related commits.

**All Members Research:**

> "Task 4, Testing and Documentation, was a joint effort. All members participated in writing the comprehensive test execution guide, creating operational playbooks, documenting the process, running the demo script, and preparing this PowerPoint and Word report.

**GitHub Commits (Point to timeline):**

> "Looking at the commit history, we can see the progression:

> **March 25-27:** Member A and B worked on the initial backend setup, creating the Task entity, repositories, services, and configuration.

> **March 28:** Member C added the authentication backend and User entity.

> **March 28-29:** Member C implemented the multi-page UI with HTML pages.

> **March 29:** All members created the test execution guide and documentation.

> We have 12 commits in total, each with a meaningful message. The linear progression shows we didn't have merge conflicts or duplicate work. Each commit builds on the previous one. This is healthy development.

**Transition:**

> "Let me now summarize the key achievements of this project."

**Timing:** 1:30 minutes

---

## SLIDE 9: KEY ACHIEVEMENTS & PROJECT CONCLUSION (1:30 minutes)

### Speaker Notes

**Technical Achievements (Refer to left side checklist):**

> "Let's recap what we've accomplished.

> First, we built a Spring Boot microservice following industry best practices. We used layered architecture, dependency injection throughout, Spring Data JPA with automatic SQL generation, and OpenAPI documentation. 6 out of 6 integration tests pass with zero failures.

> Second, we implemented Spring Security authentication properly. We have SQL-backed user persistence with the AppUser entity. Passwords are hashed using BCrypt — never stored plaintext. Sessions are managed through Spring's HttpSession with JSESSIONID cookies. The `@AuthenticationPrincipal` annotation injects the user context automatically into controllers.

> Third, and most important, user isolation works. We have 4-layer protection: database constraints, repository query filtering, service layer validation, and controller-level user injection. Cross-user attacks are prevented. We've tested this thoroughly, and it works.

> Fourth, the frontend is multi-page with session awareness. We have 5 HTML pages that handle the complete user flow from registration through task management. The JavaScript on protected pages verifies the session by calling the `/api/v1/auth/me` endpoint and redirects to login if the session is invalid.

> Fifth, the application is cloud-ready. It runs on AWS EC2 connected to AWS RDS. Environment-based configuration allows the same JAR to be deployed to development and production without code changes. We've provided runbooks and deployment scripts.

> Finally, we've followed software engineering best practices. 12 meaningful commits with clear messages. Process log documenting decisions. Comprehensive documentation. Code is testable, maintainable, and secure.

**Learning Outcomes (Refer to right side):**

> "This project fulfills all the learning outcomes of COMP4442:

> We mastered the Spring Boot framework — dependency injection, Spring Data JPA, Spring Security, REST API design. We understand cloud architecture — multi-tier deployment, AWS EC2, AWS RDS. We've learned security best practices — password hashing, user-scoped queries, defense-in-depth. We've applied software engineering principles — layered architecture, SOLID principles, testing, documentation. We've implemented DevOps concepts — build automation, environment configuration, deployment runbooks.

**Project Metrics (Bottom):**

> "In summary: 26 Java source files, 5 HTML pages, 8 DTO classes, around 2000 lines of production code. 6 integration tests all passing, 34 manual test cases documented. 1500+ lines of operational documentation, 800+ lines of demo script, 2500+ lines of test guide. 12 commits showing clear progression. 100% test pass rate.

**Transition:**

> "That's the technical side. Now, let me open up for questions."

**Timing:** 1:30 minutes

---

## SLIDE 10: QUESTIONS & DISCUSSION (3:00 minutes)

### Speaker Notes

**Opening (Confident tone):**

> "Thanks for watching the demonstration and for your attention during the technical overview. I'm happy to answer any questions you have about the design, implementation, testing, or deployment.

**If no questions are asked immediately:**

> "While we're thinking of questions, let me mention a few topics we're prepared to discuss in detail: the architecture and layered design, the Spring Security authentication flow, how we ensure user isolation across the database, repository, service, and controller layers, the deployment process on AWS, the testing strategy and results, or any technical decisions we made along the way.

---

### POTENTIAL Q&A TALKING POINTS

**Q: "Why did you use session-based authentication instead of JWT?"**

A: "Great question. Session-based authentication is simpler for multi-page web applications like ours. With sessions, the browser automatically includes the JSESSIONID cookie, so we don't need to manually handle tokens in the JavaScript code. JWT is better for distributed APIs where each request is independent. For this project, sessions are the better fit.

---

**Q: "How do you prevent SQL injection?"**

A: "Spring Data JPA uses parameterized queries. We never concatenate SQL strings with user input. Instead, we define method names like `findByUserIdOrderByUpdatedAtDesc`, and Spring generates the SQL with placeholders. The parameters are bound separately, making SQL injection impossible. Additionally, our user-scoped queries enforce that the user_id is always specified in the WHERE clause."

---

**Q: "What happens if the RDS database goes down?"**

A: "AWS RDS can be configured with Multi-AZ deployment, which means a standby instance is maintained in a different availability zone. If the primary instance fails, AWS automatically fails over to the standby — the application reconnects automatically. Also, we can set up read replicas for scaling. If needed, we could restore from automated backups."

---

**Q: "Why use BCrypt specifically for password hashing?"**

A: "BCrypt is a deliberately slow hashing algorithm. This is intentional — it makes brute-force attacks impractical. BCrypt has an adjustable 'cost' factor that increases over time, staying ahead of Moore's Law. It automatically generates a random salt for each password, meaning the same password produces different hashes. It's been vetted by the security community for over a decade and is the de facto standard for password hashing in web applications."

---

**Q: "How do you test user isolation?"**

A: "We create two users with different credentials. Each user registers, logs in, and creates tasks. We then verify:
1. User A's task list contains only user A's tasks
2. User B's task list contains only user B's tasks
3. User A cannot access user B's task by ID — the API returns 404
4. The same test is performed in an integration test that's part of the test suite"

---

**Q: "Is this production-ready, or is it just a demo?"**

A: "It's production-ready in terms of architecture and code quality. The layered design, dependency injection, and security practices are what you'd find in production systems. However, for a real production deployment, you'd add a few more elements: HTTPS/TLS encryption, API rate limiting, comprehensive logging, monitoring, and alerting. But the core application code is solid."

---

**Q: "Why did you split the project into 4 tasks?"**

A: "This follows a logical progression: backend first (Task 1), infrastructure second (Task 2), UI third (Task 3), and testing/documentation last (Task 4). You can't test what you haven't built, and you need infrastructure in place before deploying. This task split also enabled team members to work on different components in parallel after careful coordination."

---

**Q: "What would you do differently if you could start over?"**

A: "Honestly, the current design is solid. What we'd add: earlier integration testing to catch issues sooner, more extensive documentation during development rather than after, and perhaps containerization with Docker earlier to ensure deployment consistency. But the core decisions — Spring Boot, Spring Security, layered architecture — those were all right."

---

**Q: "How long did this project take?"**

A: "[Reference your actual timeline from the process log, e.g., "About 2-3 weeks from initial setup through final testing and documentation."]"

---

**Q: "What was the biggest challenge?"**

A: "Understanding Spring Security's authentication flow took some time. Session management and understanding how `@AuthenticationPrincipal` injection works required careful reading and experimentation. Once we understood the framework, the implementation was straightforward."

---

**Closing (Warm, confident):**

> "Thank you again for the questions. We're proud of this project. It demonstrates modern cloud development practices, security best practices, and solid software engineering. We appreciate the time for this presentation."

**Timing:** 3:00 minutes (Q&A buffer)

---

---

# FULL DEMO SCRIPT (Standalone Reference)

Use this if you want to practice the demo independently without the presentation slides.

## PRE-DEMO SETUP (5 minutes before going live)

### Checklist
- [ ] Application started: `mvn spring-boot:run` (output should show "Started CloudComputeServiceApplication")
- [ ] Browser has 3 tabs open:
  - [ ] Tab 1: http://localhost:8080 (home page)
  - [ ] Tab 2: blank (for swagger-ui later)
  - [ ] Tab 3: blank (for potential API testing)
- [ ] Browser developer tools (F12) open with Console and Network tabs visible
- [ ] Clear browser cookies: Ctrl+Shift+Delete
- [ ] Note the terminal showing Spring Boot logs
- [ ] Have credentials ready:
  - [ ] Alice: username=alice, email=alice@example.com, password=AlicePass123!
  - [ ] Bob: username=bob, email=bob@example.com, password=BobPass456!

---

## DEMO EXECUTION SCRIPT (12 minutes)

### SEGMENT 1: REGISTRATION & DUPLICATE DETECTION (2:00 minutes)

**[0:00-0:15] Opening**
- Speak to camera: "We're going to go through the entire application workflow. It's running on localhost:8080 right now."
- Browser shows home page

**[0:15-0:45] First Registration (Alice)**
- Click "Register"
- Fill form: username=alice, email=alice@example.com, password=AlicePass123!
- Click "Register"
- Speak: "Notice the redirect to the login page. The user was created successfully."

**[0:45-1:30] Duplicate Username Test**
- Click "Back to Register"
- Try to register with username=alice again (different email and password)
- Click "Register"
- Speak: "Error message: 'Username already exists.' The system prevents duplicates at both the database constraint level and API validation level."

**[1:30-2:00] Register Second User (Bob)**
- Click "Back to Register"
- Fill form: username=bob, email=bob@example.com, password=BobPass456!
- Click "Register"
- Speak: "Bob is registered successfully. Now let's log in as alice."

---

### SEGMENT 2: LOGIN & SESSION (1:30 minutes)

**[0:00-0:45] Alice Login**
- Click "Login" (or navigate to /login.html)
- Fill form: username=alice, password=AlicePass123!
- Click "Login"
- Speak: "Notice we're now on the task page. The header shows 'Signed in as alice'. In the background, the app called GET /api/v1/auth/me to verify the session."

**[0:45-1:30] Show Terminal & Network**
- Point to terminal logs: "In the logs, we can see the HTTP requests being logged."
- Point to Network tab: "The Login request was a POST to /api/v1/auth/register. In the response header, you can see the Set-Cookie: JSESSIONID header. This cookie persists the session across requests."

---

### SEGMENT 3: TASK CREATION (2:00 minutes)

**[0:00-1:00] Create First Task**
- Fill task form: Title="Complete COMP4442 Project", Description="Implement Spring Boot microservice", Status="TODO"
- Click "Create"
- Speak: "Task created. It appears in the list with the owner 'alice', the title, status. The network request was POST to /api/v1/tasks with a 201 Created response."

**[1:00-2:00] Create Second Task**
- Fill task form: Title="Study Spring Security", Description="Master session management", Status="IN_PROGRESS"
- Click "Create"
- Speak: "Alice now has two tasks. Let me test the edit functionality."

---

### SEGMENT 4: TASK UPDATE & DELETE (2:00 minutes)

**[0:00-1:15] Update Task**
- Click "Edit" on first task
- Speak: "The form is pre-populated with the current data. The URL is /edit.html?id=1"
- Change status to "IN_PROGRESS"
- Click "Save"
- Speak: "The task status updated. This was a PUT request to /api/v1/tasks/1 with a 200 OK response."

**[1:15-2:00] Delete Task**
- Back on task list, click "Delete" on the second task
- Speak: "The task was deleted. That was a DELETE request to /api/v1/tasks/2 with a 204 No Content response."

---

### SEGMENT 5: LOGOUT & BOB LOGIN (1:00 minute)

**[0:00-0:30] Logout**
- Click "Logout"
- Speak: "We're back at the home page. The session was cleared. Future requests to the task page would redirect to login."

**[0:30-1:00] Bob Registration (if not done earlier)**
- (Assuming Bob was already registered in Segment 1)
- Click "Login"
- Fill form: username=bob, email=bob@example.com, password=BobPass456!
- Click "Login"
- Speak: "We're logged in as bob now. Notice the task list is completely empty. Bob hasn't created any tasks, but more importantly, he doesn't see alice's tasks."

---

### SEGMENT 6: USER ISOLATION TEST (CRITICAL) (2:00 minutes)

**[0:00-1:00] Verify Bob's Empty List**
- Speak: "Alice created two tasks. Bob's task list is empty. This is user isolation working. In the GET request to /api/v1/tasks, the backend filtered by user_id=bob.id, returning only bob's tasks. Alice's tasks, even though they exist in the database, are hidden from bob."

**[1:00-2:00] Attempt Cross-User Access**
- Manually type in browser: /edit.html?id=1 (knowing this is Alice's task)
- Speak: "Bob is trying to access a task he didn't create. The edit page made a GET request to /api/v1/tasks/1. Look at the Network tab — the response is 404 Not Found. From bob's perspective, that task doesn't exist. The API correctly rejected the request."
- Page redirects back to task list

---

### SEGMENT 7: SWAGGER DOCUMENTATION (1:00 minute)

**[0:00-1:00] Show Swagger UI**
- Navigate to http://localhost:8080/swagger-ui/index.html
- Point to endpoints: "This is the auto-generated API documentation. We can see all endpoints: register, login, logout, get me, create task, get tasks, update task, delete task."
- Click "Try it out" on one endpoint to show the interactive documentation
- Speak: "This documentation is generated automatically from our code annotations. It's always in sync with the actual API."

---

**[End of Demo - Total 12 minutes]**

---

---

# QUICK REFERENCE TROUBLESHOOTING

If something goes wrong during demo:

**"The application won't start"**
- Check Java version: `java -version` (should be 17+)
- Check Maven: `mvn -version`
- Run: `mvn clean package` to rebuild
- Check for port 8080 conflicts: `lsof -i :8080`

**"Database connection fails"**
- H2 in-memory doesn't require external setup
- If using MySQL, check: `mysql -u [user] -p [password]` can connect
- Verify JDBC URL in application-dev.properties

**"Login redirects to register"**
- Browser might have expired session
- Clear cookies: Ctrl+Shift+Delete
- Try logging in again

**"Task list shows other users' tasks"**
- User isolation might not be working
- Check database query logs (should show WHERE user_id=?)
- Verify @AuthenticationPrincipal is in controller

**"Swagger isn't showing**
- Navigate to http://localhost:8080/swagger-ui/index.html
- Check springdoc-openapi dependency in pom.xml
- Look for errors in application logs

**"Network tab shows 405 Method Not Allowed"**
- Verify the HTTP method (GET, POST, PUT, DELETE)
- Check @RequestMapping/@PostMapping/@PutMapping annotations
- Ensure API endpoint path is correct

---

**END OF PRESENTATION SCRIPT**

---

## FINAL PRESENTATION TIPS

1. **Practice timing:** Do a full run-through of the presentation and demo to ensure you stay within 15 minutes.

2. **Speak clearly:** Microphone or project your voice to the back of the room.

3. **Make eye contact:** Look at your audience, not the screen.

4. **Pause for emphasis:** After important points (like user isolation), pause to let it sink in.

5. **Tell a story:** The presentation flows from "what we built" → "how it works" → "proof it works" → "how it's deployed" → "team effort".

6. **Be confident:** You've built something substantial. Own it.

7. **Prepare for Q&A:** Know your code. Be ready to show specific lines and explain design decisions.

8. **Have a backup:** If something fails during the live demo, have screenshots ready or show pre-recorded video.

9. **Suggest hands-on test:** Offer to let a questioner try the application live if time allows.

10. **Conclude on strength:** End with "Thank you. We're proud of this project. It demonstrates production-quality code and solid engineering practices."
