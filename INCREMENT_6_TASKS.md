# Increment 6: Admin/Manager Role Implementation

**Goal:** Implement a role-based access control system where users can sign up as "Manager" or "Engineer". Managers will have administrative control over the application's data and user management.

## Step 1: Database & Model Updates
- [x] Update `User` entity to support different roles (`Engineer` vs `Manager`).
- [x] Ensure Firestore user document structure includes a `role` field.
- [ ] Create a `Manager` specific entity or extended profile if additional fields are required (e.g., specific permissions).

## Step 2: Authentication & Signup Flow Updates
- [x] Modify `SignupActivity` layout to include a Role selection (Spinner or RadioButtons for "Engineer" / "Manager").
    - *Note: In a real-world app, "Manager" signup might be restricted or require approval. For this prototype, we will allow self-selection.*
- [x] Update `createUser` logic in `SignupActivity` to save the selected role to Firestore and local Room database.

## Step 3: Login & Role-Based Navigation
- [x] Update `LoginActivity` to fetch the user's role from Firestore (or local DB) upon successful login.
- [x] Create a `ManagerDashboardActivity` for the Manager's main view.
- [x] Implement redirection logic in `LoginActivity`:
    - If `role == "Manager"` -> Navigate to `ManagerDashboardActivity`.
    - If `role == "Engineer"` -> Navigate to standard `MainActivity`.

## Step 4: Manager Dashboard Implementation
- [x] Design the `ManagerDashboardActivity` layout.
    - Features to include:
        - View all Projects (created by all engineers or assigned ones).
        - View all Daily Progress Reports (DPRs).
        - View all Material Requests.
- [x] Implement `ManagerProjectListActivity` to view/edit/delete projects.
- [x] Implement `ManagerDprListActivity` to view reports from all engineers.

## Step 5: Testing
- [ ] Test Signup as an "Engineer" and verify redirection to `MainActivity`.
- [ ] Test Signup as a "Manager" and verify redirection to `ManagerDashboardActivity`.
- [ ] Verify that a Manager can see data that might otherwise be restricted or see a broader view of the application data.
