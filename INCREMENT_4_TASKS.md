# Increment 4: Material Management

**Goal:** Reduce material leakage and ensure seamless coordination by implementing a digital Material Indent/Request system with approval tracking.

## Step 1: Database Layer (Materials)
- [x] Create `MaterialRequest` Entity.
    - Fields: `id`, `projectId`, `userId`, `itemName` (e.g., Cement), `quantity`, `unit` (e.g., Bags), `urgency` (Low/Medium/High), `status` (PENDING/APPROVED/REJECTED), `date`, `isSynced`.
- [x] Create `MaterialRequestDao`.
- [x] Update `AppDatabase` (Increment Version to 7).

## Step 2: Material Request List UI
- [x] Create `MaterialListActivity`.
- [x] Add "Materials" button to `MainActivity` (Dashboard).
- [x] Create `MaterialAdapter` (Show status colors: Yellow=Pending, Green=Approved).

## Step 3: Create Request UI
- [x] Create `CreateMaterialActivity`.
- [x] Design Form: Item Name (Spinner/Edit), Quantity, Unit, Urgency (Radio/Spinner).
- [x] Implement Save Logic (Default status = PENDING).

## Step 4: Cloud Sync & Approvals
- [x] Update `SyncWorker` to upload Material Requests.
- [x] Create `material_requests` collection in Firestore.
- [ ] (Bonus) Add a simple logic to simulate approval (e.g., Long press to approve, for testing).

## Step 5: Testing
- [ ] Verify Request Creation.
- [ ] Verify List View & Status.
- [ ] Verify Cloud Sync.

---

# Future Roadmap

### Increment 5: Invoicing & Reporting
*Aligns with: Real-time Visibility*
- [ ] Generate basic PDF reports.
- [ ] Billing dashboards.
