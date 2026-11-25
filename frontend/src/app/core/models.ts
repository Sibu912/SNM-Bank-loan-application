// TypeScript shapes of the JSON the backend returns (see docs/API.md).

export type ApplicationStatus = 'SUBMITTED' | 'UNDER_REVIEW' | 'APPROVED' | 'DECLINED';
export type AffordabilityResult = 'PASS' | 'FAIL';
export type Title = 'MR' | 'MRS' | 'MISS' | 'MS';
export type Gender = 'MALE' | 'FEMALE';
export type LoanPurpose = 'VEHICLE' | 'PROPERTY' | 'INVESTMENT';
export type LoanAmountBand =
  'LESS_THAN_250K' | 'FROM_250K_TO_499K' | 'FROM_500K_TO_999K' | 'ONE_MILLION_PLUS';
export type IncomeBand =
  'LESS_THAN_50K' | 'FROM_50K_TO_99K' | 'FROM_100K_TO_149K' | 'FROM_150K_PLUS';
export type Channel = 'WEB' | 'SOAP';

export interface ApplicationSummary {
  referenceNumber: string;
  applicantName: string;
  loanPurpose: LoanPurpose;
  loanAmountBand: LoanAmountBand;
  status: ApplicationStatus;
  affordabilityResult: AffordabilityResult;
  createdAt: string;
}

export interface Address {
  street: string;
  suburb: string;
  province: string;
  postalCode: string;
}

export interface Applicant {
  title: Title;
  givenName: string;
  surname: string;
  gender: Gender;
  address: Address;
}

export interface ApplicationDetail {
  referenceNumber: string;
  applicant: Applicant;
  loanPurpose: LoanPurpose;
  loanAmountBand: LoanAmountBand;
  termYears: number;
  incomeBand: IncomeBand;
  estimatedMonthlyRepayment: number;
  affordabilityResult: AffordabilityResult;
  status: ApplicationStatus;
  channel: Channel;
  createdAt: string;
  updatedAt: string;
}

export interface HistoryEntry {
  fromStatus: ApplicationStatus | null;
  toStatus: ApplicationStatus;
  note: string | null;
  changedBy: string;
  changedAt: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

/** The reviewer is taken from the login token, so it isn't part of the request. */
export interface StatusChangeRequest {
  status: ApplicationStatus;
  note: string | null;
}

/** What POST /api/auth/login returns. */
export interface LoginResponse {
  token: string;
  username: string;
  displayName: string;
  expiresAt: string;
}

/** The error body the backend sends (Spring's ProblemDetail). */
export interface Problem {
  title?: string;
  status?: number;
  detail?: string;
  errors?: Record<string, string>;
}

export const ALL_STATUSES: ApplicationStatus[] = [
  'SUBMITTED',
  'UNDER_REVIEW',
  'APPROVED',
  'DECLINED',
];

/** Same rules as ApplicationStatus.canMoveTo in the backend (docs/DESIGN.md 6.3). */
export const NEXT_STATUSES: Record<ApplicationStatus, ApplicationStatus[]> = {
  SUBMITTED: ['UNDER_REVIEW'],
  UNDER_REVIEW: ['APPROVED', 'DECLINED'],
  APPROVED: [],
  DECLINED: [],
};
