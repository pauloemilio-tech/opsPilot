import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AccountApiService } from '../../core/api/account-api.service';
import { AccountStatus, AccountSummary, AccountWriteRequest } from '../../core/models/account.models';

@Component({
  selector: 'app-accounts',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './accounts.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountsComponent {
  private readonly accountApi = inject(AccountApiService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly router = inject(Router);
  private readonly formBuilder = inject(FormBuilder);

  protected readonly accounts = signal<AccountSummary[]>([]);
  protected readonly loading = signal(true);
  protected readonly submitting = signal(false);
  protected readonly formOpen = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly formError = signal<string | null>(null);
  protected readonly statuses: AccountStatus[] = ['ACTIVE', 'ONBOARDING', 'INACTIVE'];
  protected readonly accountForm = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(200)]],
    industry: ['', Validators.maxLength(100)],
    region: ['', Validators.maxLength(100)],
    status: ['ACTIVE' as AccountStatus, Validators.required],
    monthlyRevenue: [0, [Validators.required, Validators.min(0)]],
    previousMonthRevenue: [0, [Validators.required, Validators.min(0)]],
    engagementScore: [0, [Validators.required, Validators.min(0), Validators.max(100)]],
  });

  constructor() {
    this.loadAccounts();
  }

  protected loadAccounts(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.accountApi
      .getAccounts()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (accounts) => {
          this.accounts.set(accounts);
          this.loading.set(false);
        },
        error: () => {
          this.errorMessage.set('Unable to load accounts. Check your connection and try again.');
          this.loading.set(false);
        },
      });
  }

  protected toggleForm(): void {
    this.formOpen.update((open) => !open);
    this.formError.set(null);
  }

  protected createAccount(): void {
    if (this.accountForm.invalid) {
      this.accountForm.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.formError.set(null);
    this.accountApi
      .createAccount(this.toRequest())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (account) => void this.router.navigate(['/accounts', account.id]),
        error: (error: HttpErrorResponse) => {
          this.formError.set(this.messageFor(error, 'Unable to create the account.'));
          this.submitting.set(false);
        },
      });
  }

  private toRequest(): AccountWriteRequest {
    const value = this.accountForm.getRawValue();
    return {
      ...value,
      name: value.name.trim(),
      industry: value.industry.trim() || null,
      region: value.region.trim() || null,
    };
  }

  private messageFor(error: HttpErrorResponse, fallback: string): string {
    if (error.status === 400 && typeof error.error?.message === 'string') {
      return `Check the account fields: ${error.error.message}`;
    }
    return error.status === 0 ? 'Unable to reach OpsPilot. Check your connection.' : fallback;
  }
}
