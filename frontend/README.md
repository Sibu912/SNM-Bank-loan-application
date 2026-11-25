# SNM Bank – Loan admin (Angular)

A small screen for reviewers: list loan applications, open one, change its status and see its history.
It talks to the Spring Boot backend in `../backend` (see `../docs/API.md`).

## Run it

Start the backend first (see `../docs/SETUP.md`), then:

```powershell
npm install
npm start          # same as: ng serve
```

Open http://localhost:4200. The backend address is in `src/environments/environment.ts`.

## Test and build

```powershell
npm test -- --watch=false   # unit tests (Vitest)
npm run build               # production build in dist/
```

## Structure

```text
src/app/
  core/
    models.ts                  TypeScript shapes of the API's JSON
    labels.ts                  Friendly text for the codes (VEHICLE -> Vehicle)
    loan-application-api.ts    HttpClient calls to /api/applications
    error-message.ts           Turns HTTP errors into a sentence for the screen
  applications/
    application-list/          /applications       table, status filter, paging
    application-detail/        /applications/:ref  details, status buttons, history
```
