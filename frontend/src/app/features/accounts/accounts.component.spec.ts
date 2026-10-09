import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { Observable, Subject, of, throwError } from 'rxjs';
import { AccountApiService } from '../../core/api/account-api.service';
import { AccountDetails, AccountSummary } from '../../core/models/account.models';
import { AccountsComponent } from './accounts.component';

const account: AccountSummary = {
  id: 'account-1', name: 'Acme Operations', industry: 'Technology', region: 'LATAM',
  status: 'ACTIVE', monthlyRevenue: 1000, previousMonthRevenue: 900, engagementScore: 80,
};
const created: AccountDetails = { ...account, revenueChangePercentage: 11.11, createdAt: '2026-10-01T12:00:00Z', updatedAt: '2026-10-01T12:00:00Z' };

describe('AccountsComponent', () => {
  async function setup(accounts$: Observable<AccountSummary[]>, create$ = of(created)): Promise<ComponentFixture<AccountsComponent>> {
    await TestBed.configureTestingModule({
      imports: [AccountsComponent],
      providers: [
        provideRouter([]),
        { provide: AccountApiService, useValue: { getAccounts: () => accounts$, createAccount: vi.fn(() => create$) } },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(AccountsComponent);
    fixture.detectChanges();
    return fixture;
  }

  afterEach(() => TestBed.resetTestingModule());

  it('loads and renders accounts returned by the API', async () => {
    const fixture = await setup(of([account]));
    expect(fixture.nativeElement.textContent).toContain('Acme Operations');
    expect(fixture.nativeElement.textContent).toContain('Technology');
    expect(fixture.nativeElement.querySelector('.open-account')?.getAttribute('href')).toBe('/accounts/account-1');
  });

  it('shows a create-first-account empty state', async () => {
    const fixture = await setup(of([]));
    expect(fixture.nativeElement.textContent).toContain('No accounts yet');
    expect(fixture.nativeElement.textContent).toContain('Create first account');
  });

  it('keeps a truthful loading state until the API responds', async () => {
    const accounts$ = new Subject<AccountSummary[]>();
    const fixture = await setup(accounts$);
    expect(fixture.nativeElement.textContent).toContain('Loading accounts');
    accounts$.next([]); accounts$.complete(); fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('No accounts yet');
  });

  it('shows an actionable account-list API error', async () => {
    const fixture = await setup(throwError(() => new HttpErrorResponse({ status: 503 })));
    expect(fixture.nativeElement.textContent).toContain('Accounts unavailable');
    expect(fixture.nativeElement.textContent).toContain('Try again');
  });

  it('creates an account and navigates to its detail page', async () => {
    const fixture = await setup(of([]));
    const router = TestBed.inject(Router);
    const navigate = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    click(fixture, 'Create first account');
    setValue(fixture, '#account-name', 'New Account');
    setValue(fixture, '#monthly-revenue', '1000');
    setValue(fixture, '#previous-revenue', '900');
    setValue(fixture, '#engagement-score', '80');
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));
    fixture.detectChanges();
    expect(TestBed.inject(AccountApiService).createAccount).toHaveBeenCalled();
    expect(navigate).toHaveBeenCalledWith(['/accounts', 'account-1']);
  });

  it('shows client validation without calling the API', async () => {
    const fixture = await setup(of([]));
    click(fixture, 'Create first account');
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Enter an account name');
    expect(TestBed.inject(AccountApiService).createAccount).not.toHaveBeenCalled();
  });

  it('shows actionable backend validation failures', async () => {
    const failure = throwError(() => new HttpErrorResponse({ status: 400, error: { message: 'name: must not be blank' } }));
    const fixture = await setup(of([]), failure);
    click(fixture, 'Create first account');
    setValue(fixture, '#account-name', 'New Account');
    setValue(fixture, '#engagement-score', '80');
    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Check the account fields');
  });
});

function click(fixture: ComponentFixture<AccountsComponent>, label: string): void {
  const button = [...fixture.nativeElement.querySelectorAll('button')].find((item: HTMLButtonElement) => item.textContent?.includes(label));
  button?.click(); fixture.detectChanges();
}

function setValue(fixture: ComponentFixture<AccountsComponent>, selector: string, value: string): void {
  const input = fixture.nativeElement.querySelector(selector) as HTMLInputElement;
  input.value = value; input.dispatchEvent(new Event('input')); fixture.detectChanges();
}
