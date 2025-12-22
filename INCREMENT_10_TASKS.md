# Increment 10: Material Request Management (Manager)

**Goal:** To empower managers with the ability to review, approve, and reject material requests submitted by engineers, ensuring tight control over material procurement.

## Step 1: Create Manager-Side UI for Material Requests
- [x] Create a `ManagerMaterialListActivity.java` and a corresponding layout file `activity_manager_material_list.xml`.
- [x] This activity will display a list of all material requests from all projects and engineers.
- [x] Add a "Material Requests" button on the `ManagerDashboardActivity` to open this new activity.

## Step 2: Develop an Adapter for the Manager's View
- [x] Create a `ManagerMaterialAdapter.java` that extends `RecyclerView.Adapter`.
- [x] The adapter will display crucial request details: item name, quantity, requesting engineer, date, and status.
- [x] For each request with a "Pending" status, the layout should include "Approve" and "Reject" buttons.

## Step 3: Implement Approval/Rejection Logic
- [x] In `ManagerMaterialAdapter`, set up click listeners for the "Approve" and "Reject" buttons.
- [x] Create a new `updateRequestStatus` method in the `SyncRepository` that takes a `MaterialRequest` object and the new status string.
- [x] This method will update the `status` field of the corresponding document in the "material_requests" collection in Firestore.

## Step 4: Real-time Updates and Data Sync
- [x] In `ManagerMaterialListActivity`, use a `ViewModel` to observe `LiveData` of all material requests from the local Room database.
- [x] Ensure the `SyncRepository`'s real-time listeners for the "material_requests" collection are active so that changes made by the manager are automatically reflected in the local database and UI.
- [x] Implement an on-launch sync in `ManagerMaterialListActivity` by calling a `syncRepository.syncMaterialRequests()` method to fetch any requests that might have been missed.

## Step 5: Testing
- [x] As an Engineer, create and submit a new material request.
- [x] Log in as a Manager and navigate to the "Material Requests" screen.
- [x] Verify the new request is visible with a "Pending" status.
- [x] Click "Approve" on the request and confirm the UI updates to show "Approved". Check Firestore to verify the status change.
- [x] Log back in as the Engineer and verify that the status of their submitted request has been updated.
- [x] Repeat the process for rejecting a request.
- (ADD NEW FILES CREATED TO MAINIFEST) (IF THE RESPONSE GIVE KEEP THE REST OF THE METHOD AS IT KEPP IT AS IT IS DONT GIVE ME CODEE WTH COMMENT)(ENSURE THAT  THERE IS no-argument constructor SO IT SYNCS DATA ONLINE IF AVAILABLE ELSE THROUGH LOCAL STORAGE)
