// Settings for running the admin on your PC (npm start).
export const environment = {
  // Where the Spring Boot backend runs. Change this if you start it on another port.
  apiUrl: 'http://localhost:8080',
  // The loan form served by 2-run-snm-bank.bat; on your PC it saves to the backend above.
  loanFormUrl: 'http://127.0.0.1:5500/index.html',
  // Normal URLs like /applications/SNM-2026-000001
  useHashRouting: false,
};
