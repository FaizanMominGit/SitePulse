# Increment 13: GST-Ready Invoicing System

**Goal:** To implement a digitized, GST-ready invoicing system that allows creation, management, and synchronization of invoices for construction projects.

## Step 1: Data Modeling and Local Database
- [x] Create the `Invoice` entity class with fields for invoice details (number, client, subtotal, GST, total, status) and a no-argument constructor.
- [x] Create the `InvoiceDao` interface for database operations.
- [x] Update `AppDatabase` to include the `Invoice` entity.

## Step 2: Repository and Synchronization
- [x] Update `SyncRepository` to include methods for syncing invoices (`syncInvoices`, `fetchInvoices`).
- [x] Implement real-time Firestore listeners for the "invoices" collection.

## Step 3: Invoice List UI
- [x] Create `InvoiceAdapter` to display invoices in a RecyclerView.
- [x] Create `InvoiceListActivity` to show the list of invoices for a specific project.
- [x] Add an "Invoices" button to the `ManagerDashboardActivity` (or Project Detail) and `MainActivity`.

## Step 4: Create Invoice UI
- [x] Create `CreateInvoiceActivity` with input fields for Client Name, Invoice Number, Items/Description, Subtotal, and GST Rate (%).
- [x] Implement logic to automatically calculate GST Amount and Total Amount.
- [x] Save the new invoice to the local database and trigger a sync.

## Step 5: Manifest and Configuration
- [x] Add the new activities (`InvoiceListActivity`, `CreateInvoiceActivity`) to `AndroidManifest.xml`.

## Step 6: Testing
- [ ] Create a new invoice as a Manager.
- [ ] Verify the GST and Total calculations are correct.
- [ ] Verify the invoice appears in the list.
- [ ] Verify the invoice is synced to Firestore.
