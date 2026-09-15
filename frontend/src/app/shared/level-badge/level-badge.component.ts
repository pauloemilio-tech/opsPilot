import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { AnalyticsLevel } from '../../core/models/account.models';

@Component({
  selector: 'app-level-badge',
  template: '<span [class]="badgeClass()">{{ displayLevel() }}</span>',
  styleUrl: './level-badge.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LevelBadgeComponent {
  readonly level = input.required<AnalyticsLevel>();
  protected readonly displayLevel = computed(() => LEVEL_LABELS[this.level()]);
  protected readonly badgeClass = computed(() => `level-badge level-${this.level().toLowerCase().replace('_', '-')}`);
}

const LEVEL_LABELS: Record<AnalyticsLevel, string> = {
  LOW: 'Low',
  MEDIUM: 'Medium',
  HIGH: 'High',
  CRITICAL: 'Critical',
  VERY_HIGH: 'Very High',
  URGENT: 'Urgent',
};
