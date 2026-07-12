# ФИНКИ Проекти Хаб

University project submission system for FINKI students and professors.

---

## Tech Stack

| Layer | Tech |
|---|---|
| Backend | Spring Boot 4, Java 21, Spring Security + JWT, Spring Data JPA, Flyway |
| Frontend | Vue 3, Pinia, Vue Router, Axios, Tailwind CSS |
| Database | PostgreSQL (via Docker) |
| Email (dev) | MailHog — fake local SMTP, no account needed |
| Queue | QRQ.app — free QR-based queue tracker |

---

## Quick Start

### 1. Start the database and mail server

You need Docker Desktop installed. Open a terminal in the **root `projecthub/` folder** (not `backend/`) and run:

```bash
docker-compose up -d
```

> ⚠️ **Important:** always run this from the root folder where `docker-compose.yml` lives.
> If you run it from `backend/` by mistake, Docker creates a different database with wrong credentials and the backend won't connect.

This starts:
- PostgreSQL on port **5433** (5433 because port 5432 is taken by a local PostgreSQL install)
- MailHog on port **8025** — open `http://localhost:8025` to see sent emails

Verify it's working:
```bash
docker exec -it projecthub-db psql -U projecthub -d projecthub -c "SELECT 1;"
```
Must print `1 row` before starting the backend.

### 2. Start the backend

Open IntelliJ IDEA → **File → Open** → navigate to `projecthub/backend/` → click on `pom.xml` → **Open as Project**.

> **SDK:** Must be Java 21. Go to File → Project Structure → Project → SDK and select Java 21.
> Download from https://adoptium.net if not installed (Temurin 21).

Click the green run button next to `ProjectHubApplication.java`. Flyway runs all SQL migrations automatically on first start.

### 3. Start the frontend

Open a terminal (either VS Code or IntelliJ's built-in terminal):

```bash
cd frontend
npm install   # first time only
npm run dev
```

Open `http://localhost:5173`

---

## Registration & Roles

Roles are assigned **automatically from the email domain** — no manual setup needed:

| Email domain | Role |
|---|---|
| `@students.finki.ukim.mk` | Student |
| `@finki.ukim.mk` | Professor |

Any other domain is rejected. Students must also provide their 6-digit university index (e.g. `190042`) — the first 2 digits are the enrollment year.

> **To test as a professor:** register with any `name@finki.ukim.mk` email. No index needed.

---

## Features

### Student
- Register with student index — role assigned automatically from email domain
- Submit project ideas with title and description
- Queue number assigned on submission (#1001, #1002...)
- QR code generated linking to QRQ queue tracker
- View all own projects and their current status
- Edit or delete projects while status is PENDING or REVISION_REQUESTED
- Resubmit after professor requests revision
- See queue position — how many projects are ahead in the queue
- Email notification on submission and when professor reviews

### Professor
- Register with professor email — role assigned automatically
- Dashboard with stats (total / pending / approved / declined)
- View all submitted projects with status filters
- Search projects by student index or by title
- Review projects — approve, decline, or request revision with comment
- Assign yourself to a project after approving it
- Set a submission deadline

### Shared
- JWT authentication — stateless, token stored in localStorage
- Expired tokens handled gracefully — redirects to login automatically
- Email notifications via MailHog locally (real SMTP for production)

---

## How Emails Work

In development, **MailHog** catches all emails locally. No real emails are sent.

1. Run the app and submit a project
2. Open `http://localhost:8025`
3. You'll see the submission confirmation and professor alert emails

**For a real demo with actual emails**, replace the mail section in `application.yml`:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-gmail@gmail.com
    password: xxxx-xxxx-xxxx-xxxx   # Gmail App Password (not your regular password)
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

Generate a Gmail App Password at: Google Account → Security → 2-Step Verification → App Passwords.

---

## QRQ Queue Setup

1. Go to `https://qrq.app/create`
2. Create a free queue — no account needed
3. You get two URLs — paste both into `application.yml`:

```yaml
app:
  queue:
    qrq-visitor-url: "https://qrq.app/q/YOUR_ID"    # goes inside the QR code
    qrq-status-url: "https://qrq.app/status/YOUR_ID" # display board for a TV/screen
```

The QR code on each project links to the visitor URL. Students scan it to track their queue position live.

---

## Architecture

### Service Layers

**Domain Service** (`domain/service/`) — pure business rules, no infrastructure:
- "A project cannot go from APPROVED back to REVISION_REQUESTED"
- "Only the student who owns the project can edit it"
- These rules are the same regardless of framework

**Application Service** (`service/`) — coordinates use cases:
- Loads entities from repositories
- Calls domain services for business rules
- Publishes events
- Maps to response DTOs
- Interface + implementation separation so the controller depends on the interface, not the concrete class

**Infrastructure Service** (`service/EmailService.java`) — sends emails via SMTP. Called by the event listener only.

### Events and Listeners

`ProjectApplicationServiceImpl` never calls `emailService` directly. Instead it publishes events:

```java
eventPublisher.publishEvent(new ProjectSubmittedEvent(this, project));
```

`NotificationEventListener` listens with `@TransactionalEventListener(phase = AFTER_COMMIT)` — this means emails only fire **after the database transaction commits**, so the project is guaranteed saved before any email is sent.

`@Async` on the listener means emails send in a background thread — the HTTP response returns immediately.

### Security

**`JwtUtil`** — creates, reads, and validates JWT tokens. Think of a token like a signed wristband: you show it at every door instead of re-entering your password.

**`JwtAuthFilter`** — runs on every request. Reads the `Authorization: Bearer ...` header, validates the token, and tells Spring Security who the user is. Expired tokens are caught silently and the request continues unauthenticated — Spring Security then returns 401, and the frontend's Axios interceptor clears localStorage and redirects to login.

**`SecurityConfig`** — the rulebook. Defines which routes are public, which need a role, and wires in the filter.

---

## Database

PostgreSQL runs in Docker. The app uses **Flyway** to manage the schema — never edit migration files after they've run.

```
db/migration/
├── V1__init_schema.sql       — creates all tables
├── V2__seed_data.sql         — (empty, professors register themselves now)
├── V3__add_student_index.sql — no-op (student_index moved to V1)
└── V4__add_deadline_and_assignment.sql — deadline + assignment columns
```

To add a column later, create `V5__description.sql` — Flyway runs it automatically on restart.

**Useful commands:**
```bash
docker-compose up -d          # start
docker-compose down           # stop (data preserved)
docker-compose down -v        # stop + wipe all data (fresh start)
docker exec -it projecthub-db psql -U projecthub -d projecthub   # open psql
```

To browse data visually: install **DBeaver** (free) and connect with host `localhost`, port `5433`, database `projecthub`, user `projecthub`, password `projecthub123`.

---

## Project Structure

```
projecthub/
├── docker-compose.yml
├── README.md
│
├── backend/
│   ├── pom.xml                          (Spring Boot 4, Java 21)
│   └── src/main/
│       ├── java/com/uni/projecthub/
│       │   ├── ProjectHubApplication.java
│       │   ├── config/
│       │   │   └── SecurityConfig.java
│       │   ├── controller/
│       │   │   ├── AuthController.java
│       │   │   └── ProjectController.java
│       │   ├── domain/
│       │   │   ├── event/
│       │   │   │   ├── ProjectSubmittedEvent.java
│       │   │   │   └── ProjectReviewedEvent.java
│       │   │   └── service/
│       │   │       ├── ProjectDomainService.java
│       │   │       └── ProjectDomainServiceImpl.java
│       │   ├── dto/
│       │   │   ├── request/
│       │   │   └── response/
│       │   ├── entity/
│       │   │   ├── User.java
│       │   │   ├── Project.java
│       │   │   └── Review.java
│       │   ├── enums/
│       │   │   ├── Role.java
│       │   │   └── ProjectStatus.java
│       │   ├── exception/
│       │   │   ├── GlobalExceptionHandler.java
│       │   │   └── ResourceNotFoundException.java
│       │   ├── listener/
│       │   │   └── NotificationEventListener.java
│       │   ├── repository/
│       │   │   ├── UserRepository.java
│       │   │   ├── ProjectRepository.java
│       │   │   └── ReviewRepository.java
│       │   ├── security/
│       │   │   ├── JwtUtil.java
│       │   │   └── JwtAuthFilter.java
│       │   ├── service/
│       │   │   ├── ProjectApplicationService.java
│       │   │   ├── ProjectApplicationServiceImpl.java
│       │   │   ├── AuthService.java
│       │   │   ├── EmailService.java
│       │   │   └── UserDetailsServiceImpl.java
│       │   └── util/
│       │       └── QrCodeUtil.java
│       └── resources/
│           ├── application.yml
│           └── db/migration/
│               ├── V1__init_schema.sql
│               ├── V2__seed_data.sql
│               ├── V3__add_student_index.sql
│               └── V4__add_deadline_and_assignment.sql
│
└── frontend/
    ├── index.html
    ├── package.json
    ├── vite.config.js
    ├── tailwind.config.js
    └── src/
        ├── api/
        │   ├── axios.js
        │   └── index.js
        ├── assets/
        │   └── main.css
        ├── components/common/
        │   ├── StatusBadge.vue
        │   ├── BaseSpinner.vue
        │   └── QrCodeCard.vue
        ├── router/
        │   └── index.js
        ├── stores/
        │   ├── auth.js
        │   └── projects.js
        └── views/
            ├── auth/
            │   ├── LoginView.vue
            │   └── RegisterView.vue
            ├── student/
            │   ├── StudentLayout.vue
            │   ├── StudentProjectsView.vue
            │   ├── SubmitProjectView.vue
            │   └── StudentProjectDetailView.vue
            ├── professor/
            │   ├── ProfessorLayout.vue
            │   ├── ProfessorDashboardView.vue
            │   ├── ProfessorProjectsView.vue
            │   └── ProfessorProjectDetailView.vue
            └── NotFoundView.vue
```

---

## Common Issues

**`password authentication failed for user "projecthub"`**
You ran `docker-compose up` from the wrong folder. Run it from the root `projecthub/` folder, not `backend/`. Then run `docker-compose down -v` to wipe the bad volume and `docker-compose up -d` again from the right folder.

**`Unable to determine Dialect`**
Make sure `application.yml` has `database-platform: org.hibernate.dialect.PostgreSQLDialect` under `spring.jpa`.

**JWT expired error**
Open browser console and run `localStorage.clear()`, then refresh and log in again.

**Packages not recognized in IntelliJ**
Right-click `pom.xml` → Maven → Reload Project. If that fails: File → Invalidate Caches → Invalidate and Restart.

**Frontend has no styling**
Run `npm install` inside the `frontend/` folder first, then `npm run dev`.
