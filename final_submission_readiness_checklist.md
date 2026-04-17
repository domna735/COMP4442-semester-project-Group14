# Final Submission Readiness Checklist (COMP4442)

Use this checklist during the final review meeting before uploading the submission zip.

Date checked: ____________
Checked by: ____________

---

## A. Core Deliverables

- [ ] PowerPoint file prepared (.pptx)
- [ ] Word report prepared (.docx)
- [ ] Both files are included in final zip
- [ ] Group leader confirmed one submission to Learn@PolyU by deadline

---

## B. Format Compliance (Strict)

### PowerPoint
- [ ] Maximum 10 content slides (cover page excluded)
- [ ] Slide content matches live presentation and demo flow
- [ ] Team member role division clearly shown

### Word Report
- [ ] Maximum 10 pages (cover page excluded)
- [ ] Font is Times New Roman, 12pt throughout
- [ ] Paper size is A4
- [ ] Margins are 1 inch on all sides
- [ ] Single column layout
- [ ] Line spacing is not less than single
- [ ] Report content matches slides and presentation narrative

---

## C. Metric 1 - Technological Merit

- [ ] Spring Boot layered architecture explained (controller/service/repository)
- [ ] JWT access + refresh authentication explained and demonstrated
- [ ] User-scoped task isolation explained and demonstrated
- [ ] File API protection (upload/list/download) demonstrated
- [ ] Cloud deployment architecture (EC2 + RDS or fallback strategy) documented
- [ ] OpenAPI/Swagger usage included in demo or slides

Evidence links:
- [README.md](README.md)
- [playbook.md](playbook.md)
- [realtime_demo_playbook.md](realtime_demo_playbook.md)
- [full_technical_report.md](full_technical_report.md)

---

## D. Metric 2 - GitHub Development Trace Completeness

- [ ] Commit history shows continuous development (not single-commit repository)
- [ ] Meaningful commit messages exist across backend/deploy/frontend/testing/docs
- [ ] Team contribution trace is visible in git history
- [ ] Repository access shared with teacher account: wchshapp_business@icloud.com
- [ ] Team informed about opt-out email rule for future teaching/research use

Evidence links:
- [process log.md](process%20log.md)
- [plan for project.md](plan%20for%20project.md)
- [README.md](README.md)

---

## E. Metric 3 - Presentation and Demo Clarity

- [ ] Presentation rehearsal completed under 12-minute accumulated exam clock
- [ ] Demo script tested end-to-end on rehearsal machine
- [ ] Team can answer likely technical Q&A (auth, isolation, deployment, security)
- [ ] Job divisions are clearly stated and consistent across slides/script
- [ ] Demo backup plan prepared (local + cloud fallback)

Evidence links:
- [ppt.md](ppt.md)
- [ppt_script.md](ppt_script.md)
- [full real time demo playbook.md](full%20real%20time%20demo%20playbook.md)

---

## F. Metric 4 - Document Quality

- [ ] Language reviewed for clarity and consistency
- [ ] Outdated architecture wording removed (session/H2-only references)
- [ ] Current architecture reflected consistently (JWT/SQLite/file security)
- [ ] No contradictions between slides, report, README, and live demo flow

Evidence links:
- [report.md](report.md)
- [full_technical_report.md](full_technical_report.md)
- [README.md](README.md)

---

## G. Deployment and Runtime Validation

- [ ] systemd template verified and typo-free
- [ ] Production env file prepared from template
- [ ] DB precheck executed successfully
- [ ] Deployment verifier executed successfully against target endpoint
- [ ] Verification output archived as evidence

Commands to run:

```bash
./deploy/ec2/setup-db.sh deploy/ec2/.env.prod
./deploy/ec2/run-prod.sh
./deploy/ec2/verify-deploy.sh http://<EC2_PUBLIC_IP>:8080
```

Evidence links:
- [deploy/systemd/cloud-compute.service](deploy/systemd/cloud-compute.service)
- [deploy/ec2/setup-db.sh](deploy/ec2/setup-db.sh)
- [deploy/ec2/run-prod.sh](deploy/ec2/run-prod.sh)
- [deploy/ec2/verify-deploy.sh](deploy/ec2/verify-deploy.sh)

---

## H. Final Go/No-Go Decision

- [ ] GO: All critical items above checked
- [ ] NO-GO: Remaining blockers documented below

Blockers (if any):
- ____________________________________________
- ____________________________________________
- ____________________________________________
