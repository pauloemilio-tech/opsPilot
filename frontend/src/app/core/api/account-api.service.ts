import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AccountAnalytics,
  AccountAiAnalysis,
  AccountDetails,
  AccountOperationalSnapshot,
  AccountPriority,
  AccountSummary,
  AccountWriteRequest,
  InteractionCreateRequest,
  InteractionRecord,
  OrderCreateRequest,
  OrderRecord,
  SupportTicketCreateRequest,
  SupportTicketRecord,
} from '../models/account.models';

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

  createAccount(request: AccountWriteRequest): Observable<AccountDetails> {
    return this.http.post<AccountDetails>(this.baseUrl, request);
  }

  updateAccount(accountId: string, request: AccountWriteRequest): Observable<AccountDetails> {
    return this.http.put<AccountDetails>(`${this.baseUrl}/${accountId}`, request);
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

  getOrders(accountId: string): Observable<OrderRecord[]> {
    return this.http.get<OrderRecord[]>(`${this.baseUrl}/${accountId}/orders`);
  }

  createOrder(accountId: string, request: OrderCreateRequest): Observable<OrderRecord> {
    return this.http.post<OrderRecord>(`${this.baseUrl}/${accountId}/orders`, request);
  }

  getTickets(accountId: string): Observable<SupportTicketRecord[]> {
    return this.http.get<SupportTicketRecord[]>(`${this.baseUrl}/${accountId}/tickets`);
  }

  createTicket(
    accountId: string,
    request: SupportTicketCreateRequest,
  ): Observable<SupportTicketRecord> {
    return this.http.post<SupportTicketRecord>(`${this.baseUrl}/${accountId}/tickets`, request);
  }

  getInteractions(accountId: string): Observable<InteractionRecord[]> {
    return this.http.get<InteractionRecord[]>(`${this.baseUrl}/${accountId}/interactions`);
  }

  createInteraction(
    accountId: string,
    request: InteractionCreateRequest,
  ): Observable<InteractionRecord> {
    return this.http.post<InteractionRecord>(
      `${this.baseUrl}/${accountId}/interactions`,
      request,
    );
  }

  analyzeAccount(accountId: string): Observable<AccountAiAnalysis> {
    return this.http.post<AccountAiAnalysis>(`${this.baseUrl}/${accountId}/ai-analysis`, null);
  }
}
