import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { HomeComponent } from './home.component';

describe('HomeComponent', () => {
  it('presents the implemented product workflow and real application routes', async () => {
    await TestBed.configureTestingModule({
      imports: [HomeComponent],
      providers: [provideRouter([])],
    }).compileComponents();

    const fixture = TestBed.createComponent(HomeComponent);
    fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('h1')?.textContent).toContain('explainable priorities');
    expect(element.querySelector<HTMLAnchorElement>('.primary-action')?.getAttribute('href')).toBe(
      '/dashboard',
    );
    expect(element.querySelector<HTMLAnchorElement>('.secondary-action')?.getAttribute('href')).toBe(
      '/accounts',
    );
    expect(element.querySelectorAll('.priority-steps li')).toHaveLength(6);
    expect(element.querySelectorAll('h1')).toHaveLength(1);
  });

  it('distinguishes deterministic analytics from AI-assisted interpretation', async () => {
    await TestBed.configureTestingModule({
      imports: [HomeComponent],
      providers: [provideRouter([])],
    }).compileComponents();

    const fixture = TestBed.createComponent(HomeComponent);
    fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;

    expect(element.textContent).toContain('Rules produce the scores');
    expect(element.textContent).toContain('AI interprets the context');
    expect(element.textContent).toContain('Risk, Potential and Priority remain deterministic');
    expect(element.querySelector('.signal-categories')?.textContent).toContain('Orders');
    expect(element.querySelector('.evidence-path')?.textContent).toContain('Factor contributions');
  });
});
