import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ApiConfigService {
  private baseUrl = '';
  readonly mockMode = signal(false);

  getBaseUrl(): string {
    return this.baseUrl;
  }

  setBaseUrl(url: string): void {
    this.baseUrl = url;
  }

  setMockMode(value: boolean): void {
    this.mockMode.set(value);
  }
}
