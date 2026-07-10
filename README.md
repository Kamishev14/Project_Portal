# ProjectHub — University Project Submission System

## Tech Stack
- **Backend:** Spring Boot 3, Java 17, Spring Security + JWT, Spring Data JPA, Flyway
- **Frontend:** Vue 3, Pinia, Vue Router, Axios, Tailwind CSS
- **Database:** PostgreSQL (via Docker)
- **Email (dev):** MailHog — a fake SMTP server, no real emails, no Google account needed
- **Queue:** QRQ.app — free QR-based queue display

---

## Quick Start

### 1. Start the database and mail server
You need Docker Desktop installed. Then run:
```bash
cd projecthub
docker-compose up -d
```
This starts two things:
- PostgreSQL on port 5432
- MailHog on port 8025 (open http://localhost:8025 to see "sent" emails)

### 2. Start the backend
Open the `backend/` folder in IntelliJ IDEA.

**Finding application.yml in IntelliJ:**
Go to `src → main → resources → application.yml`. If you don't see it, make sure the `resources` folder is marked as a Resources Root: right-click it → "Mark Directory as" → "Resources Root".

**Run:** Click the green play button next to `ProjectHubApplication.java`.

Flyway will automatically run your SQL files and create all tables on first start.

### 3. Start the frontend
```bash
cd frontend
npm install
npm run dev
```
Open http://localhost:5173

---

## Understanding the Architecture

### Service Layers Explained

The project has three layers of services — here's what each one does:

#### 1. Domain Service (`domain/service/`)
- Contains **pure business rules** — no database, no emails, no HTTP
- Example: "a project cannot go from APPROVED back to REVISION_REQUESTED"
- Example: "only the student who owns the project can view it"
- These rules would be the same even if you switched from Spring Boot to something else
- `ProjectDomainService` / `ProjectDomainServiceImpl`

#### 2. Application Service (`service/`)
- **Coordinates** a use case end to end
- Loads entities from repositories, calls domain services for rules, fires events
- Does NOT contain business rules itself
- Does NOT send emails itself — it fires an event and lets the listener handle that
- `ProjectApplicationService` (interface) / `ProjectApplicationServiceImpl`
- Using an **interface** here is important — the controller depends on the interface, not the
  implementation. This means you could swap the implementation (e.g. for testing) without
  changing the controller at all.

#### 3. Infrastructure Service (`service/EmailService.java`)
- Handles technical concerns — sending actual emails via SMTP
- Called by the event listener, not directly by application services

---

### Events and Listeners Explained

This is the pattern your uni taught. Here's a plain explanation:

**The problem without events:**
```
// Without events, ProjectApplicationService has to know about emails:
projectRepository.save(project);
emailService.sendConfirmation(project);   // now tightly coupled to email
slackService.notify(project);             // and Slack too
smsService.alert(project);               // and SMS too
```
If you add a new notification type, you have to edit `ProjectApplicationService` every time.

**The solution with events:**
```
// With events, ProjectApplicationService just announces what happened:
projectRepository.save(project);
eventPublisher.publishEvent(new ProjectSubmittedEvent(this, project));
// Done. It doesn't know or care what listens.
```

Then separately, `NotificationEventListener` listens:
```java
@EventListener
public void onProjectSubmitted(ProjectSubmittedEvent event) {
    emailService.sendSubmissionConfirmation(event.getProject());
}
```

To add Slack notifications later, you just add a `SlackEventListener` class — you never touch
`ProjectApplicationServiceImpl` at all. That's the point: **the service only knows what happened,
not what to do about it**.

The `@Async` annotation means the email sends in a background thread, so the HTTP response
returns to the student immediately without waiting for the email.

---

### Security Explained (JWT + Spring Security)

Here's how the three security classes work together, in plain terms:

#### `JwtUtil.java`
A helper that knows how to:
- **Create** a JWT token: takes a user's email, packs it into a signed token string
- **Read** a JWT token: unpacks the token to get the email back
- **Validate** a token: checks it hasn't expired and the signature is valid

Think of a JWT token like a signed wristband at an event. Once you have it, you show it at every
door instead of re-entering your name and password each time.

#### `JwtAuthFilter.java`
Runs on **every single HTTP request** before it reaches your controller.
1. Looks for `Authorization: Bearer <token>` in the request header
2. If found, asks `JwtUtil` to read the email from the token
3. Loads the user from the database
4. If valid, tells Spring Security "this request is authenticated as this user"
5. If no token (or invalid), it just moves on — Spring Security will block if the route requires auth

#### `SecurityConfig.java`
The rulebook. Defines:
- Which routes are public (`/api/auth/**` — anyone can hit login/register)
- Which routes need a specific role (`/api/projects/*/review` needs `PROFESSOR`)
- Everything else needs to be logged in (`anyRequest().authenticated()`)
- Plugs in `JwtAuthFilter` so it runs before every request
- Disables sessions (stateless — the server doesn't remember you, the token proves who you are)

**Flow for a protected request:**
```
Client → sends request with "Authorization: Bearer eyJ..."
  ↓
JwtAuthFilter → reads token → validates → sets authentication in Spring's context
  ↓
SecurityConfig rules → checks if the authenticated user has the right role
  ↓
Controller → @AuthenticationPrincipal gives you the logged-in user object
```

---

### Email — Does It Cost Money?

**No — in development we use MailHog** which is 100% free and local.

MailHog is a fake SMTP server that catches all "sent" emails and shows them in a web UI at
http://localhost:8025. No real emails are sent, no Google account needed, no credit card.

**If you want real emails for production** (e.g. for your final demo), the cheapest options are:
- **Gmail App Password** — free, 500 emails/day. You enable 2FA on a Gmail account, generate an
  "App Password", and paste it into application.yml. (See instructions below.)
- **Brevo (ex-Sendinblue)** — free tier: 300 emails/day, no credit card

**To switch to real Gmail for demo day:**
In `application.yml`, replace the mail section with:
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

---

### How to Find application.yml in IntelliJ

1. Open IntelliJ → open the `backend/` folder as a Maven project
2. In the Project panel on the left, expand: `src → main → resources`
3. `application.yml` is there
4. If the `resources` folder has a plain folder icon instead of a resources icon:
   - Right-click `resources` → "Mark Directory as" → "Resources Root"
5. The file will now be recognized by Spring Boot and shown with a leaf icon

---

### How the SQL Flyway Files Work in IntelliJ

Flyway reads SQL files from `src/main/resources/db/migration/` and runs them automatically when
Spring Boot starts.

**Naming convention is strict:** `V{number}__{description}.sql`
- `V1__init_schema.sql` — runs first, creates all tables
- `V2__seed_data.sql` — runs second, inserts the default professor
- If you need to add a column later: create `V3__add_something.sql` — never edit V1 or V2

**In IntelliJ:**
1. Expand `src → main → resources → db → migration`
2. You'll see V1 and V2 already there
3. To create a new migration: right-click the folder → New → File → name it `V3__...sql`
4. Write your SQL there
5. Restart the app — Flyway runs it automatically

**You do NOT run these files manually.** Flyway tracks which ones it has already run in a
`flyway_schema_history` table in your database.

---

### Do You Need to Install PostgreSQL?

**No — Docker handles it.** You don't install PostgreSQL on your machine at all.

`docker-compose up -d` starts a PostgreSQL container with:
- Database name: `projecthub`
- Username: `projecthub`
- Password: `projecthub123`
- Port: 5432 (same as a normal PostgreSQL install)

These match exactly what's in `application.yml`, so Spring Boot connects automatically.

**To view your data** (optional):
- Install **DBeaver** (free) or use IntelliJ's built-in Database panel
- Connect with the credentials above, host `localhost`, port `5432`

**Useful Docker commands:**
```bash
docker-compose up -d        # start in background
docker-compose down         # stop
docker-compose down -v      # stop AND delete all data (fresh start)
docker-compose logs -f      # see live logs
```

---

## Project Structure

```
projecthub/
├── docker-compose.yml              ← starts PostgreSQL + MailHog
│
├── backend/
│   ├── pom.xml
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
│       │   │       ├── ProjectDomainService.java        ← interface
│       │   │       └── ProjectDomainServiceImpl.java    ← implementation
│       │   ├── dto/
│       │   │   ├── request/  (LoginRequest, RegisterRequest, ProjectRequest, ReviewRequest)
│       │   │   └── response/ (AuthResponse, ProjectResponse, ReviewResponse, DashboardStatsResponse)
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
│       │   │   └── NotificationEventListener.java      ← handles events → sends emails
│       │   ├── repository/
│       │   │   ├── UserRepository.java
│       │   │   ├── ProjectRepository.java
│       │   │   └── ReviewRepository.java
│       │   ├── security/
│       │   │   ├── JwtUtil.java                        ← creates/reads/validates JWT tokens
│       │   │   └── JwtAuthFilter.java                  ← runs on every request
│       │   ├── service/
│       │   │   ├── ProjectApplicationService.java      ← interface
│       │   │   ├── ProjectApplicationServiceImpl.java  ← implementation
│       │   │   ├── AuthService.java
│       │   │   ├── EmailService.java
│       │   │   └── UserDetailsServiceImpl.java
│       │   └── util/
│       │       └── QrCodeUtil.java
│       └── resources/
│           ├── application.yml
│           └── db/migration/
│               ├── V1__init_schema.sql
│               └── V2__seed_data.sql
│
└── frontend/
    ├── src/
    │   ├── api/          (axios.js, index.js)
    │   ├── assets/       (main.css)
    │   ├── components/common/
    │   │   ├── StatusBadge.vue
    │   │   ├── BaseSpinner.vue
    │   │   └── QrCodeCard.vue
    │   ├── router/       (index.js — routes + guards)
    │   ├── stores/       (auth.js, projects.js)
    │   └── views/
    │       ├── auth/     (LoginView, RegisterView)
    │       ├── student/  (Layout, Projects, Submit, Detail)
    │       ├── professor/(Layout, Dashboard, Projects, Detail with review form)
    │       └── NotFoundView.vue
    ├── package.json
    ├── vite.config.js
    └── tailwind.config.js

```

---

## Default Accounts

| Role      | Email                | Password      |
|-----------|----------------------|---------------|
| Professor | professor@uni.edu    | professor123  |

Students register themselves at `/register`.

---

## QRQ Queue Setup

1. Go to https://qrq.app/create
2. Create a free queue (no account needed)
3. Copy the status URL they give you — looks like `https://qrq.app/status/810407`
4. Paste it into `application.yml` under `app.queue.qrq-status-url`
5. Every submitted project will have a QR code that links to this page
6. The professor advances the queue from their phone as they review projects
