# Agent Implementation Protocol

This protocol defines the strict operational rules and boundaries for the AI coding assistant working on the Ecommerce Security project.

## 1. Execution Order & Automatic Progression
The project strictly follows this execution order:
- **Phase 3:** Authentication & Authorization
- **Phase 4:** Core E-commerce
- **Phase 5:** Payment Security
- **Phase 6:** Security Hardening & Attack Testing
- **Phase 7:** Frontend Integration
- **Phase 8:** Final Security Audit, Demo & Evidence

**Automatic progression:** After a phase passes all criteria, the agent may automatically continue to the next phase.

## 2. Phase Workflow
Every phase must rigidly follow this automatic workflow:
`inspect → plan → implement → test → fix → security review → verify → completion report`

## 3. Hard-Stop Conditions
If any of the following actions occurs or becomes necessary, the agent must **STOP and report immediately**:
- `DROP DATABASE`
- `DROP TABLE`
- delete project directory
- recursive destructive deletion
- `git reset --hard`
- `git clean -fd`
- kill native PostgreSQL
- modify native PostgreSQL
- delete Docker volumes
- expose real credentials
- hardcode secrets
- store plaintext passwords
- store raw card/CVV/payment credentials
- bypass authentication/authorization
- change approved architecture
- change approved 9-entity database design
- change approved API contracts
- destructive database migration

## 4. Database Safety
- **Native PostgreSQL** remains on host port `5432`.
- **Project Docker PostgreSQL** remains mapped as `5433:5432`.
- Never reset or drop the database automatically.

## 5. Secret Management & Security
- **No Plaintext Passwords:** Never store, log, or commit plaintext passwords. Seed data must use Argon2id hashes.
- **No Real Secrets:** No real passwords, JWT secrets, API keys, payment credentials, or cloud credentials may be used.
- **Environment Driven:** Use environment variables or runtime-generated test secrets. If a required secret is missing, the application must fail-fast.

## 6. Implementation Boundaries
- **Phased Execution:** Only implement the specific Phase currently active. Do not implement features, business logic, or UI components for subsequent phases prematurely.
- **Strict Compliance:** Adhere strictly to the `system-design.md` and `architecture-review.md` artifacts.
- **Role Terminology:** Use exact role names as specified in the design (e.g., `CUSTOMER` and `ADMIN`).

## 7. Testing Standard
- Every phase requires relevant unit tests, integration tests, security tests, regression tests, build verification, and read-only secret scanning where applicable.
- **Never weaken or delete tests to make them pass.**

## 8. Reporting Requirements

### Standard Completion Report Format
At the end of each phase, output a report with exactly this format:
- Phase Status
- Files Created
- Files Modified
- Dependencies Added
- Tests Executed
- Test Results
- Security Controls
- Security Tests
- Known Issues
- Deferred Items
- Next Phase

### Final Phase 8 Report
The Phase 8 report must include: functional tests, security tests, evidence, architecture, API endpoints, database changes, known issues, demo flow, and report evidence.
