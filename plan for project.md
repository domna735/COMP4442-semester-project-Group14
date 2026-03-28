# Group Task Split Plan (3 Member Tasks + 2 Joint Task)

This file divides the COMP4442 project into 3 main member-owned tasks, plus 2 final joint task, so each group member has clear ownership while still collaborating on integration and final delivery.

## Execution Order (One by One)
- [x] Start with Task 1 first
- [x] Continue Task 2 after Task 1 core completion (current focus)
- [ ] Complete Task 3 UI design in parallel with backend stabilization
- [ ] Complete Task 4 as full-team quality/documentation work
- [ ] Complete Task 5 as final-team integration and submission

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

### Detailed Responsibilities
- Design wireframe/mockup and final UI screens
- Implement frontend pages/components for task list, add, edit, and delete flows
- Connect UI with backend APIs and verify basic error-state display
- Prepare UI screenshots and walkthrough notes for report/presentation

### Deliverables
- UI prototype/mockup and implemented UI pages
- Frontend-to-backend integration demo for core task flows
- UI evidence assets for final presentation

### Milestone Target
- Complete before full-team testing and final demo rehearsal

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

