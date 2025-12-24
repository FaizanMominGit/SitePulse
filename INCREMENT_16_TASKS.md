# Increment 16: Push Notifications

## Goal
Improve coordination between site and office by alerting users of important events in real-time using Firebase Cloud Messaging (FCM).

## Key Changes

### 1. Dependencies
- [x] Add Firebase Messaging dependency to `build.gradle` (Already added).
- [x] Ensure `google-services.json` is present.

### 2. Firebase Cloud Messaging Service
- [x] Create `MyFirebaseMessagingService.java` extending `FirebaseMessagingService`.
- [x] Override `onMessageReceived` to handle incoming messages and show system notifications.
- [x] Override `onNewToken` to log or update the device token.

### 3. User Token Management
- [x] Update `User` entity to include a `fcmToken` field.
- [x] Update `AppDatabase` version.
- [x] Update `UserDao` to allow updating the token.
- [x] In `MainActivity` (or `LoginActivity`), fetch the current FCM token and update it in Firestore/Room for the logged-in user.

### 4. Notification Logic (Sender Side - Cloud Functions Simulation)
*Note: In a production app, notifications are typically triggered by Cloud Functions. Since we are doing a client-side prototype:*
- **Simplified Approach for Prototype**: We will implement the *Receiving* logic fully. Sending usually requires a server key which shouldn't be in the app, or Cloud Functions.
- **Alternative**: We will focus on the **Client-Side Implementation**:
    - Service Setup.
    - Token Registration.
    - Handling Foreground/Background messages.
    - Showing the Notification UI.
- *Testing*: We will use the Firebase Console "Compose Notification" tool to test sending messages to the device.

### 5. Notification Channels
- [x] Create a `NotificationHelper` class to create Android Notification Channels (required for Android O+).
    - Channel IDs: `project_updates`, `approvals`, `general`.

## Files to Create/Modify
- [x] `app/src/main/java/com/example/sitepulse/service/MyFirebaseMessagingService.java` (Create)
- [x] `app/src/main/java/com/example/sitepulse/util/NotificationHelper.java` (Create)
- [x] `app/src/main/java/com/example/sitepulse/MainActivity.java` (Modify to init channels & get token)
- [x] `app/src/main/java/com/example/sitepulse/data/local/entity/User.java` (Modify)
- [x] `app/src/main/AndroidManifest.xml` (Modify to register service)

## Testing Plan
- [x] **Service Registration**: Run the app and check Logcat for "FCM Token".
- [x] **Console Test**: Go to Firebase Console > Messaging > New Campaign.
- [x] **Send Notification**: Target the app package or paste the FCM Token from Logcat.
- [x] **Verify**:
    - **Foreground**: Check if `onMessageReceived` triggers and shows a notification (or custom UI).
    - **Background**: Check if the system tray shows the notification.
    - **Click Action**: Tap the notification and ensure it opens the app.
