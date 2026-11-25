import { DatePipe, DecimalPipe, LowerCasePipe } from '@angular/common';
import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Auth } from '../../core/auth';
import { errorMessage } from '../../core/error-message';
import {
  AMOUNT_LABELS,
  GENDER_LABELS,
  INCOME_LABELS,
  PURPOSE_LABELS,
  STATUS_ACTIONS,
  STATUS_LABELS,
  TITLE_LABELS,
} from '../../core/labels';
import { LoanApplicationApi } from '../../core/loan-application-api';
import {
  ApplicationDetail as Detail,
  ApplicationStatus,
  HistoryEntry,
  NEXT_STATUSES,
} from '../../core/models';

/**
 * One application: its details, its history, and buttons to move it to the next status.
 * The backend records the logged-in reviewer as the person who made each change.
 */
@Component({
  selector: 'app-application-detail',
  imports: [RouterLink, DatePipe, DecimalPipe, LowerCasePipe],
  templateUrl: './application-detail.html',
  styleUrl: './application-detail.css',
})
export class ApplicationDetail implements OnInit {
  private readonly api = inject(LoanApplicationApi);
  protected readonly auth = inject(Auth);

  /** Filled from the URL /applications/:ref (withComponentInputBinding in app.config.ts). */
  readonly ref = input.required<string>();

  protected readonly statusLabels = STATUS_LABELS;
  protected readonly statusActions = STATUS_ACTIONS;
  protected readonly purposeLabels = PURPOSE_LABELS;
  protected readonly amountLabels = AMOUNT_LABELS;
  protected readonly incomeLabels = INCOME_LABELS;
  protected readonly titleLabels = TITLE_LABELS;
  protected readonly genderLabels = GENDER_LABELS;

  protected readonly application = signal<Detail | null>(null);
  protected readonly history = signal<HistoryEntry[]>([]);
  protected readonly note = signal('');
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly message = signal<string | null>(null);

  /** Only the moves the backend allows from the current status. */
  protected readonly nextStatuses = computed<ApplicationStatus[]>(() => {
    const application = this.application();
    return application ? NEXT_STATUSES[application.status] : [];
  });

  ngOnInit(): void {
    this.load();
  }

  /** An application that failed the affordability check can't be approved. */
  protected isBlocked(status: ApplicationStatus): boolean {
    return status === 'APPROVED' && this.application()?.affordabilityResult === 'FAIL';
  }

  protected changeStatus(status: ApplicationStatus): void {
    this.saving.set(true);
    this.error.set(null);
    this.message.set(null);

    this.api.changeStatus(this.ref(), { status, note: this.note().trim() || null }).subscribe({
      next: (application) => {
        this.application.set(application);
        this.note.set('');
        this.saving.set(false);
        this.message.set(`STATUS CHANGED TO ${STATUS_LABELS[application.status].toUpperCase()}`);
        this.loadHistory();
      },
      error: (error) => {
        // For example a 409 "Cannot change status from SUBMITTED to APPROVED"
        this.error.set(errorMessage(error));
        this.saving.set(false);
      },
    });
  }

  private load(): void {
    this.api.get(this.ref()).subscribe({
      next: (application) => this.application.set(application),
      error: (error) => this.error.set(errorMessage(error)),
    });
    this.loadHistory();
  }

  private loadHistory(): void {
    this.api.history(this.ref()).subscribe({
      next: (history) => this.history.set(history),
      error: () => this.history.set([]),
    });
  }
}
