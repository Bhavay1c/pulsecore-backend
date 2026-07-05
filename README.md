# PulseBoard — Live Conference Check-In API

A Spring Boot REST API for real-time conference check-in, pre-registration, and
attendee dashboards. Front-desk staff check attendees in by search; attendees
can pre-register for a session ahead of time; organizers can spin up new
sessions at runtime; the dashboard reflects live capacity, VIP status, and
recent check-ins.

Built as a v2 of an earlier Django/ASP.NET event platform, re-architected around
the stack and concepts (REST APIs, SQL, real-time event flow) used in
event-driven, enterprise backend systems.

## Live Demo

Deployed on [Render](https://render.com): https://pulsecore-backend.onrender.com/

Free-tier Render services spin down after inactivity — the first request
after a while may take 30-60s to wake the instance up. Try, e.g.,
`https://pulsecore-backend.onrender.com/api/sessions`.

The live frontend consuming this API is at
https://eventcheckin-pulsecore-frontend.onrender.com/.

## Tech Stack

- Java 17, Spring Boot 3.2
- Spring Data JPA + H2 (in-memory, zero setup) — Postgres profile included for production
- Bean Validation
- Lombok
- JUnit 5

## Data model

- **Session** — a track/talk within the conference (e.g. "Opening Keynote"), with a `name` (unique) and `capacity`.
- **Attendee** — belongs to a `Session`; has `fullName`, `email` (unique), a generated `ticketId` (`TCK-XXXXXXXX`), `vip` flag, `checkedIn` flag, `checkInTime`, and `registeredAt`.

There is currently a single implicit conference made up of multiple `Session`s — not a multi-tenant "Event" concept.

## Running locally

Requires Java 17+. Use the bundled Maven wrapper — no local Maven install needed:

```bash
./mvnw spring-boot:run
```

The API starts on `http://localhost:8080`. Demo sessions and attendees (some
VIP, some already checked in) load automatically on startup against the
in-memory H2 database, and reset on every restart.

Run tests:

```bash
./mvnw test
```

H2 console (inspect the in-memory DB while the app runs):
`http://localhost:8080/h2-console` — JDBC URL `jdbc:h2:mem:pulseboard`, user `sa`, no password.

### Via Docker

From the repo root:

```bash
docker compose up --build backend
```

## API Endpoints

All endpoints are under `/api`.

### Pre-register an attendee (no check-in)
```
POST /api/attendees/register
Content-Type: application/json

{ "fullName": "Jane Doe", "email": "jane@example.com", "sessionId": 1, "vip": false }
```
Rejects duplicate emails (409) and unknown session IDs (404).

### Add a walk-in (register + immediate check-in)
```
POST /api/attendees/walk-in
Content-Type: application/json

{ "fullName": "Jane Doe", "email": "jane@example.com", "sessionId": 1 }
```
Fails with 409 if the session is already at capacity.

### Check in / undo check-in
```
POST /api/attendees/{id}/checkin
POST /api/attendees/{id}/undo-checkin
```
Re-checking in an already-checked-in attendee is idempotent — it returns 200
with their current state instead of erroring. Checking in fails with 409 if
the attendee's session is at capacity.

### List attendees
```
GET /api/attendees
GET /api/attendees?vip=true
```

### List sessions
```
GET /api/sessions
```
Each session response includes its current checked-in count.

### Create a session
```
POST /api/sessions
Content-Type: application/json

{ "name": "Scaling Real-Time Systems", "capacity": 100 }
```
Rejects duplicate session names (409).

### Dashboard summary
```
GET /api/dashboard
```
Returns overall capacity, total registered, total checked in, spots
remaining, VIP counts, the 10 most recent check-ins, and the full session
list with per-session capacity.

## Configuration

Overall event capacity (used for the top-level dashboard stat; per-session
capacity is enforced separately) defaults to 200. Override via environment
variable:
```bash
EVENT_CAPACITY=500 ./mvnw spring-boot:run
```

CORS-allowed origins default to `http://localhost:5173,http://localhost:3000`,
overridable via `APP_CORS_ALLOWED_ORIGINS`.

A Postgres profile is available for production use — activate with
`SPRING_PROFILES_ACTIVE=postgres` and set `DB_HOST`/`DB_PORT`/`DB_NAME`/`DB_USER`/`DB_PASSWORD`.

## Next steps (see project roadmap)

- WebSocket/STOMP layer so the dashboard updates live without polling
- Message broker (RabbitMQ/Kafka) publishing check-in events to a
  separate notification/analytics service
- A real multi-tenant `Event` entity if PulseBoard needs to host more than
  one conference per deployment
