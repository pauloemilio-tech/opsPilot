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
});
