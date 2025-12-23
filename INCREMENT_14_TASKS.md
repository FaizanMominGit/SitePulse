# Increment 14: Invoice PDF Generation & Sharing

## Goal
Complete the invoicing module by allowing managers to generate professional, GST-compliant PDF invoices and share them with clients.

## Key Changes
1.  **InvoiceDao Updates**:
    - Added `getInvoiceById(String id)` to fetch individual invoice details.

2.  **PDF Generation (PdfGenerator.java)**:
    - Implemented `generateInvoicePdf()` method using `itext7`.
    - Created a professional layout including:
        - Invoice Header (Number, Date).
        - Client and Project details.
        - Itemized list (Description, Amount).
        - Financial breakdown (Subtotal, GST Calculation, Total Amount).

3.  **UI Implementation**:
    - Created `InvoiceDetailActivity` to view invoice summary.
    - Added "Share Invoice PDF" button.
    - Designed layout in `activity_invoice_detail.xml`.

4.  **Sharing Functionality**:
    - Integrated Android `FileProvider` to securely share generated PDFs.
    - Configured `file_paths.xml` to allow sharing from the `Documents` directory.
    - Implemented Intent chooser for sharing via Email, WhatsApp, etc.

5.  **Logic Updates**:
    - Automatically updates Invoice Status to "SENT" upon successful PDF generation/sharing.
    - Triggers sync flag (`isSynced = false`) to update status on the server.

## Files Created/Modified
- `app/src/main/java/com/example/sitepulse/data/local/dao/InvoiceDao.java` (Modified)
- `app/src/main/java/com/example/sitepulse/util/PdfGenerator.java` (Modified)
- `app/src/main/java/com/example/sitepulse/InvoiceListActivity.java` (Modified)
- `app/src/main/java/com/example/sitepulse/InvoiceDetailActivity.java` (Created)
- `app/src/main/res/layout/activity_invoice_detail.xml` (Created)
- `app/src/main/res/xml/file_paths.xml` (Modified)
- `app/src/main/AndroidManifest.xml` (Modified)

## Testing Status
- **PDF Generation**: Verified PDF is created in `Documents` folder.
- **Sharing**: Verified sharing intent launches correctly.
- **Status Update**: Confirmed invoice status changes to "SENT" after sharing.
- **Bug Fix**: Resolved `FileProvider` path error by adding `Documents` to `file_paths.xml`.
