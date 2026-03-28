# ** Project Plan - Cloud‑Hosted Task Management Microservice (Spring Boot + AWS)**

## **1. Project Overview**
This project aims to design, implement, and deploy a cloud‑based microservice application using **Spring Boot** and **Amazon Web Services (AWS)**. The application provides a RESTful Task Management Service that supports task creation, retrieval, updating, and deletion. The system will be deployed on **AWS EC2**, with persistent data storage on **AWS RDS** and optional use of **AWS EBS** for application storage.

The project demonstrates the integration of modern backend development practices with cloud‑native deployment, aligning with the technologies and methodologies taught in COMP4442.

### **Current Progress Snapshot (2026-03-29 - Full Technical Check Completed)**
- Base setting is completed.
- Completed items: Spring Boot baseline, Task Management CRUD APIs, OpenAPI setup, environment profiles, unified exception handling, SQL-backed user authentication with protected task UI pages, multi-page UI flow (home/login/register/task/edit), comprehensive operational playbooks (playbook.md and realtime_demo_playbook.md), comprehensive test execution guide (test_execution_guide.md with 7 phases, 34 test cases, user isolation verification).
- Task 3 Status: ✅ **COMPLETE** (Backend auth, Frontend UI, User-scoped CRUD, Integration tests all working, 6/6 tests passing)
- Test Documentation: ✅ **COMPLETE** (test_execution_guide.md with critical user isolation tests, cross-user protection validation, session management verification)
- Full technical verification (2026-03-29): ✅ `mvn clean test` passed, ✅ automation script syntax checks passed.
- Local runtime caveat: ⚠️ one-command smoke execution requires `curl` on the demo machine.
- Current focus: Manual test execution (using test_execution_guide.md), evidence collection (screenshots), final `.pptx` and `.docx` creation, and Task 5 submission packaging.

---

## **2. Objectives**
The project is designed to achieve the following objectives:

### **Technical Objectives**
- Develop a production‑ready REST API using Spring Boot.
- Implement persistent storage using AWS RDS (MySQL/PostgreSQL).
- Deploy the application on AWS EC2 with EBS‑backed storage.
- Provide API documentation using Swagger/OpenAPI.
- Demonstrate secure, scalable, cloud‑native architecture.

### **Course‑Related Objectives**
- Show mastery of backend development concepts taught in COMP4442.
- Maintain a complete development trace on GitHub with meaningful commits.
- Prepare a clear and concise presentation and demonstration.
- Produce high‑quality documentation (PowerPoint + Word report).

---

## **2.1 Assessment Alignment (Direct Mapping to ASM Requirements)**

### **Metric 1: Technological Merit**
- Spring Boot layered architecture (controller, service, repository, DTO, exception handling).
- Cloud deployment on AWS EC2 with EBS storage and AWS RDS database integration.
- API validation, error standardization, observability endpoints, and OpenAPI documentation.

### **Metric 2: GitHub Development Trace Completeness**
- Commit regularly with meaningful messages tied to features, tests, configuration, and deployment.
- Maintain branch-based development for larger tasks (for example `feature/task-crud`, `feature/aws-deploy`).
- Keep process documentation synchronized with implementation milestones.
- Share repository access with the teacher account: `wchshapp_business@icloud.com`.

### **Metric 3: Presentation and Demo Clarity (12 Accumulative Minutes)**
- Use a fixed demo script with timed segments and fallback checkpoints.
- Demonstrate live API flow: create, retrieve, update, delete task on deployed cloud instance.
- Explicitly state contribution split and evidence per team member.

### **Metric 4: Document Quality Compliance**
- PowerPoint: maximum 10 pages excluding cover page.
- Word report: maximum 10 pages excluding cover page, Times New Roman 12 pt, A4, 1-inch margins, single column, line spacing not less than single.
- Ensure slides/report content is consistent with the live presentation.

---

## **3. System Architecture**
The system follows a simple but robust cloud‑native architecture.

### **3.1 Architecture Description**
- **Client Layer**: Browser, Postman, or Swagger UI.
- **Application Layer**: Spring Boot REST API hosted on AWS EC2.
- **Data Layer**: AWS RDS for persistent relational storage.
- **Storage Layer**: AWS EBS attached to EC2 for application files and logs.

### **3.2 Architecture Diagram (textual form)**

```
Client (Browser / Postman / Swagger)
                |
                v
        Spring Boot REST API
                |
                v
          AWS EC2 Instance
                |
                v
        AWS RDS (MySQL/PostgreSQL)
                |
                v
        AWS EBS (Persistent Storage)
```

---

## **4. Functional Requirements**
### **4.1 Core Features**
| Feature | Description |
|--------|-------------|
| Create Task | Add a new task with title, description, and status |
| Retrieve Tasks | Get all tasks or a specific task by ID |
| Update Task | Modify task details |
| Delete Task | Remove a task from the system |
| API Documentation | Accessible via Swagger UI |

### **4.2 Non‑Functional Requirements**
- **Scalability**: Cloud‑hosted, horizontally scalable.
- **Reliability**: RDS ensures durable storage.
- **Security**: Session-based Spring Security authentication with BCrypt password hashing and user-scoped task access.
- **Maintainability**: Clean architecture with service and repository layers.
- **Observability**: Logging via Spring Boot + AWS CloudWatch (optional).

---

## **5. Technology Stack**
### **Backend**
- Java 17+
- Spring Boot 3.x
- Spring Web
- Spring Data JPA
- Spring Validation
- Swagger/OpenAPI

### **Database**
- AWS RDS (MySQL or PostgreSQL)

### **Cloud Infrastructure**
- AWS EC2 (Ubuntu)
- AWS EBS (storage)
- AWS RDS (database)
- AWS Security Groups

### **Development Tools**
- Git + GitHub (with full commit history)
- Maven
- IntelliJ IDEA / VS Code

---

## **6. Implementation Plan**
### **Phase 1 — Project Initialization (Week 1)**
- [x] Create GitHub repository  
- [x] Initialize Spring Boot project  
- [x] Set up basic folder structure  
- [x] Commit initial project skeleton  
- [x] Complete baseline settings (profiles, OpenAPI, global error handling, test scaffold)  

### **Phase 2 — Core API Development (Week 2)**
- [x] Implement Task entity  
- [x] Implement JPA repository  
- [x] Implement service layer  
- [x] Implement REST controller  
- [x] Add validation and error handling for Task APIs  
- [x] Finalize Swagger documentation for Task APIs  

### **Phase 3 — Database Integration (Week 3)**
- [x] Configure Spring Boot datasource profiles (dev H2, prod env-based RDS)  
- [x] Prepare deployment checklist/templates for RDS + EC2 + systemd  
- Set up AWS RDS instance  
- Test CRUD operations with cloud database  

### **Phase 4 — Deployment (Week 4)**
- Launch AWS EC2 instance  
- Install Java + Maven  
- Build and deploy Spring Boot JAR  
- Configure EBS storage  
- Set up systemd service for auto‑start  
- Optional: configure Nginx reverse proxy  

### **Phase 5 — Testing & Optimization (Week 5)**
- [x] Start UI-based functional tests (homepage + Task CRUD API flow)  
- [x] Automated integration tests verified (`mvn clean test` pass on 2026-03-29)  
- Functional testing via Postman  
- Load testing (optional)  
- Logging and monitoring setup  

### **Phase 6 — Documentation & Presentation (Week 6)**
- Prepare PowerPoint slides  
- Prepare Word report  
- Finalize GitHub README  
- Rehearse 12‑minute demo  
- Verify report format compliance (font, spacing, margins, page limits)  
- Prepare member contribution evidence for examiner questions  

---

## **7. GitHub Development Trace Plan**
To satisfy the evaluation rubric:

### **Commit Strategy**
- Minimum 25–40 commits  
- Each commit represents meaningful progress  
- Use feature branches (e.g., `feature/api`, `feature/deploy`)  
- Merge via pull requests (even if working alone)

### **Repository Contents**
- Source code  
- Deployment scripts  
- README with setup instructions  
- Architecture diagram  
- API documentation link  
- Process log with dated milestones  
- Evidence artifacts for demo (test outputs, deployment screenshots, API runs)  

---

## **8. Risk Assessment**
| Risk | Impact | Mitigation |
|------|--------|------------|
| AWS EC2 misconfiguration | Deployment failure | Follow AWS setup checklist |
| RDS connection issues | API downtime | Use correct security group rules |
| Time constraints | Incomplete features | Keep scope small and focused |
| GitHub trace too sparse | Low score | Commit frequently and meaningfully |

---

## **9. Expected Deliverables**
### **Software Deliverables**
- Fully functional Spring Boot microservice  
- Deployed EC2 instance with public endpoint  
- RDS database with persistent task data  

### **Documentation Deliverables**
- PowerPoint slides (≤10 pages)  
- Word report (≤10 pages)  
- GitHub repository with full development trace  

### **Presentation Deliverables**
- 12‑minute live demo  
- Architecture explanation  
- Clear explanation of individual contribution  

---

## **10. Pre-Submission Compliance Checklist**
- [ ] Teacher repository access granted to `wchshapp_business@icloud.com`.
- [ ] GitHub trace shows continuous development (not a single commit pattern).
- [ ] Live demo path tested end-to-end on deployed cloud endpoint.
- [ ] PowerPoint within 10-page limit (excluding cover page).
- [ ] Word report within 10-page limit (excluding cover page).
- [ ] Word report format verified: Times New Roman 12 pt, A4, 1-inch margins, single column, line spacing not less than single.
- [ ] Slides/report content matches the final presentation.
- [ ] Final submission package (PPT + Word) zipped for Learn@PolyU submission by deadline.
- [ ] If objecting to repository reuse, email sent to `csqwang@polyu.edu.hk` with required title before final exam weeks.

---

## **11. Conclusion**
This project provides a complete demonstration of cloud‑native backend development using Spring Boot and AWS. It is intentionally scoped to be achievable by a single developer while still meeting all COMP4442 evaluation criteria, including technological merit, development trace, presentation clarity, and documentation quality.