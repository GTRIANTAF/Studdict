[README.md](https://github.com/user-attachments/files/28429156/README.md)
# Studdict

**Studdict** is a collaborative study‑space platform. Students discover and reserve study tables at venues, check in with a QR code, borrow digital e‑books, order food & drinks to their table, pay and split the bill, and earn / redeem loyalty points along the way.

The system has two parts:

- **Backend** — a Spring Boot REST API (Java) with a layered architecture and a relational database.
- **Android client** — a native Java app (one `Activity` per screen) that talks to the backend over REST via Retrofit.

---

## Table of contents

- [Features](#features)
- [Architecture](#architecture)
- [Tech stack](#tech-stack)
- [Repository structure](#repository-structure)
- [Getting started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Run the backend](#run-the-backend)
  - [Run the Android app](#run-the-android-app)
- [REST API overview](#rest-api-overview)
- [Domain model](#domain-model)

---

## Features

Studdict is built around a set of use cases (`UC1`–`UC12`), which map directly to the features below:

| Area | What it does |
|------|--------------|
| **Accounts** (UC11/UC12) | Register and log in. Profiles store name, email, university and department; a loyalty wallet is created per student. |
| **Private reservations** (UC1) | Find available tables in a venue for a date/time/duration and book one privately. A 2‑minute *soft lock* prevents two students from grabbing the same table at once. |
| **Public reservations & matchmaking** (UC2) | Create a public study session for a subject, or join an existing public session for the same subject at the same date/time that still has free seats. Sessions appear on a live board. |
| **Invite codes** (UC3) | A host generates a short‑lived invite code so friends can join a reservation. |
| **Modify / cancel** (UC4) | Change a reservation's time, duration or capacity, or cancel it (which frees the table). |
| **QR check‑in** (UC5) | Scan the table's QR code to validate the reservation (right table, right time) and check in participants. |
| **Digital e‑book loans** (UC7) | Browse/search an e‑book catalog and borrow a title while checked in. Loans are license‑based; they are released on early return, at check‑out, or automatically when the reservation expires. |
| **Food & beverage orders** (UC8) | Browse the menu, build a cart, place an order to the table, and view active orders on a kitchen screen. Orders roll up into the table's bill. |
| **Check‑out, payment & split** (UC6) | Generate the bill (with an itemized list of the table's F&B order lines), pay by card or cash, optionally split it across people, and free the table. |
| **Gamification** (UC9) | Earn points for time spent studying (1 pt/min, capped), redeem points for a discount on the bill, and view a points history. |

---

## Architecture

```
                Android client (Retrofit2 + Gson)
        Screen* Activities ── ApiClient ── StuddictApi
                              │  HTTP / JSON (REST)
                              ▼
        ┌─────────────────────────────────────────────┐
        │            Spring Boot backend                │
        │                                               │
        │   @RestController   →  REST endpoints          │
        │        │                                       │
        │   @Service          →  business logic          │
        │        │                                       │
        │   Repository (JPA)  →  data access             │
        │        │                                       │
        │   @Entity           →  domain model            │
        └─────────────────────────────────────────────┘
                              │
                              ▼
                     Relational database
```

**Backend** follows a classic layered design: `controller → service → repository → entity`.
- **Controllers** expose the REST API and translate DTOs ⇄ domain objects.
- **Services** hold the business rules (reservation overlap checks, soft‑locking, loan licensing, points math, bill calculation, payment flow).
- **Repositories** are Spring Data JPA interfaces (derived queries, no boilerplate).
- A scheduled component (`LoanExpiryScheduler`) runs every 60s to revoke e‑book loans whose reservation has expired, and data initializers seed venues, tables, the menu and the e‑book catalog on startup.

**Android client** uses one `Activity` per screen (the `Screen*` classes). `ApiClient` builds a single Retrofit instance against the `StuddictApi` interface, and `SessionManager` (backed by `SharedPreferences`) keeps the logged‑in student and the active check‑in across screens.

---

## Tech stack

**Backend**
- Java 21, Spring Boot 3.2.4
- Spring Web (REST), Spring Data JPA (Hibernate)
- **MySQL 8** — Hibernate `ddl-auto: update` auto‑creates the schema; the DB password is injected via `spring-dotenv` (`${DB_PASSWORD}`)
- Maven build

**Android**
- Java 17, Android SDK 34 (Gradle build); `minSdk 23`, `targetSdk 34`
- Retrofit 2.11.0 + Gson converter for networking/serialization
- ZXing (`zxing-android-embedded`) for QR‑code scanning at check‑in
- AndroidX AppCompat + RecyclerView
- `SharedPreferences` for session state

> Full configuration lives in `pom.xml`, `src/main/resources/application.yml`, and `android-app/app/build.gradle`.

---

## Repository structure

```
Studdict/
├── pom.xml                       # Backend Maven build
├── src/
│   ├── main/
│      ├── java/com/studdict/
│      │   ├── StuddictApplication.java   # @SpringBootApplication, @EnableScheduling
│      │   ├── DataInitializer.java       # seeds venues/tables/students/menu
│      │   ├── LoanExpiryScheduler.java   # @Scheduled loan revocation
│      │   ├── config/                    # e.g. EBookDataInitializer
│      │   ├── controller/                # @RestController endpoints
│      │   ├── service/                   # business logic (@Service)
│      │   ├── repository/                # Spring Data JPA repositories
│      │   ├── model/                     # JPA entities
│      │   └── dto/                       # request/response payloads
│      └── resources/application.yml      # DB + app configuration
│
├── android-app/                  # Android client (Gradle)
│   └── app/src/main/
│       ├── java/com/studdict/mobile/
│       │   ├── Screen*.java               # one Activity per screen
│       │   ├── SessionManager.java        # session state
│       │   ├── api/{ApiClient, StuddictApi}.java
│       │   └── model/                     # Gson DTOs mirroring backend JSON
│       ├── res/                           # layouts, drawables, values
│       └── AndroidManifest.xml
│
├── class_diagram.png            # design class diagram (backend + client)
```

---

## Getting started

### Prerequisites

- **JDK 21** (backend)
- **Maven 3.9+** (or use your IDE's bundled Maven)
- **MySQL 8** running locally on `localhost:3306`. The `Studdict` database is created automatically (`createDatabaseIfNotExist=true`) and the schema is managed by Hibernate (`ddl-auto: update`). The default user is `root`; set the password via a `DB_PASSWORD` environment variable (or a `.env` file, read by `spring-dotenv`).
- **Android Studio** (Giraffe or newer) with an Android emulator or device, for the client

### Run the backend

The backend's entry point is `com.studdict.StuddictApplication`.

1. Start MySQL and export the DB password, e.g.:
   ```bash
   # macOS/Linux
   export DB_PASSWORD=your_mysql_root_password
   # Windows (PowerShell)
   $env:DB_PASSWORD = "your_mysql_root_password"
   ```
   The `Studdict` schema is created and migrated automatically on first run. Connection details are in `src/main/resources/application.yml`.
2. Start the application:
   - **From your IDE:** run the `StuddictApplication` class, **or**
   - **From the command line:** build and launch the packaged app, e.g.
     ```bash
     mvn clean package
     java -jar target/Studdict-1.0-SNAPSHOT.jar
     ```
3. The API serves on **`http://localhost:8080`** by default (the Android client expects this port — override with `server.port` in `application.yml` if needed).

> On startup the data initializers seed sample venues, tables, menu items and e‑books so you can exercise the full flow immediately.

### Run the Android app

1. Open the `android-app/` folder in **Android Studio** and let Gradle sync.
2. Make sure the backend is running.
3. Check the API base URL in `android-app/app/src/main/java/com/studdict/mobile/api/ApiClient.java`:
   ```java
   public static final String BASE_URL = "http://10.0.2.2:8080/";
   ```
   - `10.0.2.2` is the special alias that lets the **Android emulator** reach `localhost` on the host machine.
   - To run on a **physical device**, change this to your computer's LAN IP (e.g. `http://192.168.x.x:8080/`) and ensure both are on the same network.
4. Run the app on an emulator/device. The launcher screen is **Login** (`ScreenLogin`); register a new account, then log in to reach the venues/home screen.

> The app uses cleartext HTTP for local development (see `AndroidManifest.xml`).

---

## REST API overview

Base URL: `http://localhost:8080`. Selected endpoints, grouped by feature:

| Feature | Method & path |
|---------|---------------|
| **Accounts** | `POST /api/students/register`, `POST /api/students/login` |
| **Tables** | `GET /api/tables/available`, `GET /api/tables/matchmaking` (by subject + date/time/duration/capacity), `POST /api/tables/{id}/lock`, `POST /api/tables/{id}/unlock` |
| **Reservations** | `POST /api/reservations/private`, `POST /api/reservations/public`, `POST /api/reservations/{id}/join`, `PUT /api/reservations/{id}/modify`, `DELETE /api/reservations/{id}`, `GET /api/reservations/student/{studentId}` |
| **Live board** | `GET /api/liveboard/published` |
| **Invite codes** | `POST /invite-code/generate`, `POST /invite-code/validate`, `POST /invite-code/join`, `POST /invite-code/join-result` |
| **Check‑in** | `POST /check-in/validate`, `GET /check-in/participants`, `POST /check-in/confirm`, `POST /check-in/perform` |
| **E‑books** | `GET /api/ebooks/catalog`, `GET /api/ebooks/search`, `GET /api/ebooks/availability/{id}`, `GET /api/ebooks/{id}/content`, `POST /api/ebooks/loan`, `GET /api/ebooks/loan/{loanId}`, `POST /api/ebooks/return/{loanId}`, `GET /api/ebooks/loans/active/{checkInId}` |
| **Orders (F&B)** | `GET /api/orders/catalog`, `POST /api/orders/cart/add`, `POST /api/orders/summary`, `POST /api/orders/create`, `GET /api/orders/table/{tableId}/items`, `GET /api/orders/kitchen/active` |
| **Bills & payments** | `GET /api/bills/table/{tableId}`, `POST /payments/process`, `POST /payments/split` |
| **Gamification** | `GET /api/gamification/wallet/{studentId}`, `POST /api/gamification/earn`, `POST /api/gamification/redeem`, `POST /api/gamification/validate`, `GET /api/gamification/history/{studentId}` |

The complete client‑side contract is the Retrofit interface `android-app/.../api/StuddictApi.java`.

---

## Domain model

Core entities and their relationships:

- **Student** owns a **LoyaltyWallet** (1‑to‑1) and accumulates **PointsTransaction**s (EARN / REDEEM).
- **Venue** *contains* many **StudyTable**s.
- **Reservation** is abstract with two subtypes — **PrivateReservation** and **PublicReservation** — each tied to a **StudyTable**. A **PublicReservation** carries a **StudySubject**. Either reservation type may have **InviteCode**s and **ReservationParticipant**s.
- **CheckIn** records a student arriving at a table for a reservation, and is the gateway to **EBookLoan**s.
- **EBook** has multiple **EBookLicense**s; an **EBookLoan** consumes a license for the duration of a check‑in.
- **Order** *contains* **OrderItem**s referencing **MenuItem**s; orders for a table roll up into a **Bill**, which is settled by one or more **Payment**s.

See `class_diagram.png` for the full design class diagram (controllers, services, repositories, entities and the Android client).
