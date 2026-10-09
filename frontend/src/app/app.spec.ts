import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';
import { ThemeService } from './core/theme/theme.service';

describe('App', () => {
  it('renders the OpsPilot application shell', async () => {
    localStorage.setItem('opspilot-theme', 'light');
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter([])],
    }).compileComponents();

    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    const logo = compiled.querySelector<HTMLImageElement>('.brand img');
    expect(logo?.getAttribute('src')).toBe('/assets/opsPilot.logoLight.png');
    expect(logo?.alt).toBe('OpsPilot — Signal workspace');

    TestBed.inject(ThemeService).toggle();
    fixture.detectChanges();
    expect(logo?.getAttribute('src')).toBe('/assets/opsPilot.logoDark.png');

    const links = [...compiled.querySelectorAll<HTMLAnchorElement>('nav a')];
    expect(links.map((link) => link.textContent?.trim())).toEqual(['Home', 'Dashboard', 'Accounts']);
    expect(links.map((link) => link.getAttribute('href'))).toEqual(['/', '/dashboard', '/accounts']);
    expect(compiled.querySelector('.skip-link')?.getAttribute('href')).toBe('#main-content');
    expect(compiled.querySelector('main')?.id).toBe('main-content');
    expect(compiled.querySelector('app-theme-toggle button')?.getAttribute('aria-label')).toMatch(
      /mode/,
    );
  });
});
