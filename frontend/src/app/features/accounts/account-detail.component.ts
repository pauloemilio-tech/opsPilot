import { DatePipe, DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AccountApiService } from '../../core/api/account-api.service';
import { AccountAnalytics, AccountDetails, AccountOperationalSnapshot } from '../../core/models/account.models';
import { LevelBadgeComponent } from '../../shared/level-badge/level-badge.component';

interface AnalyticsFactor {
  label: string;
  value: number;
}

@Component({
  selector: 'app-account-detail',
  imports: [DatePipe, DecimalPipe, RouterLink, LevelBadgeComponent],
  templateUrl: './account-detail.component.html',
  styleUrl: './account-detail.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountDetailComponent {
  private readonly accountApi = inject(AccountApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly accountId = this.route.snapshot.paramMap.get('accountId');

  protected readonly account = signal<AccountDetails | null>(null);
  protected readonly snapshot = signal<AccountOperationalSnapshot | null>(null);
  protected readonly analytics = signal<AccountAnalytics | null>(null);
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly riskFactors = computed<AnalyticsFactor[]>(() => {
    const risk = this.analytics()?.risk;
    return risk
      ? [
          { label: 'Revenue decline', value: risk.revenueRisk },
          { label: 'Delayed orders', value: risk.delayedOrdersRisk },
          { label: 'Open support tickets', value: risk.supportTicketsRisk },
          { label: 'Critical tickets', value: risk.criticalTicketsRisk },
          { label: 'Low engagement', value: risk.engagementRisk },
          { label: 'Inactivity', value: risk.inactivityRisk },
        ]
      : [];
  });
  protected readonly potentialFactors = computed<AnalyticsFactor[]>(() => {
    const potential = this.analytics()?.potential;
    return potential
      ? [
          { label: 'Revenue growth', value: potential.revenueGrowthPotential },
          { label: 'Engagement', value: potential.engagementPotential },
          { label: 'Revenue strength', value: potential.revenueStrengthPotential },
          { label: 'Recent interaction', value: potential.interactionPotential },
          { label: 'Operational stability', value: potential.operationalStabilityPotential },
        ]
      : [];
  });

  constructor() {
    this.loadAccount();
  }

  protected loadAccount(): void {
    if (!this.accountId) {
      this.errorMessage.set('The account identifier is missing.');
      this.loading.set(false);
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    forkJoin({
      account: this.accountApi.getAccount(this.accountId),
      snapshot: this.accountApi.getAccountSnapshot(this.accountId),
      analytics: this.accountApi.getAccountAnalytics(this.accountId),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ({ account, snapshot, analytics }) => {
          this.account.set(account);
          this.snapshot.set(snapshot);
          this.analytics.set(analytics);
          this.loading.set(false);
        },
        error: () => {
          this.errorMessage.set('Unable to load this account. It may no longer exist, or the backend may be unavailable.');
          this.loading.set(false);
        },
      });
  }
}
