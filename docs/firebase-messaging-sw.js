importScripts("https://www.gstatic.com/firebasejs/11.0.0/firebase-app-compat.js");
importScripts("https://www.gstatic.com/firebasejs/11.0.0/firebase-messaging-compat.js");

firebase.initializeApp({
    apiKey: "AIzaSyB9Gg2coAJOR81TbQH7jE7rK2FSbr-4Vys",
    authDomain: "unilabs-notifications.firebaseapp.com",
    projectId: "unilabs-notifications",
    storageBucket: "unilabs-notifications.firebasestorage.app",
    messagingSenderId: "908485781504",
    appId: "1:908485781504:web:aabcfd46751ea7d2bbd0e5"
});

const messaging = firebase.messaging();

messaging.onBackgroundMessage(function(payload) {
    const { title, body } = payload.notification;
    self.registration.showNotification(title, { body });
});
