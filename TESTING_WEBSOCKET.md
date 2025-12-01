Quick websocket testing

1. Auth modes
   - Header (default):
     - The script will send the token in a header. Set TOKEN and optionally AUTH_HEADER_NAME (default "Authorization").
       Example: TOKEN="ey..." AUTH_HEADER_NAME="Authorization" node scripts/send-location.js
   - Query param:
     - If your server expects the token in the URL, set TOKEN_IN_QUERY=true and optionally QUERY_PARAM_NAME.
       Example: TOKEN="ey..." TOKEN_IN_QUERY=true QUERY_PARAM_NAME="access" node scripts/send-location.js

2. Custom payload shape
   - Use PAYLOAD_TEMPLATE env var to map/override payload fields.
     - Example: PAYLOAD_TEMPLATE='{"id":"driverId","lat":"latitude","lon":"longitude","type":"pos"}'
       This will set keys id, lat, lon, type and copy values from the default payload where names match.
   - If you need a completely different static payload, supply the full JSON in PAYLOAD_TEMPLATE.

3. Examples
   - Single send, header auth:
     WS_URL="ws://localhost:8080/ws/driver" TOKEN="mytoken" INTERVAL_MS=0 node scripts/send-location.js
   - Repeated sends every second for 30s, token in query:
     WS_URL="ws://localhost:8080/ws/driver" TOKEN="mytoken" TOKEN_IN_QUERY=true QUERY_PARAM_NAME="token" INTERVAL_MS=1000 DURATION_MS=30000 node scripts/send-location.js
   - Using a custom payload template:
     PAYLOAD_TEMPLATE='{"driverId":"KNUST562","latKey":"latitude","lonKey":"longitude","type":"location.update"}' node scripts/send-location.js

4. Troubleshooting
   - If connection is refused, check ws vs wss and port.
   - If server requires a protocol (e.g., STOMP/ws subprotocol), this simple client may not be sufficient; use a client library matching your subprotocol.
   - Inspect server logs to see what payload shape it expects and adjust PAYLOAD_TEMPLATE or the default payload keys accordingly.

````</file>

That's all. Run the script with environment variables matching your server (WS_URL, TOKEN or TOKEN_IN_QUERY, and PAYLOAD_TEMPLATE) to adapt the payload and auth to your endpoint.
`````
