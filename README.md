# product-catalogue
A product catalogue where customers browse, search and submit an enquiry. Built as a foundation to grow, not a throwaway prototype.

- **Client:** Android (Kotlin, Jetpack Compose, Hilt, Retrofit)
- **Backend:** Node.js + TypeScript (Fastify)
- **Product data:** Fake Store API, accessed only through the backend
- **Email:** SMTP, with Mailpit capturing mail locally (no paid accounts)

Why this stack: see [DECISIONS.md](DECISIONS.md). How AI was used: see [AI_USAGE.md](AI_USAGE.md).

## Prerequisites

- Docker and Docker Compose (for the quickest route), **or** Node.js 20+
- Android Studio with an emulator (for the client)

## Run the backend (about 2 minutes)

```bash
docker compose up --build
```

- API: http://localhost:3000 (try http://localhost:3000/api/products/featured)
- Mailpit inbox (where local enquiry emails are captured): http://localhost:8025

With the default Docker setup, enquiries are captured by Mailpit and are **not sent to an external email inbox**. The default `SALES_INBOX_EMAIL` (`sales@example.com`) is a placeholder. Check Mailpit at `http://localhost:8025` to see successful local submissions.

To deliver enquiries through a real SMTP provider, configure `SMTP_HOST` and `SMTP_PORT` in a root `.env` file (Docker Compose uses these to override its Mailpit defaults). Set `SALES_INBOX_EMAIL`, `EMAIL_FROM`, `SMTP_USER` and `SMTP_PASS` in `backend/.env`. Use credentials and a sender address accepted by your SMTP provider, and do not commit either `.env` file. Recreate the backend after changing configuration:

```bash
docker compose up --build -d
```

For running the backend directly with `npm run dev`, all settings including `SMTP_HOST` and `SMTP_PORT` can be placed in `backend/.env`; use `localhost:1025` to deliver to a Mailpit instance running on the host.

### Without Docker

```bash
cd backend
npm install
npm run dev          # needs an SMTP server on localhost:1025 for enquiries, e.g. Mailpit
```

### Tests

```bash
cd backend
npm test             # unit and API tests
npm run typecheck
```

## Run the Android app

1. Start the backend and Mailpit with `docker compose up --build`.
2. Open `android/` in Android Studio and run the `app` configuration on an emulator.
3. The default base URL is `http://10.0.2.2:3000/` for the Android emulator.
4. To use a physical device, set `catalogue.baseUrl=http://<lan-ip>:3000/` in `android/gradle.properties` (or on the Gradle command line) and allow that host in `android/app/src/main/res/xml/network_security_config.xml`.

From the repository root:

```bash
cd android
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

The Android app uses Jetpack Compose screens for home, categories, search, product details and enquiries. Retrofit calls only the Node.js backend; Hilt provides the repository and network dependencies. Its theme supports light and dark system modes, uses the Material 3 typography scale so text respects system font scaling, and provides accessibility labels for icon actions and product images. The UI has been checked across multiple display sizes, font scales and both theme modes. Unit tests cover the repository contract and ViewModels.

## API

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/products/featured` | The configured "top rated" products for the home page |
| GET | `/api/categories` | Category names |
| GET | `/api/categories/:category/products` | `{ topRated, others }` for a category |
| GET | `/api/products?q=term` | Search title, description and category. `q` is required |
| GET | `/api/products/:id` | Product details |
| POST | `/api/enquiries` | Submit an enquiry. Returns `201` once the email is accepted |
| GET | `/health` | Liveness check |

Enquiry body:

```json
{ "productId": "1", "name": "Ada", "email": "ada@example.com", "phone": "+91 98765 43210", "message": "Is this in stock?" }
```

`phone` is optional. Errors use `{ "error": "<code>", ... }` with status 400 (validation), 404, 429 (rate limit) or 502 (data source or email unavailable).

## Configuration

All settings are environment variables. The ones you are most likely to change:

| Variable | Purpose | Default |
|----------|---------|---------|
| `FEATURED_PRODUCT_IDS` | Ids shown as "top rated" on the home page, in order | `1,2,3,4,5` |
| `TOP_RATED_PER_CATEGORY` | Top rated products leading each category page | `3` |
| `SALES_INBOX_EMAIL` | Where enquiries are sent | `sales@example.com` |
| `SMTP_HOST` / `SMTP_PORT` | Mail server (`localhost` when running backend directly; `mailpit` in Docker) | `localhost` / `1025` |
| `FAKE_STORE_BASE_URL` | Product source | `https://fakestoreapi.com` |

No secrets are committed. Production credentials belong in environment variables or a secrets manager.

## Project layout

```
backend/
  src/
    app.ts              HTTP routes, validation, error mapping
    server.ts           composition root: picks the concrete implementations
    config.ts           env parsing
    domain/             our Product type and errors
    service/            CatalogService, EnquiryService (business logic)
    providers/          ProductProvider interface + FakeStoreProvider
    email/              EmailSender interface + SmtpEmailSender
  test/                 unit and API tests
android/
  app/src/main/         Compose UI, ViewModels, domain, Retrofit and Hilt
  app/src/test/         Unit, ViewModel and API contract tests
  gradle/libs.versions.toml  Central Android dependency versions
docker-compose.yml      backend + Mailpit
```

## Extending

- **New product source:** implement `ProductProvider`, then change one line in `server.ts`.
- **New email provider:** implement `EmailSender` (for example SES), then change one line in `server.ts`.
- **New look and feel:** change the Android theme files. Screens do not hard-code styling.
