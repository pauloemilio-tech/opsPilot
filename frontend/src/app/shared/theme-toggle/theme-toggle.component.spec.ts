import { TestBed } from '@angular/core/testing';
import { ThemeService } from '../../core/theme/theme.service';
import { ThemeToggleComponent } from './theme-toggle.component';

describe('ThemeToggleComponent', () => {
  it('exposes an accessible action and toggles the active theme', () => {
    localStorage.setItem('opspilot-theme', 'dark');
    const fixture = TestBed.createComponent(ThemeToggleComponent);
    fixture.detectChanges();
    const button = (fixture.nativeElement as HTMLElement).querySelector('button')!;
    expect(button.getAttribute('aria-label')).toBe('Switch to light mode');

    button.click();
    fixture.detectChanges();
    expect(TestBed.inject(ThemeService).theme()).toBe('light');
    expect(button.getAttribute('aria-label')).toBe('Switch to dark mode');
  });
});
