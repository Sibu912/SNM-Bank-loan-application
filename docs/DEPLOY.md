# Running SNM Bank online

This puts the whole project on the internet for free, with nothing that expires:

| Part | Where it runs | Address |
|------|---------------|---------|
| Loan form | GitHub Pages | `https://sibu912.github.io/SNM-Bank-loan-application/` |
| Admin (Angular) | GitHub Pages | `https://sibu912.github.io/SNM-Bank-loan-application/admin/` |
| Backend (Spring Boot) | Koyeb, free instance | `https://<your-app>.koyeb.app` |
| Database (PostgreSQL) | Neon, free plan | only the backend connects to it |

Free plans have limits. Koyeb's free instance stops after an hour with no visitors and takes about a minute to start again, and Neon's database sleeps after a few minutes. The form and the admin show a "waking up" message while that happens.

The public form at the address above stays a demo and doesn't save, so the Blue Prism bot never has to wait for a sleeping server. Add `?api=<backend address>` to the form's address to save applications. The admin's "Back to loan form" link and the README's live demo link already include it.

## 1. Create the database on Neon

1. Go to [neon.com](https://neon.com) and sign up with your GitHub account. No card is needed.
2. Create a project:
   - **Name:** `snm-bank`
   - **Postgres version:** 16 or newer
   - **Region:** AWS Europe (Frankfurt). Keep it close to the backend in step 2.
3. On the project dashboard, click **Connect**. Copy the connection string, which looks like this:

   ```
   postgresql://neondb_owner:npg_xxxxxxxx@ep-xxxx-xxxx.eu-central-1.aws.neon.tech/neondb?sslmode=require&channel_binding=require
   ```

   Keep it secret, because it contains the password. You don't need to create any tables: the backend does that the first time it starts (Flyway).

## 2. Run the backend on Koyeb

1. Go to [koyeb.com](https://www.koyeb.com) and sign up with your GitHub account.
2. Click **Create Web Service**, choose **GitHub**, and pick the `SNM-Bank-loan-application` repository and the `main` branch.
3. **Builder:** choose **Dockerfile**. The Dockerfile is in the root of the repo, so leave the location as it is.
4. **Instance:** choose **Free**. **Region:** Frankfurt.
5. **Environment variables.** Add these three. Use the **Secret** type for the first two.

   | Name | Value |
   |------|-------|
   | `DATABASE_URL` | the Neon connection string from step 1 |
   | `JWT_SECRET` | at least 32 random characters (see below) |
   | `CORS_ALLOWED_ORIGINS` | `https://sibu912.github.io` |

   `JWT_SECRET` signs the reviewer logins. To make one, run this in PowerShell and copy the result:

   ```powershell
   -join ((48..57 + 65..90 + 97..122) | Get-Random -Count 48 | ForEach-Object { [char]$_ })
   ```

6. **Ports:** set the exposed port to `8080`, protocol HTTP, path `/`.
7. **Health check:** if Koyeb lets you set a grace period, make it 120 seconds. Spring Boot takes about a minute to start on the small free CPU.
8. Name the service, for example `snm-bank-loans`, and click **Deploy**.

The first build takes a few minutes. When the service shows **Healthy**, copy its public address. It looks like `https://snm-bank-loans-<your-name>.koyeb.app`.

To check it, open `https://<your-app>.koyeb.app/ws/loans.wsdl` in a browser. You should see the SOAP contract (XML).

## 3. Connect GitHub Pages to the backend

1. In the GitHub repo, go to **Settings → Pages → Build and deployment → Source** and choose **GitHub Actions**. The **Pages** workflow (`.github/workflows/pages.yml`) now publishes the form and the admin together.
2. Go to **Settings → Secrets and variables → Actions → Variables** and click **New repository variable**:
   - **Name:** `API_URL`
   - **Value:** the Koyeb address from step 2, for example `https://snm-bank-loans-<your-name>.koyeb.app`, with no `/` at the end
3. Go to **Actions → Pages → Run workflow** to publish with the new address. Every push to `main` also publishes.

## 4. Try it

1. Open the live form: `https://sibu912.github.io/SNM-Bank-loan-application/?api=<your Koyeb address>`. The footer should say **Live // saving to …**. Submit an application and note the reference number.
2. Open `https://sibu912.github.io/SNM-Bank-loan-application/admin/` and log in as `thandi` / `Reviewer#2026`.
3. The new application should be in the list. Start the review and approve it.

To add the 10 demo applications online, run the seed script against the live backend:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\seed-demo-data.ps1 -ApiUrl https://<your Koyeb address>
```

## Updating

- **Backend:** Koyeb rebuilds and redeploys on every push to `main`.
- **Form and admin:** the Pages workflow republishes on every push to `main`.
- **Database:** Flyway applies new migrations (`backend/src/main/resources/db/migration`) when the backend starts.

## If something goes wrong

| What you see | Likely cause |
|--------------|--------------|
| Koyeb build fails | Open the build logs. The **Container** job in GitHub Actions builds the same Dockerfile, so a failure there shows up first. |
| Koyeb service keeps restarting | Check the logs for `password authentication failed` (wrong `DATABASE_URL`), or for out-of-memory errors. |
| Admin says it can't reach the loan service | `API_URL` isn't set or has a typo. Set it and run the Pages workflow again. |
| Browser console shows a CORS error | `CORS_ALLOWED_ORIGINS` on Koyeb must be exactly `https://sibu912.github.io`, with no path and no `/` at the end. |
| Logged out after every restart | `JWT_SECRET` isn't set on Koyeb. |
