import Keycloak from 'keycloak-js';

interface KeycloakConfig {
  clientId: string;
  realm: string;
  url: string;
}

export class KeycloakService {
  private readonly keycloak: Keycloak;

  constructor(config?: KeycloakConfig) {
    this.keycloak = new Keycloak(
      config ?? {
        url: 'http://localhost:8082',
        realm: 'ticketmaster',
        clientId: 'ui-client',
      }
    );
  }

  init(): Promise<boolean> {
    return this.keycloak.init({
      onLoad: 'login-required',
      checkLoginIframe: false
    });
  }

  login(): Promise<void> {
    return this.keycloak.login();
  }

  logout(): Promise<void> {
    return this.keycloak.logout();
  }

  getToken(): string | undefined {
    return this.keycloak.token;
  }

  isLoggedIn(): boolean {
    return !!this.keycloak.token;
  }

  isAdmin(): boolean {
    return !!this.keycloak.tokenParsed?.['realm_access']?.['roles']?.includes('admin');
  }

  getSubject(): string | undefined {
    return this.keycloak.subject;
  }

  getUsername(): string | undefined {
    return this.keycloak.tokenParsed?.['name'];
  }
}