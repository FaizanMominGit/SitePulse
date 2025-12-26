const {onDocumentUpdated, onDocumentWritten} = require("firebase-functions/v2/firestore");
const {initializeApp} = require("firebase-admin/app");
const {getFirestore} = require("firebase-admin/firestore");
const {getMessaging} = require("firebase-admin/messaging");

// Explicitly initialize the app to avoid ambiguity
initializeApp();

/**
 * Triggers when a document in 'material_requests' is updated.
 * Sends a notification to the user if their request status has changed.
 */
exports.onMaterialRequestUpdate = onDocumentUpdated({
    document: "material_requests/{requestId}",
    region: "asia-south1",
}, async (event) => {
    const beforeData = event.data.before.data();
    const afterData = event.data.after.data();

    if (beforeData.status === afterData.status) {
        console.log(`Status unchanged for request ${event.params.requestId}. No notification sent.`);
        return null;
    }

    const userId = afterData.userId;
    const itemName = afterData.itemName;
    const newStatus = afterData.status;

    const userDoc = await getFirestore().collection("users").doc(userId).get();
    if (!userDoc.exists) {
        console.error("User not found:", userId);
        return null;
    }

    const fcmToken = userDoc.data().fcmToken;
    if (!fcmToken) {
        console.log(`User ${userId} does not have an FCM token. No notification sent.`);
        return null;
    }

    const payload = {
        notification: {
            title: "Material Request Updated",
            body: `Your request for '${itemName}' has been ${newStatus}.`,
        },
        token: fcmToken, // Explicitly target the token
    };

    console.log(`Sending notification to user ${userId} with token ${fcmToken}`);
    return getMessaging().send(payload);
});

/**
 * Triggers when a project is created or updated.
 * Sends a notification to any engineer who has been newly assigned.
 */
exports.onProjectWrite = onDocumentWritten({
    document: "projects/{projectId}",
    region: "asia-south1",
}, async (event) => {
    if (!event.data.after.exists) {
        return null;
    }

    const projectName = event.data.after.data().name;

    const beforeData = event.data.before.exists ? event.data.before.data() : { assignedEngineerIds: "" };
    const afterData = event.data.after.data();

    const beforeIds = new Set(beforeData.assignedEngineerIds ? beforeData.assignedEngineerIds.split(",") : []);
    const afterIds = new Set(afterData.assignedEngineerIds ? afterData.assignedEngineerIds.split(",") : []);

    const newEngineerIds = [...afterIds].filter((id) => id && !beforeIds.has(id));

    if (newEngineerIds.length === 0) {
        console.log(`No new engineers assigned to project ${projectName}.`);
        return null;
    }

    const notificationPromises = newEngineerIds.map(async (userId) => {
        const userDoc = await getFirestore().collection("users").doc(userId).get();
        if (!userDoc.exists) return;

        const fcmToken = userDoc.data().fcmToken;
        if (!fcmToken) return;

        const payload = {
            notification: {
                title: "New Project Assignment",
                body: `You have been assigned to a new project: ${projectName}`,
            },
            token: fcmToken, // Explicitly target the token
        };

        console.log(`Sending assignment notification to user ${userId}`);
        return getMessaging().send(payload);
    });

    return Promise.all(notificationPromises);
});
