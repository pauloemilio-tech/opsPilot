import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { HomeComponent } from './home.component';

describe('HomeComponent', () => {
  it('presents the product purpose and links to the existing dashboard', async () => {
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
    expect(element.querySelectorAll('.priority-steps li')).toHaveLength(3);
  });
});
