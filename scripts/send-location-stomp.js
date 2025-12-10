// send-location-stomp.js
// Usage: node send-location-stomp.js <JWT_TOKEN> <SHUTTLE_ID> <LAT> <LNG>

const { Client } = require('@stomp/stompjs');
const WebSocket = require('ws');

const token = process.argv[2];
const shuttleId = parseInt(process.argv[3] || '1', 10);
const lat = parseFloat(process.argv[4] || '5.65');
const lng = parseFloat(process.argv[5] || '-0.17');

if (!token) {
  console.error('Usage: node send-location-stomp.js <JWT_TOKEN> <SHUTTLE_ID> <LAT> <LNG>');
  process.exit(1);
}

const WS_URL = 'ws://localhost:8080/ws';

console.log('Connecting to STOMP broker at', WS_URL);

const client = new Client({
  webSocketFactory: () => new WebSocket(WS_URL),
  connectHeaders: {
    Authorization: `Bearer ${token}`
  },
  debug: (msg) => console.log('[STOMP DEBUG]', msg),
  reconnectDelay: 5000,
  heartbeatIncoming: 4000,
  heartbeatOutgoing: 4000,
  onConnect: () => {
    console.log('STOMP connected, publishing location');
    const payload = {
      shuttleId: shuttleId,
      latitude: lat,
      longitude: lng,
      createdAt: new Date().toISOString()
    };

    client.publish({ destination: '/app/driver/location', body: JSON.stringify(payload) });
    console.log('Published:', payload);

    // leave connection open for a short while to allow broker delivery
    setTimeout(() => {
      client.deactivate();
      console.log('Disconnected');
      process.exit(0);
    }, 1000);
  },
  onStompError: (frame) => {
    console.error('Broker error:', frame);
    process.exit(2);
  }
});

client.activate();

