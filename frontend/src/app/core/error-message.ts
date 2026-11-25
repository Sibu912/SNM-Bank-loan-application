import { HttpErrorResponse } from '@angular/common/http';
import { Problem } from './models';

/** Turns an HTTP error into a sentence to show on screen. */
export function errorMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return 'Could not reach the loan service. Is the backend running on port 8080?';
    }
    const problem = error.error as Problem | null;
    if (problem?.errors) {
      return Object.entries(problem.errors)
        .map(([field, message]) => `${field}: ${message}`)
        .join('; ');
    }
    if (problem?.detail) {
      return problem.detail;
    }
    return `The server returned an error (${error.status}).`;
  }
  return 'Something went wrong.';
}
