import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { errorMessage } from '../../core/error-message';
import { AMOUNT_LABELS, PURPOSE_LABELS, STATUS_LABELS } from '../../core/labels';
import { LoanApplicationApi } from '../../core/loan-application-api';
import {
  ALL_STATUSES,
  ApplicationStatus,
  ApplicationSummary,
  PageResponse,
} from '../../core/models';

/**
 * The list of applications, newest first, with a status filter and paging.
 */
@Component({
  selector: 'app-application-list',
  imports: [RouterLink, DatePipe],
  templateUrl: './application-list.html',
  styleUrl: './application-list.css',
})
export class ApplicationList implements OnInit {
  static readonly PAGE_SIZE = 10;

  private readonly api = inject(LoanApplicationApi);

  protected readonly statuses = ALL_STATUSES;
  protected readonly statusLabels = STATUS_LABELS;
  protected readonly purposeLabels = PURPOSE_LABELS;
  protected readonly amountLabels = AMOUNT_LABELS;

  protected readonly statusFilter = signal<ApplicationStatus | null>(null);
  protected readonly page = signal(0);
  protected readonly result = signal<PageResponse<ApplicationSummary> | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.load();
  }

  protected onFilterChange(value: string): void {
    this.statusFilter.set(value ? (value as ApplicationStatus) : null);
    this.page.set(0);
    this.load();
  }

  protected previousPage(): void {
    this.page.update((page) => page - 1);
    this.load();
  }

  protected nextPage(): void {
    this.page.update((page) => page + 1);
    this.load();
  }

  protected load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api.list(this.statusFilter(), this.page(), ApplicationList.PAGE_SIZE).subscribe({
      next: (result) => {
        this.result.set(result);
        this.loading.set(false);
      },
      error: (error) => {
        this.error.set(errorMessage(error));
        this.loading.set(false);
      },
    });
  }
}
