# Setup – running the project locally (Windows)

The commands below are for **PowerShell**.

---

## 1. Prerequisites

| Tool | Version | Check with |
|------|---------|------------|
| Java JDK (Eclipse Temurin) | 21 | `java -version` |
| Git | any recent | `git --version` |
| PostgreSQL | 16 | `psql --version` |
| Node.js (for Angular) | 22 LTS or newer | `node -v` |
| Angular CLI (optional) | 22 | `ng version`. `npm start` works without a global install. |
| SoapUI (open source) | any | for testing SOAP |
| Postman or `curl.exe` | any | for testing REST |

You don't need to install Maven: the project includes the **Maven wrapper** (`mvnw.cmd`), which downloads the right version the first time it runs.

## 2. Get the code

```powershell
git clone https://github.com/Sibu912/SNM-Bank-loan-application.git
cd SNM-Bank-loan-application
```

## 3. Create the database (one time)

Open **SQL Shell (psql)** or pgAdmin's query tool as the `postgres` user and run:

```sql
CREATE USER snm_app WITH PASSWORD 'choose-a-password';
CREATE DATABASE snm_loans OWNER snm_app;       -- used by the app
CREATE DATABASE snm_loans_test OWNER snm_app;  -- used by the automated tests
```

You don't need to create any tables. **Flyway** creates them when the backend (or a test) starts.

## 4. Run the backend

```powershell
cd backend
$env:DB_PASSWORD = "choose-a-password"   # only for this PowerShell window
.\mvnw.cmd spring-boot:run
```

The backend reads the password from the `DB_PASSWORD` environment variable, so it's **never saved in Git**. The settings in `backend/src/main/resources/application.yml` look like this:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/snm_loans
    username: snm_app
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false

snm:
  loan:
    annual-interest-rate: 0.115
    max-repayment-to-income: 0.30
  cors:
    allowed-origins: http://localhost:4200,http://localhost:5500,http://127.0.0.1:5500
```

It's running when you see `Started LoanServiceApplication` in the console.

## 5. Try the REST API

> In PowerShell, plain `curl` is a shortcut for `Invoke-WebRequest`. Type **`curl.exe`**, or use Postman.

```powershell
$body = @{
  title = "MR"; givenName = "Thabo"; surname = "Nkosi"; gender = "MALE"
  address = @{ street = "12 Jan Smuts Avenue"; suburb = "Braamfontein"; province = "Gauteng"; postalCode = "2001" }
  loanPurpose = "VEHICLE"; loanAmountBand = "FROM_250K_TO_499K"; termYears = 5; incomeBand = "FROM_50K_TO_99K"
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/applications -ContentType "application/json" -Body $body
Invoke-RestMethod http://localhost:8080/api/applications
```

## 6. Try the SOAP endpoint

1. Open SoapUI → **File → New SOAP Project**
2. Initial WSDL: `http://localhost:8080/ws/loans.wsdl`
3. Open the `SubmitLoanApplication` request, fill it in with the example from [API.md](API.md#example-submit) and click ▶

## 7. Run the tests

PostgreSQL must be running, and `DB_PASSWORD` must be set in the same window (step 4).

```powershell
cd backend
$env:DB_PASSWORD = "choose-a-password"
.\mvnw.cmd verify

cd ..\frontend
npm install
npm test -- --watch=false         # 15 tests
```

Backend tests use the **`snm_loans_test`** database (`src/test/resources/application-test.yml`), never your real `snm_loans` data. Tests that save data empty the loan tables first (`clean-loan-tables.sql`). GitHub Actions starts its own PostgreSQL 16 and runs the same commands on every push (`.github/workflows/ci.yml`).

## 8. Use the loan form with the backend

1. In VS Code, install the **Live Server** extension
2. Right-click `index.html` → **Open with Live Server** (it opens on port 5500)
3. Add the API address to the URL:
   `http://127.0.0.1:5500/index.html?api=http://localhost:8080`
4. Fill in the form. The thank-you page shows your reference number.

Without `?api=...` the form works exactly as before, so GitHub Pages and the Blue Prism bot aren't affected.

## 9. Run the Angular admin

Keep the backend running (step 4), then in a second PowerShell window:

```powershell
cd frontend
npm install
npm start
```

Open `http://localhost:4200`. You'll see the list of applications. Click a reference number to open it, type your name and use the buttons to move it through the review.

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `password authentication failed for user "snm_app"` | `DB_PASSWORD` isn't set in **this** PowerShell window, or it doesn't match the password from step 3 |
| Tests fail with `database "snm_loans_test" does not exist` | Create it (step 3) |
| `Connection refused` on port 5432 | PostgreSQL isn't running. Start it in **Services** (`postgresql-x64-16`). |
| `Schema-validation: missing table` | Flyway didn't run. Check that the migration is in `src/main/resources/db/migration` and its name starts with `V1__` (two underscores). |
| `Validate failed: Migration checksum mismatch` | A migration that already ran was edited. Undo the edit and put the change in a new `V2__` file. |
| Browser shows a **CORS** error | Add the page's origin, exactly as shown in the error, to `snm.cors.allowed-origins` |
| `Port 8080 was already in use` | Stop the other app, or add `server.port: 8081` to `application.yml` (then also change `frontend/src/environments/environment.ts`) |
| Angular page says "Could not reach the loan service" | The backend isn't running, or it's on a different port |
| `npm install` is very slow | OneDrive is syncing `node_modules`. Pause OneDrive syncing while it installs. |
