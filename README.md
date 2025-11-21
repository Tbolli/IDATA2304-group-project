# 🌾 Smart Farming Control System – IDATA2304 Group Project

A Smart Farming monitoring system built for the **IDATA2304** course.  
It simulates real greenhouse environments where distributed sensor nodes send data to a central server and control panel.  
The system collects, processes, and displays real-time data, including temperature, humidity, and actuator status.

---

## 📌 Table of Contents
1. 📝 Project Overview  
2. 👥 Group Contributions  
3. 📂 Project Modules  
4. ⚙️ How to Download  
5. 🚀 How to Build & Run  
---

## 📝 1. Project Overview

The Smart Farming System is designed to simulate a real farming environment using nodes that send sensor data to a central control system.

### Main Features:
- 🌐 Custom communication protocol (group-designed)
- 🖧 TCP server for node communication
- 💻 JavaFX dashboard for real-time visualization
- 📡 Node simulators sending sensor data (temperature, humidity, actuators)
- 🔌 Control Panel connecting server and client together
- 📑 Documentation and sprint reports included

---

## 📂 3. Project Modules

| Module            | Description |
|-------------------|-------------|
| `sfp-library`     | Shared utilities, data models, protocol formats |
| `sfp-server`      | Handles socket communication between nodes and control panel |
| `sfp-sensorNode`  | Sensor simulator sending JSON data |
| `sfp-controlPanel`| JavaFX GUI for real-time monitoring and control |

---

## 📥 4. How to Download the Project

### 🔹 Option 1 – Clone using HTTPS

git clone https://github.com/Tbolli/IDATA2304-group-project.git

🔹 Option 2 – Clone using SSH

git clone git@github.com:Tbolli/IDATA2304-group-project.git

🔹 Option 3 – Download ZIP

💾 Open GitHub → Code → Download ZIP → Extract to your desired location

## 🛠 5. How to Build & Run

📌 Step 1 – Install sfp-library

cd sfp-library

mvn install

📌 Step 2 – Install parent project (sfp-project)

cd ../sfp-project

mvn install

📌 Step 3 – Build the Control Panel (GUI)

cd ../sfp-controlPanel

mvn clean install

---

▶️ Running the Server

 Open the sfp-server module and run:

Server.java

---

▶️ Running the GUI (Control Panel)

In Maven sidebar:

➡️ sfp-controlPanel  
➡️ Plugins  
➡️ javafx  
➡️ javafx: run
