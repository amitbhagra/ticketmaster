import Keycloak from 'keycloak-js';

export class KeycloakService {
  private keycloak: Keycloak;

  constructor(config?: { url: string; realm: string; clientId: string }) {
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

  login() {
    this.keycloak.login();
  }

  logout() {
    this.keycloak.logout();
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