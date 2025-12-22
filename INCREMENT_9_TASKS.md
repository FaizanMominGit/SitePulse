# Increment 9: On-Launch Data Refresh

**Goal:** To ensure key activities always display the most current data by triggering a targeted cloud sync upon launch whenever an internet connection is available. This complements the on-login and real-time syncs.

## Step 1: Enhance SyncRepository
- [x] Expose granular public sync methods in `SyncRepository` (e.g., `public void syncProjects(SyncCallback callback)`).
- [x] Each method will sync a specific data type (Projects, Users, DPRs) and notify completion via the callback.

## Step 2: Create a Network Utility
- [x] Create a `NetworkUtils.java` class with a static method `isNetworkAvailable(Context context)`.
- [x] This utility will check for an active internet connection (Wi-Fi or Mobile Data).

## Step 3: Implement On-Launch Sync in Project-Related Activities
- [x] In `ManagerProjectListActivity`, on `onCreate`, check for network availability.
- [x] If online, call `syncRepository.syncProjects()` and show a subtle loading indicator.
- [x] In `MainActivity`, do the same to refresh the engineer's assigned project.

## Step 4: Implement On-Launch Sync in DPR-Related Activities
- [x] In `DprListActivity` and `ManagerDprListActivity`, on `onCreate`, check for network.
- [x] If online, call `syncRepository.syncDprs()` to fetch the latest reports.

## Step 5: Implement On-Launch Sync for Attendance
- [x] In `AttendanceActivity`, on `onCreate`, check for network.
- [x] If online, call a new `syncRepository.syncAttendanceForUser(userId)` method to get the latest attendance records for that specific user.

## Step 6: Testing
- [ ] With the app running, use another device or the Firebase Console to modify a project name.
- [ ] Navigate to `ManagerProjectListActivity` and verify the name updates after a brief moment.
- [ ] Repeat similar tests for DPRs and Attendance to ensure on-launch syncing works across all modules.
