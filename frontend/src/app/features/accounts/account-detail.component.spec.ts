import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of, Subject, throwError } from 'rxjs';
import { AccountApiService } from '../../core/api/account-api.service';
import { AccountAiAnalysis, AccountAnalytics, AccountDetails, AccountOperationalSnapshot, InteractionRecord, OrderRecord, SupportTicketRecord } from '../../core/models/account.models';
import { AccountDetailComponent } from './account-detail.component';

const account: AccountDetails = {
  id: 'account-1', name: 'Horizon Supply', industry: 'Distribution', region: 'LATAM', status: 'ACTIVE',
  monthlyRevenue: 29000, previousMonthRevenue: 61000, revenueChangePercentage: -52.46,
  engagementScore: 18, createdAt: '2026-01-01T12:00:00Z', updatedAt: '2026-09-01T12:00:00Z',
};
const snapshot: AccountOperationalSnapshot = {
  accountId: 'account-1', accountName: 'Horizon Supply', monthlyRevenue: 29000, previousMonthRevenue: 61000,
  revenueChangePercentage: -52.46, engagementScore: 18, delayedOrders: 4, openTickets: 4,
  criticalOpenTickets: 1, lastInteractionAt: null,
};
const analytics: AccountAnalytics = {
  accountId: 'account-1', accountName: 'Horizon Supply',
  risk: { score: 91, level: 'CRITICAL', revenueRisk: 30, delayedOrdersRisk: 20, supportTicketsRisk: 15, criticalTicketsRisk: 10, engagementRisk: 10, inactivityRisk: 6 },
  potential: { score: 17, level: 'LOW', revenueGrowthPotential: 0, engagementPotential: 0, revenueStrengthPotential: 12, interactionPotential: 5, operationalStabilityPotential: 0 },
  priorityScore: 61, priorityLevel: 'HIGH',
};
const order: OrderRecord = { id: 'order-1', accountId: 'account-1', orderNumber: 'ORD-1', amount: 300, status: 'PROCESSING', orderedAt: '2026-09-01T12:00:00Z', expectedDeliveryAt: null, deliveredAt: null, createdAt: '2026-09-01T12:00:00Z' };
const ticket: SupportTicketRecord = { id: 'ticket-1', accountId: 'account-1', subject: 'Payment issue', status: 'OPEN', priority: 'HIGH', openedAt: '2026-09-01T12:00:00Z', resolvedAt: null, createdAt: '2026-09-01T12:00:00Z' };
const interaction: InteractionRecord = { id: 'interaction-1', accountId: 'account-1', type: 'CALL', summary: 'Renewal follow-up', occurredAt: '2026-09-01T12:00:00Z', createdAt: '2026-09-01T12:00:00Z' };
const aiAnalysis: AccountAiAnalysis = {
  summary: 'The account has elevated operational pressure.',
  keyConcerns: ['Critical support load', 'Revenue decline'],
  recommendedActions: [{ action: 'Review the support escalation', evidence: 'One critical ticket remains open.' }],
};

describe('AccountDetailComponent', () => {
  let fixture: ComponentFixture<AccountDetailComponent>;
  let api: {
    getAccount: ReturnType<typeof vi.fn>; getAccountSnapshot: ReturnType<typeof vi.fn>;
    getAccountAnalytics: ReturnType<typeof vi.fn>; getOrders: ReturnType<typeof vi.fn>;
    getTickets: ReturnType<typeof vi.fn>; getInteractions: ReturnType<typeof vi.fn>;
    updateAccount: ReturnType<typeof vi.fn>; createOrder: ReturnType<typeof vi.fn>;
    createTicket: ReturnType<typeof vi.fn>; createInteraction: ReturnType<typeof vi.fn>;
    analyzeAccount: ReturnType<typeof vi.fn>;
  };

  async function setup(records = { orders: [] as OrderRecord[], tickets: [] as SupportTicketRecord[], interactions: [] as InteractionRecord[] }): Promise<void> {
    api = {
      getAccount: vi.fn(() => of(account)), getAccountSnapshot: vi.fn(() => of(snapshot)),
      getAccountAnalytics: vi.fn(() => of(analytics)), getOrders: vi.fn(() => of(records.orders)),
      getTickets: vi.fn(() => of(records.tickets)), getInteractions: vi.fn(() => of(records.interactions)),
      updateAccount: vi.fn(() => of(account)), createOrder: vi.fn(() => of(order)),
      createTicket: vi.fn(() => of(ticket)), createInteraction: vi.fn(() => of(interaction)),
      analyzeAccount: vi.fn(() => of(aiAnalysis)),
    };
    await TestBed.configureTestingModule({
      imports: [AccountDetailComponent],
      providers: [provideRouter([]), { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap({ accountId: 'account-1' }) } } }, { provide: AccountApiService, useValue: api }],
    }).compileComponents();
    fixture = TestBed.createComponent(AccountDetailComponent); fixture.detectChanges();
  }

  afterEach(() => TestBed.resetTestingModule());

  it('renders overview and backend-provided analytics factors', async () => {
    await setup();
    const element = fixture.nativeElement as HTMLElement;
    expect(element.textContent).toContain('Horizon Supply');
    expect(element.textContent).toContain('Delayed orders');
    expect(element.querySelector('.priority-value')?.textContent).toContain('61');
    expect(element.textContent).toContain('Revenue decline');
    expect(element.textContent).toContain('+30 points');
    expect(TestBed.inject(Title).getTitle()).toBe('Horizon Supply | OpsPilot');
  });

  it('renders operational records from the API', async () => {
    await setup({ orders: [order], tickets: [ticket], interactions: [interaction] });
    const text = fixture.nativeElement.textContent;
    expect(text).toContain('ORD-1'); expect(text).toContain('Payment issue'); expect(text).toContain('Renewal follow-up');
  });

  it('shows truthful empty operational record states', async () => {
    await setup();
    const text = fixture.nativeElement.textContent;
    expect(text).toContain('No orders recorded.'); expect(text).toContain('No support tickets recorded.'); expect(text).toContain('No interactions recorded.');
  });

  it('updates the account and refreshes snapshot and analytics', async () => {
    await setup(); click('Edit account'); setValue('#edit-name', 'Updated Horizon');
    fixture.nativeElement.querySelector('#edit-account-title + form').dispatchEvent(new Event('submit')); fixture.detectChanges();
    expect(api.updateAccount).toHaveBeenCalled(); expect(api.getAccountSnapshot).toHaveBeenCalledTimes(2); expect(api.getAccountAnalytics).toHaveBeenCalledTimes(2);
    expect(fixture.nativeElement.textContent).toContain('Analytics refreshed');
  });

  it('creates an order and refreshes operational intelligence', async () => {
    await setup(); click('Add order'); setValue('#order-number', 'ORD-2'); setValue('#order-amount', '450'); setValue('#ordered-at', '2026-10-01T12:00'); submit('#order-form-title');
    expect(api.createOrder).toHaveBeenCalled(); expect(api.getOrders).toHaveBeenCalledTimes(2); expect(api.getAccountSnapshot).toHaveBeenCalledTimes(2); expect(api.getAccountAnalytics).toHaveBeenCalledTimes(2);
  });

  it('handles duplicate order conflicts with an actionable message', async () => {
    await setup(); api.createOrder.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 409 })));
    click('Add order'); setValue('#order-number', 'ORD-1'); setValue('#order-amount', '450'); setValue('#ordered-at', '2026-10-01T12:00'); submit('#order-form-title');
    expect(fixture.nativeElement.textContent).toContain('order number already exists');
  });

  it('creates a support ticket and refreshes its record list', async () => {
    await setup(); click('Add support ticket'); setValue('#ticket-subject', 'New incident'); setValue('#opened-at', '2026-10-01T12:00'); submit('#ticket-form-title');
    expect(api.createTicket).toHaveBeenCalled(); expect(api.getTickets).toHaveBeenCalledTimes(2);
  });

  it('creates an interaction and refreshes its record list', async () => {
    await setup(); click('Add interaction'); setValue('#interaction-summary', 'Customer call'); setValue('#occurred-at', '2026-10-01T12:00'); submit('#interaction-form-title');
    expect(api.createInteraction).toHaveBeenCalled(); expect(api.getInteractions).toHaveBeenCalledTimes(2);
  });

  it('shows the initial analyst state without calling AI automatically', async () => {
    await setup();
    expect(fixture.nativeElement.textContent).toContain('AI Account Analyst');
    expect(fixture.nativeElement.textContent).toContain('Generate analysis');
    expect(fixture.nativeElement.textContent).toContain('remain deterministic');
    expect(api.analyzeAccount).not.toHaveBeenCalled();
  });

  it('requests and renders the structured analysis response', async () => {
    await setup(); click('Generate analysis');
    const text = fixture.nativeElement.textContent;
    expect(api.analyzeAccount).toHaveBeenCalledWith('account-1');
    expect(text).toContain(aiAnalysis.summary);
    expect(text).toContain('Critical support load');
    expect(text).toContain('Review the support escalation');
    expect(text).toContain('One critical ticket remains open.');
    expect(text).toContain('Regenerate analysis');
  });

  it('announces loading and prevents duplicate analysis requests', async () => {
    await setup();
    const pending = new Subject<AccountAiAnalysis>();
    api.analyzeAccount.mockReturnValue(pending);
    click('Generate analysis');
    const button = findButton('Generate analysis');
    expect(fixture.nativeElement.textContent).toContain('Analyzing current account context');
    expect(button?.disabled).toBe(true);
    button?.click(); fixture.detectChanges();
    expect(api.analyzeAccount).toHaveBeenCalledTimes(1);
  });

  it('shows a truthful state when no key concerns are returned', async () => {
    await setup(); api.analyzeAccount.mockReturnValue(of({ ...aiAnalysis, keyConcerns: [] }));
    click('Generate analysis');
    expect(fixture.nativeElement.textContent).toContain('No key concerns were identified');
  });

  it.each([
    [503, 'AI analysis is currently unavailable.'],
    [502, 'OpsPilot could not produce a valid analysis. Try again.'],
    [0, 'Unable to generate the analysis right now.'],
  ])('maps AI HTTP %i failures to safe UI copy', async (status, message) => {
    await setup();
    api.analyzeAccount.mockReturnValue(throwError(() => new HttpErrorResponse({ status })));
    click('Generate analysis');
    expect(fixture.nativeElement.textContent).toContain(message);
    expect(fixture.nativeElement.textContent).toContain('Generate analysis');
  });

  it('replaces the previous result when analysis is regenerated', async () => {
    await setup(); click('Generate analysis');
    api.analyzeAccount.mockReturnValue(of({ ...aiAnalysis, summary: 'Updated interpretation.' }));
    click('Regenerate analysis');
    expect(api.analyzeAccount).toHaveBeenCalledTimes(2);
    expect(fixture.nativeElement.textContent).toContain('Updated interpretation.');
    expect(fixture.nativeElement.textContent).not.toContain(aiAnalysis.summary);
  });

  it('clears generated analysis after a successful operational write', async () => {
    await setup(); click('Generate analysis');
    expect(fixture.nativeElement.textContent).toContain(aiAnalysis.summary);
    click('Add order'); setValue('#order-number', 'ORD-2'); setValue('#order-amount', '450'); setValue('#ordered-at', '2026-10-01T12:00'); submit('#order-form-title');
    expect(fixture.nativeElement.textContent).not.toContain(aiAnalysis.summary);
    expect(fixture.nativeElement.textContent).toContain('Generate analysis');
    expect(api.analyzeAccount).toHaveBeenCalledTimes(1);
  });

  it('ignores an in-flight AI response after operational data changes', async () => {
    await setup();
    const pending = new Subject<AccountAiAnalysis>();
    api.analyzeAccount.mockReturnValue(pending);
    click('Generate analysis');
    click('Add order'); setValue('#order-number', 'ORD-3'); setValue('#order-amount', '500'); setValue('#ordered-at', '2026-10-01T12:00'); submit('#order-form-title');
    pending.next(aiAnalysis); pending.complete(); fixture.detectChanges();
    expect(fixture.nativeElement.textContent).not.toContain(aiAnalysis.summary);
    expect(fixture.nativeElement.textContent).toContain('Generate analysis');
  });

  function click(label: string): void {
    const button = findButton(label);
    button?.click(); fixture.detectChanges();
  }
  function findButton(label: string): HTMLButtonElement | undefined {
    return [...fixture.nativeElement.querySelectorAll('button')].find((item: HTMLButtonElement) => item.textContent?.includes(label));
  }
  function setValue(selector: string, value: string): void {
    const control = fixture.nativeElement.querySelector(selector) as HTMLInputElement;
    control.value = value; control.dispatchEvent(new Event('input')); fixture.detectChanges();
  }
  function submit(headingSelector: string): void {
    const heading = fixture.nativeElement.querySelector(headingSelector) as HTMLElement;
    heading.closest('form')?.dispatchEvent(new Event('submit')); fixture.detectChanges();
  }
});
