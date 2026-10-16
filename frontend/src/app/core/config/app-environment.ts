import { InjectionToken } from '@angular/core';
import { environment } from '../../../environments/environment';

export interface AppEnvironment {
  demo: boolean;
  apiBaseUrl: string;
}

export const APP_ENVIRONMENT = new InjectionToken<AppEnvironment>('APP_ENVIRONMENT', {
  providedIn: 'root',
  factory: () => environment,
});
