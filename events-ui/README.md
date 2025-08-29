# EventBookingUi

This project is an Angular application for booking and managing event tickets.  
It supports runtime configuration for backend and authentication integration.

## Prerequisites

- **Node.js** (version 18.x or higher recommended)
- **npm** (comes with Node.js)

You must have Node.js installed to run and build this application.  
Download it from [nodejs.org](https://nodejs.org/).

## Development server

To start a local development server, run:

```bash
npm install
npm run start
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.

## Runtime Configuration

The application reads its configuration from `src/assets/config.json` at startup.  
This allows you to enable/disable security and set backend URLs without rebuilding the app.

### Example `src/assets/config.json`:

```json
{
  "enableSecurity": true,
  "baseUrl": "http://localhost:8080",
  "keycloak": {
    "url": "http://localhost:8082",
    "realm": "ticketmaster",
    "clientId": "ui-client"
  }
}
```

- `enableSecurity`: Set to `true` to enable Keycloak authentication, or `false` to run without authentication.
- `baseUrl`: The base URL for backend API requests.
- `keycloak`: Keycloak server configuration (used only if `enableSecurity` is `true`).

## Technical Structure

- **src/main.ts**: Reads `config.json` and bootstraps the app with runtime configuration.
- **src/app/services/api-config.service.ts**: Provides the backend API base URL, set at runtime.
- **src/app/services/keycloak.service.ts**: Handles authentication using Keycloak, configured at runtime.
- **src/app/my-bookings/**: Contains components for viewing and managing bookings.
- **src/assets/config.json**: Central place for runtime configuration.

## Customization

- To change backend or authentication settings, edit `src/assets/config.json` and restart the dev server.
- No need to rebuild the app for configuration changes.

## License

MIT

