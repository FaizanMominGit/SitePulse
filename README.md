# SitePulse - Construction Field Management Application

**SitePulse** is a comprehensive, mobile-first Android application designed to digitize and standardize field operations for the construction industry in India. It empowers site engineers and managers by replacing fragmented communication (WhatsApp, phone calls, paper registers) with a unified, transparent, and offline-capable platform.

## 🚀 Key Features

*   **Offline-First Architecture**: Built with a robust local database (Room) synchronized with the cloud (Firebase Firestore), ensuring full functionality even in low-connectivity zones.
*   **Role-Based Access**: Dedicated interfaces for **Managers** (Administrative control, Analytics) and **Site Engineers** (Field reporting, Tasks).
*   **Project & Task Management**: Create projects, assign engineers, and track task progress in real-time.
*   **Daily Progress Reports (DPR)**: Submit daily reports with labor strength, work descriptions, and compressed site photos. Automatically generates professional PDFs.
*   **GPS-Geofenced Attendance**: Prevents labor leakage by ensuring engineers can only clock in/out when physically present at the site coordinates.
*   **Material Management**: Structured workflow for raising material requests and a Manager approval system to control procurement costs.
*   **GST-Ready Invoicing**: Create and manage invoices with automatic tax calculations. Generate and share professional PDF invoices directly from the app.
*   **Real-Time Analytics**: Manager dashboard with charts visualizing labor trends, financial overviews, and task completion rates.
*   **Push Notifications**: Instant alerts for critical updates like material approvals, new task assignments, and report submissions.
*   **Performance Optimized**: Image compression and data archiving ensure the app remains fast and lightweight.

## 🛠️ Tech Stack

*   **Language**: Java
*   **Architecture**: MVVM (Model-View-ViewModel)
*   **Local Database**: Room Persistence Library (SQLite)
*   **Cloud Backend**: Firebase Firestore, Firebase Authentication, Firebase Storage
*   **Notifications**: Firebase Cloud Messaging (FCM)
*   **Crash Reporting**: Firebase Crashlytics
*   **Background Tasks**: WorkManager
*   **PDF Generation**: iText7
*   **Charting**: MPAndroidChart
*   **Image Loading**: Glide

## 📱 User Roles

### **Manager**
*   Create and manage Projects (with Geofencing details).
*   Assign Engineers to projects.
*   View Analytics Dashboard (Costs, Labor, Progress).
*   Approve/Reject Material Requests.
*   Generate and Share Invoices.
*   Archive completed projects.

### **Site Engineer**
*   View assigned projects and tasks.
*   Mark GPS-verified Attendance (Clock In/Out).
*   Submit Daily Progress Reports (DPR) with photos.
*   Raise Material Requests.
*   Update Task status.

## 📂 Project Structure

The project follows standard Android directory structure:
*   `data/local`: Room Database, Entities, and DAOs.
*   `data/repository`: Repository pattern handling data synchronization between Local DB and Firestore.
*   `ui`: Activities, Adapters, and ViewModels.
*   `util`: Helper classes for PDF generation, Image compression, Network checks, etc.
*   `worker`: WorkManager classes for background synchronization.

## 📥 Installation

1.  Clone the repository.
2.  Open in **Android Studio**.
3.  Add your `google-services.json` file to the `app/` directory (Required for Firebase).
4.  Sync Gradle and Run on an Emulator or Physical Device.

## 📜 License

This project is created as a solution for a construction management problem statement.
