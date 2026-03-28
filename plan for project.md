# Group Task Split Plan (3 Member Tasks + 2 Joint Task)

This file divides the COMP4442 project into 3 main member-owned tasks, plus 2 final joint task, so each group member has clear ownership while still collaborating on integration and final delivery.

**Documentation Reference (Complete):**

*Task Delivery & Testing:*
- **[test_execution_guide.md](test_execution_guide.md)** — Comprehensive manual testing procedures (7 phases, 34 test cases, critical user isolation validation)

*Operational & Demo:*
- **[playbook.md](playbook.md)** — Operational setup and deployment guide
- **[realtime_demo_playbook.md](realtime_demo_playbook.md)** — 12-minute timed demo script
- **[ppt_script.md](ppt_script.md)** — Detailed speaker notes for PowerPoint presentation with full demo script (3 minutes)

*Presentation & Report (NEW - Task 4):*
- **[report.md](report.md)** — Word report template & content guide (≤10 pages, Times New Roman 12pt, A4, 1-inch margins)
- **[ppt.md](ppt.md)** — PowerPoint slide content guide (10 slides + cover, with speaker notes)

*Project Documentation:*
- **[process log.md](process log.md)** — Development activity log with dated entries (updated 2026-03-29)

## Execution Order (One by One)
- [x] Start with Task 1 first
- [x] Continue Task 2 after Task 1 core completion
- [x] Finish Task 2 in-project runbook, verification, and deployment templates
- [x] Complete Task 3 UI design and function tests (COMPLETE - including auth backend, multi-page UI, user-scoped CRUD, comprehensive test documentation)
- [x] Complete Task 4 as full-team quality/documentation work (IN PROGRESS → PRESENTATION & REPORT CREATED)
  - [x] Comprehensive test execution guide (test_execution_guide.md) ✅
  - [x] Operational playbooks (playbook.md, realtime_demo_playbook.md) ✅
  - [x] PowerPoint presentation content (ppt.md with 10 slides + speaker notes) ✅ NEW
  - [x] Word report template (report.md with full content structure) ✅ NEW
  - [x] Presentation speaker script (ppt_script.md with full demo walkthrough) ✅ NEW
  - [x] Full technical health check executed (`mvn clean test` pass, script syntax pass) ✅ NEW (2026-03-29)
  - 🔄 Manual testing execution (use test_execution_guide.md)
  - 🔄 Screenshot evidence collection (from manual testing)
  - 🔄 PowerPoint creation (using ppt.md as guide)
  - 🔄 Word report creation (using report.md as template)
  - 🔄 Demo rehearsal (using ppt_script.md)
- [ ] Complete Task 5 as final-team integration and submission
  - 🔄 Final package assembly in progress (pending `.pptx` + `.docx` final export)
  - 🔄 Final smoke run pending on machine with `curl` installed
  - 🔄 Final rubric go/no-go checklist pending

## Task 1 - Backend API and Business Logic
Owner: Group Member A

### Scope
- Complete Task Management backend implementation
- Build create, retrieve, update, and delete APIs
- Implement service-layer business logic and validation handling

### Detailed Responsibilities
- Implement repository, service, and controller for Task module
- Ensure API request/response design is consistent and testable
- Add/update Swagger OpenAPI docs for Task endpoints
- Support integration testing with database-ready flow

### Deliverables
- Working CRUD endpoints for Task management
- Updated API documentation (Swagger)
- Unit/integration test evidence for core flows

### Milestone Target
- Finish before cloud deployment phase starts

---

## Task 2 - Cloud Deployment and Database Integration
Owner: Group Member B

### Scope
- Prepare AWS infrastructure and deploy application
- Integrate persistent database on AWS RDS
- Configure runtime environment for stable demo use

### Detailed Responsibilities
- Create and configure EC2 instance and security groups
- Set up RDS (MySQL/PostgreSQL) and connect Spring Boot datasource
- Configure deployment process (build, run, restart)
- Set up EBS/log storage and service auto-start (systemd)
- Verify public endpoint availability for live demo

### Deliverables
- Deployed cloud application with public access URL
- Functional cloud database persistence for Task CRUD
- Deployment notes and troubleshooting checklist

### Milestone Target
- Complete before final testing and demo rehearsal

---

## Task 3 - UI Design and Frontend Experience
Owner: Group Member C

### Scope
- Design a clean, user-friendly UI for demonstrating Task Management flows
- Ensure frontend interactions clearly show create, retrieve, update, and delete operations
- Keep visual design aligned with presentation and demo narrative
- Implement SQL-backed user authentication flow (register/login/logout) and protect task pages

### Detailed Responsibilities
- Design wireframe/mockup and final UI screens
- Implement frontend pages/components for home, login, register, task list, and edit flows
- Connect UI with backend APIs and verify auth-state + error-state display
- Implement backend user management with SQL persistence and password hashing
- Enforce user-scoped task CRUD so each account manages its own task records
- Implement and run UI-based functional tests for key user flows
- Prepare UI screenshots and walkthrough notes for report/presentation

### Deliverables
- UI prototype/mockup and implemented multi-page auth + task UI
- Frontend-to-backend integration demo for core task flows
- SQL-backed user auth backend and protected task access flow
- UI functional test cases and execution evidence
- UI evidence assets for final presentation

### Milestone Target
- Complete before full-team testing and final demo rehearsal

---

## **Auth + UI Evidence Checklist (For Task 4 Screenshot Collection)**

This checklist documents all required evidence artifacts for auth flow and protected UI demonstration:

### Login & Registration Flow
- [ ] **Register Form Screenshot** — Registration page with username/email/password inputs
- [ ] **Register Success Screenshot** — Redirect to login page after successful registration
- [ ] **Register Validation Error** — Error message when username/email already exists or password is weak
- [ ] **Login Form Screenshot** — Login page with username/password inputs and submit button
- [ ] **Login Success Screenshot** — Display logged-in user name and redirect to task page
- [ ] **Login Failure Screenshot** — Error message for invalid credentials (401 response shown)

### Protected Pages & Redirects
- [ ] **Protected Task Page (Authenticated)** — Task page displaying after successful login with "Signed in as [username]"
- [ ] **Protected Task Page (Unauthenticated)** — Redirect to login.html when accessing /task.html without session
- [ ] **Protected Edit Page (Authenticated)** — Edit page for a single task with pre-populated form
- [ ] **Protected Edit Page (Unauthenticated)** — Redirect to login.html when accessing /edit.html without session

### User-Scoped Task Management
- [ ] **User A Task List** — Screenshot showing User A's task list only (logged in as User A)
- [ ] **User B Task List** — Screenshot showing User B's task list only (logged in as User B), different tasks from User A
- [ ] **Cross-User Isolation Test** — Demonstrate User A cannot see/access User B's tasks via direct URL or API
- [ ] **Create Task (User-Scoped)** — Screenshot of create task form submission showing task assigned to current user
- [ ] **Task Owner Field** — Screenshot showing "ownerUsername" field in task list matches logged-in user

### Task CRUD Operations (With Auth)
- [ ] **Create Task Success** — New task created and appears in logged-in user's list
- [ ] **Edit Task Form** — Populate edit form fields from existing task (get by taskId + userId)
- [ ] **Update Task Success** — Update task title/description and verify changes persisted
- [ ] **Delete Task Confirmation** — Delete button removes task from logged-in user's list
- [ ] **Task Not Found (After Delete)** — Attempt to view deleted task returns 404 error

### Session & Logout
- [ ] **Logout Button Click** — Logout clears session and redirects to login page
- [ ] **Session Expiration** — After logout, accessing task.html redirects to login.html (session cleared)
- [ ] **Session Persistence** — Navigating back/forward maintains logged-in session without re-login

### API Response Examples
- [ ] **POST /api/v1/auth/register Response** — Response body showing userId, username, email, token/session
- [ ] **POST /api/v1/auth/login Response** — Successful login response with session/auth token
- [ ] **GET /api/v1/auth/me Response** — Current authenticated user info (id, username, email, role)
- [ ] **POST /api/v1/tasks Response** — Task creation shows task assigned to authenticated user's ID
- [ ] **GET /api/v1/tasks Response** — Task list filtered by authenticated user (no other user's tasks)
- [ ] **401 Unauthorized Response** — Error response when accessing protected endpoint without auth

---

## Task 4 - Testing, Documentation, and Presentation Package
Owner: All Group Members (Joint Completion)

### Scope
- Complete quality validation, documentation, and presentation readiness together
- Ensure report and slides meet strict format requirements
- Prepare final demonstration storyline and evidence package as a team

### Detailed Responsibilities
- Execute functional testing together (Postman scenarios for full CRUD)
- Collect evidence artifacts (screenshots, API runs, logs)
- Co-maintain README and process log updates with dated progress
- Co-prepare PowerPoint (<= 10 pages excluding cover)
- Co-prepare Word report (<= 10 pages excluding cover, required format)
- Rehearse and refine the 12-minute presentation script as a full team

### Deliverables
- Test checklist and test result summary
- Finalized PPT and Word report meeting rubric requirements
- Demo script with contribution mapping by member

### Milestone Target
- Complete in final project week before submission deadline

---

## Task 5 - Final Integration and Submission (Team Joint Part)
Owner: All Group Members

### Scope
- Final cross-check of all technical and documentation outputs before submission
- Ensure consistency between codebase, deployment state, and report/presentation claims

### Detailed Responsibilities
- Perform final end-to-end verification on deployed endpoint
- Validate checklist items in the master plan and assignment rubric
- Verify contribution evidence for each member is clear and defensible
- Package final submission files and confirm deadline readiness

### Deliverables
- Final verified submission package (PPT + Word + GitHub evidence)
- Final demo run checklist with go/no-go confirmation

### Milestone Target
- Finish 1-2 days before official submission deadline

---

## Team Collaboration Rules
- Every member pushes frequent, meaningful commits to keep GitHub trace complete.
- Use feature branches for major work and merge after peer checking.
- Hold quick sync meetings at each phase boundary (API complete, deployment complete, final packaging complete).
- Cross-support rule: each member must review at least one other member's work before final submission.

## Suggested Mapping to Current Plan
- Phase 2 (Core API Development): Task 1 lead (start first), Task 3 prepares UI integration
- Phase 3-4 (DB + Deployment): Task 2 lead, Task 1 supports integration fixes
- Phase 5 (Testing): Task 4 joint completion by all members
- Phase 6 (Documentation + Presentation + Final Submission): Task 5 joint completion by all members

