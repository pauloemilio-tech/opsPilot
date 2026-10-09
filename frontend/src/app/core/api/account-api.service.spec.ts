import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AccountApiService } from './account-api.service';

describe('AccountApiService', () => {
  let service: AccountApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AccountApiService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AccountApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('uses the account collection endpoint', () => {
    service.getAccounts().subscribe();
    const request = http.expectOne('/api/accounts');
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('uses the account detail endpoints', () => {
    service.getAccount('account-1').subscribe();
    service.getAccountSnapshot('account-1').subscribe();
    service.getAccountAnalytics('account-1').subscribe();

    expect(http.expectOne('/api/accounts/account-1').request.method).toBe('GET');
    expect(http.expectOne('/api/accounts/account-1/snapshot').request.method).toBe('GET');
    expect(http.expectOne('/api/accounts/account-1/analytics').request.method).toBe('GET');
  });

  it('uses the backend priority ranking endpoint', () => {
    service.getPriorities().subscribe();
    const request = http.expectOne('/api/accounts/priorities');
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('uses the account write endpoints', () => {
    const body = { name: 'Acme', industry: null, region: null, status: 'ACTIVE' as const, monthlyRevenue: 10, previousMonthRevenue: 8, engagementScore: 75 };
    service.createAccount(body).subscribe();
    service.updateAccount('account-1', body).subscribe();
    expect(http.expectOne('/api/accounts').request.method).toBe('POST');
    expect(http.expectOne('/api/accounts/account-1').request.method).toBe('PUT');
  });

  it('uses operational record read and write endpoints', () => {
    service.getOrders('account-1').subscribe();
    service.createOrder('account-1', { orderNumber: 'ORD-1', amount: 10, status: 'PENDING', orderedAt: '2026-10-01T12:00:00Z', expectedDeliveryAt: null, deliveredAt: null }).subscribe();
    service.getTickets('account-1').subscribe();
    service.createTicket('account-1', { subject: 'Issue', status: 'OPEN', priority: 'HIGH', openedAt: '2026-10-01T12:00:00Z', resolvedAt: null }).subscribe();
    service.getInteractions('account-1').subscribe();
    service.createInteraction('account-1', { type: 'CALL', summary: 'Follow-up', occurredAt: '2026-10-01T12:00:00Z' }).subscribe();

    for (const path of ['orders', 'tickets', 'interactions']) {
      const requests = http.match(`/api/accounts/account-1/${path}`);
      expect(requests.map((request) => request.request.method)).toEqual(['GET', 'POST']);
      requests.forEach((request) => request.flush(path === 'orders' ? {} : {}));
    }
  });

  it('posts to the account AI analysis endpoint', () => {
    service.analyzeAccount('account-1').subscribe();
    const request = http.expectOne('/api/accounts/account-1/ai-analysis');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toBeNull();
    request.flush({ summary: 'Current context', keyConcerns: [], recommendedActions: [] });
  });
});
