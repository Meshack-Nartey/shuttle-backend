// send-location.js
const WebSocket = require('ws');

const TOKEN = 'eyJhbGciOiJIUzUxMiJ9.eyJqdGkiOiJlNzgxNDMwMy05MmRmLTRmM2MtODQzYS1kN2VhOTQ0NThhMmUiLCJzdWIiOiJkcml2ZXIwMUBleGFtcGxlLmNvbSIsImlhdCI6MTc2NDUyOTkzMCwiZXhwIjoxNzY0NTMwODMwLCJ0eXBlIjoiYWNjZXNzIn0.w7r7Chbfw7_D5iGBBiV2HWyKDYWbeGBMv7EkKaNQKakB65eTHp46DVwZY-IDjEH3NmDngD4f3_0Y153ry4F5rQ'; // your JWT here
const URL = `ws://localhost:8080/ws/driver/location?access_token=${TOKEN}`;

console.log("Connecting to:", URL);

const ws = new WebSocket(URL);

ws.on('open', () => {
    console.log('Connected!');

    const payload = {
        shuttleId: 1,           // fixed: use shuttleId (Integer) expected by backend
        latitude: 6.67,
        longitude: -1.57,
        createdAt: new Date().toISOString()
    };

    ws.send(JSON.stringify(payload));
    console.log("Sent:", payload);
});

ws.on('message', msg => console.log("Message:", msg.toString()));
ws.on('close', (c, r) => console.log("Closed:", c, r?.toString()));
ws.on('error', err => console.error("Error:", err));
