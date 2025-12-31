const {onDocumentUpdated, onDocumentWritten, onDocumentCreated} = require("firebase-functions/v2/firestore");
const {initializeApp} = require("firebase-admin/app");
const {getFirestore} = require("firebase-admin/firestore");
const {getMessaging} = require("firebase-admin/messaging");

// Initialize the Admin SDK. The credentials will be automatically discovered.
initializeApp();

// --- NOTIFICATIONS TO ENGINEER ---

exports.onMaterialRequestUpdate = onDocumentUpdated({
    document: "material_requests/{requestId}",
    region: "asia-south1",
}, async (event) => {
    const beforeData = event.data.before.data();
    const afterData = event.data.after.data();
    if (beforeData.status === afterData.status) return null;

    const userId = afterData.userId;
    const userDoc = await getFirestore().collection("users").doc(userId).get();
    if (!userDoc.exists || !userDoc.data().fcmToken) return null;

    const payload = {
        notification: {
            title: "Material Request Updated",
            body: `Your request for '${afterData.itemName}' has been ${afterData.status}.`,
        },
        token: userDoc.data().fcmToken,
    };

    console.log(`Sending status update notification to user ${userId}`);
    return getMessaging().send(payload);
});

exports.onProjectWrite = onDocumentWritten({
    document: "projects/{projectId}",
    region: "asia-south1",
}, async (event) => {
    if (!event.data.after.exists) return null;
    const projectName = event.data.after.data().name;
    const beforeData = event.data.before.exists ? event.data.before.data() : { assignedEngineerIds: "" };
    const afterData = event.data.after.data();
    const beforeIds = new Set(beforeData.assignedEngineerIds ? beforeData.assignedEngineerIds.split(",") : []);
    const afterIds = new Set(afterData.assignedEngineerIds ? afterData.assignedEngineerIds.split(",") : []);
    const newEngineerIds = [...afterIds].filter((id) => id && !beforeIds.has(id));

    if (newEngineerIds.length === 0) return null;

    const promises = newEngineerIds.map(async (userId) => {
        const userDoc = await getFirestore().collection("users").doc(userId).get();
        if (!userDoc.exists || !userDoc.data().fcmToken) return;

        const payload = {
            notification: {
                title: "New Project Assignment",
                body: `You have been assigned to a new project: ${projectName}`,
            },
            token: userDoc.data().fcmToken,
        };
        console.log(`Sending assignment notification to user ${userId}`);
        return getMessaging().send(payload);
    });

    return Promise.all(promises);
});


// --- NOTIFICATIONS TO MANAGER ---

exports.onMaterialRequestCreated = onDocumentCreated({
    document: "material_requests/{requestId}",
    region: "asia-south1",
}, async (event) => {
    const itemName = event.data.data().itemName;
    const managersSnapshot = await getFirestore().collection("users").where("role", "==", "Manager").get();
    if (managersSnapshot.empty) return null;

    const promises = managersSnapshot.docs.map(async (doc) => {
        const fcmToken = doc.data().fcmToken;
        if (!fcmToken) return;

        const payload = {
            notification: {
                title: "New Material Request",
                body: `A new request for '${itemName}' has been submitted.`,
            },
            token: fcmToken,
        };
        console.log(`Sending new material request notification to manager ${doc.id}`);
        return getMessaging().send(payload);
    });

    return Promise.all(promises);
});

exports.onDprCreated = onDocumentCreated({
    document: "daily_reports/{reportId}",
    region: "asia-south1",
}, async (event) => {
    const managersSnapshot = await getFirestore().collection("users").where("role", "==", "Manager").get();
    if (managersSnapshot.empty) return null;

    const promises = managersSnapshot.docs.map(async (doc) => {
        const fcmToken = doc.data().fcmToken;
        if (!fcmToken) return;

        const payload = {
            notification: {
                title: "New Daily Progress Report (DPR)",
                body: "A new DPR has been submitted for review.",
            },
            token: fcmToken,
        };
        console.log(`Sending new DPR notification to manager ${doc.id}`);
        return getMessaging().send(payload);
    });

    return Promise.all(promises);
});
