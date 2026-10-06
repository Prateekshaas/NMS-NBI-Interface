# Explainable AI-Based Network Fault Diagnosis through a REST Northbound Interface

A Java 17 + Spring Boot NMS implementing multivariate historical-baseline anomaly detection and explainable fault diagnosis through a REST-based Northbound Interface.

The system ingests device telemetry, evaluates network health using rule-based detection, and includes a separate analytics layer that scores multivariate deviations from historical behavior to support explainable diagnosis without replacing the core monitoring workflow.

This project provides a lightweight, in-memory representation of a Network Management System (NMS). Instead of depending on real hardware, it simulates a small network containing routers, switches, and access points. The application exposes HTTP endpoints that allow users to inspect device state, read telemetry history, update device configuration, restart a device, and retrieve active alerts.

The architectural design is intentionally aligned with a realistic NBI pattern:

- The core platform exposes a clean REST API for external consumers
- Device and network health data are managed internally in memory
- Rule-based health checks and alerts are derived from telemetry and fixed threshold logic
- A separate anomaly-detection layer evaluates multivariate deviations from historical baselines and produces explainable fault explanations

This keeps the operational monitoring logic distinct from the analytics layer while preserving a stable northbound interface for external consumers.

OBJECTIVE:

Modern network operations increasingly require systems that can do more than simple threshold monitoring. Operators need to answer questions such as:

- Is the network behaving normally?
- Which device is drifting out of expected behavior?
- What is the likely reason for degradation or failure?
- Which metric is the root cause versus a downstream symptom?
- Can an AI system explain its prediction in a way that an operator trusts?

This repository models that idea by combining:

- a REST-based NBI for device access
- telemetry simulation for CPU, memory, temperature, packet loss, and latency
- alert generation based on unhealthy device states
- a historical-baseline anomaly detection layer for multivariate diagnosis and explainability

## System architecture

'''
+--------------------------+
| External Consumers       |
| - Operators              |
| - Monitoring dashboards  |
| - Automation tools       |
+------------+-------------+
             |
             v
+--------------------------+
| REST Northbound API     |
| /api/devices            |
| /api/alerts             |
| /api/ai/*               |
+------------+-------------+
             |
             v
+--------------------------+
| Core NMS Services        |
| DeviceService            |
| AlertService             |
| HealthEvaluator          |
| Device/Metric Models     |
+------------+-------------+
             |
             v
+--------------------------+
| Telemetry Simulation     |
| MetricsSimulator         |
| In-memory device data    |
+------------+-------------+
             |
             v
+--------------------------+
| Historical Baseline      |
| Analyzer                 |
| Mean / SD comparison    |
| Multivariate scoring     |
| Explainable diagnosis    |
+--------------------------+
```

## Key features

### 1. Simulated network inventory
The application seeds a small set of virtual devices:

- `R1`, `R2` - routers
- `SW1`, `SW2` - switches
- `AP1` - access point

These devices are represented in memory and expose operational state and telemetry data.

### 2. Device telemetry and health scoring
Each device carries a health profile based on:

- CPU usage
- memory usage
- temperature
- packet loss
- latency

The `HealthEvaluator` determines whether a device state is healthy, degraded, or offline.

### 3. Alert generation
The system raises and filters alerts using the current device status and metric thresholds. Alert queries can be filtered by severity or device ID.

### 4. Device lifecycle operations
Users can:

- list all devices
- fetch a single device
- view health status
- inspect metric history
- update configuration
- restart a device to simulate recovery

### 5. Historical-baseline anomaly detection
The system includes a statistical anomaly-detection component that analyzes historical telemetry rather than relying only on fixed thresholds. For each metric, it computes the historical mean and standard deviation, compares the current value to that baseline, and combines the strongest deviations across CPU utilization, memory utilization, temperature, latency, and packet loss to compute an overall anomaly score.

This enables the platform to:

- detect abnormal device behavior from multivariate telemetry
- identify the metrics most strongly contributing to degradation
- assign a risk level based on the combined anomaly score
- generate an explainable diagnosis from the historical baseline
- recommend the next operational action

## API endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/devices` | List all devices |
| GET | `/api/devices/{id}` | Fetch one device |
| GET | `/api/devices/{id}/status` | Get current health and metrics |
| GET | `/api/devices/{id}/metrics` | Fetch telemetry history |
| PUT | `/api/devices/{id}/config` | Update device configuration |
| POST | `/api/devices/{id}/restart` | Simulate restart |
| GET | `/api/alerts` | List active alerts |

Example filters:

```bash
curl "http://localhost:8080/api/alerts?severity=CRITICAL"
curl "http://localhost:8080/api/alerts?deviceId=SW2"
```

## Implemented AI Diagnosis Layer

The analytics layer implements a historical-baseline anomaly detector. It computes a per-metric mean and standard deviation from recent device telemetry, then evaluates how far the current readings are from the baseline. The score is aggregated across multiple metrics to create a combined anomaly value for the device.

This is a statistical anomaly-detection approach rather than a hard-coded threshold rule set. It is designed to answer questions such as:

- Is this device behaving abnormally relative to its own history?
- Which metrics are contributing most strongly to the anomaly?
- Which fault pattern is most probable from the current multivariate signal?

For detected anomalies, the system generates:

- anomaly score
- affected device
- contributing metrics
- probable root cause
- risk level
- recommended remediation

### AI endpoints

```text
GET /api/ai/{id}/anomaly
GET /api/ai/{id}/failure-risk
GET /api/ai/{id}/diagnosis
GET /api/ai/{id}/explain
```

These endpoints consume the same metric history already managed by the Java service layer and compute their diagnosis from historical baseline comparisons, while preserving the REST NBI contract for external systems.

## Local setup

### Prerequisites

- Java 17+
- Maven

### Run the project

```bash
mvn spring-boot:run
```

The application starts on:

```text
http://localhost:8080
```

### Run tests

```bash
mvn test
```

## Example requests

```bash
curl http://localhost:8080/api/devices
curl http://localhost:8080/api/devices/R1/status
curl http://localhost:8080/api/devices/R1/metrics

curl -X PUT http://localhost:8080/api/devices/R1/config \
  -H "Content-Type: application/json" \
  -d '{"hostname":"core-r1","ntpServer":"time.google.com","snmpEnabled":true,"loggingLevel":"DEBUG"}'

curl -X POST http://localhost:8080/api/devices/AP1/restart
curl "http://localhost:8080/api/alerts?severity=CRITICAL&deviceId=SW2"
```

Import the Postman collection from `postman/NMS-NBI.postman_collection.json` for a ready-made set of requests.

## Project structure

```text
src/
  main/
    java/
      com/nms/nbi/
        controller/
        exception/
        model/
        service/
        simulation/
  test/
    java/
      com/nms/nbi/
postman/
README.md
pom.xml
```

## Design philosophy

This project follows a practical separation of concerns:

- REST API layer for northbound communication
- business logic for health and alert evaluation
- simulation for stateful telemetry and status changes
- optional AI module for diagnosis and prediction

This keeps the application flexible and aligns well with Explainable AI-based Network Fault Diagnosis systems where explainability, operational trust, and clean interface design are as important as raw prediction accuracy.

## Suggested future enhancements

- Add model-based anomaly detection using device history
- Add failure prediction for degraded switches and routers
- Add explainable root-cause outputs for each alert
- Add persistence using PostgreSQL or MongoDB
- Connect with real network telemetry sources via SNMP or APIs
- Support dashboards and Grafana integration

## Summary

This repository is a Java Spring Boot NBI prototype for a mock NMS. It demonstrates how device monitoring, health evaluation, and alerting can be exposed through a clean REST API, while also including a separate historical-baseline analytics layer for multivariate diagnosis and explainable root-cause reasoning. The architecture keeps rule-based monitoring and anomaly-based diagnosis distinct, which is important for operational clarity and trust.

The result is a practical foundation for an explainable network fault diagnosis platform that is realistic, extensible, and easy to integrate into broader operational environments.
