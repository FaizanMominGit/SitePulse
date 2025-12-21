# Project: SitePulse - Construction Field Management Application

## Business Requirements
- **Digitized Field Operations:** Enable digitized and standardized field operations across projects, DPRs, attendance, materials, and invoicing in a single unified system.
- **Real-time Visibility:** Provide real-time visibility into project progress, workforce utilization, material consumption, and billing through dashboards and reports.
- **Seamless Coordination:** Ensure seamless coordination between site and office teams via structured workflows for approvals, updates, and communication.
- **Leakage Reduction:** Reduce labor and material leakages by tightly tracking attendance, daily work, and material requests with proper authorization and history.
- **Offline-First:** Support on-ground construction conditions with a mobile-first, low-bandwidth, and offline-capable experience for site users.

---

# Increment 1: The Core Foundation & Task Management (COMPLETED)
**Goal:** Establish the offline-first architecture, user authentication, and basic task management to support "Seamless Coordination" and "Offline-First" requirements.

- [x] **Step 1: Project Setup & Dependencies**
    - [x] Update `build.gradle` with Room, Firebase, WorkManager dependencies.
    - [x] Apply Google Services plugin.
    - [x] **User Action Required:** Add `google-services.json` to `app/` folder.

- [x] **Step 2: Database Layer (Room)**
    - [x] Create `Project` Entity (Table).
    - [x] Create `Task` Entity (Table).
    - [x] Create `AppDatabase` and DAOs.
    - [x] Initialize Database Instance.

- [x] **Step 3: Authentication (Firebase Auth)**
    - [x] Create `LoginActivity` UI.
    - [x] Create `SignupActivity` UI.
    - [x] Create `ForgotPasswordActivity` UI.
    - [x] Implement Firebase Auth logic.
        - [x] Send verification email on signup.
        - [x] Check for email verification on login.
    - [x] **Task:** Store User data (Name) in Firestore and Local DB on Signup.
    - [x] Handle Offline Login state (Check if user is already logged in).

- [x] **Step 4: Dashboard & Task List**
    - [x] Create `DashboardActivity` (Using MainActivity).
    - [x] Create `TaskAdapter` for RecyclerView.
    - [x] Display dummy/local data for "My Projects" and "Tasks".
    - [x] **Offline Sync:** Implement WorkManager to sync local tasks to Firestore.

- [x] **Step 5: Testing**
    - [x] Verify Login works.
    - [x] Verify Data persists offline.
    - [x] Verify Email Verification flow.
    - [x] Verify Offline Sync (Turn off internet, add task, turn on internet, check Firestore).

---

# See `INCREMENT_2_TASKS.md` for next steps.
