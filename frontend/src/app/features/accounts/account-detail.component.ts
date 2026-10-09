import { DatePipe, DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Title } from '@angular/platform-browser';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AccountApiService } from '../../core/api/account-api.service';
import {
  AccountAnalytics, AccountDetails, AccountOperationalSnapshot, AccountStatus, AccountWriteRequest,
  InteractionRecord, InteractionType, OrderRecord, OrderStatus, SupportTicketRecord,
  TicketPriority, TicketStatus,
} from '../../core/models/account.models';
import { LevelBadgeComponent } from '../../shared/level-badge/level-badge.component';

interface AnalyticsFactor { label: string; value: number; }
type OperationalForm = 'order' | 'ticket' | 'interaction';

@Component({
  selector: 'app-account-detail',
  imports: [DatePipe, DecimalPipe, ReactiveFormsModule, RouterLink, LevelBadgeComponent],
  templateUrl: './account-detail.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountDetailComponent {
  private readonly accountApi = inject(AccountApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly title = inject(Title);
  private readonly formBuilder = inject(FormBuilder);
  private readonly accountId = this.route.snapshot.paramMap.get('accountId');

  protected readonly account = signal<AccountDetails | null>(null);
  protected readonly snapshot = signal<AccountOperationalSnapshot | null>(null);
  protected readonly analytics = signal<AccountAnalytics | null>(null);
  protected readonly orders = signal<OrderRecord[]>([]);
  protected readonly tickets = signal<SupportTicketRecord[]>([]);
  protected readonly interactions = signal<InteractionRecord[]>([]);
  protected readonly loading = signal(true);
  protected readonly submitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly formError = signal<string | null>(null);
  protected readonly successMessage = signal<string | null>(null);
  protected readonly editOpen = signal(false);
  protected readonly activeForm = signal<OperationalForm | null>(null);
  protected readonly accountStatuses: AccountStatus[] = ['ACTIVE', 'ONBOARDING', 'INACTIVE'];
  protected readonly orderStatuses: OrderStatus[] = ['PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'DELAYED', 'CANCELLED'];
  protected readonly ticketStatuses: TicketStatus[] = ['OPEN', 'IN_PROGRESS', 'WAITING_CUSTOMER', 'RESOLVED', 'CLOSED'];
  protected readonly ticketPriorities: TicketPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
  protected readonly interactionTypes: InteractionType[] = ['CALL', 'EMAIL', 'MEETING', 'FOLLOW_UP', 'NOTE'];

  protected readonly accountForm = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(200)]], industry: ['', Validators.maxLength(100)],
    region: ['', Validators.maxLength(100)], status: ['ACTIVE' as AccountStatus, Validators.required],
    monthlyRevenue: [0, [Validators.required, Validators.min(0)]],
    previousMonthRevenue: [0, [Validators.required, Validators.min(0)]],
    engagementScore: [0, [Validators.required, Validators.min(0), Validators.max(100)]],
  });
  protected readonly orderForm = this.formBuilder.nonNullable.group({
    orderNumber: ['', [Validators.required, Validators.maxLength(50)]], amount: [0, [Validators.required, Validators.min(0)]],
    status: ['PENDING' as OrderStatus, Validators.required], orderedAt: ['', Validators.required],
    expectedDeliveryAt: [''], deliveredAt: [''],
  });
  protected readonly ticketForm = this.formBuilder.nonNullable.group({
    subject: ['', [Validators.required, Validators.maxLength(255)]], status: ['OPEN' as TicketStatus, Validators.required],
    priority: ['MEDIUM' as TicketPriority, Validators.required], openedAt: ['', Validators.required], resolvedAt: [''],
  });
  protected readonly interactionForm = this.formBuilder.nonNullable.group({
    type: ['CALL' as InteractionType, Validators.required], summary: ['', Validators.required], occurredAt: ['', Validators.required],
  });

  protected readonly accountStatusLabel = computed(() => {
    const status = this.account()?.status;
    return status ? ACCOUNT_STATUS_LABELS[status] : '';
  });
  protected readonly riskFactors = computed<AnalyticsFactor[]>(() => {
    const risk = this.analytics()?.risk;
    return risk ? [
      { label: 'Revenue decline', value: risk.revenueRisk }, { label: 'Delayed orders', value: risk.delayedOrdersRisk },
      { label: 'Open support tickets', value: risk.supportTicketsRisk }, { label: 'Critical tickets', value: risk.criticalTicketsRisk },
      { label: 'Low engagement', value: risk.engagementRisk }, { label: 'Inactivity', value: risk.inactivityRisk },
    ] : [];
  });
  protected readonly potentialFactors = computed<AnalyticsFactor[]>(() => {
    const potential = this.analytics()?.potential;
    return potential ? [
      { label: 'Revenue growth', value: potential.revenueGrowthPotential }, { label: 'Engagement', value: potential.engagementPotential },
      { label: 'Revenue strength', value: potential.revenueStrengthPotential }, { label: 'Recent interaction', value: potential.interactionPotential },
      { label: 'Operational stability', value: potential.operationalStabilityPotential },
    ] : [];
  });

  constructor() {
    this.orderForm.controls.status.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((status) => {
        if (status !== 'DELIVERED') this.orderForm.controls.deliveredAt.setValue('');
      });
    this.ticketForm.controls.status.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((status) => {
        if (status !== 'RESOLVED' && status !== 'CLOSED') this.ticketForm.controls.resolvedAt.setValue('');
      });
    this.loadAccount();
  }
  protected loadAccount(): void { this.refreshAccountData(true); }
  protected toggleEdit(): void { this.editOpen.update((open) => !open); this.activeForm.set(null); this.clearFeedback(); }
  protected openOperationalForm(form: OperationalForm): void {
    this.activeForm.update((active) => active === form ? null : form);
    this.editOpen.set(false); this.clearFeedback();
  }

  protected updateAccount(): void {
    if (!this.accountId || this.accountForm.invalid) { this.accountForm.markAllAsTouched(); return; }
    this.startSubmit();
    this.accountApi.updateAccount(this.accountId, this.accountRequest()).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => { this.editOpen.set(false); this.refreshAccountData(false, 'Account details updated. Analytics refreshed.'); },
      error: (error: HttpErrorResponse) => this.failSubmit(error, 'Unable to update this account.'),
    });
  }

  protected createOrder(): void {
    if (!this.accountId || this.orderForm.invalid) { this.orderForm.markAllAsTouched(); return; }
    const value = this.orderForm.getRawValue();
    const dateError = this.orderDateError(value);
    if (dateError) { this.formError.set(dateError); return; }
    this.startSubmit();
    this.accountApi.createOrder(this.accountId, {
      orderNumber: value.orderNumber.trim(), amount: value.amount, status: value.status,
      orderedAt: this.toInstant(value.orderedAt), expectedDeliveryAt: this.optionalInstant(value.expectedDeliveryAt),
      deliveredAt: this.optionalInstant(value.deliveredAt),
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => { this.orderForm.reset({ amount: 0, status: 'PENDING' }); this.activeForm.set(null); this.refreshAccountData(false, 'Order recorded. Snapshot and analytics refreshed.'); },
      error: (error: HttpErrorResponse) => this.failSubmit(error, 'Unable to record this order.'),
    });
  }

  protected createTicket(): void {
    if (!this.accountId || this.ticketForm.invalid) { this.ticketForm.markAllAsTouched(); return; }
    const value = this.ticketForm.getRawValue();
    const dateError = this.ticketDateError(value);
    if (dateError) { this.formError.set(dateError); return; }
    this.startSubmit();
    this.accountApi.createTicket(this.accountId, {
      subject: value.subject.trim(), status: value.status, priority: value.priority,
      openedAt: this.toInstant(value.openedAt), resolvedAt: this.optionalInstant(value.resolvedAt),
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => { this.ticketForm.reset({ status: 'OPEN', priority: 'MEDIUM' }); this.activeForm.set(null); this.refreshAccountData(false, 'Support ticket recorded. Snapshot and analytics refreshed.'); },
      error: (error: HttpErrorResponse) => this.failSubmit(error, 'Unable to record this support ticket.'),
    });
  }

  protected createInteraction(): void {
    if (!this.accountId || this.interactionForm.invalid) { this.interactionForm.markAllAsTouched(); return; }
    const value = this.interactionForm.getRawValue(); this.startSubmit();
    this.accountApi.createInteraction(this.accountId, {
      type: value.type, summary: value.summary.trim(), occurredAt: this.toInstant(value.occurredAt),
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: () => { this.interactionForm.reset({ type: 'CALL' }); this.activeForm.set(null); this.refreshAccountData(false, 'Interaction recorded. Snapshot and analytics refreshed.'); },
      error: (error: HttpErrorResponse) => this.failSubmit(error, 'Unable to record this interaction.'),
    });
  }

  private refreshAccountData(showLoading: boolean, success?: string): void {
    if (!this.accountId) { this.errorMessage.set('The account identifier is missing.'); this.loading.set(false); return; }
    if (showLoading) this.loading.set(true);
    this.errorMessage.set(null);
    forkJoin({
      account: this.accountApi.getAccount(this.accountId), snapshot: this.accountApi.getAccountSnapshot(this.accountId),
      analytics: this.accountApi.getAccountAnalytics(this.accountId), orders: this.accountApi.getOrders(this.accountId),
      tickets: this.accountApi.getTickets(this.accountId), interactions: this.accountApi.getInteractions(this.accountId),
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: ({ account, snapshot, analytics, orders, tickets, interactions }) => {
        this.account.set(account); this.snapshot.set(snapshot); this.analytics.set(analytics);
        this.orders.set(orders); this.tickets.set(tickets); this.interactions.set(interactions);
        this.accountForm.reset({
          name: account.name, industry: account.industry ?? '', region: account.region ?? '', status: account.status,
          monthlyRevenue: account.monthlyRevenue, previousMonthRevenue: account.previousMonthRevenue,
          engagementScore: account.engagementScore,
        });
        this.title.setTitle(`${account.name} | OpsPilot`); this.loading.set(false); this.submitting.set(false);
        if (success) this.successMessage.set(success);
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage.set(error.status === 404 ? 'This account no longer exists.' : 'Unable to load account information. Check your connection and try again.');
        this.loading.set(false); this.submitting.set(false);
      },
    });
  }

  private accountRequest(): AccountWriteRequest {
    const value = this.accountForm.getRawValue();
    return { ...value, name: value.name.trim(), industry: value.industry.trim() || null, region: value.region.trim() || null };
  }
  private startSubmit(): void { this.submitting.set(true); this.clearFeedback(); }
  private failSubmit(error: HttpErrorResponse, fallback: string): void { this.formError.set(this.messageFor(error, fallback)); this.submitting.set(false); }
  private clearFeedback(): void { this.formError.set(null); this.successMessage.set(null); }
  private toInstant(value: string): string { return new Date(value).toISOString(); }
  private optionalInstant(value: string): string | null { return value ? this.toInstant(value) : null; }
  private orderDateError(value: ReturnType<typeof this.orderForm.getRawValue>): string | null {
    const ordered = new Date(value.orderedAt).getTime();
    if (value.expectedDeliveryAt && new Date(value.expectedDeliveryAt).getTime() < ordered) return 'Expected delivery cannot be before the order date.';
    if (value.deliveredAt && new Date(value.deliveredAt).getTime() < ordered) return 'Delivery cannot be before the order date.';
    if (value.status === 'DELIVERED' && !value.deliveredAt) return 'Delivery time is required for delivered orders.';
    if (value.status !== 'DELIVERED' && value.deliveredAt) return 'Delivery time is only allowed for delivered orders.';
    return null;
  }
  private ticketDateError(value: ReturnType<typeof this.ticketForm.getRawValue>): string | null {
    const resolved = value.status === 'RESOLVED' || value.status === 'CLOSED';
    if (value.resolvedAt && new Date(value.resolvedAt).getTime() < new Date(value.openedAt).getTime()) return 'Resolution cannot be before the opened time.';
    if (resolved && !value.resolvedAt) return 'Resolution time is required for resolved or closed tickets.';
    if (!resolved && value.resolvedAt) return 'Resolution time is only allowed for resolved or closed tickets.';
    return null;
  }
  private messageFor(error: HttpErrorResponse, fallback: string): string {
    if (error.status === 409) return 'That order number already exists. Use a unique order number.';
    if (error.status === 404) return 'This account no longer exists.';
    if (error.status === 400 && typeof error.error?.message === 'string') return `Check the submitted fields: ${error.error.message}`;
    return error.status === 0 ? 'Unable to reach OpsPilot. Check your connection.' : fallback;
  }
}

const ACCOUNT_STATUS_LABELS: Record<AccountStatus, string> = { ACTIVE: 'Active', INACTIVE: 'Inactive', ONBOARDING: 'Onboarding' };
