# product-catalogue
A product catalogue where customers browse, search and submit an enquiry. Built as a foundation to grow, not a throwaway prototype.

- **Client:** Android (Kotlin, Jetpack Compose). **Status: not implemented yet; `android/` is reserved for the client.**
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
- Mailpit inbox (where enquiry emails land): http://localhost:8025

No `.env` is needed for this route. To change settings, create `backend/.env` or pass environment variables to Docker Compose.

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

The Android application is not part of the current repository state. When it is added:

1. Start the backend as above.
2. Open `android/` in Android Studio and run it on an emulator.
3. Configure the emulator base URL as `http://10.0.2.2:3000` through build configuration, not directly in screens.

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
| `SMTP_HOST` / `SMTP_PORT` | Mail server | `localhost` / `1025` |
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
android/                Reserved for the Android/Jetpack Compose client
docker-compose.yml      backend + Mailpit
```

## Extending

- **New product source:** implement `ProductProvider`, then change one line in `server.ts`.
- **New email provider:** implement `EmailSender` (for example SES), then change one line in `server.ts`.
- **New look and feel:** change the Android theme files. Screens do not hard-code styling.
