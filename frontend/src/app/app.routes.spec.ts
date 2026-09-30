import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { Title } from '@angular/platform-browser';
import { routes } from './app.routes';

describe('application routes', () => {
  it('renders the OpsPilot home at the root route', async () => {
    TestBed.configureTestingModule({ providers: [provideRouter(routes)] });

    const harness = await RouterTestingHarness.create('/');

    expect(harness.routeNativeElement?.textContent).toContain('explainable priorities');
    expect(TestBed.inject(Title).getTitle()).toBe('Operational Intelligence | OpsPilot');
  });

  it('renders a useful not-found page and title for unknown routes', async () => {
    TestBed.configureTestingModule({ providers: [provideRouter(routes)] });

    const harness = await RouterTestingHarness.create('/missing-page');

    expect(harness.routeNativeElement?.textContent).toContain('Page not found');
    expect(harness.routeNativeElement?.textContent).toContain('Back to dashboard');
    expect(TestBed.inject(Title).getTitle()).toBe('Not Found | OpsPilot');
  });
});
