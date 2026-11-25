import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { PAGE } from '../../core/test-data';
import { ApplicationList } from './application-list';

const BASE = 'http://localhost:8080/api/applications';

describe('ApplicationList', () => {
  let fixture: ComponentFixture<ApplicationList>;
  let http: HttpTestingController;
  let element: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApplicationList],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(ApplicationList);
    element = fixture.nativeElement;
    fixture.detectChanges(); // runs ngOnInit, which loads the first page
  });

  afterEach(() => http.verify());

  async function render(): Promise<void> {
    await fixture.whenStable();
    fixture.detectChanges();
  }

  it('shows the applications from the API', async () => {
    http.expectOne((r) => r.url === BASE && !r.params.has('status')).flush(PAGE);
    await render();

    const rows = element.querySelectorAll('tbody tr');
    expect(rows).toHaveLength(1);
    expect(rows[0].textContent).toContain('SNM-2026-000001');
    expect(rows[0].textContent).toContain('Mr Thabo Nkosi');
    expect(rows[0].textContent).toContain('R250 000 – R499 999');
    expect(rows[0].textContent).toContain('Submitted');
    expect(element.querySelector('.paging')?.textContent).toContain('Page 1 of 1');
  });

  it('reloads with the chosen status filter', async () => {
    http.expectOne((r) => r.url === BASE).flush(PAGE);
    await render();

    const select = element.querySelector<HTMLSelectElement>('#statusFilter')!;
    select.value = 'APPROVED';
    select.dispatchEvent(new Event('change'));

    const request = http.expectOne((r) => r.url === BASE && r.params.get('status') === 'APPROVED');
    expect(request.request.params.get('page')).toBe('0');
    request.flush({ ...PAGE, content: [], totalElements: 0, totalPages: 0 });
    await render();

    expect(element.textContent).toContain('No applications found.');
  });

  it('shows a message when the backend cannot be reached', async () => {
    http.expectOne((r) => r.url === BASE).error(new ProgressEvent('error'), { status: 0 });
    await render();

    expect(element.querySelector('.error')?.textContent).toContain(
      'Could not reach the loan service',
    );
  });
});
