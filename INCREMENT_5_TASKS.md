# Increment 5: Invoicing & Reporting (PDF Generation)

**Goal:** Provide real-time visibility and standardized reporting by generating a basic PDF for a Daily Progress Report.

## Step 1: PDF Generation Setup
- [x] Add PDF generation library to `build.gradle` (e.g., `com.itextpdf:itext7-core`).
- [x] Sync Gradle dependencies.

## Step 2: UI Entry Point for PDF Download
- [x] Create `DprDetailActivity` to show a single report's details.
- [x] Add a "Download as PDF" button to the detail activity layout.
- [x] Update `DprListActivity` so clicking an item opens the detail view.

## Step 3: PDF Creation Logic
- [x] Create a `PdfGenerator` utility class.
- [x] Implement a method that takes a `DailyReport` object and formats it into a professional-looking PDF.
    - Include: Project Name, Date, Labor Count, Work Description, Hindrances, and the site photo.

## Step 4: File Handling & Permissions
- [x] Add `WRITE_EXTERNAL_STORAGE` permission for older Android versions (if needed, though scoped storage is preferred).
- [x] Implement logic in `DprDetailActivity` to save the generated PDF to the device's public "Downloads" folder.
- [x] Add logic to open the PDF file using an `Intent` after it's saved.

## Step 5: Testing
- [x] Verify that clicking the download button generates and saves a PDF.
- [x] Verify the PDF contains the correct data and image from the selected report.
- [x] Verify the PDF can be opened by a viewer app.
