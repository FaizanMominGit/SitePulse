# Increment 12: Manager-Led Task Management

**Goal:** To enable managers to create, assign, and monitor tasks for specific projects, and for engineers to see and update the status of those tasks in real-time.

## Step 1: Create a Project Detail View for Managers
- [x] Create a new activity `ProjectDetailActivity.java` for managers.
- [x] Create a corresponding layout `activity_project_detail.xml` that includes project information and a `RecyclerView` for tasks.
- [x] Add a `FloatingActionButton` to this layout for creating new tasks.
- [x] Update `ManagerProjectListActivity` so that clicking on a project opens `ProjectDetailActivity`, passing the `PROJECT_ID`.

## Step 2: Implement Task Creation UI/Logic for Managers
- [x] Create a new activity `CreateTaskActivity.java`.
- [x] Create its layout `activity_create_task.xml` with fields for task title and description.
- [x] When a manager clicks the "Add Task" FAB in `ProjectDetailActivity`, it should open `CreateTaskActivity`, passing the `PROJECT_ID`.
- [x] Implement the logic in `CreateTaskActivity` to save the new `Task` to the local Room database.

## Step 3: Implement Task Synchronization
- [x] Verify the `Task` entity has a no-argument constructor for Firestore deserialization.
- [x] Create a `syncTasks()` method in `SyncRepository` to fetch all tasks from Firestore.
- [x] Add a real-time snapshot listener for the "tasks" collection in `SyncRepository` to ensure both engineers and managers see updates instantly.
- [x] Ensure the `SyncWorker` (or immediate sync triggers) uploads new/updated tasks from the local database to Firestore.

## Step 4: Integrate and Display Tasks
- [x] In the manager's `ProjectDetailActivity`, use a `ViewModel` and `LiveData` to observe and display the list of tasks for the selected project.
- [x] In the engineer's `MainActivity`, ensure the existing logic correctly displays the tasks assigned to the currently selected project.
- [x] Ensure the engineer's ability to check/uncheck a task correctly updates the `isCompleted` status and syncs it.

## Step 5: Add New Activities to Manifest
- [x] Add `ProjectDetailActivity` and `CreateTaskActivity` to the `AndroidManifest.xml` file.

## Step 6: Testing
- [x] Log in as a Manager, navigate to a project, and create a new task.
- [x] Log in as an Engineer assigned to that project. Verify the new task appears on the dashboard.
- [x] As the Engineer, mark the task as complete.
- [x] As the Manager, verify the task's status is updated to "complete" in the `ProjectDetailActivity` view.
