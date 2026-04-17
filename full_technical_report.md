# Cloud Compute Service
## Comprehensive Project Report (Updated)

Date: 2026-04-17  
Project: COMP4442 Semester Project Group 14  
Repository: domna735/COMP4442-semester-project-Group14

## Table of Contents

Abstract  
Executive Summary  
Chapter 1 - Introduction  
1.1 Background and Problem Motivation  
1.2 Project Objectives and Success Criteria  
1.3 Scope, Assumptions, and Constraints  
1.4 Report Structure  

Chapter 2 - Project Context and Engineering Foundations  
2.1 Cloud-Native Service Context  
2.2 RESTful Service Design Foundations  
2.3 Security Foundations for JWT-Based Systems  
2.4 Data Consistency and Multi-User Isolation Principles  
2.5 Verification and Reproducibility Principles  
2.6 Chapter Summary  

Chapter 3 - System Requirements and Architecture Design  
3.1 Functional Requirements  
3.2 Non-Functional Requirements  
3.3 High-Level Architecture  
3.4 Authentication and Authorization Design  
3.5 Data Model and Ownership Model  
3.6 Error Handling and Observability Design  
3.7 Chapter Summary  

Chapter 4 - Implementation  
4.1 Backend Technology Stack and Project Structure  
4.2 Configuration Profiles and Environment Separation  
4.3 Authentication Implementation  
4.4 Task Management Implementation  
4.5 File Upload and Download Implementation  
4.6 Frontend Page Flow and Interaction Logic  
4.7 Automation Scripts and One-Command Workflow  
4.8 Deployment Assets and Runtime Templates  
4.9 Chapter Summary  

Chapter 5 - Verification, Testing, and Results  
5.1 Test Strategy  
5.2 Automated Integration Testing Results  
5.3 Scripted Smoke Verification Results  
5.4 Deployment Verification Results  
5.5 Security Behavior Validation  
5.6 Operational Verification Outcomes  
5.7 Chapter Summary  

Chapter 6 - Discussion and Technical Analysis  
6.1 Key Technical Findings  
6.2 Trade-offs and Design Decisions  
6.3 Risks, Limitations, and Threats to Validity  
6.4 Production Hardening Opportunities  
6.5 Chapter Summary  

Chapter 7 - Conclusion and Future Work  
7.1 Conclusion  
7.2 Lessons Learned  
7.3 Future Work Roadmap  

References and Project Artifacts

---

## Abstract

This report presents the updated design, implementation, verification, and operationalization of Cloud Compute Service, a Spring Boot microservice developed for COMP4442. The current system integrates four core capabilities in one platform: JWT-based authentication (access token plus refresh token), user-scoped task management, utility compute APIs, and protected file upload/download APIs.

The implementation follows a layered architecture (Controller, Service, Repository, Entity) with security and validation controls applied across multiple layers. Authentication now uses signed JWT tokens, with ECDSA key-based signing and verification. Authorization is enforced for task and file routes, while selected public routes remain accessible for registration, login, and health/documentation checks.

From an engineering-process perspective, the project includes repeatable local and deployment verification workflows. Local one-command scripts start the service and execute full smoke checks, including refresh-token flow and file operations. Deployment scripts support EC2 startup, database prechecks, and end-to-end deployment verification. Automated tests and script outputs demonstrate stable behavior after the latest architecture updates.

The project demonstrates a practical backend lifecycle suitable for course-scale production-style demonstration: architecture evolution, security hardening, data ownership isolation, reproducible verification, and deployability.

## Executive Summary

Cloud Compute Service solves a practical course-aligned problem: building a secure, cloud-ready multi-user backend with clear engineering quality. The current system provides:

1. Auth APIs for register, login, token refresh, logout, and current-user retrieval.
2. Task APIs for create, list, get by ID, update, and delete.
3. File APIs for authenticated upload, listing, and download.
4. Compute APIs for health ping and arithmetic calculation.
5. Static frontend pages for home, register, login, task list, and task edit flows.

The most important security property is user ownership isolation. Task access is scoped by authenticated user identity through service and repository boundaries. File APIs are protected and validated, including upload filename checks, storage boundary controls, and authentication requirements.

Verification combines JUnit integration tests, local smoke scripts, and deployment verification scripts. Current validation confirms protected-route behavior, auth flow correctness, task CRUD behavior, file API behavior, and unauthenticated rejection semantics.

Operational readiness is supported by profile separation and environment templates. Local development defaults to SQLite for quick setup, while production profile supports MySQL/PostgreSQL through environment variables. EC2 scripts validate DB connectivity and run deployment checks consistently.

---

## Chapter 1 - Introduction

### 1.1 Background and Problem Motivation

Modern cloud services are evaluated not only by endpoint correctness, but also by security, maintainability, testability, and deployability. Educational projects often ship features quickly but under-invest in operational rigor and reproducibility. This project was designed to avoid that gap.

The motivating problem is to build a cloud-hostable task and file service where multiple users can authenticate and manage their own resources without cross-user data leakage. This naturally requires strong identity handling, authorization boundaries, and repeatable verification.

### 1.2 Project Objectives and Success Criteria

The project pursued four primary objectives:

1. Implement a functional REST microservice with auth, task, file, and compute endpoints.
2. Enforce strict user-level data isolation and protected API access.
3. Deliver reproducible verification via automated tests and scripts.
4. Prepare deployment artifacts for EC2 with configurable production databases.

Success criteria include passing integration tests, passing smoke/deploy scripts, validated protected-route behavior, and synchronized documentation that matches implementation.

### 1.3 Scope, Assumptions, and Constraints

In-scope items:

1. Spring Boot backend with JWT authentication and refresh-token lifecycle.
2. SQL persistence model with user-task ownership.
3. Authenticated file upload/list/download endpoints.
4. Multi-page static frontend for demo interaction.
5. Local and cloud-oriented run scripts and verification guides.

Assumptions:

1. Development uses Java 17 and Maven.
2. Local default database is SQLite.
3. Cloud runtime targets EC2 with optional RDS (MySQL/PostgreSQL).

Constraints:

1. Semester timeline and team bandwidth.
2. Course-scale infrastructure complexity.
3. Production hardening is meaningful but not enterprise-complete.

### 1.4 Report Structure

This report mirrors the full engineering lifecycle: context and design decisions, implementation details, verification evidence, and future hardening roadmap.

---

## Chapter 2 - Project Context and Engineering Foundations

### 2.1 Cloud-Native Service Context

Cloud-native service quality depends on environment-configurable runtime behavior and clear deployment automation. This project applies profile-based configuration and environment-driven secrets/DB settings to support local and EC2 execution with minimal code changes.

### 2.2 RESTful Service Design Foundations

The API follows standard HTTP semantics:

1. POST for creation and auth operations.
2. GET for retrieval operations.
3. PUT for updates.
4. DELETE for removals.

Status-code contracts are explicit (for example, 201 for created resources and 401 for unauthorized protected routes), enabling reliable test automation.

### 2.3 Security Foundations for JWT-Based Systems

The system uses JWT bearer tokens for API authentication:

1. Access token for protected API calls.
2. Refresh token for controlled access token renewal.
3. ECDSA key pair for token signing and verification.
4. BCrypt password hashing for credential storage.

Security boundaries are centralized in SecurityConfig. Public routes are explicitly allowlisted, while protected API groups such as tasks and files require valid bearer tokens.

### 2.4 Data Consistency and Multi-User Isolation Principles

User isolation is enforced by design:

1. Tasks are persisted with user ownership.
2. Service operations resolve identity from authenticated principal.
3. Repository lookups are constrained by user identity.
4. Unauthorized cross-user access does not expose target resources.

### 2.5 Verification and Reproducibility Principles

Verification combines complementary layers:

1. Integration tests for deterministic backend and route behavior.
2. Smoke scripts for full API flow (auth, refresh, tasks, files).
3. Deployment verifier scripts for environment-level checks.
4. Playbooks for manual demo consistency.

### 2.6 Chapter Summary

The foundation emphasizes security-by-design, reproducibility, and cloud-ready operability rather than feature count alone.

---

## Chapter 3 - System Requirements and Architecture Design

### 3.1 Functional Requirements

The current system supports:

1. User registration and login.
2. Access token refresh.
3. Current-user retrieval and logout.
4. Task create/list/get/update/delete for authenticated users.
5. File upload/list/download for authenticated users.
6. Compute ping and calculation endpoints.

### 3.2 Non-Functional Requirements

The system targets:

1. Security: protected routes and user isolation.
2. Reliability: stable API contracts and predictable behavior.
3. Maintainability: layered architecture and clear package separation.
4. Operability: one-command local validation and script-based deployment checks.
5. Reproducibility: test/script workflows that produce consistent results.

### 3.3 High-Level Architecture

Primary layers and responsibilities:

1. Controllers expose APIs and static route behavior.
2. Services apply business rules and ownership constraints.
3. Repositories implement persistence operations.
4. Entities define relational models.
5. Security layer handles JWT parsing, validation, and authorization.
6. Exception layer standardizes error responses.

### 3.4 Authentication and Authorization Design

Authentication flow:

1. User logs in with username/password.
2. Server authenticates credentials.
3. Server returns access token and refresh token.
4. Client sends bearer access token to protected APIs.
5. Client uses refresh token to obtain a new access token when needed.

Authorization flow:

1. JWT filter validates incoming bearer token.
2. Security context is populated for valid tokens.
3. Protected endpoints enforce authentication and ownership semantics.

### 3.5 Data Model and Ownership Model

Core model includes AppUser, Task, and RefreshToken entities. Ownership enforcement avoids trusting client-supplied user IDs and always derives identity from authenticated context.

### 3.6 Error Handling and Observability Design

Global exception handling maps validation and domain errors to consistent HTTP responses. Actuator and ping endpoints provide lightweight runtime observability for script-based health checks.

### 3.7 Chapter Summary

The architecture translates core requirements into enforceable boundaries for identity, data ownership, and operational reliability.

---

## Chapter 4 - Implementation

### 4.1 Backend Technology Stack and Project Structure

The backend uses Spring Boot 3.3.5 with package-level modularization:

1. config
2. controller
3. dto
4. entity
5. exception
6. repository
7. security
8. service

### 4.2 Configuration Profiles and Environment Separation

Configuration files support environment separation:

1. application.properties for shared/default settings.
2. application-dev.properties for local development defaults.
3. application-prod.properties for production runtime variables.

Runtime variables include DB URL/credentials/driver and JWT key file paths. This supports SQLite local defaults and MySQL/PostgreSQL production deployment.

### 4.3 Authentication Implementation

Auth implementation includes:

1. Register endpoint with validation and duplicate checks.
2. Login endpoint issuing access and refresh tokens.
3. Refresh endpoint returning new access token.
4. Me endpoint for authenticated user profile.
5. Logout endpoint to invalidate session/token state as configured.

### 4.4 Task Management Implementation

Task APIs are implemented with user-scoped ownership:

1. Create associates task with authenticated user.
2. List retrieves only current-user tasks.
3. Get/Update/Delete enforce ownership-bound lookup.
4. Validation and exception handling provide stable API semantics.

### 4.5 File Upload and Download Implementation

File APIs support secure authenticated file handling:

1. Upload endpoint stores files with sanitized/randomized names.
2. List endpoint returns available uploaded file names.
3. Download endpoint serves selected file if authorized.
4. Security checks prevent unauthenticated access to file routes.
5. Storage checks reduce path traversal and unsafe filename risks.

### 4.6 Frontend Page Flow and Interaction Logic

Frontend pages include index, register, login, task, and edit views. Public pages are accessible directly. Protected task/edit views are blocked for unauthenticated access and redirect to login behavior as configured.

### 4.7 Automation Scripts and One-Command Workflow

Operational scripts include:

1. scripts/one-click-dev.sh for local startup and smoke execution.
2. scripts/smoke-test.sh for API flow verification.
3. deploy/ec2/setup-db.sh for DB connectivity prechecks.
4. deploy/ec2/run-prod.sh for production startup with env validation.
5. deploy/ec2/verify-deploy.sh for end-to-end deployment verification.

Current smoke/deploy scripts validate auth, refresh, task CRUD, file upload/list/download, and unauthenticated 401 checks.

### 4.8 Deployment Assets and Runtime Templates

Deployment assets include env template, systemd template, DB setup checker, and deployment verifier. This reduces manual setup errors and improves repeatability on EC2 environments.

### 4.9 Chapter Summary

Implementation aligns with updated requirements and includes both feature delivery and practical operational tooling.

---

## Chapter 5 - Verification, Testing, and Results

### 5.1 Test Strategy

The test strategy uses layered verification:

1. Integration tests for backend behaviors and route protection.
2. Smoke scripts for end-to-end API flow.
3. Deployment verification scripts for environment checks.
4. Manual playbooks for live demo execution.

### 5.2 Automated Integration Testing Results

Recent integration runs confirm:

1. Spring context load success.
2. Public page serving and protected page behavior.
3. Register/login and authenticated task CRUD flows.
4. Validation rejection behavior.

Latest full test execution completed with zero failures.

### 5.3 Scripted Smoke Verification Results

Smoke script coverage includes:

1. Ping endpoint.
2. Register and login.
3. Me endpoint.
4. Task create and list.
5. Token refresh.
6. File upload, list, and download.
7. Logout and unauthenticated access rejection checks.

Latest runs passed all checks.

### 5.4 Deployment Verification Results

Deployment verifier checks include:

1. Health and documentation endpoints.
2. Unauthenticated protection on task and file APIs.
3. Auth/register/login flow.
4. Task and file operation flow under valid token.
5. Refresh and logout behavior.

Recent verification against local deployment endpoint passed all checks.

### 5.5 Security Behavior Validation

Validated security outcomes:

1. Protected task/file APIs reject unauthenticated requests with 401.
2. Protected frontend task/edit routes redirect unauthenticated users.
3. JWT signing keys load correctly at runtime.
4. Passwords remain hashed with BCrypt.
5. Ownership checks prevent cross-user task access.

### 5.6 Operational Verification Outcomes

Operational readiness evidence includes:

1. Repeatable local one-command setup and smoke pass.
2. Deployment precheck scripts for DB and env validation.
3. Deployment verifier pass for full API workflow.
4. Updated runbooks and demo playbooks synchronized with current code behavior.

### 5.7 Chapter Summary

Verification confirms functional correctness, security consistency, and deployment reproducibility for the updated project state.

---

## Chapter 6 - Discussion and Technical Analysis

### 6.1 Key Technical Findings

1. JWT access+refresh flow improves API-oriented auth consistency for script and frontend clients.
2. User ownership constraints in service/repository layers are effective for isolation.
3. Script-based verification greatly improves confidence before demos and pushes.
4. Keeping documentation synchronized with architecture changes prevents team confusion.

### 6.2 Trade-offs and Design Decisions

1. SQLite default local setup reduces onboarding friction.
2. MySQL/PostgreSQL production support keeps cloud deployment realistic.
3. Static HTML/JS frontend keeps scope manageable while still demonstrating end-to-end flows.
4. File API protection increased security coverage but required stronger validation logic.

### 6.3 Risks, Limitations, and Threats to Validity

1. Current checks are course-scale; no large-load benchmarking was performed.
2. Token revocation and advanced session policies can be further improved.
3. Upload scanning and content-type hardening can be extended.
4. Production schema migration remains update-driven unless migration tooling is introduced.

### 6.4 Production Hardening Opportunities

1. Introduce Flyway or Liquibase migrations.
2. Add login throttling, account lockout, and stricter token lifecycle controls.
3. Expand observability with structured logs and metrics dashboards.
4. Add file malware scanning and stricter upload policy enforcement.
5. Add CI/CD pipeline automation for test and deployment verification.

### 6.5 Chapter Summary

The project reaches a strong course-production baseline and provides a clear roadmap for next-level hardening.

---

## Chapter 7 - Conclusion and Future Work

### 7.1 Conclusion

Cloud Compute Service now delivers a complete secure microservice workflow with JWT authentication, user-scoped task management, protected file operations, and cloud-oriented deployment tooling. The system demonstrates architecture clarity, practical security controls, and repeatable verification across local and deployment contexts.

### 7.2 Lessons Learned

1. Security boundaries should be designed early and tested continuously.
2. Repeatable scripts are essential for team consistency and demo confidence.
3. Documentation must be updated whenever auth or deployment behavior changes.
4. Verification should include both backend APIs and frontend access-control behavior.

### 7.3 Future Work Roadmap

1. Add migration-based schema governance.
2. Add pagination/search for task and file listings.
3. Strengthen token revocation and key-rotation workflows.
4. Add performance and concurrency test coverage.
5. Integrate full CI/CD and environment promotion checks.

---

## References and Project Artifacts

Primary project artifacts:

1. README and one-command playbook.
2. Operational playbook and real-time demo playbooks.
3. Comprehensive test execution guide.
4. Project plan and process log.
5. Source modules under src/main/java and src/test/java.
6. Deployment assets under deploy/ec2 and deploy/systemd.

Technical framework references:

1. Spring Boot documentation.
2. Spring Security documentation.
3. Spring Data JPA documentation.
4. Springdoc OpenAPI documentation.
5. Hibernate ORM documentation.
