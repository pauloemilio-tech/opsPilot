import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { AccountApiService } from '../../core/api/account-api.service';
import { AccountPriority, PriorityLevel } from '../../core/models/account.models';
import { LevelBadgeComponent } from '../../shared/level-badge/level-badge.component';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, LevelBadgeComponent],
  templateUrl: './dashboard.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardComponent {
  private readonly accountApi = inject(AccountApiService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly priorities = signal<AccountPriority[]>([]);
  protected readonly loading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly summary = computed(() => {
    const priorities = this.priorities();
    return {
      total: priorities.length,
      attention: this.countByLevel(priorities, 'URGENT') + this.countByLevel(priorities, 'HIGH'),
      medium: this.countByLevel(priorities, 'MEDIUM'),
      low: this.countByLevel(priorities, 'LOW'),
    };
  });

  constructor() {
    this.loadPriorities();
  }

  protected loadPriorities(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    this.accountApi
      .getPriorities()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (priorities) => {
          this.priorities.set(priorities);
          this.loading.set(false);
        },
        error: () => {
          this.errorMessage.set(
            'Unable to load account priorities. Check your connection and try again.',
          );
          this.loading.set(false);
        },
      });
  }

  private countByLevel(priorities: AccountPriority[], level: PriorityLevel): number {
    return priorities.filter((priority) => priority.priorityLevel === level).length;
  }
}
