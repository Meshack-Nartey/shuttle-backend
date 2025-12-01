// stomp-sub.js
// Usage:
//   node stomp-sub.js "<ACCESS_TOKEN>" <SHUTTLE_ID>

const { Client } = require('@stomp/stompjs');
const WebSocket = require('ws');

const ACCESS_TOKEN = process.argv[2];
const SHUTTLE_ID   = process.argv[3] || "1";

if (!ACCESS_TOKEN) {
    console.error("\n❌ Missing token.\n");
    console.error("Usage:");
    console.error('   node stomp-sub.js "<JWT_TOKEN>" <SHUTTLE_ID>');
    process.exit(1);
}

const WS_URL = "ws://localhost:8080/ws";

console.log("🔌 Connecting to:", WS_URL);

const client = new Client({
    webSocketFactory: () => new WebSocket(WS_URL),
    brokerURL: WS_URL,
    connectHeaders: {
        Authorization: `Bearer ${ACCESS_TOKEN}`
    },
    debug: (str) => console.log("DEBUG:", str),
    reconnectDelay: 5000,

    onConnect: () => {
        console.log("✔ STOMP CONNECTED");

        // 🔥 OPTION A IMPLEMENTED — CORRECT TOPIC
        const topic = `/topic/shuttle/${SHUTTLE_ID}/location`;

        console.log(`📡 Subscribing to ${topic}`);

        client.subscribe(topic, (msg) => {
            try {
                const data = JSON.parse(msg.body);
                console.log("📥 RECEIVED:", JSON.stringify(data, null, 2));
            } catch {
                console.log("📥 RAW:", msg.body);
            }
        });
    },

    onWebSocketError: (err) => {
        console.error("❌ WS ERROR:", err.message || err);
    },

    onStompError: (frame) => {
        console.error("❌ STOMP ERROR:", frame.headers['message']);
        console.error(frame.body);
    }
});

client.activate();
