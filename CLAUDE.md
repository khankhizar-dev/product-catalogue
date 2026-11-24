# CLAUDE.md

## Project overview

Product catalogue with an Android client in `android/` and a Node.js + TypeScript backend in `backend/`.

- The Android app must use the backend API; it must not call Fake Store directly.
- The backend uses Fastify, Zod, Nodemailer, and Vitest.
- Fake Store is the product data provider.
- Mailpit is used for local SMTP capture.
- Docker Compose starts the backend and Mailpit.

For architectural intent and trade-offs, read `DECISIONS.md`.

## Repository layout

```text
backend/src/
  app.ts              Fastify routes and error handling
  server.ts           Composition root
  config.ts           Environment parsing
  domain/             Internal domain types and errors
  service/            CatalogService and EnquiryService
  providers/          ProductProvider and FakeStoreProvider
  email/              EmailSender and SMTP implementation
backend/test/         Unit and API tests
android/
  app/src/main/       Compose UI, Hilt DI, Retrofit API, domain models and repository
  app/src/test/       Android unit and repository contract tests
docker-compose.yml    Local backend and Mailpit services
```

## Backend conventions

- Keep NodeNext/ESM import paths and include the `.js` extension, even for TypeScript source imports.
- Treat `backend/src/service/` as the service directory; do not introduce or reference a `backend/src/services/` directory.
- Keep provider and email implementations behind `ProductProvider` and `EmailSender`.
- Put concrete implementation selection in `server.ts`, not in routes or services.
- Keep source data shapes at the provider boundary and expose the internal `Product` model.
- Use Zod for request validation and preserve existing API error codes.
- For all file changes, maintain strict TypeScript compatibility and `noUncheckedIndexedAccess`.
- Do not add secrets or real credentials to the repository.

## API behaviour

- Featured product IDs are backend configuration, not selected by rating.
- Category top-rated products are ranked by rating, then rating count, then product ID.
- Search is case-insensitive and requires every query term to match title, description, or category.
- Blank search queries return 400.
- Enquiries require one product and valid name, email, phone (optional), and message fields.
- Email is sent synchronously. Delivery failures return a generic 502 response while details are logged server-side.
- Strip line breaks from email subjects and HTML-escape enquiry data.

## Commands

Run backend commands from `backend/`:

```bash
npm install
npm test
npm run typecheck
npm run build
npm run dev
```

Run the full local stack from the repository root:

```bash
docker compose up --build
```

Local endpoints:

- API: `http://localhost:3000`
- Mailpit: `http://localhost:8025`

## Validation requirements

After backend changes, run:

```bash
cd backend
npm test
npm run typecheck
npm run build
```

Prefer focused tests for the affected behaviour, but do not leave existing tests failing.

## Android conventions

- Use Kotlin and Jetpack Compose.
- Keep networking in the Retrofit/data layer.
- Keep domain models and repository interfaces separate from Compose UI.
- Do not hard-code the emulator base URL in screens; use build configuration.
- Use Hilt for application, network and repository composition.
- Keep catalogue operations behind `CatalogRepository`.
- Preserve client-side validation as feedback only; the backend remains authoritative.
- Keep light and dark palettes aligned with system theme selection.
- Use Material 3 typography so system font scaling is respected; avoid fixed text sizing.
- Provide accessibility descriptions for non-text controls and meaningful images, and labels for form fields.
- Check UI changes in both theme modes, across display sizes and with increased system font scaling.

## Android commands

Run Android commands from `android/`:

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

The emulator default is `http://10.0.2.2:3000/`. Override it with `-Pcatalogue.baseUrl=...` or `catalogue.baseUrl=...` in `gradle.properties`. For a physical device, add the LAN host to `app/src/main/res/xml/network_security_config.xml`.
