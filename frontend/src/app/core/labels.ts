// Friendly text for the codes the API uses, matching the labels on the loan form.
import {
  ApplicationStatus,
  Gender,
  IncomeBand,
  LoanAmountBand,
  LoanPurpose,
  Title,
} from './models';

export const TITLE_LABELS: Record<Title, string> = {
  MR: 'Mr',
  MRS: 'Mrs',
  MISS: 'Miss',
  MS: 'Ms',
};

export const GENDER_LABELS: Record<Gender, string> = {
  MALE: 'Male',
  FEMALE: 'Female',
};

export const STATUS_LABELS: Record<ApplicationStatus, string> = {
  SUBMITTED: 'Submitted',
  UNDER_REVIEW: 'Under review',
  APPROVED: 'Approved',
  DECLINED: 'Declined',
};

/** Text for the button that moves an application from one status to the next. */
export function statusAction(from: ApplicationStatus, to: ApplicationStatus): string {
  switch (to) {
    case 'UNDER_REVIEW':
      return from === 'SUBMITTED' ? 'Start review' : 'Reopen';
    case 'APPROVED':
      return 'Approve';
    case 'DECLINED':
      return 'Decline';
    case 'SUBMITTED':
      return 'Send back';
  }
}

export const PURPOSE_LABELS: Record<LoanPurpose, string> = {
  VEHICLE: 'Vehicle',
  PROPERTY: 'Property',
  INVESTMENT: 'Investment',
};

export const AMOUNT_LABELS: Record<LoanAmountBand, string> = {
  LESS_THAN_250K: 'Less than R250 000',
  FROM_250K_TO_499K: 'R250 000 – R499 999',
  FROM_500K_TO_999K: 'R500 000 – R999 999',
  ONE_MILLION_PLUS: 'R1 million and more',
};

export const INCOME_LABELS: Record<IncomeBand, string> = {
  LESS_THAN_50K: 'Less than R50 000',
  FROM_50K_TO_99K: 'R50 000 – R99 999',
  FROM_100K_TO_149K: 'R100 000 – R149 999',
  FROM_150K_PLUS: 'R150 000 or more',
};
