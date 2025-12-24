# Increment 18: Performance & Offline Optimization

**Goal:** Ensure the app remains fast and reliable even with growing data and poor connectivity.

## Step 1: Image Compression
- [x] Create `ImageUtils` helper class.
- [x] Implement `compressImage(Context, Uri)` method to resize and compress bitmaps.
- [x] Integrate this into `CreateDprActivity` (before saving to DB/Firebase).

## Step 2: Data Archiving (Local DB Optimization)
- [x] Update `Project` entity to include `isArchived` boolean field.
- [x] Update `AppDatabase` version.
- [x] Update `ProjectDao` to filter out archived projects by default.
- [x] Create a "Archive Project" option in `ManagerProjectListActivity`.

## Step 3: Crash Reporting (Crashlytics)
- [x] Add Firebase Crashlytics dependency to `build.gradle` (if not already present).
- [x] Initialize Crashlytics in `MainActivity`.
- [x] Force a test crash (development only) to verify reporting.

## Step 4: Testing
- [x] Take a photo in DPR creation and verify file size is reduced.
- [x] Archive a project and verify it disappears from the main list.
- [x] Check Firebase Console for Crashlytics report (after forcing a crash).
