import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { loginResponse } from '../core/test-data';
import { Login } from './login';

const LOGIN_URL = 'http://localhost:8080/api/auth/login';

describe('Login', () => {
  let fixture: ComponentFixture<Login>;
  let http: HttpTestingController;
  let element: HTMLElement;
  let navigateByUrl: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    sessionStorage.clear();
    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    navigateByUrl = vi
      .spyOn(TestBed.inject(Router), 'navigateByUrl')
      .mockResolvedValue(true) as never;
    fixture = TestBed.createComponent(Login);
    element = fixture.nativeElement;
    await render();
  });

  afterEach(() => http.verify());

  async function render(): Promise<void> {
    await fixture.whenStable();
    fixture.detectChanges();
  }

  function type(id: string, value: string): void {
    const input = element.querySelector<HTMLInputElement>(`#${id}`)!;
    input.value = value;
    input.dispatchEvent(new Event('input'));
  }

  function submit(): void {
    element.querySelector('form')!.dispatchEvent(new Event('submit'));
  }

  it('logs in and opens the applications list', async () => {
    type('username', 'thandi');
    type('password', 'Reviewer#2026');
    submit();

    const request = http.expectOne(LOGIN_URL);
    expect(request.request.body).toEqual({ username: 'thandi', password: 'Reviewer#2026' });
    request.flush(loginResponse());
    await render();

    expect(navigateByUrl).toHaveBeenCalledWith('/applications');
  });

  it('goes back to the page the reviewer asked for', async () => {
    fixture.componentRef.setInput('returnUrl', '/applications/SNM-2026-000001');
    type('username', 'thandi');
    type('password', 'Reviewer#2026');
    submit();

    http.expectOne(LOGIN_URL).flush(loginResponse());
    await render();

    expect(navigateByUrl).toHaveBeenCalledWith('/applications/SNM-2026-000001');
  });

  it('never redirects to another website', async () => {
    fixture.componentRef.setInput('returnUrl', '//evil.example');
    type('username', 'thandi');
    type('password', 'Reviewer#2026');
    submit();

    http.expectOne(LOGIN_URL).flush(loginResponse());
    await render();

    expect(navigateByUrl).toHaveBeenCalledWith('/applications');
  });

  it('shows ACCESS DENIED for a wrong password', async () => {
    type('username', 'thandi');
    type('password', 'wrong');
    await render();
    submit();

    http
      .expectOne(LOGIN_URL)
      .flush({ title: 'Login failed', status: 401 }, { status: 401, statusText: 'Unauthorized' });
    await render();

    expect(element.querySelector('.login-error')?.textContent).toContain('ACCESS DENIED');
    expect(element.querySelector<HTMLInputElement>('#password')!.value).toBe('');
    expect(navigateByUrl).not.toHaveBeenCalled();
  });

  it('asks for both fields before calling the backend', async () => {
    submit();
    await render();

    expect(element.querySelector('.login-error')?.textContent).toContain(
      'ENTER YOUR USERNAME AND PASSWORD',
    );
  });

  it('can show and hide the password', async () => {
    const password = () => element.querySelector<HTMLInputElement>('#password')!;
    expect(password().type).toBe('password');

    element.querySelector<HTMLButtonElement>('.toggle-password')!.click();
    await render();

    expect(password().type).toBe('text');
  });
});
