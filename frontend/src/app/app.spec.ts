import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';
import { storeSession } from './core/test-data';

describe('App', () => {
  async function render(): Promise<HTMLElement> {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  beforeEach(() => sessionStorage.clear());

  it('shows only the page (the login) when nobody is logged in', async () => {
    const element = await render();

    expect(element.querySelector('.top-bar')).toBeNull();
    expect(element.querySelector('router-outlet')).toBeTruthy();
  });

  it('shows the SNM Bank header and the reviewer when logged in', async () => {
    storeSession();
    const element = await render();

    expect(element.querySelector('.logo')?.textContent).toContain('SNM BANK');
    expect(element.querySelector('.signed-in')?.textContent).toContain('Thandi Mokoena');
  });
});
