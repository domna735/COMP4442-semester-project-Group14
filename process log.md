# Process Log
This document captures the daily activities, decisions, and reflections for building and deploying our Spring Boot cloud computing service.

Follow the template below to document your activities, decisions, and reflections for each day of the week.

## YYYY-MM-DD | Short Title
Intent:
Action:
Result:
Decision / Interpretation:
Next:

---

## 2026-03-20 | Repository and Service Initialization
Intent:
Set up the semester project repository and establish a working Spring Boot baseline for the cloud computing service.

Action:
Created and cloned the GitHub repository, scaffolded a Spring Boot 3 project with Maven, added core dependencies (web, validation, actuator, test), and implemented initial API endpoints for service health and arithmetic computation.

Result:
The repository now contains a clean service structure, initial controller/service/DTO classes, application configuration, and README documentation. The first commit was successfully pushed to the remote repository.

Decision / Interpretation:
Starting with a small but complete API gave us a reliable foundation and a clear GitHub development trace, which aligns with the assignment grading criteria.

Next:
Install Java 17 and Maven on the local environment to run and test the service, then add automated tests and deployment artifacts for AWS EC2.

---

## 2026-03-22 | Process Documentation Update
Intent:
Improve project documentation quality by updating the process log with concrete development activities and outcomes.

Action:
Filled the process log template with dated records of setup and implementation milestones, including technical decisions and follow-up tasks.

Result:
The process log is now ready to support presentation/report evidence and demonstrates continuous project progress.

Decision / Interpretation:
Maintaining this log incrementally will reduce report-writing effort near the deadline and help explain team decisions during the demo.

Next:
Continue adding one log entry per meaningful development session (coding, testing, deployment, troubleshooting, and presentation preparation).

---

## 2026-03-24 | Base Setting Completion
Intent:
Complete the project baseline configuration before implementing task management business functions.

Action:
Added foundational project settings, including OpenAPI dependency setup, environment profiles (dev/prod), standardized API error model and global exception handling, improved validation messages, and a basic Spring Boot context test.

Result:
The project now has a stable base architecture and configuration structure for the next development phase. The baseline setup changes were committed and pushed to GitHub.

Decision / Interpretation:
Finishing infrastructure-level setup first reduces rework risk and keeps later feature development focused on business logic.

Next:
Start implementing Task Management domain functions (entity, repository, service, controller CRUD APIs) and then document that phase in this log.

---

## 2026-03-25 | Task Entity Implementation
Intent:
Begin Phase 2 core API development by completing the first Task Management domain artifact.

Action:
Implemented the Task domain model with JPA annotations, including task ID, title, description, status enum, and created/updated timestamps with lifecycle callbacks. Added persistence API dependency support and updated the implementation plan checklist to mark the Task entity item as completed.

Result:
The project now contains a concrete Task entity foundation for upcoming repository and CRUD API work. Maven test execution passed after the changes, confirming no baseline regression.

Decision / Interpretation:
Completing and validating the entity first establishes a stable schema contract for repository and service layer development, reducing integration rework.

Next:
Implement the JPA repository as the next Phase 2 step, then continue service and controller CRUD endpoints.

---

## 2026-03-25 | Task Repository and Service Layer Implementation
Intent:
Complete the persistence and business logic layers for Task Management CRUD operations.

Action:
Added Spring Data JPA and H2 in-memory database dependencies for local testing. Implemented TaskRepository interface for database access. Created TaskService with full CRUD methods (create, getAll, getById, update, delete). Added TaskNotFoundException for 404 handling and extended GlobalExceptionHandler to catch task-not-found errors with proper HTTP 404 response. Configured dev profile with JPA auto-update and SQL logging for debugging.

Result:
The project now has a complete service layer with business logic separation from the controller. Repository, service, and exception handling are all tested and passing. JPA repository is auto-detected by Spring. Task entity schema is auto-created on startup via Hibernate.

Decision / Interpretation:
Implementing the service layer before the controller decouples business logic from HTTP concerns and makes future testing and refactoring easier. Adding dedicated exceptions and handlers ensures consistent API error responses across all Task endpoints.

Next:
Implement the REST controller for Task CRUD endpoints with proper HTTP methods, request/response DTOs, validation annotations, and Swagger/OpenAPI documentation tags.

---

## 2026-03-25 | Task REST Controller and API Completion
Intent:
Complete Phase 2 by implementing the full REST API layer with request/response DTOs, validation, and OpenAPI documentation.

Action:
Created CreateTaskRequest DTO with validation annotations (NotBlank, Size) for title and description fields. Created TaskResponse DTO to serialize Task entities back to clients. Implemented TaskController with five REST endpoints:
- POST /api/v1/tasks (create new task)
- GET /api/v1/tasks (retrieve all tasks)
- GET /api/v1/tasks/{id} (retrieve task by ID)
- PUT /api/v1/tasks/{id} (update task)
- DELETE /api/v1/tasks/{id} (delete task)
Added comprehensive Swagger/OpenAPI annotations (@Tag, @Operation, @ApiResponse, @ApiResponses) for automatic API documentation. All endpoints return proper HTTP status codes (201 Created, 200 OK, 204 No Content, 404 Not Found).

Result:
Phase 2 Core API Development is now complete. The entire Task Management CRUD API is functional with validation, error handling, persistence, and documentation. Tests pass. All five Task CRUD endpoints are ready for integration testing and deployment. API documentation is automatically generated and accessible via Swagger UI.

Decision / Interpretation:
Building DTOs separately from entities provides a clean API contract independent of database schema changes. Swagger annotations enable automatic documentation which reduces maintenance burden and keeps API docs always in sync with code.

Next:
Phase 2 complete. Ready to proceed with Phase 3 (Database Integration) and Phase 4 (Deployment). Task 1 backend work is now ready for Team Member B's AWS infrastructure setup.

---

## 2026-03-28 | Task 2 Kickoff - Database Integration Baseline
Intent:
Start Task 2 by preparing environment-specific database configuration for local development and future AWS RDS deployment.

Action:
Updated application profile configuration to explicitly separate development and production datasource behavior. Development profile now uses an in-memory H2 datasource with H2 console support and SQL logging for rapid local testing. Production profile now reads datasource URL, username, password, and JDBC driver from environment variables so the same artifact can be deployed to EC2 and connected to AWS RDS without code changes. Added runtime JDBC driver dependencies for both MySQL and PostgreSQL in Maven to support either RDS engine choice.

Result:
The project now has a clean Task 2 starting point for database integration: local development remains fast and isolated, while production settings are externalized and deployment-ready. This reduces migration risk when switching from local testing to cloud database usage.

Decision / Interpretation:
Using profile-based datasource isolation ensures environment parity while avoiding hardcoded secrets. Keeping RDS credentials in environment variables follows safer deployment practice and supports CI/CD or systemd-based runtime configuration later.

Next:
Provision AWS RDS instance and security group rules, then set EC2 environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_DRIVER_CLASS_NAME`) and run end-to-end CRUD verification against the cloud database.

---

## 2026-03-28 | Task 2 Deployment Runbook Preparation
Intent:
Provide a teammate-ready, step-by-step deployment guide so Task 2 execution can be performed consistently by any member.

Action:
Updated README with a detailed AWS RDS and EC2 runbook covering: RDS creation requirements, security group rules, EC2 runtime installation, build command, production environment variable setup for MySQL/PostgreSQL, production startup command, API verification steps, and Swagger verification. Added a production `systemd` service template with environment variable wiring and restart policy to support stable EC2 operation after reboot.

Result:
Task 2 now has an executable checklist instead of high-level notes. Team members can follow the same procedure line-by-line, reducing setup variance and deployment errors. The project also has a reusable service-management template for demonstration readiness.

Decision / Interpretation:
Converting deployment knowledge into a concrete runbook improves team handoff quality and aligns with the assignment requirement for clear process traceability and reproducibility.

Next:
Execute the runbook on AWS by creating the actual RDS and EC2 resources, then complete end-to-end Task CRUD verification against the cloud database endpoint.

---

## 2026-03-28 | Task 2 Deployment Artifact Packaging
Intent:
Turn Task 2 deployment instructions into reusable repository artifacts so all team members can run the same production flow.

Action:
Added deployment files under `deploy/` including an EC2 environment template (`.env.prod.example`), an executable production startup script (`run-prod.sh`), and a systemd service template (`cloud-compute.service`) that reads variables from an environment file. Updated README to point teammates to these files directly.

Result:
Task 2 now includes both documentation and executable templates, reducing manual copy/paste errors and improving team consistency during AWS deployment.

Decision / Interpretation:
Committing deployment artifacts into source control improves reproducibility and keeps operations knowledge versioned with application code.

Next:
Run the deployment artifacts on EC2 with real RDS credentials, validate service startup through systemd, and verify live Task CRUD operations from the public endpoint.

