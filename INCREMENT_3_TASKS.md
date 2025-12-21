# Increment 3: Daily Progress Reports (DPR)

**Goal:** Enable site engineers to submit daily work reports including labor strength, work descriptions, and site photos, ensuring digitized field operations.

## Step 1: Database Layer (DPR)
- [ ] Create `DailyReport` Entity.
    - Fields: `id`, `projectId`, `userId`, `date` (timestamp), `laborCount`, `workDescription`, `hindrances`, `imagePath` (local URI), `isSynced`.
- [ ] Create `DailyReportDao`.
- [ ] Update `AppDatabase` (Increment Version to 6).

## Step 2: DPR List & Entry Point
- [ ] Create `DprListActivity` to show past reports.
- [ ] Add "Daily Report" button to `MainActivity` (Dashboard).
- [ ] Create `DprAdapter` for the RecyclerView.

## Step 3: DPR Creation Form (Basic)
- [ ] Create `CreateDprActivity`.
- [ ] Design Layout: Inputs for Labor Count, Work Description, Hindrances.
- [ ] Implement "Save Report" logic (Save to Room DB).

## Step 4: Camera Integration (CameraX)
- [ ] Add Camera permissions to Manifest.
- [ ] Implement CameraX in `CreateDprActivity` (or a fragment).
- [ ] Capture photo -> Save to internal storage -> Store file path in `DailyReport` entity.

## Step 5: Cloud Sync (Images & Data)
- [ ] Create `ImageUploadWorker` (or update SyncWorker) to:
    1. Upload image to Firebase Storage.
    2. Get Download URL.
    3. Push Report Data (with Image URL) to Firestore.
- [ ] Handle offline queuing for images.

## Step 6: Testing
- [ ] Verify Local Save (Offline).
- [ ] Verify Camera Capture.
- [ ] Verify Sync (Data + Image) when online.

---

# Future Roadmap

### Increment 4: Material Management
*Aligns with: Leakage Reduction & Seamless Coordination*
- [ ] Material Indent/Request Form.
- [ ] Approval workflow (Office team).
- [ ] Material Receipt & Stock Updates.

### Increment 5: Invoicing & Reporting
*Aligns with: Real-time Visibility*
- [ ] Generate basic PDF reports.
- [ ] Billing dashboards.
