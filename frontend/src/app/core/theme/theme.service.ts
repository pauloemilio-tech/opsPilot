import { DOCUMENT } from '@angular/common';
import { Inject, Injectable, signal } from '@angular/core';

export type Theme = 'dark' | 'light';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private static readonly storageKey = 'opspilot-theme';
  readonly theme = signal<Theme>('dark');

  constructor(@Inject(DOCUMENT) private readonly document: Document) {
    this.applyTheme(this.savedTheme() ?? this.systemTheme(), false);
  }

  toggle(): void {
    this.applyTheme(this.theme() === 'dark' ? 'light' : 'dark');
  }

  private applyTheme(theme: Theme, persist = true): void {
    this.theme.set(theme);
    this.document.documentElement.dataset['theme'] = theme;
    if (persist) {
      try {
        this.document.defaultView?.localStorage.setItem(ThemeService.storageKey, theme);
      } catch {
        // Theme selection still works when browser storage is unavailable.
      }
    }
  }

  private savedTheme(): Theme | null {
    try {
      const saved = this.document.defaultView?.localStorage.getItem(ThemeService.storageKey);
      return saved === 'dark' || saved === 'light' ? saved : null;
    } catch {
      return null;
    }
  }

  private systemTheme(): Theme {
    return this.document.defaultView?.matchMedia?.('(prefers-color-scheme: dark)').matches
      ? 'dark'
      : 'light';
  }
}
