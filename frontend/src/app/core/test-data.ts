// Sample API responses for the unit tests.
import {
  ApplicationDetail,
  ApplicationSummary,
  HistoryEntry,
  LoginResponse,
  PageResponse,
} from './models';

export const SUMMARY: ApplicationSummary = {
  referenceNumber: 'SNM-2026-000001',
  applicantName: 'Mr Thabo Nkosi',
  loanPurpose: 'VEHICLE',
  loanAmountBand: 'FROM_250K_TO_499K',
  status: 'SUBMITTED',
  affordabilityResult: 'PASS',
  createdAt: '2026-11-25T18:30:00',
};

export const PAGE: PageResponse<ApplicationSummary> = {
  content: [SUMMARY],
  page: 0,
  size: 10,
  totalElements: 1,
  totalPages: 1,
};

export function detail(overrides: Partial<ApplicationDetail> = {}): ApplicationDetail {
  return {
    referenceNumber: 'SNM-2026-000001',
    applicant: {
      title: 'MR',
      givenName: 'Thabo',
      surname: 'Nkosi',
      gender: 'MALE',
      address: {
        street: '12 Jan Smuts Avenue',
        suburb: 'Braamfontein',
        province: 'Gauteng',
        postalCode: '2001',
      },
    },
    loanPurpose: 'VEHICLE',
    loanAmountBand: 'FROM_250K_TO_499K',
    termYears: 5,
    incomeBand: 'FROM_50K_TO_99K',
    estimatedMonthlyRepayment: 10996.3,
    affordabilityResult: 'PASS',
    status: 'SUBMITTED',
    channel: 'WEB',
    createdAt: '2026-11-25T18:30:00',
    updatedAt: '2026-11-25T18:30:00',
    ...overrides,
  };
}

export const HISTORY: HistoryEntry[] = [
  {
    fromStatus: null,
    toStatus: 'SUBMITTED',
    note: null,
    changedBy: 'system',
    changedAt: '2026-11-25T18:30:00',
  },
];

export function loginResponse(overrides: Partial<LoginResponse> = {}): LoginResponse {
  return {
    token: 'test-token',
    username: 'thandi',
    displayName: 'Thandi Mokoena',
    expiresAt: new Date(Date.now() + 60 * 60 * 1000).toISOString(), // in one hour
    ...overrides,
  };
}

/** Pretend a reviewer logged in earlier (Auth reads this when it is created). */
export function storeSession(session: LoginResponse = loginResponse()): void {
  sessionStorage.setItem('snm_admin_session', JSON.stringify(session));
}
