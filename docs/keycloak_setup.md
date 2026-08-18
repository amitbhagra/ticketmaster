This guide provides a comprehensive, step-by-step walkthrough to run Keycloak locally on Docker Desktop, configure a realm named ticketmaster, set up client roles (admin and user), and provision users assigned to those roles.
------------------------------
## Phase 1: Run Keycloak on Docker Desktop
We will use the official Quarkus-based Keycloak image. We will also spin up a PostgreSQL database container to ensure your configuration and users persist even if the Keycloak container restarts.
## Step 1: Create a Docker Compose File - Available in docker-compose directory

## Step 2: Start the Containers

   1. Wait about 10–15 seconds for Keycloak to initialize, then open your browser and navigate to: http://localhost:8082 
   2. Click on Administration Console and log in using the credentials defined in the compose file:
   * Username: admin
   * Password: admin
   
------------------------------
## Phase 2: Create the ticketmaster Realm
Keycloak uses "Realms" to isolate spaces for different applications. By default, you start in the Master realm (used only for Keycloak administration).

   1. In the top-left corner of the Keycloak Admin Console, click the dropdown menu that says master.
   2. Click Create Realm.
   3. In the Realm name field, type: ticketmaster
   4. Click Create. You are now managing the ticketmaster realm.

------------------------------

## Step 1: Initialize the Client Creation

   1. Access your Keycloak Admin Console at http://localhost:8082 and log in.
   2. In the top-left dropdown, ensure your active realm is set to ticketmaster (do not use the master realm).
   3. On the left navigation panel, click on Clients.
   4. Click the Create client button.

------------------------------
## Step 2: Configure Client Settings (3-Step Wizard)
Keycloak will guide you through a wizard. Configure the settings based on the standard architecture for modern frontend user interfaces (like React, Angular, Vue, or a Next.js single-page application):
## 1. General Settings

* Client type: OpenID Connect (Leave as default)
* Client ID: ui-client
* Name: Ticketmaster UI Frontend (An easily recognizable display name)
* Click Next.

## 2. Capability Config
Modern frontend user interfaces should use the secure Authorization Code Flow with PKCE and operate without embedded client credentials (since code running in a user's browser cannot securely hide a secret).

* Always OIDC compliant: On (Default)
* Standard flow: On (Enables the Authorization Code Flow)
* Implicit flow: Off (Leave disabled as it is deprecated for security)
* Direct access grants: On (Helpful if you want to test authentication via API tools like Postman using raw usernames/passwords)
* Client authentication: Off (Turning this Off makes it a Public Client, which is required for browser-based JavaScript apps so they don't leak a client secret)
* Click Next.

## 3. Login Settings
This tells Keycloak exactly where it is allowed to redirect users back to after a successful login or logout attempt.
(Assuming your UI application runs locally on port 3000 during development, adjust this port if you use 5173 for Vite, 4200 for Angular, etc.)

* Root URL: http://localhost:3000
* Valid redirect URIs: http://localhost:3000/* (The * wildcard permits routing to any sub-path inside your app after logging in)
* Valid post logout redirect URIs: http://localhost:3000 (Where to send the browser after logging out)
* Web Origins: + (Typing a plus sign is a special shortcut that automatically syncs your CORS configuration to match the domains allowed in your Valid Redirect URIs, preventing browser blocking)
* Click Save.

------------------------------
## Step 3: Map Realm Roles to the Token (OIDC Compliance)
By default, Keycloak places user roles inside a complex namespace in the issued JSON Web Token (JWT). To make it easy for your frontend app (ui-client) to read whether a user is an admin or a standard user, we will map the realm roles directly into a clean array in the token.

   1. While still inside your newly created ui-client workspace, click on the Client scopes tab across the top row.
   2. Click on the scope named ui-client-dedicated (this is a scope automatically dedicated specifically to your client).
   3. Click the Configure a new mapper button (or click Add mapper -> By configuration).
   4. Select User Realm Role from the list of available mapper templates.
   5. Configure the mapper values exactly like this:
   * Name: realm roles mapper
      * Token Claim Name: roles (This dictates exactly where the roles array appears in your JWT)
      * Claim JSON Type: String
      * Add to ID token: On (Allows your UI frontend to read roles immediately upon login)
      * Add to access token: On (Allows your UI frontend to pass this token to your backend API to protect endpoints)
      * Add to userinfo: On
   6. Click Save.


------------------------------
## Phase 3: Create Roles (admin and user)
Before creating users, we need to establish the roles they will be assigned. We will create these as Realm Roles so they apply globally across this specific realm. 

   1. In the left navigation menu under Realm info, click Realm roles.
   2. Click Create role.
   3. In the Role name field, enter: admin
   4. (Optional) Add a description like "Administrative user with full access".
   5. Click Save.
   6. Click Realm roles in the left menu again to go back, and click Create role once more.
   7. In the Role name field, enter: user
   8. Click Save. 

------------------------------
## Phase 4: Create Users and Assign Roles
Now we will create the requested users and link them to their respective roles.
## Category A: Creating Admin Users

   1. In the left navigation menu, click Users. 
   2. Click Add user.
   3. Enter a Username (e.g., tm_admin1). Fill out email/first/last names if desired. 
   4. Click Join Groups / scroll down and click Create. 
   5. Set a Password:
   * Go to the Credentials tab at the top of the user profile.
      * Click Set password.
      * Enter a password (e.g., Password123!).
      * Toggle Temporary to Off (so you aren't forced to change it on first login).
      * Click Save, then confirm by clicking Save password. 
   6. Assign the Admin Role:
   * Go to the Role mapping tab at the top of the user profile.
      * Click Assign role.
      * Select the admin checkbox from the list.
      * Click Assign. 
   
(Repeat these steps for any additional admin users, e.g., tm_admin2)
## Category B: Creating Standard Users

   1. Click Users in the left navigation menu to return to the user list. [33] 
   2. Click Add user.
   3. Enter a Username (e.g., tm_user1) and click Create. [34, 35, 36] 
   4. Set a Password:
   * Go to the Credentials tab.
      * Click Set password.
      * Enter a password (e.g., UserPassword123!).
      * Toggle Temporary to Off.
      * Click Save -> Save password. [37, 38, 39] 
   5. Assign the User Role:
   * Go to the Role mapping tab.
      * Click Assign role.
      * Select the user checkbox from the list.
      * Click Assign. [40] 
   
(Repeat these steps for any additional regular users, e.g., tm_user2)
------------------------------
## Verification Checklist
To test and confirm that your environment is fully configured, try authenticating against your new realm:

| Action | Navigation / Endpoint | Expected Result |
|---|---|---|
| Verify Account Console | Go to http://localhost:8080/realms/ticketmaster/account/ | Opens the login page specifically branded for the ticketmaster realm. |
| Test Standard User | Log in with user tm_user1 credentials | Successfully logs into the personal account profile page. |
| Inspect Token Layout | Look up your realm configuration JSON | Validates that your client applications will read admin or user from the OpenID Connect token payloads. |

------------------------------
## Testing and Verification Layout
To inspect your configuration and verify that everything is working flawlessly without having to write a single line of application code yet, use Keycloak's testing endpoint:

| Verification Metric | Target Endpoint URL | Action / Expected Outcome |
|---|---|---|
| Discover OIDC Config | http://localhost:8082/realms/ticketmaster/.well-known/openid-configuration | Open this in your browser. It yields a JSON configuration payload listing all authorization, token, and logout endpoints your frontend library needs to initiate the login handshake. |
| Evaluate Client Settings | Check Clients -> ui-client in Admin Console | Ensure Client Authentication is set to Off, and Web Origins displays + or your specific frontend domain. |

