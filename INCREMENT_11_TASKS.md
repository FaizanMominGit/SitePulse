# Increment 11: Multi-Project Switching for Engineers

**Goal:** To allow engineers who are assigned to multiple projects to easily switch between them from their main dashboard, ensuring all actions (like creating DPRs or material requests) are contextually linked to the selected project.

## Step 1: Update the Engineer Dashboard UI
- [x] Modify `activity_main.xml`.
- [x] Replace the static `TextView` for the project name with a `Spinner` (dropdown menu).
- [x] This `Spinner` will be used to list all projects assigned to the logged-in engineer.

## Step 2: Implement Project Selection Logic
- [x] In `MainActivity.java`, populate the `Spinner` with the list of projects assigned to the user from the local database.
- [x] Set up an `OnItemSelectedListener` for the `Spinner`.
- [x] When an engineer selects a project from the dropdown, update the dashboard UI (project details, tasks, etc.) to reflect the newly selected project.

## Step 3: Scope Data to the Selected Project
- [x] Update the `TaskDao` to include a method `getTasksForProject(String projectId)`. (Already existed)
- [x] Modify `MainActivity.java` to load tasks based on the `projectId` of the project selected in the `Spinner`.
- [x] Ensure that when creating new DPRs or Material Requests from the dashboard, the `projectId` of the currently selected project is passed to the corresponding creation activity.

## Step 4: Update Child Activities to Use Passed Project ID
- [x] Refactor `DprListActivity`, `MaterialListActivity`, and `AttendanceActivity`.
- [x] These activities should now receive the `projectId` from the `Intent` extras instead of fetching the first project from the database.

## Step 5: Testing
- [ ] Log in as a Manager.
- [ ] Create at least two projects and assign them to a single engineer.
- [ ] Log in as that engineer.
- [ ] Verify the dropdown on the dashboard lists both assigned projects.
- [ ] Switch between projects and confirm that the project details and task list update correctly.
- [ ] Create a new DPR for one project and a new Material Request for the other. Verify they are associated with the correct projects in the database (and on the Manager's side).
