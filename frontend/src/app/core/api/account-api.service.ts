import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AccountAnalytics, AccountDetails, AccountOperationalSnapshot, AccountPriority, AccountSummary } from '../models/account.models';

@Injectable({ providedIn: 'root' })
export class AccountApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/accounts';

  getAccounts(): Observable<AccountSummary[]> {
    return this.http.get<AccountSummary[]>(this.baseUrl);
  }

  getAccount(accountId: string): Observable<AccountDetails> {
    return this.http.get<AccountDetails>(`${this.baseUrl}/${accountId}`);
  }

  getAccountSnapshot(accountId: string): Observable<AccountOperationalSnapshot> {
    return this.http.get<AccountOperationalSnapshot>(`${this.baseUrl}/${accountId}/snapshot`);
  }

  getAccountAnalytics(accountId: string): Observable<AccountAnalytics> {
    return this.http.get<AccountAnalytics>(`${this.baseUrl}/${accountId}/analytics`);
  }

  getPriorities(): Observable<AccountPriority[]> {
    return this.http.get<AccountPriority[]>(`${this.baseUrl}/priorities`);
  }
}
