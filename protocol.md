# Smart Farming Protocol Documentation (`protocol.md`)

## 1. Introduction

This document describes the **application-layer communication protocol** used in the IDATA2302 assignment. It defines how **sensor nodes (SNs)**, **control-panel nodes (CPs)**, and a central **server/broker** exchange data. The protocol supports transmission of sensor readings, actuator commands, image data, subscriptions, capability discovery, and error handling.

The protocol is built on top of **TCP** with optional **TLS** encryption for secure communication. It is designed to be **scalable, reliable, and extensible**, enabling dynamic registration of nodes, flexible data flow (push and pull), and interoperable communication between different node types.

---

## 2. Terminology

| Term | Description |
|------|-------------|
| Sensor Node (SN) | A node that exposes sensors, actuators, or the ability to capture images. |
| Control Panel (CP) | A user-facing node providing monitoring and actuation functions. |
| Server / Broker | Central component receiving all messages and routing them to intended recipients. |
| UID | Unique numeric identifier assigned to nodes after registration. |
| DATA_REPORT | Message with sensor readings, actuator states, and aggregate values. |
| DATA_REQUEST | Pull request for specific sensor/actuator/image data. |
| COMMAND | Message to change actuator values. |
| SUBSCRIBE | Request to receive push updates from a given SN. |
| CAPABILITIES_QUERY | Request for a list of available nodes and their descriptor information. |
| CAPABILITIES_LIST | Response listing node descriptors. |
| ANNOUNCE | Message by a node joining the system (includes metadata). |
| IMAGE_METADATA | Metadata describing an image to be sent in chunks. |
| IMAGE_CHUNK | Raw chunk of image data. |
| ERROR | Error message indicating malformed requests or failures. |

---

## 3. Transport

- **Transport Protocol:** TCP  
- **Reasoning:** TCP ensures reliable, ordered, and loss-free delivery—critical for sensor values, aggregated data, commands, and image chunks.  
- **Encryption Layer:** TLS is used to authenticate nodes and encrypt all communication.  
- **Port:** 5050 (default)

---

## 4. Architecture

### Sensor Nodes
- Provide sensor readings and actuator outputs.
- Can optionally provide images.
- Maintain a persistent TCP/TLS connection to the server.
- Periodically send `DATA_REPORT`.
- Include a simulation loop emulating real sensor/actuator values.

### Server / Broker
- Accepts all incoming connections.
- Assigns UIDs to nodes.
- Validates, parses, and routes messages.
- Stores subscriptions, descriptors, and session information.
- Tracks node availability and handles errors.

### Control Panel
- GUI application for monitoring and controlling the system.
- Displays real-time sensor data and historical information.
- Sends `COMMAND` and `DATA_REQUEST`.
- Can spawn new simulated sensor nodes.
- Subscribes to sensor nodes for updates.

### Library
- Shared across SNs, CPs, and Server.
- Defines:
  - Message types and body structures
  - CBOR serialization/deserialization
  - Node descriptors
  - Capabilities handling
  - Image chunking utilities
  - Validation logic

---

## 5. Information Flow

### 5.1 Joining the System
1. A node connects via TCP/TLS.
2. It immediately sends an `ANNOUNCE` containing:
   - node type (SN or CP)  
   - its sensors, actuators, and capabilities  
3. The server responds with `ANNOUNCE_ACK`, assigning a UID.  
4. CPs may send `CAPABILITIES_QUERY` to learn about other nodes.

### 5.2 Regular Sensor Updates (Push Model)
- SNs periodically send `DATA_REPORT` messages.
- Server forwards them to subscribed CPs.
- Format follows `DataReportBody` (lists of sensors, actuators, aggregates).

### 5.3 On-Demand Data Retrieval (Pull Model)
- CP sends a `DATA_REQUEST` with desired sensors/aggregates/images.
- Server forwards request to SN.
- SN replies with:
  - `DATA_REPORT` for numeric data  
  - `IMAGE_METADATA` followed by `IMAGE_CHUNK`s for images  

### 5.4 Actuator Commands
- CP sends `COMMAND` message with a list of actuator changes.
- SN applies changes and returns a `COMMAND_ACK`.

### 5.5 Subscriptions
- CP → Server: `SUBSCRIBE` specifying SN ID.
- Server → CP: `SUBSCRIBE_ACK`.
- Server forwards `DATA_REPORT` to CP automatically.
- Unsubscribe follows the same pattern using `UNSUBSCRIBE` and `UNSUBSCRIBE_ACK`.

### 5.6 Image Transfer
1. SN sends `IMAGE_METADATA`.
2. SN sends `IMAGE_CHUNK` messages sequentially.
3. CP acknowledges completion using `IMAGE_TRANSFER_ACK`.

### 5.7 Errors
- Any invalid or unexpected message results in an `ERROR` response.
- The sender may retry (idempotent due to Message ID).

---

## 6. Protocol Type

### Connection-Oriented
The Smart Farming Protocol is fundamentally **connection-oriented**, relying on persistent TCP streams to maintain communication between nodes and the central server. This guarantees message ordering and reliable delivery, allowing sensor data, actuator commands, and image chunks to arrive exactly as intended without the need for application-level retransmission.

### Stateful
The protocol also operates in a **stateful** manner.  
The server keeps track of active sessions, assigned UIDs, active subscriptions, known node capabilities, and ongoing data or image transfers. Sensor nodes maintain internal state for their simulated or real sensors and actuators, while control panels maintain UI-related state such as which nodes a user is monitoring.  
This shared notion of state makes coordinated routing, subscription updates, and consistent interaction across the system possible.

---

## 7. Message Types and Constants

The protocol defines a set of message types that cover all interactions between nodes. Each type is associated with a unique hexadecimal identifier, allowing the server to differentiate between data reports, control commands, capability queries, image transfers, and error notifications. The table below summarizes these message types together with their typical direction and purpose.

| Type | Hex | Sender | Description |
|------|-----|--------|-------------|
| DATA_REPORT | 0x01 | SN | Sensor data, actuator states, and aggregates |
| DATA_REQUEST | 0x02 | CP | Pull request for specific sensor, actuator, or image data |
| COMMAND | 0x12 | CP | Instruction to modify one or more actuator values |
| COMMAND_ACK | 0x13 | SN | Confirms that a command was successfully applied |
| SUBSCRIBE | 0x0B | CP | Requests push-based updates from an SN |
| UNSUBSCRIBE | 0x0C | CP | Cancels a previous subscription |
| SUBSCRIBE_ACK | 0x0D | Server | Indicates whether a subscription was accepted |
| UNSUBSCRIBE_ACK | 0x0E | Server | Indicates whether a subscription was removed |
| CAPABILITIES_QUERY | 0x21 | CP / Server | Requests descriptor information about nodes |
| CAPABILITIES_LIST | 0x22 | SN / Server | Contains detailed node descriptors |
| ANNOUNCE | 0x1E | SN / CP | Sent when a node joins the system |
| ANNOUNCE_ACK | 0x1D | Server | Confirms registration and assigns a UID |
| IMAGE_METADATA | 0x07 | SN | Describes the structure and size of an upcoming image |
| IMAGE_CHUNK | 0x08 | SN | Sends a portion of an image's binary data |
| IMAGE_TRANSFER_ACK | 0x09 | CP | Confirms that all image chunks were received and validated |
| ERROR | 0xFE | Any | Conveys errors or protocol violations |

While the table lists nominal directions, the server may forward, proxy, or transform messages as required by the system.

---

## 8. Message Format

All messages use a structured binary format composed of a fixed header followed by a body encoded using CBOR.  
The header identifies the message type and routing information, while the body carries the actual payload such as sensor readings, actuator updates, or image data.

### 8.1 Header (Fixed Size)

| Field | Size | Description |
|------|------|-------------|
| Protocol Name | 3 bytes | Always `0x53 0x46 0x50` (“SFP”) |
| Version | 1 byte | Protocol version (`0x01`) |
| Message Type | 1 byte | Indicates which type of body follows |
| Source ID | 4 bytes | UID of the sender |
| Target ID | 4 bytes | UID of the intended receiver |
| Payload Length | 4 bytes | Size of the CBOR body in bytes |
| Message ID | 16 bytes | UUID used for tracking/logging and idempotency |

### 8.2 Body Format

The message body is encoded using **CBOR**, chosen for its compactness and ability to represent structured data efficiently.  
Most bodies contain JSON-like structures (e.g., sensor readings or command lists). The exception is `IMAGE_CHUNK`, which stores raw binary data for image transmission.

---

## 9. Errors

### 9.1 Error Codes

The protocol defines a consistent set of error codes to help nodes understand why a message failed. These range from malformed requests and unauthorized operations to chunking or checksum issues in image transfers.

| Code | Meaning |
|------|---------|
| 1 | BAD_REQUEST |
| 2 | UNAUTHORIZED |
| 3 | UNSUPPORTED_OPERATION |
| 4 | NODE_NOT_FOUND |
| 5 | INVALID_ID |
| 6 | CHUNK_MISSING |
| 7 | CHECKSUM_MISMATCH |
| 8 | CAPABILITIES_MISSING |
| 100 | INTERNAL_SERVER_ERROR |
| 101 | UNKNOWN |

### 9.2 Error Handling Behaviour

When a node receives a malformed or unexpected message, the protocol expects it to respond with an `ERROR` message that includes both an error code and an optional human-readable description. Nodes should handle these errors gracefully by logging them, informing the user if appropriate (for CPs), and retrying when it is safe to do so.

Errors play a crucial role in maintaining robustness—especially for image transfers, where missing or corrupted chunks must be detected early to avoid constructing invalid images.

---

## 10. Realistic Scenario Example

Consider a user interacting with a control panel to monitor temperature and adjust a fan in a greenhouse. When the system starts, a sensor node announces itself to the server and receives a UID. The control panel later queries the server for available nodes and discovers the new sensor node, including its sensors and actuators. The user chooses to subscribe to this sensor node, after which temperature updates begin flowing to the control panel in real time through `DATA_REPORT` messages.

When the user sets the fan speed to 50%, the control panel sends a `COMMAND` instructing the sensor node to update the actuator. The sensor node applies the change and returns a `COMMAND_ACK` confirming the update. If the user then requests an image, the control panel issues a `DATA_REQUEST`, prompting the sensor node to provide `IMAGE_METADATA` followed by a sequence of `IMAGE_CHUNK` messages. After assembling the full image and verifying its checksum, the control panel sends an `IMAGE_TRANSFER_ACK`, completing the exchange.

Throughout this interaction, the server continuously routes messages, manages subscriptions, and monitors node health.

---

## 11. Reliability Mechanisms

The protocol’s reliability is built upon several layers working together. TCP ensures in-order and loss-free delivery, while TLS prevents tampering. On top of this, each application-level message includes a `Message ID` that allows nodes to detect duplicates or retry failed transmissions safely.  
Image transfers include additional reliability features such as chunk indexes and cryptographic checksums to confirm integrity. If an image chunk is missing or corrupted, the receiver can report a numbered error using the `ERROR` frame.

The server also plays a role by rejecting malformed messages early and maintaining state about which nodes are active, reducing the risk of sending requests to unavailable nodes.

---

## 12. Security Mechanisms

Security is enforced primarily through TLS, which encrypts the entire data stream and ensures nodes communicate with an authenticated server. The initial `ANNOUNCE` message acts as a logical handshake, communicating a node’s identity and capabilities, while the server can apply additional authentication logic as needed.

CBOR-decoding on all nodes prevents injection of malformed data, and strict message typing helps nodes detect unauthorized or inappropriate actions.  
If required, the system can enforce role-based permissions—for example, restricting command messages so that only CPs may send them.

---

## 13. Example Message Bodies

All message bodies in the Smart Farming Protocol are encoded using **CBOR** during transmission.  
For human readability, the following examples present the data in **JSON form**, mirroring the exact structure defined by the Java `record` types in the shared protocol library.  
These JSON-like structures illustrate the meaning and layout of each message, even though actual communication uses CBOR.

---

### 13.1 `DataReportBody`
```json
{
  "sensors": [
    {
      "id": "temperature",
      "value": 23.4,
      "minValue": 18.0,
      "maxValue": 28.5,
      "unit": "C",
      "timestamp": "2025-10-20T11:03:00Z"
    }
  ],
  "actuators": [
    {
      "id": "fan",
      "value": 50.0,
      "minValue": 0.0,
      "maxValue": 100.0,
      "unit": "%",
      "timestamp": "2025-10-20T11:03:00Z"
    }
  ],
  "aggregates": [
    {
      "id": "temperature",
      "period": "1h",
      "min": 19.1,
      "max": 24.7,
      "avg": 22.5
    }
  ]
}
```

### 13.2 `DataRequestBody`
```json
{
  "requestId": "req-2025-22",
  "sensors": {
    "metrics": ["temperature", "humidity"],
    "includeAggregates": true,
    "aggregates": {
      "metrics": ["temperature"],
      "periods": ["1h", "24h"],
      "types": ["min", "max", "avg"]
    }
  },
  "actuators": {
    "includeStates": true
  },
  "images": {
    "includeLatest": true
  }
}
```

### 13.3 `CommandBody`
```json
{
  "requestId": 42,
  "actuators": [
    {
      "name": "fan",
      "newValue": 75.0
    }
  ]
}
```
### 13.4 `CommandAckBody`
```json
{
  "requestId": 42,
  "status": 1,
  "message": "Fan speed set successfully."
}
```

### 13.5 `AnnounceBody`
```json
{
  "requestId": 1,
  "descriptor": {
    "nodeId": null,
    "nodeType": 1,
    "sensors": [
      {
        "id": "temperature",
        "unit": "C",
        "minValue": 0.0,
        "maxValue": 50.0
      }
    ],
    "actuators": [
      {
        "id": "fan",
        "value": 0.0,
        "minValue": 0.0,
        "maxValue": 100.0,
        "unit": "%"
      }
    ],
    "supportsImages": true,
    "supportsAggregates": true
  }
}
```

### 13.6 `AnnounceAckBody`
```json
{
  "requestId": 1,
  "status": 1
}
```

### 13.7 `CapabilitiesQueryBody`
```json
{
  "requestId": 100
}
```

### 13.8 `CapabilitiesListBody`
```json
{
  "requestId": 100,
  "nodes": [
    {
      "nodeId": 5,
      "nodeType": 1,
      "sensors": [
        {
          "id": "temperature",
          "unit": "C",
          "minValue": 0.0,
          "maxValue": 50.0
        }
      ],
      "actuators": [
        {
          "id": "fan",
          "value": 40.0,
          "minValue": 0.0,
          "maxValue": 100.0,
          "unit": "%"
        }
      ],
      "supportsImages": true,
      "supportsAggregates": true
    }
  ]
}
```

### 13.9 `ImageMetadataBody`
```json
{
  "imageId": "img-2025-04-01-14-22",
  "timestamp": "2025-04-01T14:22:00Z",
  "contentType": "image/jpeg",
  "totalSize": 345678,
  "chunkCount": 34,
  "chunkSize": 10240,
  "checksum": "e3b0c44298fc1c149afbf4c8996fb924..."
}
```

### 13.10 `ImageChunkBody`
```json
{
  "imageId": "img-2025-04-01-14-22",
  "chunkIndex": 3,
  "data": "YmFzZTY0LWltYWdlLWRhdGEtYnV0LWluLWNhcHR1cmVkLWZvcm0="
}
```

### 13.11 `ImageTransferAckBody`
```json
{
  "imageId": "img-2025-04-01-14-22",
  "status": 0
}
```

### 13.12 `SubscribeBody`
```json
{
  "requestId": 200,
  "sensorNodeId": 5
}
```

### 13.13 `SubscribeAckBody`
```json
{
  "requestId": 200,
  "status": 1
}
```

### 13.14 `UnsubscribeBody`
```json
{
  "requestId": 201,
  "sensorNodeId": 5
}
```

### 13.15 `SubscribeAckBody`
```json
{
  "requestId": 201,
  "status": 1
}
```

### 13.16 `ErrorBody`
```json
{
  "errorCode": 1,
  "errorText": "Malformed CBOR structure"
}
```
