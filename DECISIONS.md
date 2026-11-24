# DECISIONS

## 1. Stack choice

| Layer | Choice | Why |
|-------|--------|-----|
| Client | Android, Kotlin + Jetpack Compose | Strongest area of expertise, so I can defend every line. Compose keeps UI declarative and easy to restyle when the creative work lands. |
| Backend | Node.js + TypeScript (Fastify) | Fast to stand up, typed, small footprint, easy to containerise for AWS (ECS/Lambda). |
| Repository | Android and backend in one repository | Keeps the client and API implementation, contracts, documentation, and local development setup together; neither component is maintained in a separate repository. |
| Infra | `docker compose` for local run, with Mailpit for email | Reviewer runs one command and needs no paid accounts. Docker images are AWS-compatible. |

## 2. Assumptions (where the acceptance criteria were unclear)

1. **"Top rated" selection.** The entrepreneur "chooses" 5 products, so featured products are a **backend-configured list of product IDs** (`config/featured.json` or env var), not derived from Fake Store ratings. Changing the list needs no client release.
2. **Category top rated.** Within a category, the top 3 products by Fake Store `rating.rate` (tiebreakers: `rating.count`, then id) are "top rated" and shown first, followed by the rest. The 3 is configurable (`TOP_RATED_PER_CATEGORY`). Category names match case-insensitively; an unknown category returns 404.
3. **Search.** Case-insensitive match on title, description and category, performed server-side. A product matches only if **every** word in the query appears (so "gold ring" narrows results rather than widening them). No fuzzy matching or ranking in the PoC. An empty or blank query returns 400 rather than the whole catalogue.
4. **Search scope.** Search runs across the whole catalogue, not within the current category.
5. **Enquiry.** One enquiry is about one product. Fields: name, email, phone (optional), message. Product ID and title are attached automatically.
6. **Sales inbox.** A single address from env config (`SALES_INBOX_EMAIL`). Locally, mail is captured by Mailpit.
7. **Thank-you message.** Shown only after the backend confirms the email was accepted by the provider. If sending fails the API returns 502 and the customer sees an error, so an enquiry is never silently lost behind a false thank-you. Trade-off: the customer must retry. A persisted outbox is the proper fix (see next steps).
8. **No authentication or user accounts.** Not in the criteria, and enquiries are anonymous.
9. **Emulator networking.** The Android emulator reaches the host backend at `10.0.2.2`. The base URL is a build config value.
10. **Pagination.** Not paginated. Fake Store returns about 20 products, so this is simplest. The API shape leaves room for adding `limit`/`cursor`.
11. **Currency and locale.** Prices are displayed as USD because the configured Fake Store source returns USD prices.

## 3. Requirements dropped or simplified

| Item | Decision | Why |
|------|----------|-----|
| Pagination / infinite scroll | Dropped | Catalogue is about 20 items. Cost outweighs benefit within the time-box. |
| Offline caching on Android | Dropped | The PoC reads live backend data; the repository interface already isolates data access for a later cache implementation. |
| Email delivery retries / queue | Simplified: synchronous send, clear error response | A queue is the right next step, but is overkill for a PoC. |
| Visual polish | Simplified: Material 3 light/dark palettes, typography and central spacing tokens | Styling is implemented and isolated for later visual refinement. |
| Accessibility and adaptive UI | Core support implemented | Icon actions and product images have accessibility descriptions, controls use labels, and Material 3 typography respects system font scaling. Screens were checked across multiple display sizes, font scales and both theme modes; a formal accessibility conformance audit remains future work. |
| CAPTCHA on enquiry | Dropped, rate limiting only | Avoids third-party accounts. |

## 4. How the design supports growth beyond the PoC

- **Product data source is swappable.** The backend depends on a `ProductProvider` interface. `FakeStoreProvider` is the only implementation today. Moving to a database or a real PIM means adding a class and changing one binding. Services and routes do not change.
- **Internal domain model.** The backend maps Fake Store's shape to its own `Product` type at the provider boundary, so the source's quirks never leak to clients.
- **Email provider is swappable.** An `EmailSender` interface with an SMTP implementation. SES, SendGrid or similar slot in the same way. Config via env.
- **Client talks only to our backend.** The Android app never calls Fake Store (constraint 2). The API contract is the only coupling.
- **Android layering.** Data (Retrofit, DTOs) → domain (models, repository interfaces) → UI (ViewModels, Compose). A central theme and design-token file means the new look and feel is a localised change.
- **Config over code.** Featured product IDs, sales inbox, provider settings and base URLs live in env/config.
- **Container-first.** The backend ships as a Docker image that runs unchanged on AWS.

## 5. Security basics

- Server-side validation of all enquiry input (schema validation, length limits).
- Subject lines have line breaks stripped to prevent mail header injection. The customer's address goes in `Reply-To`, not `From`.
- Rate limiting on the enquiry endpoint (5 per minute per client by default). Errors from the data source or email provider return generic 502s with details logged only.
- Output encoding in the email body. No raw HTML interpolation of user input.
- No secrets committed. Configuration is supplied through environment variables.
- Security headers via `@fastify/helmet`. CORS is not enabled because the client is native Android, so a future web client would need it added.

## 6. What I would do next with another day

1. Add pagination and sorting to the catalogue API.
2. Move email sending to a queue (e.g. SQS) with retries and a dead-letter path.
3. Persist enquiries in a database so none are lost if email fails.
4. Add contract tests between the API and the Android client (e.g. from an OpenAPI spec).
5. Add caching in front of the provider (the source is slow and rate limited).
6. Add CI (build, lint, test) and an AWS deployment definition (e.g. ECS Fargate with Terraform or CDK).
7. Improve search: ranking, typo tolerance, then a search engine if the catalogue grows.
8. Android: offline cache and a formal accessibility conformance audit.

## 7. Time spent

| Phase | Time | Work |
|-------|------|------|
| Planning and architecture | 50 minutes | Understood the requirements, defined the architecture, and decided Android and backend should live in the same repository rather than separate repositories. |
| Implementation | 1 hour 15 minutes | Implemented the backend and Android app, including test cases and coverage. |
| Testing | 40 minutes | Tested the app on the Android emulator. |
| **Total** | **2 hours 45 minutes** | |
