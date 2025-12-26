package com.example.sitepulse.util;

import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class NotificationTrigger {

    private static final String TAG = "NotificationTrigger";

    /**
     * Finds a user's FCM token and logs the intent to send a notification.
     * In a production app, this would be replaced by a call to a Cloud Function.
     *
     * @param targetUserId The ID of the user to notify.
     * @param title        The title of the notification.
     * @param body         The message body of the notification.
     */
    public static void sendNotification(String targetUserId, String title, String body) {
        if (targetUserId == null || targetUserId.isEmpty()) {
            return;
        }

        FirebaseFirestore.getInstance().collection("users").document(targetUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fcmToken = documentSnapshot.getString("fcmToken");
                        if (fcmToken != null && !fcmToken.isEmpty()) {
                            // SIMULATION: In a real app, you would trigger a Cloud Function here
                            // that uses the FCM Admin SDK to send a message to this token.
                            Log.d(TAG, "SUCCESS: Would send notification to user: " + targetUserId);
                            Log.d(TAG, "         FCM Token: " + fcmToken);
                            Log.d(TAG, "         Title: " + title);
                            Log.d(TAG, "         Body: " + body);
                        } else {
                            // Explicitly log the failure reason
                            Log.e(TAG, "FAILURE: Cannot send notification. User " + targetUserId + " does not have an FCM token registered in Firestore.");
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "FAILURE: Could not fetch user document to get FCM token for user: " + targetUserId, e));
    }
}