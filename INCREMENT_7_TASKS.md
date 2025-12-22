# Increment 7: Advanced Project Management (Manager)

**Goal:** Enable Managers to create and edit projects with detailed information (coordinates, radius) and assign Engineers to those projects.

## Step 1: Database Updates
- [x] Update `Project` entity (if needed) to store a list of assigned engineer IDs (or create a join table `ProjectEngineer`).
    - *Plan:* For simplicity in this prototype, we can store a list of engineer IDs as a JSON string or simpler, just create a separate table if we need M:N. For now, we will add an `assignedEngineerIds` string field to `Project` or just assume all engineers see all projects for now (or implement the assignment).
    - *Refined Plan:* Let's stick to adding an `assignedEngineerIds` field (comma separated string) to the `Project` entity for simplicity.
- [x] Update `UserDao` to allow fetching all users with role "Engineer" so the manager can select them.

## Step 2: Create/Edit Project UI
- [x] Create `CreateProjectActivity`.
- [x] Design layout (`activity_create_project.xml`):
    - Fields: Name, Location (Text), Description, Latitude, Longitude, Radius (Meters).
    - "Select Engineers" Multi-select list (Checkbox list or similar).
    - "Save Project" Button.

## Step 3: Engineer Selection Logic
- [x] In `CreateProjectActivity`, fetch all "Engineer" users from Firestore (or local DB if synced).
- [x] Display engineers in a `RecyclerView` with checkboxes.
- [x] Capture selected engineer IDs.

## Step 4: Saving Project Data
- [x] Implement logic to save the new Project to Firestore.
- [x] Save the list of assigned engineers within the project document.
- [x] Sync/Save to local Room database.

## Step 5: Updating Project List
- [x] Update `ManagerProjectListActivity` to open `CreateProjectActivity` when "Add" is clicked.
- [x] (Optional) Allow clicking a project in the list to "Edit" it (re-using `CreateProjectActivity` with pre-filled data).

## Step 6: Engineer's View (Filtering)
- [x] (Bonus/Next Step) Update `MainActivity` for engineers to only show projects they are assigned to.
