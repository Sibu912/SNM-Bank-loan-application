import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { LoanApplicationApi } from './loan-application-api';
import { ApplicationDetail, ApplicationSummary, HistoryEntry, PageResponse } from './models';
import { HISTORY, PAGE, detail } from './test-data';

const BASE = 'http://localhost:8080/api/applications';

describe('LoanApplicationApi', () => {
  let api: LoanApplicationApi;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    api = TestBed.inject(LoanApplicationApi);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify()); // fails if a test made a request it didn't expect

  it('lists applications with paging and a status filter', () => {
    let result: PageResponse<ApplicationSummary> | undefined;
    api.list('SUBMITTED', 2, 10).subscribe((page) => (result = page));

    const request = http.expectOne((r) => r.url === BASE);
    expect(request.request.method).toBe('GET');
    expect(request.request.params.get('status')).toBe('SUBMITTED');
    expect(request.request.params.get('page')).toBe('2');
    expect(request.request.params.get('size')).toBe('10');
    request.flush(PAGE);

    expect(result).toEqual(PAGE);
  });

  it('leaves out the status when listing everything', () => {
    api.list(null, 0, 10).subscribe();

    const request = http.expectOne((r) => r.url === BASE);
    expect(request.request.params.has('status')).toBe(false);
    request.flush(PAGE);
  });

  it('gets one application by reference number', () => {
    let result: ApplicationDetail | undefined;
    api.get('SNM-2026-000001').subscribe((application) => (result = application));

    http.expectOne(`${BASE}/SNM-2026-000001`).flush(detail());

    expect(result?.applicant.surname).toBe('Nkosi');
  });

  it('gets the history', () => {
    let result: HistoryEntry[] | undefined;
    api.history('SNM-2026-000001').subscribe((history) => (result = history));

    http.expectOne(`${BASE}/SNM-2026-000001/history`).flush(HISTORY);

    expect(result).toHaveLength(1);
  });

  it('sends a status change as PATCH', () => {
    api
      .changeStatus('SNM-2026-000001', {
        status: 'UNDER_REVIEW',
        note: 'Payslips received',
      })
      .subscribe();

    const request = http.expectOne(`${BASE}/SNM-2026-000001/status`);
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({
      status: 'UNDER_REVIEW',
      note: 'Payslips received',
    });
    request.flush(detail({ status: 'UNDER_REVIEW' }));
  });
});
