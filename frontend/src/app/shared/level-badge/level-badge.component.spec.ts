import { TestBed } from '@angular/core/testing';
import { LevelBadgeComponent } from './level-badge.component';

describe('LevelBadgeComponent', () => {
  it('formats backend enum values as professional labels', () => {
    const fixture = TestBed.createComponent(LevelBadgeComponent);
    fixture.componentRef.setInput('level', 'VERY_HIGH');
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Very High');
  });
});
