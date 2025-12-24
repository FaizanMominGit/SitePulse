# Increment 17: User Profile & Settings

**Goal:** Allow users to manage their personal details and app preferences.

## Step 1: Data Model Update
- [x] Update `User` entity to include `phoneNumber` field.
- [x] Update `AppDatabase` version.

## Step 2: Create Profile UI
- [x] Create `ProfileActivity.java`.
- [x] Create `activity_profile.xml`.
- [x] Design UI with fields for Name, Email (Read-only), Phone Number, and Role (Read-only).
- [x] Add "Save" button and "Logout" button.
- [x] Display App Version at the bottom.

## Step 3: Implement Logic
- [x] Fetch current user data from `AppDatabase` (or Firestore) and populate fields.
- [x] Implement "Save" logic to update `phoneNumber` and `name` in both Room and Firestore.
- [x] Ensure sync works for profile updates.

## Step 4: Navigation
- [x] Add "Profile" icon/button to `MainActivity` (Top AppBar or Menu) for Engineers.
- [x] Add "Profile" button to `ManagerDashboardActivity` for Managers.

## Step 5: Manifest Registration
- [x] Register `ProfileActivity` in `AndroidManifest.xml`.

## Step 6: Testing
- [ ] Open Profile screen.
- [ ] Edit Name and Phone Number.
- [ ] Save and verify changes persist after app restart.
