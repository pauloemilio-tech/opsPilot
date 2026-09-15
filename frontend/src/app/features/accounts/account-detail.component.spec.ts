import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { Title } from '@angular/platform-browser';
import { of } from 'rxjs';
import { AccountApiService } from '../../core/api/account-api.service';
import { AccountAnalytics, AccountDetails, AccountOperationalSnapshot } from '../../core/models/account.models';
import { AccountDetailComponent } from './account-detail.component';

const account: AccountDetails = {
  id: 'account-1', name: 'Horizon Supply', industry: 'Distribution', region: 'LATAM', status: 'ACTIVE',
  monthlyRevenue: 29000, previousMonthRevenue: 61000, revenueChangePercentage: -52.46,
  engagementScore: 18, createdAt: '2026-01-01T12:00:00Z', updatedAt: '2026-09-01T12:00:00Z',
};

const snapshot: AccountOperationalSnapshot = {
  accountId: 'account-1', accountName: 'Horizon Supply', monthlyRevenue: 29000,
  previousMonthRevenue: 61000, revenueChangePercentage: -52.46, engagementScore: 18,
  delayedOrders: 4, openTickets: 4, criticalOpenTickets: 1, lastInteractionAt: null,
};

const analytics: AccountAnalytics = {
  accountId: 'account-1', accountName: 'Horizon Supply',
  risk: {
    score: 91, level: 'CRITICAL', revenueRisk: 30, delayedOrdersRisk: 20,
    supportTicketsRisk: 15, criticalTicketsRisk: 10, engagementRisk: 10, inactivityRisk: 6,
  },
  potential: {
    score: 17, level: 'LOW', revenueGrowthPotential: 0, engagementPotential: 0,
    revenueStrengthPotential: 12, interactionPotential: 5, operationalStabilityPotential: 0,
  },
  priorityScore: 61, priorityLevel: 'HIGH',
};

describe('AccountDetailComponent', () => {
  let fixture: ComponentFixture<AccountDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountDetailComponent],
      providers: [
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ accountId: 'account-1' }) } } },
        {
          provide: AccountApiService,
          useValue: {
            getAccount: () => of(account),
            getAccountSnapshot: () => of(snapshot),
            getAccountAnalytics: () => of(analytics),
          },
        },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(AccountDetailComponent);
    fixture.detectChanges();
  });

  it('renders overview and operational snapshot values', () => {
    const element = fixture.nativeElement as HTMLElement;
    expect(element.textContent).toContain('Horizon Supply');
    expect(element.textContent).toContain('Delayed orders');
    expect(element.textContent).toContain('4');
    expect(element.textContent).toContain('No interactions recorded');
    expect(TestBed.inject(Title).getTitle()).toBe('Horizon Supply | OpsPilot');
  });

  it('renders backend-provided analytics factors without recalculation', () => {
    const element = fixture.nativeElement as HTMLElement;
    const factors = [...element.querySelectorAll('.factor-list')].map((list) => list.textContent);
    expect(element.textContent).toContain('Analytics summary');
    expect(element.textContent).toContain('Critical');
    expect(factors[0]).toContain('Revenue decline');
    expect(factors[0]).toContain('+30');
    expect(factors[1]).toContain('Revenue strength');
    expect(factors[1]).toContain('+12');
  });
});
