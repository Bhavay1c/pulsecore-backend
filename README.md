# PulseBoard — Live Conference Check-In API

A Spring Boot REST API for real-time conference check-in and attendee dashboards.
Front desk checks attendees in by name; the organizer's dashboard reflects live
capacity, VIP status, and recent check-ins.

Built as a v2 of an earlier Django/ASP.NET event platform, re-architected around
the stack and concepts (REST APIs, SQL, real-time event flow) used in
event-driven, enterprise backend systems.

## Tech Stack

- Java 17, Spring Boot 3.2
- Spring Data JPA + H2 (in-memory, zero setup) — Postgres profile included for production
- Bean Validation
- JUnit 5

## Running locally

Requires Java 17+ and Maven (or use the bundled `./mvnw` wrapper if you add one).

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080`. Demo data (4 attendees, 2 VIP) loads
automatically on startup.

Run tests:

```bash
mvn test
```

H2 console (inspect the in-memory DB while the app runs):
`http://localhost:8080/h2-console` — JDBC URL `jdbc:h2:mem:pulseboard`, user `sa`, no password.

## API Endpoints

### Register an attendee
```
POST /api/attendees/register
Content-Type: application/json

{ "fullName": "Jane Doe", "email": "jane@example.com", "vip": false }
```

### Check in by name
```
POST /api/checkin
Content-Type: application/json

{ "fullName": "Jane Doe" }
```
Re-scanning an already-checked-in name is idempotent — it returns 200 with
their current state instead of erroring.

### List attendees
```
GET /api/attendees
GET /api/attendees?vip=true
```

### Dashboard summary
```
GET /api/dashboard
```
Returns capacity, total registered, total checked in, spots remaining,
VIP counts, and the 10 most recent check-ins.

## Configuration

Event capacity defaults to 200. Override via environment variable:
```bash
EVENT_CAPACITY=500 mvn spring-boot:run
```

## Next steps (see project roadmap)

- React frontend consuming this API (dashboard + check-in desk view)
- WebSocket/STOMP layer so the dashboard updates live without polling
- Dockerize (Dockerfile + docker-compose with Postgres)
- Message broker (RabbitMQ/Kafka) publishing check-in events to a
  separate notification/analytics service
