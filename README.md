# 🌾 Smart Farming Control System – IDATA2304 Group Project

A Smart Farming monitoring system built for the **IDATA2304** course.  
It simulates real greenhouse environments where distributed sensor nodes send data to a central server and control panel.  
The system collects, processes, and displays real-time data, including temperature, humidity, and actuator status.

---

## 📌 Table of Contents
1. 📝 Project Overview  
2. 👥 Group Contributions  
3. 📂 Project Modules  
4. ⚙️ How to Download with Maven 
5. 🚀 How to Build & Run with Maven 
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

## 🛠 5. How to Build & Run (with images)
---

go to maven 


<img width="85" height="95" alt="Image" src="https://github.com/user-attachments/assets/eef213b4-ce27-403c-aba3-3639ad5bdc6e" />


---

📌 Step 1 – Install sfp-library

<img width="415" height="386" alt="image" src="https://github.com/user-attachments/assets/84402120-c3c3-41bb-ba91-f51afe8d52fa" />


pick 
cd sfp-library

and find mvn install below lifecycel


<img width="370" height="338" alt="image" src="https://github.com/user-attachments/assets/53f6c0a7-24f5-4fa4-9e23-29afc61d14ed" />


---

📌 Step 2 – Install parent project (sfp-project)

Same thing you can do with sfp-project 

cd ../sfp-project    mvn install

<img width="400" height="341" alt="image" src="https://github.com/user-attachments/assets/db4f53a2-f6cf-430a-9c28-1d45b0ec577f" />

---

📌 Step 3 – Build the Control Panel (GUI)

cd ../sfp-controlPanel   mvn clean install

<img width="407" height="344" alt="image" src="https://github.com/user-attachments/assets/dfcd84e3-18d9-4514-ba7a-d232493d76d5" />

----


▶️ Running the Server

 Open the sfp-server module and run:

Server.java


<img width="522" height="571" alt="image" src="https://github.com/user-attachments/assets/7ace130a-4546-4689-b9a7-1c42f51973d3" />

---

▶️ Running the GUI (Control Panel)

In the Maven sidebar:

➡️ sfp-controlPanel  
➡️ Plugins  
➡️ javafx  
➡️ javafx: run


<img width="400" height="360" alt="image" src="https://github.com/user-attachments/assets/c5940feb-d5bd-42fd-ba6c-a5434c586492" />


---
