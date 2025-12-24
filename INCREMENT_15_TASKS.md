# Increment 15: Manager Analytics Dashboard

## Goal
Provide high-level visibility into project health, costs, and workforce as per business requirements. The dashboard will now include visual charts and key metrics.

## Key Changes

### 1. Dependencies
- Add **MPAndroidChart** library to `build.gradle` for rendering graphs.
  ```gradle
  implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
  ```

### 2. Database (DAO) Updates
- **DailyReportDao**: Add query to get labor count for the last 7 days.
  - `SELECT date, laborCount FROM daily_reports WHERE projectId = :projectId ORDER BY date ASC LIMIT 7`
- **InvoiceDao**: Add query to get total invoiced amount for a project.
  - `SELECT SUM(totalAmount) FROM invoices WHERE projectId = :projectId`
- **MaterialRequestDao**: Add query to get estimated material costs (pending/approved).
  - `SELECT SUM(estimatedCost) FROM material_requests WHERE projectId = :projectId`
- **TaskDao**: Add query to get task completion status counts.
  - `SELECT status, COUNT(*) FROM tasks WHERE projectId = :projectId GROUP BY status`

### 3. Manager Dashboard UI (`ManagerDashboardActivity`)
- Replace the simple button list with a **Dashboard Layout**.
- **Top Section**: Project Selector (Spinner).
- **Cards Section**:
    - **Total Invoiced**: Text view showing total revenue.
    - **Pending Material Cost**: Text view showing potential expenses.
- **Charts Section**:
    - **Labor Trend**: Line Chart showing labor count over the last week.
    - **Task Progress**: Pie Chart showing Completed vs. Pending tasks.

### 4. Logic Implementation
- Fetch data asynchronously using `AppDatabase` executors.
- Aggregate data and populate charts using `MPAndroidChart` APIs.
- Ensure charts handle empty states (e.g., "No Data Available").

## Files to Create/Modify
- `app/build.gradle` (Add Dependency)
- `app/src/main/java/com/example/sitepulse/data/local/dao/DailyReportDao.java` (Modify)
- `app/src/main/java/com/example/sitepulse/data/local/dao/InvoiceDao.java` (Modify)
- `app/src/main/java/com/example/sitepulse/data/local/dao/MaterialRequestDao.java` (Modify)
- `app/src/main/java/com/example/sitepulse/data/local/dao/TaskDao.java` (Modify)
- `app/src/main/res/layout/activity_manager_dashboard.xml` (Modify UI)
- `app/src/main/java/com/example/sitepulse/ManagerDashboardActivity.java` (Implement Logic)

## Testing Plan
1.  **Dependency**: Sync Gradle and ensure no build errors.
2.  **Data Fetching**: Verify correct sums and counts are returned from Room.
3.  **Visualization**:
    - Check if Line Chart correctly plots labor data.
    - Check if Pie Chart accurately reflects task status ratios.
4.  **Project Switch**: Verify dashboard updates when a different project is selected.
