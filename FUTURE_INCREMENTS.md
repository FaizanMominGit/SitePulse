# Future Increments Roadmap

Based on the completed increments and the original problem statement, here is the roadmap for the remaining features to fully realize the SitePulse vision.

## Increment 14: Invoice PDF Generation & Sharing
**Goal:** Complete the invoicing module by allowing managers to generate professional, GST-compliant PDF invoices and share them with clients.
- Implement PDF generation logic for Invoices (similar to DPRs but with financial layouts).
- Include tax breakdown (SGST/CGST/IGST) in the PDF.
- Add "Share" functionality (Intent to Email/WhatsApp).
- Update Invoice status to "SENT" automatically upon sharing.

## Increment 15: Manager Analytics Dashboard
**Goal:** Provide high-level visibility into project health, costs, and workforce as per business requirements.
- Add charts/graphs to `ManagerDashboardActivity`.
- **Attendance:** Show daily labor strength trends.
- **Financials:** Display Total Invoiced Amount vs. Pending Material Costs.
- **Progress:** Visualize Task completion rates per project.

## Increment 16: Push Notifications
**Goal:** Improve coordination between site and office by alerting users of important events in real-time.
- Integrate Firebase Cloud Messaging (FCM).
- **Scenarios:**
    - Engineer receives a notification when a Material Request is Approved/Rejected.
    - Engineer receives a notification when a new Task is assigned.
    - Manager receives a notification when a new DPR is submitted.

## Increment 17: User Profile & Settings
**Goal:** Allow users to manage their personal details and app preferences.
- Create `ProfileActivity` accessible from the dashboard.
- Allow editing of Name and Phone Number.
- Display App Version and User Role.
- Include a "Help & Support" section.

## Increment 18: Performance & Offline Optimization
**Goal:** Ensure the app remains fast and reliable even with growing data and poor connectivity.
- **Image Compression:** Compress DPR photos before saving/uploading to save bandwidth and storage.
- **Data Archiving:** Implement logic to archive completed projects or old reports to keep the local database query performance high.
- **Crash Reporting:** Integrate Crashlytics for stability monitoring.