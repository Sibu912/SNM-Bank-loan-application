import { HttpClient, HttpParams } from '@angular/common/http';
import { Service, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  ApplicationDetail,
  ApplicationStatus,
  ApplicationSummary,
  HistoryEntry,
  PageResponse,
  StatusChangeRequest,
} from './models';

/**
 * Calls the backend's REST API (docs/API.md).
 * @Service() is Angular's newer way to write @Injectable({ providedIn: 'root' }):
 * one shared instance that components can inject.
 */
@Service()
export class LoanApplicationApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/applications`;

  list(
    status: ApplicationStatus | null,
    page: number,
    size: number,
  ): Observable<PageResponse<ApplicationSummary>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<PageResponse<ApplicationSummary>>(this.baseUrl, { params });
  }

  get(referenceNumber: string): Observable<ApplicationDetail> {
    return this.http.get<ApplicationDetail>(
      `${this.baseUrl}/${encodeURIComponent(referenceNumber)}`,
    );
  }

  history(referenceNumber: string): Observable<HistoryEntry[]> {
    return this.http.get<HistoryEntry[]>(
      `${this.baseUrl}/${encodeURIComponent(referenceNumber)}/history`,
    );
  }

  changeStatus(
    referenceNumber: string,
    request: StatusChangeRequest,
  ): Observable<ApplicationDetail> {
    return this.http.patch<ApplicationDetail>(
      `${this.baseUrl}/${encodeURIComponent(referenceNumber)}/status`,
      request,
    );
  }
}
