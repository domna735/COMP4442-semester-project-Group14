# Cloud Compute Service
## Comprehensive Project Report (Essay Format)

Date: 2026-03-29  
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
2.3 Security Foundations for Session-Based Systems  
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
4.5 Compute API Implementation  
4.6 Frontend Page Flow and Interaction Logic  
4.7 Automation Scripts and One-Command Workflow  
4.8 Deployment Assets and Runtime Templates  
4.9 Chapter Summary  

Chapter 5 - Verification, Testing, and Results  
5.1 Test Strategy  
5.2 Automated Integration Testing Results  
5.3 Scripted Smoke Verification Results  
5.4 Manual End-to-End Verification Plan  
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

This report presents the design, implementation, verification, and operationalization of Cloud Compute Service, a Spring Boot microservice developed for COMP4442. The system combines three major capabilities in one cohesive platform: session-based authentication, user-scoped task management, and a utility compute API. The project goal is not only to implement APIs that function correctly but also to demonstrate software engineering quality through architecture clarity, security controls, reproducible testing, and deployment readiness.

The implemented system follows a layered architecture (Controller, Service, Repository, Entity) and applies security and validation at multiple levels. Authentication relies on Spring Security with server-managed sessions and BCrypt password hashing. Task management is intentionally designed as a multi-user isolation scenario, where every task operation is constrained by the authenticated user identity. This ensures users cannot read, modify, or delete resources owned by other users.

From an engineering-process perspective, the project includes substantial technical documentation and repeatable operational tooling. A one-command local verification path was created using shell scripts to bootstrap the service and run smoke checks. Deployment templates and scripts were also prepared for cloud hosting on EC2 with RDS-backed persistence. Automated integration tests and smoke scripts were executed successfully, and verification outputs were synchronized with runbooks and planning documents.

The project demonstrates a complete, practical backend service lifecycle: architecture design, secure coding, data modeling, verification, documentation, and deployability. The implementation is suitable for course-scale production-style demonstration and provides a solid foundation for future hardening and feature expansion.

## Executive Summary

Cloud Compute Service was built to solve a practical course-aligned problem: how to design a secure cloud-ready service that supports multi-user task workflows while preserving clear software architecture and reproducible engineering process. The final system includes:

1. Authentication APIs for register, login, logout, and current-user retrieval.
2. Task APIs for create, list, get by ID, update, and delete.
3. Compute APIs for service health ping and arithmetic calculation.
4. Static frontend pages for registration, login, task management, and task editing.

The technical core of the system is a layered Spring Boot architecture with JPA-based persistence. The most important security property implemented is user ownership isolation. In controller logic, authenticated identity is injected from Spring Security context. In service logic, every task operation is parameterized by user identity. In repository queries, ownership predicates are enforced directly through methods such as findByUserIdOrderByUpdatedAtDesc and findByIdAndUserId. This design prevents cross-user data leakage by construction.

The team emphasized verification and reproducibility. Automated tests validate endpoint behavior, page protection, authentication flow, CRUD operations, and expected error semantics. Shell-based smoke tests provide rapid endpoint health checks and regression detection for day-to-day development and demo rehearsal. During verification, an expectation mismatch was found for register endpoint status code (expected 200 versus actual 201). This was corrected across smoke scripts and documentation, resulting in consistent and reliable checks.

Operational readiness was addressed through environment separation and deployment templates. Development uses H2 for speed and simplicity; production profile externalizes datasource settings through environment variables for EC2/RDS deployment. Systemd templates and verifier scripts were included to standardize service startup and health checks on cloud hosts.

Overall, this project achieved its technical goals and demonstrates strong engineering discipline for a semester-scale system: secure behavior, clean design, reproducible tests, and deployment-oriented documentation.

---

## Chapter 1 - Introduction

### 1.1 Background and Problem Motivation

Modern cloud services are often evaluated not only by whether endpoints return correct responses, but by whether they are secure, maintainable, testable, and deployable under realistic constraints. In educational projects, teams commonly over-focus on feature count while under-investing in architecture discipline and verification reproducibility. This project was intentionally framed to avoid that imbalance.

The motivating problem is straightforward but meaningful: build a cloud-hostable task management service where multiple users can authenticate and manage their own tasks, while guaranteeing that one user's data never leaks to another user. This creates a natural testbed for security boundaries, ownership-aware query design, and end-to-end workflow validation.

### 1.2 Project Objectives and Success Criteria

The project pursued four primary objectives:

1. Implement a functional REST microservice with authentication, task CRUD, and utility compute endpoints.
2. Enforce strong user-level data isolation across all task operations.
3. Deliver reproducible verification through automated tests and scripted smoke checks.
4. Prepare cloud deployment artifacts and operational documentation suitable for demonstration.

Success criteria include passing automated tests, successful smoke verification, consistent API semantics, complete implementation trace in version control, and clear documentation for setup, testing, and deployment.

### 1.3 Scope, Assumptions, and Constraints

In-scope items:

1. Spring Boot backend with session authentication.
2. SQL persistence model with user-task relationships.
3. Multi-page static frontend for interaction and demo.
4. Local and cloud-oriented runtime configuration.
5. Verification tooling and runbooks.

Assumptions:

1. Local development occurs on Java 17 and Maven-compatible environments.
2. Cloud production uses EC2 and RDS with environment-driven datasource configuration.
3. Browser clients accept cookie-based session handling.

Constraints:

1. Course timeline and team bandwidth.
2. Limited infrastructure complexity acceptable for semester scope.
3. Security hardening optimized for project demonstration level rather than full enterprise baseline.

### 1.4 Report Structure

This report is organized to mirror the engineering lifecycle from problem framing through implementation and analysis. Chapter 2 explains foundations and design rationale. Chapter 3 defines requirements and architecture decisions. Chapter 4 details implementation. Chapter 5 presents verification and results. Chapter 6 discusses findings and limitations. Chapter 7 concludes with lessons and future roadmap.

---

## Chapter 2 - Project Context and Engineering Foundations

### 2.1 Cloud-Native Service Context

Cloud-native services should be stateless where possible, environment-configurable, and observable. Even when full distributed patterns are not required, a service should separate environment concerns from code and support predictable runtime behavior across development and production. This project applies those principles with profile-based configuration and environment variable injection for production data sources.

### 2.2 RESTful Service Design Foundations

The service follows resource-oriented API design where task resources are manipulated through conventional HTTP semantics:

1. POST for creation.
2. GET for retrieval.
3. PUT for update.
4. DELETE for removal.

Status codes are used to encode operation outcomes (for example 201 for creation and 204 for successful delete without response body). This improves client interoperability and test clarity.

### 2.3 Security Foundations for Session-Based Systems

A core design choice was server-managed session authentication rather than token-based JWT. For this project context, session-based design simplifies frontend integration and allows Spring Security to provide robust defaults. Authentication state is represented server-side in SecurityContext and linked to client requests via JSESSIONID cookie.

Key security principles applied:

1. Passwords are never stored in plaintext.
2. Password hashes use BCrypt.
3. Protected endpoints require authenticated context.
4. Unauthorized API access returns 401.
5. Unauthorized HTML page access redirects to login.

### 2.4 Data Consistency and Multi-User Isolation Principles

The system enforces ownership consistency through both schema and query design. Tasks reference users through foreign keys, and repository methods always constrain reads by user identity. This avoids insecure patterns where user identifiers are accepted directly from untrusted request parameters.

### 2.5 Verification and Reproducibility Principles

Software quality claims are valid only when reproducible. This project therefore combines:

1. Automated integration tests for deterministic backend behavior.
2. Smoke scripts for rapid end-to-end endpoint checks.
3. Manual test execution guide for UI and evidence collection.
4. Process logging to preserve development traceability.

### 2.6 Chapter Summary

Chapter 2 establishes the design principles behind the implementation. The project is positioned as an engineering-quality service effort, not merely a feature list.

---

## Chapter 3 - System Requirements and Architecture Design

### 3.1 Functional Requirements

The system must support:

1. User registration with validation and uniqueness checks.
2. User login and logout with session state handling.
3. Retrieval of authenticated user profile.
4. Task creation and task listing for current user.
5. Task retrieval, update, and delete by ID for current user.
6. Compute ping and arithmetic calculation endpoints.

### 3.2 Non-Functional Requirements

The system should maintain:

1. Security: authenticated route protection and user data isolation.
2. Reliability: stable API responses and predictable status codes.
3. Maintainability: clear layered architecture and modular code.
4. Operability: easy local startup/testing and cloud runtime templates.
5. Reproducibility: repeatable tests and documented verification paths.

### 3.3 High-Level Architecture

Architecture layers:

1. Controllers expose REST interfaces and page access behavior.
2. Services encapsulate business logic and ownership constraints.
3. Repositories provide database operations through Spring Data JPA.
4. Entities model data relationships and lifecycle metadata.
5. Security and exception layers provide cross-cutting controls.

### 3.4 Authentication and Authorization Design

Authorization boundaries are defined centrally in SecurityConfig. Public endpoints include register, login, compute APIs, and documentation routes. Task endpoints and authenticated user APIs require valid session context.

The authentication sequence is:

1. Credentials submitted to login endpoint.
2. AuthenticationManager verifies credentials through UserDetailsService.
3. SecurityContext is created and stored in session.
4. Subsequent requests reuse session cookie.

### 3.5 Data Model and Ownership Model

Data model includes users and tasks. A task belongs to exactly one user. Ownership logic does not rely on client-provided user identifiers. Instead, identity originates from authenticated principal and propagates through service/repository operations.

### 3.6 Error Handling and Observability Design

A global exception handler normalizes error responses with code, message, and timestamp fields. Validation errors, authentication failures, not-found cases, and unexpected failures are mapped to consistent HTTP statuses. Actuator endpoints for health/info are exposed to support operational checks.

### 3.7 Chapter Summary

Chapter 3 translates the engineering goals into explicit requirements and architecture mechanisms. The next chapter details concrete implementation.

---

## Chapter 4 - Implementation

### 4.1 Backend Technology Stack and Project Structure

Backend implementation uses Spring Boot 3.3.5 with modular packages:

1. config
2. controller
3. dto
4. entity
5. exception
6. repository
7. security
8. service

This structure supports separation of concerns and easier future extension.

### 4.2 Configuration Profiles and Environment Separation

Three configuration files are used:

1. application.properties for shared settings and default profile.
2. application-dev.properties for H2 local development.
3. application-prod.properties for externalized production datasource.

Production profile reads DB_URL, DB_USERNAME, DB_PASSWORD, and DB_DRIVER_CLASS_NAME from runtime environment variables.

### 4.3 Authentication Implementation

Register flow:

1. Validate payload constraints.
2. Check duplicate username and email.
3. Encode password with BCrypt.
4. Persist user with default USER role.
5. Return created response.

Login flow:

1. Authenticate credentials.
2. Build SecurityContext.
3. Store context in HTTP session.
4. Return user payload.

Logout flow clears context/session through Spring logout handler.

### 4.4 Task Management Implementation

Task creation enforces ownership by attaching authenticated user entity before persistence. Task retrieval and mutation use user-scoped repository methods. Update and delete operations first retrieve by id and userId; unauthorized cross-user attempts cannot retrieve target entities.

### 4.5 Compute API Implementation

Compute module implements arithmetic on validated input DTOs and supports ADD, SUBTRACT, MULTIPLY, and DIVIDE operators. Domain validation rejects division by zero and unsupported operators through controlled exceptions.

### 4.6 Frontend Page Flow and Interaction Logic

Frontend uses static pages:

1. index page for navigation.
2. register/login pages for authentication.
3. protected task page for CRUD operations.
4. protected edit page for update workflow.

Protected pages depend on session state and redirect to login when unauthenticated.

### 4.7 Automation Scripts and One-Command Workflow

Two scripts are central:

1. smoke-test.sh executes API-level verification sequence.
2. one-click-dev.sh starts server when needed, waits for readiness, and runs smoke checks.

During final verification, register status expectation was aligned to 201 to match API design. Script output behavior was also improved to avoid misleading stop messages when server is pre-existing.

### 4.8 Deployment Assets and Runtime Templates

Deployment support includes:

1. Environment variable template for production profile.
2. Run script for EC2.
3. Deployment verification script for endpoint checks.
4. systemd unit template for service lifecycle management.

### 4.9 Chapter Summary

Implementation reflects architecture decisions directly and preserves traceability from requirement to concrete code and scripts.

---

## Chapter 5 - Verification, Testing, and Results

### 5.1 Test Strategy

The verification strategy combines automated, scripted, and manual approaches:

1. Automated integration tests for deterministic backend behavior.
2. Scripted smoke checks for quick runtime confidence.
3. Manual phase-based guide for UI and evidence collection.

### 5.2 Automated Integration Testing Results

Integration tests validate:

1. Public and protected page behavior.
2. Registration and login flow.
3. Authenticated task CRUD lifecycle.
4. Validation failure semantics.
5. Not-found handling after deletion.

Result: automated test suite passed in recent full verification run.

### 5.3 Scripted Smoke Verification Results

Smoke workflow validates:

1. Ping endpoint.
2. Registration (201).
3. Login.
4. Current user endpoint.
5. Task create (201).
6. Task list.
7. Logout.

Result: all smoke checks pass on current local environment after curl installation and status alignment.

### 5.4 Manual End-to-End Verification Plan

Manual test_execution_guide defines phase-based checks for:

1. Auth success and failure cases.
2. Protected route behavior.
3. User-scoped task behavior.
4. Cross-user isolation validation.
5. Session persistence/logout behavior.
6. Documentation endpoint checks.

### 5.5 Security Behavior Validation

Observed validated behavior:

1. Unauthorized access to protected APIs returns 401.
2. Unauthenticated HTML access redirects to login.
3. Cross-user resource access returns not-found behavior.
4. Passwords are stored as BCrypt hashes.

### 5.6 Operational Verification Outcomes

Operational readiness checks include:

1. Local one-command path.
2. Deployment verifier for cloud endpoint checks.
3. Process log updates aligned with technical milestones.

### 5.7 Chapter Summary

Verification confirms that the implementation is functionally correct, security-consistent, and operationally reproducible.

---

## Chapter 6 - Discussion and Technical Analysis

### 6.1 Key Technical Findings

1. Ownership enforcement works best when identity originates from authenticated context and persists through service and repository boundaries.
2. Scripted verification significantly reduces debugging time before demos and pushes.
3. Documentation synchronized with code changes prevents process drift.

### 6.2 Trade-offs and Design Decisions

1. Session auth was selected for simpler browser integration and lower implementation overhead.
2. Static-page frontend was chosen over SPA frameworks to prioritize backend correctness and timeline control.
3. H2 for development plus externalized production datasource offered fast iteration and cloud compatibility.

### 6.3 Risks, Limitations, and Threats to Validity

1. CSRF is globally disabled and should be revisited for broader production scenarios.
2. Production profile still uses ddl-auto=update, which is less strict than migration-based governance.
3. No explicit login throttling or account lockout currently exists.
4. Current evaluation is primarily course-scale and not load-tested for high concurrency.

### 6.4 Production Hardening Opportunities

1. Introduce migration tooling such as Flyway or Liquibase.
2. Add rate limiting and authentication abuse protection.
3. Add session externalization (for example Redis-backed sessions) for horizontal scaling.
4. Add centralized logging, tracing, and alerting.
5. Introduce pagination for task list endpoints.

### 6.5 Chapter Summary

The project reaches a strong course-production baseline, with clear paths toward stricter production readiness.

---

## Chapter 7 - Conclusion and Future Work

### 7.1 Conclusion

Cloud Compute Service delivers a complete secure microservice workflow with clear architecture, validated behavior, and cloud-oriented operational support. The system demonstrates that semester-scale projects can still achieve professional engineering traits: layered design, security boundaries, reproducible checks, and structured documentation.

### 7.2 Lessons Learned

1. Security should be encoded in architecture, not patched after feature completion.
2. Repeatable scripts are critical for confidence and consistency during team collaboration.
3. Documentation must evolve in lockstep with implementation to remain credible.
4. Clear status-code contracts between API and tests are essential to avoid false failures.

### 7.3 Future Work Roadmap

1. Implement migration-based schema evolution.
2. Add richer task capabilities (search, pagination, metadata filters).
3. Improve observability with structured metrics and tracing dashboards.
4. Add more negative-path and resilience test cases.
5. Package deployment workflows into CI/CD pipelines.

---

## References and Project Artifacts

Primary project artifacts:

1. README and one-command playbook.
2. Operational playbook and real-time demo playbook.
3. Comprehensive test execution guide.
4. Project plan and process log.
5. Source code modules under src/main/java and src/test/java.
6. Deployment assets under deploy.

Technical framework references:

1. Spring Boot documentation.
2. Spring Security documentation.
3. Spring Data JPA documentation.
4. Springdoc OpenAPI documentation.
5. Hibernate ORM documentation.

