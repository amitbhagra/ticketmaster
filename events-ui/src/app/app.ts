import { Component, computed, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ApiConfigService } from './services/api-config.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  private readonly apiConfig = inject(ApiConfigService);
  protected readonly title = signal('event-booking-ui');
  protected readonly isMockMode = computed<boolean>(() => this.apiConfig.mockMode());
}
