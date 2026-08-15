import { enableProdMode } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';
import { App } from './app/app';
import { KeycloakService } from './app/services/keycloak.service';
import { ApiConfigService } from './app/services/api-config.service';
import { provideZonelessChangeDetection } from '@angular/core';
import { routes } from './app/app.routes';
import { provideRouter } from '@angular/router';
import { HttpInterceptorFn, provideHttpClient, withInterceptors } from '@angular/common/http';
import { providePrimeNG } from 'primeng/config';
import Aura from '@primeuix/themes/aura';
import { provideAnimations } from '@angular/platform-browser/animations';
import { mockApiInterceptor } from './app/services/mock-api.interceptor';

interface RuntimeConfig {
  baseUrl?: string;
  enableSecurity?: boolean;
  keycloak?: {
    clientId: string;
    realm: string;
    url: string;
  };
  useMockApi?: boolean;
}

// Remove direct instantiation
let keycloakService: KeycloakService;
const apiConfigService = new ApiConfigService();

export const ngrokHeaderInterceptor: HttpInterceptorFn = (req, next) => {
  const cloned = req.clone({
    setHeaders: {
      'ngrok-skip-browser-warning': 'true'
    }
  });
  return next(cloned);
};

export const authHeaderInterceptor: HttpInterceptorFn = (req, next) => {
  const token = keycloakService?.getToken();
  if (token) {
    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(cloned);
  }
  return next(req);
};

function bootstrapApp(useMockApi: boolean) {
  const interceptors: HttpInterceptorFn[] = useMockApi
    ? [mockApiInterceptor, ngrokHeaderInterceptor, authHeaderInterceptor]
    : [ngrokHeaderInterceptor, authHeaderInterceptor];

  bootstrapApplication(App, {
    providers: [
      { provide: KeycloakService, useValue: keycloakService },
      { provide: ApiConfigService, useValue: apiConfigService },
      provideZonelessChangeDetection(),
      provideRouter(routes),
      provideHttpClient(withInterceptors(interceptors)),
      provideAnimations(),
      providePrimeNG({
        theme: { preset: Aura, options: { darkModeSelector: '.p-dark' } },
      }),
    ]
  }).catch(err => console.error(err));
}

fetch('/assets/config.json')
  .then(res => res.json())
  .then((config: RuntimeConfig) => {
    const useMockApi = !!config.useMockApi;
    const shouldEnableSecurity = !!config.enableSecurity && !useMockApi;

    keycloakService = new KeycloakService(config.keycloak);
    apiConfigService.setMockMode(useMockApi);
    if (config.baseUrl) {
      apiConfigService.setBaseUrl(config.baseUrl);
    }
    if (shouldEnableSecurity) {
      keycloakService.init().then(() => {
        bootstrapApp(useMockApi);
      });
    } else {
      bootstrapApp(useMockApi);
    }
  })
  .catch(() => {
    keycloakService = new KeycloakService();
    apiConfigService.setMockMode(false);
    bootstrapApp(false);
  });