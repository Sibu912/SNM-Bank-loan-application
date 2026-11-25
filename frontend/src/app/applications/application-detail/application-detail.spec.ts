import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { ApplicationDetail as Detail } from '../../core/models';
import { HISTORY, detail } from '../../core/test-data';
import { ApplicationDetail } from './application-detail';

const BASE = 'http://localhost:8080/api/applications/SNM-2026-000001';

describe('ApplicationDetail', () => {
  let fixture: ComponentFixture<ApplicationDetail>;
  let http: HttpTestingController;
  let element: HTMLElement;

  async function open(application: Detail): Promise<void> {
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

  function typeName(name: string): void {
    const input = element.querySelector<HTMLInputElement>('#changedBy')!;
    input.value = name;
    input.dispatchEvent(new Event('input'));
  }

  afterEach(() => http.verify());

  it('shows the application and its history', async () => {
    await open(detail());

    expect(element.querySelector('h2')?.textContent).toContain('SNM-2026-000001');
    expect(element.textContent).toContain('Thabo');
    expect(element.textContent).toContain('R 10,996.30 per month');
    expect(element.querySelector('.history')?.textContent).toContain('Submitted');
  });

  it('only offers the next allowed status, after a name is entered', async () => {
    await open(detail({ status: 'SUBMITTED' }));

    expect(buttons().map((b) => b.textContent?.trim())).toEqual(['Start review']);
    expect(buttons()[0].disabled).toBe(true);

    typeName('reviewer.thandi');
    await render();

    expect(buttons()[0].disabled).toBe(false);
  });

  it('changes the status and reloads the history', async () => {
    await open(detail({ status: 'SUBMITTED' }));
    typeName('reviewer.thandi');
    await render();

    buttons()[0].click();

    const request = http.expectOne(`${BASE}/status`);
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({
      status: 'UNDER_REVIEW',
      note: null,
      changedBy: 'reviewer.thandi',
    });
    request.flush(detail({ status: 'UNDER_REVIEW' }));
    http.expectOne(`${BASE}/history`).flush(HISTORY);
    await render();

    expect(buttons().map((b) => b.textContent?.trim())).toEqual(['Approve', 'Decline']);
    expect(element.querySelector('.success')?.textContent).toContain(
      'Status changed to Under review',
    );
  });

  it('shows the error from the API when a change is refused', async () => {
    await open(detail({ status: 'UNDER_REVIEW' }));
    typeName('reviewer.thandi');
    await render();

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
    typeName('reviewer.thandi');
    await render();

    const [approve, decline] = buttons();
    expect(approve.disabled).toBe(true);
    expect(decline.disabled).toBe(false);
    expect(element.querySelector('.hint')?.textContent).toContain('can only be declined');
  });

  it('shows no buttons for a final status', async () => {
    await open(detail({ status: 'APPROVED' }));

    expect(buttons()).toHaveLength(0);
    expect(element.textContent).toContain('No more changes are possible');
  });
});
