// Settings for the online admin on GitHub Pages (built by .github/workflows/pages.yml).
// __API_URL__ is replaced during the build with the API_URL repository variable,
// the address of the online backend (docs/DEPLOY.md).
export const environment = {
  apiUrl: '__API_URL__',
  // The public form, opened in live mode so it saves to the same backend
  loanFormUrl: 'https://sibu912.github.io/SNM-Bank-loan-application/?api=__API_URL__',
  // GitHub Pages can't send every path to index.html, so the route goes after a #
  // (/admin/#/applications/SNM-2026-000001). Refreshing any page then still works.
  useHashRouting: true,
};
