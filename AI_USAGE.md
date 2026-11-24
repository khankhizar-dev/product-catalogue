## 1. Tools and models used

| Tool | Model | Used for |
|------|-------|----------|
| GitHub Copilot CLI / Copilot SDK in VS Code | Runtime-selected AI model | Repository analysis, implementation, debugging, tests, and documentation |

## 2. Agent instruction files

Committed to the repo:

- [`CLAUDE.md`](CLAUDE.md) at the repository root

Suggested contents, so the agent follows your boundaries:

- Architecture rules: routes → services → `ProductProvider`, never call Fake Store from the client.
- Android layering: data → domain → UI, with Hilt for DI.
- Conventions: TypeScript strict mode, Kotlin style, naming, error handling.
- Testing expectations: every service and ViewModel gets tests.
- Never commit secrets. Supply local configuration through environment variables.
- Keep changes small, and explain trade-offs before large refactors.

## 3. Plan / spec

The implementation planning is recorded in [`DECISIONS.md`](DECISIONS.md). The repository does not contain a separate plan file.

It covered:

- Stack choice and the reason for it
- Architecture and boundaries (provider and email interfaces)
- Acceptance criteria mapped to tasks
- Assumptions and cuts
- Test plan for the critical path

## 4. Key prompts and moments

### Moment 1: Establishing the architecture
- **Prompt (paraphrase):** Diagnose and fix the reported issue.
- **Outcome:** The build and tests revealed incorrect imports referencing `src/services`, while the actual directory is `src/service`. I corrected the imports consistently in app, composition root, and test helpers.
- **Why it mattered:** It exposed a repository layout mismatch and restored working TypeScript builds and test imports.

### Moment 2: Creating repository instructions
- **Prompt:** Create a `CLAUDE.md` file.
- **Outcome:** I added repository-specific guidance covering architecture boundaries, NodeNext imports, API behaviour, commands, and Android conventions.
- **Why it mattered:** The instruction file gives future AI coding sessions persistent context instead of relying on chat history.

### Moment 3: Completing the Android implementation and documentation
- **Prompt:** Fill all Markdown files properly and update them based on the Android code.
- **Outcome:** I removed status placeholders, documented the implemented Compose/Hilt/Retrofit app, and aligned run, test and architecture guidance with the source.
- **Why it mattered:** The submission documentation now reflects both implemented layers rather than describing Android as reserved or unimplemented.

### Moment 4: Dependency restoration
- **Prompt:** Test and build the backend.
- **Outcome:** Dependencies were restored with `npm install`; then Vitest, TypeScript type-checking, and the production build were run.
- **Why it mattered:** It verified the import fix and established that the backend is in a working state.

## 5. Where the AI got something wrong, and how I caught it

| What the AI did | How I caught it | What I did about it |
|-----------------|-----------------|---------------------|
| A few source and test imports referenced `./services/...` even though the files live in `./service/...`. | The backend build and test runner reported module-not-found errors. | I corrected all affected imports and reran tests, type-checking, and build. |
| Early documentation described a configured `config/featured.json` example that does not exist in the repository. | I compared `DECISIONS.md` with `config.ts`. | The implementation uses `FEATURED_PRODUCT_IDS` directly, so the documentation is being kept aligned with that env-based approach. |

## 6. How I reviewed AI output

- The implementation and documentation changes were reviewed for consistency with the existing source layout.
- Backend behavior was verified with the Vitest suite.
- Backend compilation was verified with both `npm run typecheck` and `npm run build`.
- Android source was reviewed for Gradle configuration, Compose navigation, Hilt composition, Retrofit contracts and ViewModel behavior.
- Android unit-test sources were reviewed. The UI was checked across multiple display sizes, font scales and light/dark modes; runtime build and test commands require a configured JDK and Android SDK.
- The PR diff should be reviewed before submission, especially generated build output and project configuration.
- Verified the backend with 20 passing Vitest tests, `npm run typecheck`, and `npm run build`.
- Reviewed the affected imports and package scripts rather than relying only on test success.
- Confirmed that Android uses the backend API only and does not call Fake Store directly.

## 7. Ownership statement

The submitter remains responsible for reviewing, understanding, and defending the resulting code and documentation. This record is intended to make the AI-assisted decisions, corrections, and verification steps auditable.

## 8. Time spent

| Phase | Time | Summary |
|-------|------|---------|
| Planning and architecture | 50 minutes | Clarified the requirements, planned the architecture, and chose to keep Android and backend in the same repository. |
| Implementation | 1 hour 15 minutes | Implemented both components and added test cases and coverage. |
| Testing | 40 minutes | Tested on the Android emulator. |
| **Total** | **2 hours 45 minutes** | |

See [`DECISIONS.md`](DECISIONS.md) section 1 for the repository structure decision and section 7 for the detailed time breakdown.
