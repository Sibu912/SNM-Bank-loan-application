# SNM Bank – Loan admin (Angular)

A sci-fi themed admin for reviewers: log in, list loan applications, open one, change its status and see its history.
It talks to the Spring Boot backend in `../backend` (see `../docs/API.md`).

Demo login: `thandi` / `Reviewer#2026` (created by the backend's Flyway script `V2__create_reviewer_table.sql`).

## Run it

Start the backend first (see `../docs/SETUP.md`), then:

```powershell
npm install
npm start          # same as: ng serve
```

Open http://localhost:4200 and log in. The backend address is in `src/environments/environment.ts`.

## Test and build

```powershell
npm test -- --watch=false   # 31 unit tests (Vitest)
npm run build               # production build in dist/
```

## Look and feel

The theme (dark navy, neon cyan / magenta / green, cut-corner panels, Orbitron and Syncopate fonts) lives in `src/styles.css` as CSS variables, so every page shares it. The fonts are installed from npm (`@fontsource/*`), so the app doesn't load anything from Google. Animations are switched off for people who choose "reduce motion" in their system settings.

## Structure

```text
src/app/
  core/
    models.ts                  TypeScript shapes of the API's JSON
    labels.ts                  Friendly text for the codes (VEHICLE -> Vehicle)
    loan-application-api.ts    HttpClient calls to /api/applications
    error-message.ts           Turns HTTP errors into a sentence for the screen
    auth.ts                    Login state: calls /api/auth/login, keeps the JWT in sessionStorage
    auth-interceptor.ts        Adds "Authorization: Bearer <token>"; logs out on a 401
    auth-guard.ts              Sends visitors who aren't logged in to /login
  login/                       /login              sci-fi login page with an animated SNM emblem
  applications/
    application-list/          /applications       table, status filter, paging
    application-detail/        /applications/:ref  details, status buttons, history
```
