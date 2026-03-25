# Group Task Split Plan (3 Main Tasks)

This file divides the COMP4442 project into 3 main tasks so each group member has clear ownership while still collaborating on integration and final delivery.

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

## Task 3 - Testing, Documentation, and Presentation Package
Owner: Group Member C

### Scope
- Lead quality validation, documentation, and presentation readiness
- Ensure report and slides meet strict format requirements
- Prepare final demonstration storyline and evidence package

### Detailed Responsibilities
- Execute functional testing (Postman scenarios for full CRUD)
- Collect evidence artifacts (screenshots, API runs, logs)
- Maintain README and process log updates with dated progress
- Prepare PowerPoint (<= 10 pages excluding cover)
- Prepare Word report (<= 10 pages excluding cover, required format)
- Rehearse 12-minute presentation script with team

### Deliverables
- Test checklist and test result summary
- Finalized PPT and Word report meeting rubric requirements
- Demo script with contribution mapping by member

### Milestone Target
- Complete in final project week before submission deadline

---

## Team Collaboration Rules
- Every member pushes frequent, meaningful commits to keep GitHub trace complete.
- Use feature branches for major work and merge after peer checking.
- Hold quick sync meetings at each phase boundary (API complete, deployment complete, final packaging complete).
- Cross-support rule: each member must review at least one other member's work before final submission.

## Suggested Mapping to Current Plan
- Phase 2 (Core API Development): Task 1 lead, Task 3 supports testing/doc updates
- Phase 3-4 (DB + Deployment): Task 2 lead, Task 1 supports integration fixes
- Phase 5-6 (Testing + Presentation): Task 3 lead, Task 1 and Task 2 provide technical evidence

