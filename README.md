# Smart Farming System – Group Project (IDATA2304)

This project is a Smart Farming monitoring system developed as part of the IDATA2304 course.  
It includes a custom communication protocol, server logic, and a GUI dashboard.  
The goal is to simulate a real-world agriculture monitoring solution, where data is collected, processed, and visualized.

---

## Table of Contents
1. Project Overview
2.  Group Contributions    
3. How to Run the Project  

---

## 1. Project Overview

The Smart Farming System is designed to communicate with greenhouse “nodes” that send temperature, humidity, and actuator data to the control panel.  
Control Panel sends the data forward to the client 

The project includes:
- A custom communication protocol (designed by the group)
- A  TCP server to handle node messages
- A JavaFX GUI to visualize node data
- Node simulators for sending sensor data
- a control panel to hold the server and client together. 
- Documentation and sprint reports

## 2. How to run the project 

A) To run the application, you need to use Maven. You can follow one of these methods:

### Method 1: Using Maven in the IDE

1. Press on **Maven** in your IDE.
2. Navigate to **Plugins**.
3. Scroll down and find **JavaFX**, then click on it.
4. Press the second button: **JavaFX:run**.

### Method 2: Using the Terminal

1. Open the terminal.
2. Run the following command:
   ```bash
   mvn javafx:run

B) Run the server 

c) Run the controlPanel 

D) 
