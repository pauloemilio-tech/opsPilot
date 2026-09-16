import { DOCUMENT } from '@angular/common';
import { TestBed } from '@angular/core/testing';
import { ThemeService } from './theme.service';

describe('ThemeService', () => {
  const storageKey = 'opspilot-theme';

  beforeEach(() => {
    localStorage.clear();
    document.documentElement.removeAttribute('data-theme');
    TestBed.resetTestingModule();
  });

  it('uses and applies a persisted preference', () => {
    localStorage.setItem(storageKey, 'dark');
    const service = TestBed.inject(ThemeService);
    expect(service.theme()).toBe('dark');
    expect(document.documentElement.dataset['theme']).toBe('dark');
  });

  it('toggles immediately and persists the new preference', () => {
    localStorage.setItem(storageKey, 'light');
    const service = TestBed.inject(ThemeService);
    service.toggle();
    expect(service.theme()).toBe('dark');
    expect(document.documentElement.dataset['theme']).toBe('dark');
    expect(localStorage.getItem(storageKey)).toBe('dark');
  });

  it('uses the system preference when no preference is saved', () => {
    const matchMedia = vi.fn().mockReturnValue({ matches: true });
    const documentRef = {
      documentElement: { dataset: {} },
      defaultView: { localStorage: { getItem: () => null }, matchMedia },
    } as unknown as Document;
    TestBed.configureTestingModule({ providers: [{ provide: DOCUMENT, useValue: documentRef }] });
    const service = TestBed.inject(ThemeService);
    expect(matchMedia).toHaveBeenCalledWith('(prefers-color-scheme: dark)');
    expect(service.theme()).toBe('dark');
  });
});
