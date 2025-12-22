# Increment 8: Robust Real-time & On-Demand Synchronization

**Goal:** Refactor the data layer to ensure data from Firestore is proactively fetched and synchronized with the local Room database. This will provide a reliable and up-to-date experience, especially on first login or when data changes on the server, fixing the "No Sites Allotted" issue on new devices.

## Step 1: Create a Central Sync Repository
- [x] Create a new class `SyncRepository.java`.
- [x] This class will be responsible for all interactions between Firestore and Room.
- [x] Add methods to the repository for each data type we need to sync, for example: `syncProjects()`, `syncUsers()`, `syncDprs()`.
- [x] The repository will take `AppDatabase` and `FirebaseFirestore` instances in its constructor.

## Step 2: Implement On-Demand Sync on Login
- [x] In `LoginActivity`, after a user successfully logs in and is verified (`checkRoleAndRedirect`), make a call to the new `SyncRepository` to trigger a full data sync.
- [x] For example: `syncRepository.syncAllData(userId)`.
- [x] The `syncAllData` method will call all the individual sync methods (`syncProjects`, `syncUsers`, etc.) to ensure the local database is fully populated *before* the user lands on their dashboard.

## Step 3: Refactor Activities to Use the Local Database First
- [x] Review key activities (`MainActivity`, `ManagerProjectListActivity`, `CreateProjectActivity`).
- [x] Modify them to **only** query the local Room database for data (using `LiveData` as is currently done).
- [x] Remove any direct Firestore fetch calls from the Activities (like the `fetchEngineersFromFirestore` we recently added to `CreateProjectActivity`), as this responsibility will now be handled by the `SyncRepository` on login.

## Step 4: Implement Real-time Listeners (Optional but Recommended)
- [x] In the `SyncRepository`, implement Firestore's `addSnapshotListener` for key collections (like `projects` and `users`).
- [x] When a listener detects a change on the server (e.g., a manager edits a project), it will automatically fetch the changes and update the local Room database.
- [x] This ensures the app stays up-to-date in real-time without needing a manual refresh or a new login.

## Step 5: Testing the New Sync Strategy
- [x] Log in as a Manager on one device.
- [x] Log in as an Engineer on a **separate device** (or after clearing app data).
- [x] As the Manager, create a new project and assign it to the Engineer.
- [x] Verify that the new project appears automatically on the Engineer's device (if real-time listeners are implemented) or after a re-login.
- [x] Verify the "No Sites Allotted" error is gone for new logins.
