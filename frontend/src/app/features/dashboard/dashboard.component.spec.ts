import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { Observable, of, Subject, throwError } from 'rxjs';
import { AccountApiService } from '../../core/api/account-api.service';
import { AccountPriority } from '../../core/models/account.models';
import { DashboardComponent } from './dashboard.component';

@Component({ template: '' })
class AccountStubComponent {}

const priorities: AccountPriority[] = [
  {
    accountId: 'horizon-id', accountName: 'Horizon Supply', riskScore: 91, riskLevel: 'CRITICAL',
    potentialScore: 17, potentialLevel: 'LOW', priorityScore: 61, priorityLevel: 'HIGH',
  },
  {
    accountId: 'nova-id', accountName: 'Nova Distribution', riskScore: 4, riskLevel: 'LOW',
    potentialScore: 86, potentialLevel: 'VERY_HIGH', priorityScore: 37, priorityLevel: 'MEDIUM',
  },
];

describe('DashboardComponent', () => {
  let fixture: ComponentFixture<DashboardComponent>;
  let response: Observable<AccountPriority[]>;

  beforeEach(async () => {
    response = of(priorities);
    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        provideRouter([{ path: 'accounts/:accountId', component: AccountStubComponent }]),
        { provide: AccountApiService, useValue: { getPriorities: () => response } },
      ],
    }).compileComponents();
  });

  function render(): HTMLElement {
    fixture = TestBed.createComponent(DashboardComponent);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('shows a loading state while priorities are pending', () => {
    response = new Subject<AccountPriority[]>();
    expect(render().textContent).toContain('Loading priorities');
  });

  it('renders the returned ranking in backend order with score levels', () => {
    const element = render();
    const rows = [...element.querySelectorAll('tbody tr')];
    expect(rows).toHaveLength(2);
    expect(rows[0].textContent).toContain('Horizon Supply');
    expect(rows[1].textContent).toContain('Nova Distribution');
    expect(rows[0].textContent).toContain('CRITICAL');
    expect(rows[0].textContent).toContain('61');
    expect(element.textContent).toContain('Total monitored');
  });

  it('shows an intentional empty state', () => {
    response = of([]);
    expect(render().textContent).toContain('No monitored accounts');
  });

  it('shows a recoverable error state', () => {
    response = throwError(() => new Error('offline'));
    const element = render();
    expect(element.textContent).toContain('Priorities unavailable');
    expect(element.querySelector('button')?.textContent).toContain('Try again');
  });

  it('navigates to the selected account', async () => {
    const element = render();
    element.querySelector<HTMLAnchorElement>('.account-link')?.click();
    await fixture.whenStable();
    expect(TestBed.inject(Router).url).toBe('/accounts/horizon-id');
  });
});
