import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { ApplicationDetail as Detail } from '../../core/models';
import { HISTORY, detail, storeSession } from '../../core/test-data';
import { ApplicationDetail } from './application-detail';

const BASE = 'http://localhost:8080/api/applications/SNM-2026-000001';

describe('ApplicationDetail', () => {
  let fixture: ComponentFixture<ApplicationDetail>;
  let http: HttpTestingController;
  let element: HTMLElement;

  async function open(application: Detail): Promise<void> {
    sessionStorage.clear();
    storeSession(); // logged in as Thandi Mokoena
    await TestBed.configureTestingModule({
      imports: [ApplicationDetail],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(ApplicationDetail);
    element = fixture.nativeElement;
    fixture.componentRef.setInput('ref', 'SNM-2026-000001');
    fixture.detectChanges();

    http.expectOne(BASE).flush(application);
    http.expectOne(`${BASE}/history`).flush(HISTORY);
    await render();
  }

  async function render(): Promise<void> {
    await fixture.whenStable();
    fixture.detectChanges();
  }

  function buttons(): HTMLButtonElement[] {
    return Array.from(element.querySelectorAll<HTMLButtonElement>('.actions button'));
  }

  afterEach(() => http.verify());

  it('shows the application and its history', async () => {
    await open(detail());

    expect(element.querySelector('h2')?.textContent).toContain('SNM-2026-000001');
    expect(element.textContent).toContain('Thabo');
    expect(element.querySelector('.repayment')?.textContent).toContain('R 10,996.30');
    expect(element.querySelector('.timeline')?.textContent).toContain('Submitted');
  });

  it('only offers the next allowed status and shows who is signed in', async () => {
    await open(detail({ status: 'SUBMITTED' }));

    expect(buttons().map((b) => b.textContent?.trim())).toEqual(['Start review', 'Decline']);
    expect(buttons()[0].disabled).toBe(false);
    expect(element.querySelector('.signed-in')?.textContent).toContain('Thandi Mokoena');
  });

  it('changes the status and reloads the history', async () => {
    await open(detail({ status: 'SUBMITTED' }));
    buttons()[0].click();

    const request = http.expectOne(`${BASE}/status`);
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({
      status: 'UNDER_REVIEW',
      note: null,
    });
    request.flush(detail({ status: 'UNDER_REVIEW' }));
    http.expectOne(`${BASE}/history`).flush(HISTORY);
    await render();

    expect(buttons().map((b) => b.textContent?.trim())).toEqual([
      'Approve',
      'Decline',
      'Send back',
    ]);
    expect(element.querySelector('.success')?.textContent).toContain(
      'STATUS CHANGED TO UNDER REVIEW',
    );
  });

  it('shows the error from the API when a change is refused', async () => {
    await open(detail({ status: 'UNDER_REVIEW' }));
    buttons()[0].click(); // Approve
    http.expectOne(`${BASE}/status`).flush(
      {
        title: 'Invalid status change',
        status: 409,
        detail: 'Cannot change status from SUBMITTED to APPROVED',
      },
      { status: 409, statusText: 'Conflict' },
    );
    await render();

    expect(element.querySelector('.error')?.textContent).toContain(
      'Cannot change status from SUBMITTED to APPROVED',
    );
  });

  it('does not allow approving an application that failed the affordability check', async () => {
    await open(detail({ status: 'UNDER_REVIEW', affordabilityResult: 'FAIL' }));
    const [approve, decline, sendBack] = buttons();
    expect(approve.disabled).toBe(true);
    expect(decline.disabled).toBe(false);
    expect(sendBack.disabled).toBe(false);
    expect(element.querySelector('.hint')?.textContent).toContain("can't be approved");
  });

  it('lets a reviewer reopen a decided application', async () => {
    await open(detail({ status: 'APPROVED' }));
    expect(buttons().map((b) => b.textContent?.trim())).toEqual(['Reopen']);

    buttons()[0].click();
    const request = http.expectOne(`${BASE}/status`);
    expect(request.request.body).toEqual({ status: 'UNDER_REVIEW', note: null });
    request.flush(detail({ status: 'UNDER_REVIEW' }));
    http.expectOne(`${BASE}/history`).flush(HISTORY);
    await render();

    expect(element.querySelector('.success')?.textContent).toContain(
      'STATUS CHANGED TO UNDER REVIEW',
    );
  });
});
